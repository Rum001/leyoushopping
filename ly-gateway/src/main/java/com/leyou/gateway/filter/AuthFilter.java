package com.leyou.gateway.filter;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.utils.CookieUtils;
import com.leyou.gateway.config.AllowPathProperties;
import com.leyou.gateway.config.JwtProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
@EnableConfigurationProperties({JwtProperties.class, AllowPathProperties.class})
public class AuthFilter extends ZuulFilter {

    @Autowired
    private JwtProperties jwtProperties;
    
    @Autowired
    private AllowPathProperties allowPathProperties;

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;  //过滤器的类型 前置过滤
    }

    @Override
    public int filterOrder() {
        return FilterConstants.PRE_DECORATION_FILTER_ORDER-1; //过滤的顺序
    }

    @Override
    public boolean shouldFilter() {
        //获取上下文
        RequestContext ctx = RequestContext.getCurrentContext();
        //获取 request
        HttpServletRequest request = ctx.getRequest();
        //获取路径
        String path = request.getRequestURI();
        return !isAllowPath(path); //是否过滤
    }

    private boolean isAllowPath(String path) {
        for (String allow : allowPathProperties.getAllowPath()) {
            if (path.startsWith(allow)){//判断allow 这个集合里面是否有 path的前缀
                return true;
            }
        }
        return false;
    }

    @Override
    public Object run() throws ZuulException { //过滤的逻辑
        //获取上下文
        RequestContext ctx = RequestContext.getCurrentContext();
        //获取 request
        HttpServletRequest request = ctx.getRequest();
        //获取 token
        String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName());
        try{
            //解析token
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
        }catch (Exception e){
            ctx.setSendZuulResponse(false); //拦截
            ctx.setResponseStatusCode(403);
        }
        return null;
    }

}
