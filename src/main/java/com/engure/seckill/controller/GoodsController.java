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

import java.util.List;

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

        model.addAttribute("user", user);
        model.addAttribute("goods", goodsService.findGoodsVoByGoodsId(goodsId));

        return "goodsDetail";
    }


}
