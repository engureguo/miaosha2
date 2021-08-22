package com.engure.seckill.config;

import com.engure.seckill.pojo.User;
import com.engure.seckill.service.IUserService;
import com.engure.seckill.utils.CookieUtil;
import com.engure.seckill.utils.JsonUtil;
import com.engure.seckill.vo.RespBean;
import com.engure.seckill.vo.RespTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class AccessLimitInterceptor implements HandlerInterceptor {

    @Autowired
    private IUserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 1）拦截每个请求，解析请求，将用户信息放入 UseContext 的 threadlocal 中 </br>
     * 2）判断接口是否需要登陆
     * 3）如果需要登陆则进行限流
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws IOException
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {

        if (handler instanceof HandlerMethod) {

            HandlerMethod hm = (HandlerMethod) handler;
            AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);

            User user = getUserByRequest(request, response);
            UserContext.set(user);

            //方法没有使用 AccessLimit 标记
            //需要在此之前获取用户信息，有的接口不限流但需要 user，而 user 是从 UserContext 中取得
            if (accessLimit == null) return true;

            int count = accessLimit.count();
            int seconds = accessLimit.seconds();
            boolean needLogin = accessLimit.needLogin();

            String requestURI = request.getRequestURI();

            //需要登陆
            if (needLogin) {
                String key;
                if (user == null) {
                    render(response, RespBean.error(RespTypeEnum.SESSION_NOT_EXIST));
                    return false;
                } else {
                    key = requestURI + ":uid-" + user.getId();
                }

                //检查访问频率
                Integer count0 = (Integer) redisTemplate.opsForValue().get(key);
                if (null == count0) {
                    redisTemplate.opsForValue().set(key, 0, seconds, TimeUnit.SECONDS);
                } else if (count0 < count) {
                    redisTemplate.opsForValue().increment(key);
                } else {
                    render(response, RespBean.error(RespTypeEnum.ACCESS_LIMIT_REACHED));
                    return false;
                }
            }
            //对不需要登陆的接口不进行限制

            return true;
        }

        return true;
    }

    /**
     * 写入响应 RespBean，代替 @ResponseBody + 返回 RespBean
     *
     * @param response
     * @param error
     * @throws IOException
     */
    private void render(HttpServletResponse response, RespBean error) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        writer.write(JsonUtil.object2JsonStr(error));
        writer.flush();
        writer.close();
    }

    /**
     * 获取登录信息
     *
     * @param request
     * @param response
     * @return
     */
    private User getUserByRequest(HttpServletRequest request, HttpServletResponse response) {

        String ticket = CookieUtil.getCookieValue(request, "user_ticket");

        if (StringUtils.hasLength(ticket)) {
            return userService.getUserInfoByTicket(request, response, ticket);
        }

        return null;
    }
}
