package com.engure.seckill.controller;

import com.engure.seckill.service.IUserService;
import com.engure.seckill.vo.LoginVO;
import com.engure.seckill.vo.RespBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("login")
@Slf4j
public class LoginController {

    @Autowired
    private IUserService userService;

    @GetMapping("")
    public String login() {
        return "login";
    }

    @PostMapping("doLogin")
    @ResponseBody
    public RespBean doLogin(LoginVO vo) {

        RespBean resp = userService.doLogin(vo);

        return resp;
    }


}
