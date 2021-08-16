package com.engure.seckill.controller;

import com.engure.seckill.pojo.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/goods")
public class GoodsController {

    @RequestMapping("/list")
    public String list(@CookieValue(value = "user_ticket", required = false) String ticket,
                       Model model,
                       HttpSession session) {

        if (!StringUtils.hasLength(ticket)) return "login";

        User user = (User) session.getAttribute(ticket);

        if (null == user) return "login";

        model.addAttribute("user", user);

        return "goodsList";
    }

}
