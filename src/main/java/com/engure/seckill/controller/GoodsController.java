package com.engure.seckill.controller;

import com.engure.seckill.exception.GlobalException;
import com.engure.seckill.pojo.User;
import com.engure.seckill.service.IGoodsService;
import com.engure.seckill.utils.CookieUtil;
import com.engure.seckill.vo.GoodsDetailVo;
import com.engure.seckill.vo.GoodsVo;
import com.engure.seckill.vo.RespBean;
import com.engure.seckill.vo.RespTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * 商品控制器
 * <p>
 * <p>
 * 压测： /goods/toList
 * QPS 1000 * 10:    前      页面缓存
 *      windows    1384        3220
 *
 *
 */


@Controller
@RequestMapping("/goods")
@Slf4j
public class GoodsController {

    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ThymeleafViewResolver thymeleafViewResolver;

    /**
     * 商品列表
     *
     * @param user
     * @param model
     * @return
     */
    @RequestMapping(value = "/toList", produces = "text/html;charset=utf-8")
    @ResponseBody
    public String list(User user,       // 在 controller 入口之前做校验
                       Model model,
                       HttpServletRequest request,
                       HttpServletResponse response) throws IOException {

        if (null == user) {
            CookieUtil.deleteCookie(request, response, "user_ticket");//过期cookie导致，清除cookie
            return "<script>location.href='/login'</script>";
        }

        ValueOperations opsFV = redisTemplate.opsForValue();
        Object html = opsFV.get("html:goods:toList");

        if (null != html) return (String) html;

        model.addAttribute("user", user);//返回用户信息1
        List<GoodsVo> goodsVoList = goodsService.findAllGoodsVo();
        model.addAttribute("goodsList", goodsVoList);//返回商品信息2

        WebContext context = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goodsList", context);
        opsFV.set("html:goods:toList", html, 10, TimeUnit.SECONDS);

        return (String) html;
    }

    /**
     * 商品详情页
     *
     * @param goodsId require=true，默认不会出现null，否则 404
     * @param user
     * @param model
     * @return
     */
    @RequestMapping(value = "/toDetail/{goodsId}", produces = "text/html;charset=utf-8")
    @ResponseBody
    public String detail(@PathVariable Long goodsId, User user, Model model,
                         HttpServletRequest request,
                         HttpServletResponse response) {

        if (null == user) {
            CookieUtil.deleteCookie(request, response, "user_ticket");//过期cookie导致，清除cookie
            return "<script>location.href='/login'</script>";
        }

        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
        model.addAttribute("user", user);
        model.addAttribute("goods", goodsVo);

        Date startDate = goodsVo.getStartDate();
        Date endDate = goodsVo.getEndDate();
        Date nowDate = new Date();
        //秒杀状态 0未开始，1进行中，2已结束
        int secKillStatus = 0;
        //秒杀倒计时 >0倒计时，0进行中，-1已结束
        int remainSeconds = 0;
        //秒杀还未开始
        if (nowDate.before(startDate)) {
            remainSeconds = ((int) ((startDate.getTime() - nowDate.getTime()) / 1000));
        } else if (nowDate.after(endDate)) {
            //秒杀已结束
            secKillStatus = 2;
            remainSeconds = -1;
        } else {
            //秒杀中
            secKillStatus = 1;
        }
        model.addAttribute("remainSeconds", remainSeconds);
        model.addAttribute("secKillStatus", secKillStatus);


        ValueOperations opsFV = redisTemplate.opsForValue();
        Object html = opsFV.get("html:goods:toDetail" + goodsId);

        if (null != html) return (String) html;

        WebContext context = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goodsDetail", context);
        opsFV.set("html:goods:toDetail" + goodsId, html, 10, TimeUnit.SECONDS);

        return (String) html;
    }


    @RequestMapping(value = "/detail/{goodsId}")
    @ResponseBody
    public RespBean detail2(@PathVariable Long goodsId, User user,
                            HttpServletRequest request,
                            HttpServletResponse response) {

        if (null == user) {
            CookieUtil.deleteCookie(request, response, "user_ticket");//过期cookie导致，清除cookie
            throw new GlobalException(RespTypeEnum.SESSION_NOT_EXIST);
            //用户不存在，此处在前端缺少一个重定向的动作
        }

        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);

        Date startDate = goodsVo.getStartDate();
        Date endDate = goodsVo.getEndDate();
        Date nowDate = new Date();
        //秒杀状态 0未开始，1进行中，2已结束
        int secKillStatus = 0;
        //秒杀倒计时 >0倒计时，0进行中，-1已结束
        int remainSeconds = 0;
        //秒杀还未开始
        if (nowDate.before(startDate)) {
            remainSeconds = ((int) ((startDate.getTime() - nowDate.getTime()) / 1000));
        } else if (nowDate.after(endDate)) {
            //秒杀已结束
            secKillStatus = 2;
            remainSeconds = -1;
        } else {
            //秒杀中
            secKillStatus = 1;
        }

        GoodsDetailVo goodsDetailVo = new GoodsDetailVo();
        goodsDetailVo.setGoodsVo(goodsVo);
        goodsDetailVo.setUser(user);
        goodsDetailVo.setRemainSeconds(remainSeconds);
        goodsDetailVo.setSecKillStatus(secKillStatus);

        return RespBean.success(goodsDetailVo);
    }

}
