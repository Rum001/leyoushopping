package com.leyou.test;


import com.leyou.user.pojo.User;
import com.leyou.user.utils.CodecUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class queryUser  {

    @Test
    public void test(){
        User user = new User();
        user.setPassword("leyou");
        String salt = "c46c663c1b2347d8b8feee449bb1d1e0";
        String pulsKey=CodecUtils.md5Hex(user.getPassword(),salt);
        String key=CodecUtils.md5Hex(user.getPassword(),salt);
        System.out.println(key);
        if (key.equals(pulsKey)){
            System.out.println("OK");
        }

    }
    @Test
    public void tes(){
        String s="4635.9";
        Double ss =Double.valueOf(s);
        System.out.println(ss);
    }
}
