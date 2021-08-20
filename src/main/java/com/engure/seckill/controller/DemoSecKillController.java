package com.engure.seckill.controller;

import com.engure.seckill.rabbitmq.MQSender;
import com.engure.seckill.vo.RespBean;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class DemoSecKillController {

    @Autowired
    private MQSender mqSender;

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

    /**
     * 测试发送 rabbitmq 消息
     *
     * @return
     */
    @GetMapping("/mq")
    @ResponseBody
    public RespBean mq() {
        mqSender.send("hello RabbitMQ ~");
        return RespBean.success();
    }

}
