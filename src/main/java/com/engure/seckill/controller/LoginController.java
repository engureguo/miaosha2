package com.engure.seckill.controller;

import com.engure.seckill.service.IUserService;
import com.engure.seckill.vo.LoginVO;
import com.engure.seckill.vo.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private IUserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ThymeleafViewResolver thymeleafViewResolver;

    /**
     * 登录界面
     *
     * @param ticket
     * @return
     */
    @GetMapping(value = "", produces = "text/html;charset=utf-8")     // 写 / 是需要访问 8080/login/ 才通？？？
    @ResponseBody
    public String login(@CookieValue(value = "user_ticket", required = false) String ticket,
                        HttpServletRequest request,
                        HttpServletResponse response) {

        // 已登陆，防止二次登录
        // session写法
        //if (StringUtils.hasLength(ticket) && session != null && session.getAttribute(ticket) != null)
        //    return "redirect:/goods/list";
        // redis写法
        /*if (StringUtils.hasLength(ticket)) {
            User user = userService.getUserInfoByTicket(ticket);
            if (user != null)
                return "redirect:/goods/toList";
        }

        // 未登录则转发 login.html
        return "login";*/

        if (StringUtils.hasLength(ticket)) {
            return "<script>location.href='/goods/toList'</script>";
        }

        ValueOperations opsFV = redisTemplate.opsForValue();
        Object html = opsFV.get("html:login");
        if (null == html) {
            WebContext context = new WebContext(request, response, request.getServletContext(), request.getLocale());
            html = thymeleafViewResolver.getTemplateEngine().process("login", context);
            opsFV.set("html:login", html, 10, TimeUnit.SECONDS);
        }

        return (String) html;
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
