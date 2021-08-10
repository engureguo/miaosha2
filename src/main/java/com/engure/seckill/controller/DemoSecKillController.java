package com.engure.seckill.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sk")
public class DemoSecKillController {

    /**
     * 测试页面跳转
     */
    @GetMapping("/hello")
    public String hello(Model model) {
        model.addAttribute("name", "hello");
        return "hello";
    }

}
