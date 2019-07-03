package com.leyou.order.config;

import com.github.wxpay.sdk.WXPayConfig;
import com.leyou.common.vo.PageResult;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Data
@Configuration
@EnableConfigurationProperties(PayProperties.class)
public class PayConfig implements WXPayConfig {

   @Autowired
    private PayProperties payProperties;


    @Override
    public String getAppID() {
        return payProperties.getAppId();
    }

    @Override
    public String getMchID() {
        return payProperties.getMchId();
    }

    @Override
    public String getKey() {
        return payProperties.getKey();
    }

    @Override
    public InputStream getCertStream() {
        return null;
    }

    @Override
    public int getHttpConnectTimeoutMs() {
        return payProperties.getHttpConnectTimeoutMs();
    }

    @Override
    public int getHttpReadTimeoutMs() {
        return payProperties.getHttpReadTimeoutMs();
    }
}
