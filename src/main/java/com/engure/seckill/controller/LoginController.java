package com.engure.seckill.controller;

import com.engure.seckill.service.IUserService;
import com.engure.seckill.vo.LoginVO;
import com.engure.seckill.vo.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Controller
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private IUserService userService;

    /**
     * 登录界面
     *
     * @param ticket
     * @param session
     * @return
     */
    @GetMapping("")     // 写 / 是需要访问 8080/login/ 才通？？？
    public String login(
            @CookieValue(value = "user_ticket", required = false) String ticket,
            HttpSession session) {

        // 已登陆，防止二次登录
        if (StringUtils.hasLength(ticket) && session != null && session.getAttribute(ticket) != null)
            return "redirect:/goods/list";

        // 未登录则转发 login.html
        return "login";
    }

    /**
     * 登录身份验证接口，前提是用户没有登陆
     *
     * @param vo
     * @param request
     * @param response
     * @return
     */
    @PostMapping("/doLogin")
    @ResponseBody
    public RespBean doLogin(@Valid LoginVO vo, // 使用 @Valid 注解，进行入参校验
                            HttpServletRequest request,
                            HttpServletResponse response) {

        return userService.doLogin(vo, request, response);
    }


}
