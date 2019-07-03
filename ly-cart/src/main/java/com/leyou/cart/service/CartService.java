package com.leyou.cart.service;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.cart.intetceptor.UserInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String PREFIX_KEY="cart:uid:";
    public void addCart(Cart cart) {
        //获取用户
        UserInfo user = UserInterceptor.getUser();
        Integer num=cart.getNum();
        //判断购物车是否在redis中存在
        String key=PREFIX_KEY+user.getId();
        String hashKey=cart.getSkuId().toString();
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(key);
        //存在  则数量累加
        if (operations.hasKey(hashKey)){
            //取出 值
            String cacheCart = operations.get(hashKey).toString();
            //把他转为 cart对象
            cart=JsonUtils.parse(cacheCart,Cart.class);
            cart.setNum(cart.getNum()+num);
        }
        //不存在 则加入redis
        operations.put(hashKey,JsonUtils.serialize(cart));
    }

    public List<Cart> queryCartList() {
        //获取用户
        UserInfo user = UserInterceptor.getUser();
        String key=PREFIX_KEY+user.getId();
        //判断key 是否存在 如果不存在 则 告诉用户 购物车为空
        if (!redisTemplate.hasKey(key)){
            throw new LyException(ExceptionEnum.CART_NOT_FOUND);
        }
        //在redis中取出
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(key);
        return operations.values().stream().map(o -> JsonUtils.parse(o.toString(), Cart.class)).collect(Collectors.toList());
    }


    public void updateCartNum(Long skuId, Integer num) {
        //获取用户信息
        UserInfo user = UserInterceptor.getUser();
        String key=PREFIX_KEY+user.getId();
        String hashKey = skuId.toString();
        //判断key是否存在
        if (!redisTemplate.hasKey(key)) {
            throw new LyException(ExceptionEnum.CART_NOT_FOUND);
        }
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(key);
        Cart cart = JsonUtils.parse(operations.get(hashKey).toString(), Cart.class);
        cart.setNum(num);
        operations.put(hashKey,JsonUtils.serialize(cart));
    }

    public void deleteCart(Long skuId) {
        //获取用户信息
        UserInfo user = UserInterceptor.getUser();
        String key=PREFIX_KEY+user.getId();
        String hashKey = skuId.toString();
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(key);
        operations.delete(hashKey);
    }
}
