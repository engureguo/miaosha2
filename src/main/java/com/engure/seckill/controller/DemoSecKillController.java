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

    /////////////  default exchange  //////////////////////

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

    /////////////  fanout exchange  //////////////////////

    /**
     * 验证 fanout 模式
     *
     * @return
     */
    @GetMapping("/mq/fanout")
    @ResponseBody
    public RespBean mqFanout() {
        mqSender.sendByFanout("hello Fanout, are you broadcast?");
        return RespBean.success();
    }

    /////////////  direct exchange  //////////////////////

    /**
     * 验证 direct 模式
     *
     * @return
     */
    @GetMapping("/mq/direct01")
    @ResponseBody
    public RespBean mqDirect01() {
        mqSender.sendByDirect_a("这是一个A类型消息");
        return RespBean.success();
    }

    @GetMapping("/mq/direct02")
    @ResponseBody
    public RespBean mqDirect02() {
        mqSender.sendByDirect_b("这是一个A类型消息");
        return RespBean.success();
    }

}
