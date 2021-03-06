package com.leyou.cart.intetceptor;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.cart.config.JwtProperties;
import com.leyou.common.utils.CookieUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j

public class UserInterceptor implements HandlerInterceptor {
    private JwtProperties jwtProperties;

    private static ThreadLocal<UserInfo>tl=new ThreadLocal<>();

    public UserInterceptor(JwtProperties jwtProperties) {
        this.jwtProperties=jwtProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取token
        String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName());
        try{
            //解析token
            UserInfo user = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
            //将user 放入 threadLocal中
            tl.set(user);
            //放行
            return true;
        }catch (Exception e){
            log.error("[用户未登录]");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //清空 threadLocal
        tl.remove();
    }

    public static UserInfo getUser(){
        return tl.get();
    }
}
