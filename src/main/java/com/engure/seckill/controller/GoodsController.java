package com.engure.seckill.controller;

import com.engure.seckill.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/goods")
@Slf4j
public class GoodsController {

    @RequestMapping("/list")
    public String list(User user,       // 在 controller 入口之前做校验
                       Model model) {

        log.info(user == null ? "user=null" : user.toString());

        if (null == user) return "login";

        model.addAttribute("user", user);

        return "goodsList";
    }

}
