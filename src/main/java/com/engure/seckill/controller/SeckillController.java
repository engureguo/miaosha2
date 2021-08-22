package com.engure.seckill.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.engure.seckill.exception.GlobalException;
import com.engure.seckill.pojo.Order;
import com.engure.seckill.pojo.SeckillOrder;
import com.engure.seckill.pojo.User;
import com.engure.seckill.rabbitmq.MQSender;
import com.engure.seckill.service.IGoodsService;
import com.engure.seckill.service.IOrderService;
import com.engure.seckill.service.ISeckillOrderService;
import com.engure.seckill.utils.JsonUtil;
import com.engure.seckill.vo.GoodsVo;
import com.engure.seckill.vo.RespBean;
import com.engure.seckill.vo.RespTypeEnum;
import com.engure.seckill.vo.SeckillMessage;
import com.wf.captcha.ArithmeticCaptcha;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀接口
 * <p>
 * <p>
 * 压测： /seckill/doSeckill
 * QPS 1000 * 10:
 * 前        缓存优化和防止超卖后      预减库存、内存标记、消息队列
 * 1339         2376                        4921
 */

@Controller
@RequestMapping("/seckill")
@Slf4j
public class SeckillController implements InitializingBean {
// 实现InitializingBean接口：Interface to be implemented by beans that need to react once all their properties have been set
// bean初始化

    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private ISeckillOrderService seckillOrderService;

    @Autowired
    private IOrderService orderService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MQSender mqSender;

    //内存标记，减少 redis 的访问
    private final Map<Long, Boolean> goodsIsEmptyMap = new HashMap<>();

    @Autowired
    private DefaultRedisScript<Long> redisScript;

    @RequestMapping("doSeckill")
    public String kill(User user, Long goodsId, Model model) {

        if (user == null) return "login";
        if (goodsId == null) return "redirect:/goods/toList";

        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);

        // 判断秒杀库存，而不是商品库存
        if (goodsVo.getStockCount() < 1) {
            model.addAttribute("errmsg", RespTypeEnum.OUT_OF_STOCK.getMsg());
            return "secKillFail";
        }

        // 判断不得多买
        SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>()
                .eq("user_id", user.getId())
                .eq("goods_id", goodsId)); // 在秒杀记录中查看用户是否秒杀过该商品
        if (null != seckillOrder) {
            model.addAttribute("errmsg", RespTypeEnum.REPEATED_BUY_ERROR.getMsg());
            return "secKillFail";
        }

        Order order = orderService.seckill(user, goodsVo);

        model.addAttribute("order", order);
        model.addAttribute("goods", goodsVo);

        return "orderDetail";
    }

    /**
     * 缓存优化，防止超卖的秒杀接口
     * redis预减库存（lua和原来的）、内存标记、消息队列
     *
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "doSeckill2", method = RequestMethod.POST)
    @ResponseBody
    public RespBean kill2(User user, @RequestParam("goodsId") Long goodsId) {

        if (user == null)
            return RespBean.error(RespTypeEnum.SESSION_NOT_EXIST);

        //使用“内存标记”，减少redis访问量
        if (goodsIsEmptyMap.get(goodsId)) {
            return RespBean.error(RespTypeEnum.OUT_OF_STOCK);
        }

        ValueOperations opsFV = redisTemplate.opsForValue();

        //从缓存中取“可能买过的记录”，可以拦截大部分的“二次购买”
        Object orderInfo = opsFV.get("seckOrder:userId-" + user.getId()
                + ":goodsId-" + goodsId);
        if (null != orderInfo) {
            return RespBean.error(RespTypeEnum.REPEATED_BUY_ERROR);
        }

        // redis预减库存
        /*Long afterDecr = opsFV.decrement("seckill:goodsVo-" + goodsId);
        if (afterDecr == null)
            return RespBean.error(RespTypeEnum.GOODS_NOT_EXIST);//商品不存在

        if (afterDecr < 0) {
            goodsIsEmptyMap.put(goodsId, true);//标记该秒杀商品已经售罄
            opsFV.set("isSeckillGoodsEmpty:" + goodsId, "0");//标记该秒杀商品已经售空，在 查询秒杀结果时需要用到 OrderServiceImpl.qrySeckillOrder
            opsFV.increment("seckill:goodsVo-" + goodsId);
            return RespBean.error(RespTypeEnum.OUT_OF_STOCK);
        }*/

        // 使用 lua 脚本，逻辑更严谨
        // 使用客户端的 lua 脚本，网络传输增大，QPS降低
        Long afterDecr = (Long) redisTemplate.execute(redisScript,
                Collections.singletonList("seckill:goodsVo-" + goodsId));
        if (afterDecr == null)
            return RespBean.error(RespTypeEnum.GOODS_NOT_EXIST);//商品不存在

        if (afterDecr == 0) {
            goodsIsEmptyMap.put(goodsId, true);//标记该秒杀商品已经售罄
            opsFV.set("isSeckillGoodsEmpty:" + goodsId, "0");//标记该秒杀商品已经售空，在 查询秒杀结果时需要用到 OrderServiceImpl.qrySeckillOrder
            //opsFV.increment("seckill:goodsVo-" + goodsId);
            return RespBean.error(RespTypeEnum.OUT_OF_STOCK);
        }


        /*
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);//查找秒杀商品信息

        // 判断秒杀库存，而不是商品库存
        if (goodsVo.getStockCount() < 1) {
            return RespBean.error(RespTypeEnum.OUT_OF_STOCK);
        }

        // 判断不得多买
        //SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>()
        //        .eq("user_id", user.getId())
        //        .eq("goods_id", goodsId)); // 在秒杀记录中查看用户是否秒杀过该商品
        //if (null != seckillOrder) {
        //    return RespBean.error(RespTypeEnum.REPEATED_BUY_ERROR);
        //}
        Order order = orderService.seckill(user, goodsVo);
        */

        //将秒杀信息放入消息队列
        SeckillMessage seckillMessage = new SeckillMessage(user, goodsId);
        mqSender.sendSeckillMessage(JsonUtil.object2JsonStr(seckillMessage));

        return RespBean.success(0);//“快速”将结果返回给用户
    }

    /**
     * 秒杀前，获取秒杀路径
     *
     * @param user    用户信息
     * @param goodsId 可以不带goodsId参数或者goodsId为空，此时goodsId为null
     * @param captcha 验证码
     * @return 返回个性化秒杀关键路径
     */
    @PostMapping(value = "/path")
    @ResponseBody
    public RespBean getSeckillPath(User user, Long goodsId, String captcha) {

        if (null == user || null == goodsId || !StringUtils.hasLength(captcha))
            return RespBean.error(RespTypeEnum.REQUEST_ILLEGAL);

        //校验验证码
        Integer check = orderService.checkCaptcha(user, goodsId, captcha);
        if (check == 1)
            return RespBean.error(RespTypeEnum.CAPTCHA_INVALID);
        else if (check == 2) {
            return RespBean.error(RespTypeEnum.CAPTCHA_ERROR);
        }

        //获取秒杀路径
        String path = orderService.createPath(user, goodsId);

        return RespBean.success(path);
    }

    @GetMapping("/captcha")
    public void captcha(User user, Long goodsId,
                        HttpServletResponse response) {

        if (null == user | goodsId == null)
            throw new GlobalException(RespTypeEnum.REQUEST_ILLEGAL);

        // 设置请求头为输出图片类型
        response.setContentType("image/jpg");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        // 算术类型
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(130, 32);
        captcha.setLen(3);  // 几位数运算，默认是两位
        captcha.getArithmeticString();  // 获取运算的公式：3+2=?
        String res = captcha.text();// 获取运算的结果：5

        redisTemplate.opsForValue().set("captcha:uid-" + user.getId() + ":gid-" + goodsId,
                res, 60, TimeUnit.SECONDS);

        try {
            captcha.out(response.getOutputStream());  // 输出验证码
        } catch (IOException e) {
            //e.printStackTrace();
            log.info("获取验证码失败~");
        }

    }

    /**
     * 秒杀接口，在 kill2() 的基础上继续完善
     * 判断秒杀路径
     *
     * @param path    秒杀路径，可能为null
     * @param user    用户信息
     * @param goodsId 必须携带 “goodsId=” 部分，可能为null
     * @return
     */
    @PostMapping(value = "/{path}/doSeckill3")
    @ResponseBody
    public RespBean kill3(@PathVariable String path,
                          User user, @RequestParam("goodsId") Long goodsId) {

        if (user == null)
            return RespBean.error(RespTypeEnum.SESSION_NOT_EXIST);

        if (goodsId == null)
            return RespBean.error(RespTypeEnum.GOODS_NOT_EXIST);

        if (null == path)
            return RespBean.error(RespTypeEnum.SECKILL_PATH_ERROR);

        //检查秒杀路径 path 是否是用户的
        Boolean rightPath = orderService.checkPath(user, goodsId, path);
        if (!rightPath)
            return RespBean.error(RespTypeEnum.SECKILL_PATH_ERROR);

        //使用“内存标记”，减少redis访问量
        if (goodsIsEmptyMap.get(goodsId)) {
            return RespBean.error(RespTypeEnum.OUT_OF_STOCK);
        }

        ValueOperations opsFV = redisTemplate.opsForValue();

        //从缓存中取“可能买过的记录”，可以拦截大部分的“二次购买”
        Object orderInfo = opsFV.get("seckOrder:userId-" + user.getId()
                + ":goodsId-" + goodsId);
        if (null != orderInfo) {
            return RespBean.error(RespTypeEnum.REPEATED_BUY_ERROR);
        }

        // 使用 lua 脚本，逻辑更严谨
        // 使用客户端的 lua 脚本，网络传输增大，QPS降低
        Long afterDecr = (Long) redisTemplate.execute(redisScript,
                Collections.singletonList("seckill:goodsVo-" + goodsId));
        if (afterDecr == null)
            return RespBean.error(RespTypeEnum.GOODS_NOT_EXIST);//商品不存在

        if (afterDecr == 0) {
            goodsIsEmptyMap.put(goodsId, true);//标记该秒杀商品已经售罄
            opsFV.set("isSeckillGoodsEmpty:" + goodsId, "0");//标记该秒杀商品已经售空，在 查询秒杀结果时需要用到 OrderServiceImpl.qrySeckillOrder
            //opsFV.increment("seckill:goodsVo-" + goodsId);
            return RespBean.error(RespTypeEnum.OUT_OF_STOCK);
        }

        //将秒杀信息放入消息队列
        SeckillMessage seckillMessage = new SeckillMessage(user, goodsId);
        mqSender.sendSeckillMessage(JsonUtil.object2JsonStr(seckillMessage));

        return RespBean.success(0);//“快速”将结果返回给用户
    }


    /**
     * 项目启动，将秒杀商品信息存入 redis，主要是 商品id和库存量
     */
    @Override
    public void afterPropertiesSet() {
        List<GoodsVo> allGoodsVo = goodsService.findAllGoodsVo();

        ValueOperations opsFV = redisTemplate.opsForValue();
        allGoodsVo.forEach(goodsVo -> {
                    opsFV.set("seckill:goodsVo-" + goodsVo.getId(), goodsVo.getStockCount());
                    goodsIsEmptyMap.put(goodsVo.getId(), false);
                }
        );

    }
}
