package com.engure.seckill.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DemoSecKillController {

    /**
     * 测试页面跳转
     */
    @GetMapping("/hello")
    public String hello(Model model) {
        model.addAttribute("name", "hello");
        return "hello";
    }

    /**
     * 404
     *
     * @return
     */
    @GetMapping("/error/404")
    public String error() {
        return "error/404";
    }

}
