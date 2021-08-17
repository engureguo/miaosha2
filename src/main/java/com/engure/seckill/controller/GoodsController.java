package com.engure.seckill.controller;

import com.engure.seckill.pojo.User;
import com.engure.seckill.service.IGoodsService;
import com.engure.seckill.vo.GoodsVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/goods")
@Slf4j
public class GoodsController {

    @Autowired
    private IGoodsService goodsService;

    @RequestMapping("/toList")
    public String list(User user,       // 在 controller 入口之前做校验
                       Model model) {

        if (null == user) return "login";

        List<GoodsVo> goodsVoList = goodsService.findAllGoodsVo();
        model.addAttribute("goodsList", goodsVoList);

        return "goodsList";
    }

}
