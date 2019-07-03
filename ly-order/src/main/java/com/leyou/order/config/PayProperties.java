package com.leyou.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author: HuYi.Zhang
 * @create: 2018-06-07 11:38
 **/
@Data
@ConfigurationProperties(prefix = "ly.pay")
public class PayProperties {

    private String appId; // 公众账号ID

    private String mchId; // 商户号

    private String key; // 生成签名的密钥

    private int httpConnectTimeoutMs; // 连接超时时间

    private int httpReadTimeoutMs;// 读取超时时间

    private String notifyUrl;//回调的地址


}
