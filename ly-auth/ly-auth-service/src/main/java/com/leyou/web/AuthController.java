package com.leyou.web;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.CookieUtils;
import com.leyou.config.JwtProperties;
import com.leyou.service.AuthService;
import com.leyou.user.pojo.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {
    @Autowired
    private AuthService authService;

    @Autowired
    private JwtProperties jwtProperties;

    @PostMapping("accredit")
    public ResponseEntity<Void>accredit(@RequestParam("username")String username,@RequestParam("password")String password, HttpServletRequest request, HttpServletResponse response){
       String token= authService.accredit(username,password);
       //判断token是否为空
       if (StringUtils.isBlank(token)){
           throw new LyException(ExceptionEnum.TOKEN_ERROR);
       }
       //把 获取到的信息 写入Cookie
        CookieUtils.setCookie(request,response,jwtProperties.getCookieName(),token,jwtProperties.getExpire());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("verify")
    public ResponseEntity<UserInfo>verify(@CookieValue("LY_TOKEN") String token,HttpServletRequest request,HttpServletResponse response){
        try{
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
            String newToken = JwtUtils.generateToken(userInfo, jwtProperties.getPrivateKey(), jwtProperties.getExpire());
            CookieUtils.setCookie(request,response,jwtProperties.getCookieName(),newToken,jwtProperties.getExpire());
            return ResponseEntity.ok(userInfo);
        }catch (Exception e){
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }
    }


}
