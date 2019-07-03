package com.leyou.user.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.NumberUtils;
import com.leyou.user.config.UserProperties;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import com.leyou.user.utils.CodecUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
@Service
@EnableConfigurationProperties(UserProperties.class)
public class UserService {

    @Autowired
    private UserProperties userProperties;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private AmqpTemplate amqpTemplate;

    private static final String KEY_PREFIX="USER:PHONE";
    public Boolean checkData(String data, Integer type) {
        User user = new User();
        //检查数据类型   1为用户名  2为手机号  其他结果则是参数有误
        switch (type){
            case 1:
                user.setUsername(data);
                break;
            case 2:
                user.setPassword(data);
                break;
            default:
                throw new LyException(ExceptionEnum.DATA_TYPE_ERROR);
        }
        return  userMapper.selectCount(user)==0;
    }

    public void sendCode(String phone) {
        String key=KEY_PREFIX+phone;
        //生成验证码
        String code= NumberUtils.generateCode(6);
        Map<String,String>map=new HashMap<>();
        map.put("phone",phone);
        map.put("code",code);
        //发送短信
//        amqpTemplate.convertAndSend(userProperties.getExChange(),userProperties.getRoutingKey(),map);
        //保存验证码 并 把验证码的有效时间设置为 5分钟
        redisTemplate.opsForValue().set(key,code,userProperties.getTimeOut(), TimeUnit.MINUTES);
    }

    public void register(User user, String code) {
        String key=KEY_PREFIX+user.getPhone();
        //对验证码进行判断
        if(!StringUtils.equals(redisTemplate.opsForValue().get(key),code)){
            throw new LyException(ExceptionEnum.VERIFICATION_CODE_ERROR);
        }
        //加盐
        String salt = CodecUtils.generateSalt();
        user.setSalt(salt);
        //对密码进行加密处理
        user.setPassword(CodecUtils.md5Hex(user.getPassword(),salt));
        user.setId(null);
        user.setCreated(new Date());
       Boolean flag=userMapper.insert(user)==1;
        if (!flag){
            throw new LyException(ExceptionEnum.USER_REGISTER_ERROR);
        }
        //把redis的验证码删除
        redisTemplate.delete(key);
    }

    public User queryUser(String username, String password) {
        User record = new User();
        record.setUsername(username);
        //判断用户是否存在
        User user = userMapper.selectOne(record);
        if (user==null){
            throw new LyException(ExceptionEnum.USERNAME_NOT_EXIST);
        }
        //判断密码是否正确
        password=CodecUtils.md5Hex(password,user.getSalt());
        System.out.println(user.getPassword());
        if (!password.equals(user.getPassword())){
            throw new LyException(ExceptionEnum.PASSWORD_WRONG);
        }
        //返回
        return user;
    }
}
