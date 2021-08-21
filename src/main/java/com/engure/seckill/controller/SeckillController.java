package com.engure.seckill.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 秒杀接口
 * <p>
 * <p>
 * 压测： /seckill/doSeckill
 * QPS 1000 * 10:
 * 前        缓存优化和防止超卖后
 * 1339         2376
 */

@Controller
@RequestMapping("/seckill")
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
        Long afterDecr = opsFV.decrement("seckill:goodsVo-" + goodsId);
        if (afterDecr == null)
            return RespBean.error(RespTypeEnum.GOODS_NOT_EXIST);//商品不存在

        if (afterDecr < 0) {
            goodsIsEmptyMap.put(goodsId, true);//标记该秒杀商品已经售罄
            opsFV.increment("seckill:goodsVo-" + goodsId);
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
