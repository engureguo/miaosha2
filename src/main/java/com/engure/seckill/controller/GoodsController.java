package com.engure.seckill.controller;

import com.engure.seckill.pojo.User;
import com.engure.seckill.service.IGoodsService;
import com.engure.seckill.vo.GoodsVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;
import java.util.List;


/**
 *
 * 商品控制器
 *
 *
 * 压测： /goods/toList
 *  QPS 1000 * 10:   前          后
 *      windows    1384
 *      linux       494
 *
 *
 *
 */


@Controller
@RequestMapping("/goods")
@Slf4j
public class GoodsController {

    @Autowired
    private IGoodsService goodsService;

    /**
     * 商品列表
     *
     * @param user
     * @param model
     * @return
     */
    @RequestMapping("/toList")
    public String list(User user,       // 在 controller 入口之前做校验
                       Model model) {

        if (null == user) return "login";

        model.addAttribute("user", user);//返回用户信息1

        List<GoodsVo> goodsVoList = goodsService.findAllGoodsVo();
        model.addAttribute("goodsList", goodsVoList);//返回商品信息2

        return "goodsList";
    }

    /**
     * 商品详情页
     *
     * @param goodsId
     * @param user
     * @param model
     * @return
     */
    @RequestMapping("/toDetail/{goodsId}")
    public String detail(@PathVariable Long goodsId, User user, Model model) {
        if (null == user) return "login";

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

        return "goodsDetail";
    }


}
