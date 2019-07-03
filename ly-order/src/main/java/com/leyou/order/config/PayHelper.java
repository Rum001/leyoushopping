package com.leyou.order.config;

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.order.enums.OrderStatusEnum;
import com.leyou.order.enums.PayState;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderStatus;
import com.leyou.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class PayHelper {

    @Autowired
    private PayConfig config;
    @Autowired
    private WXPay wxPay;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderStatusMapper statusMapper;

    public String createOrder(Long orderId, Long totalPay,String desc) {
        try {
            Map<String, String> data = new HashMap<>();
            // 商品描述
            data.put("body", desc);
            // 订单号
            data.put("out_trade_no", orderId.toString());
            //金额，单位是分
            data.put("total_fee", totalPay.toString());
            //调用微信支付的终端IP（estore商城的IP）
            data.put("spbill_create_ip", "127.0.0.1");
            //回调地址
            data.put("notify_url", config.getPayProperties().getNotifyUrl());
            // 交易类型为扫码支付
            data.put("trade_type", "NATIVE");
            Map<String, String> result = this.wxPay.unifiedOrder(data);
            isSuccess(result);
            //下单成功获取支付的连接
            String codeUrl = result.get("code_url");
            return codeUrl;
        } catch (Exception e) {
            log.error("创建预交易订单异常", e);
            return null;
        }
    }

    public void isSuccess(Map<String, String> result) {
        if (WXPayConstants.FAIL.equals(result.get("return_code"))){
            log.error("[微信下单] 下单失败 原因{}",result.get("return_msg"));
            throw new LyException(ExceptionEnum.WXPAY_OREDER_ERROR);
        }
        //业务结果的标识
        if (WXPayConstants.FAIL.equals(result.get("result_code"))){
            log.error("[微信下单] 下单失败 错误代码{} ,描述{}",result.get("err_code"),result.get("err_code_des"));
            throw new LyException(ExceptionEnum.WXPAY_OREDER_ERROR);
        }
    }

    public void checkSign(Map<String, String> result) {
        try{
            //重新生成签名
            String sign1 = WXPayUtil.generateSignature(result, this.config.getKey(), WXPayConstants.SignType.HMACSHA256);
            String sign2 = WXPayUtil.generateSignature(result, this.config.getKey(), WXPayConstants.SignType.MD5);
            //判断签名是否和传过来的签名一致
            String sign = result.get("sign");
            if (!StringUtils.equals(sign1,sign)&&!StringUtils.equals(sign2,sign)){
                throw new LyException(ExceptionEnum.INVALID_SIGN_ERROR);
            }
        }catch (Exception e){
            throw new LyException(ExceptionEnum.INVALID_SIGN_ERROR);
        }
    }

    public PayState orderQuery(Long id) {
        Map<String, String> data = new HashMap<>();
        // 订单号
        data.put("out_trade_no", id.toString());
        try{
            Map<String, String> result = wxPay.orderQuery(data);
            //校验订单
            isSuccess(result);
            //校验签名
            checkSign(result);
            //校验金额
            String totalFeeStr = result.get("total_fee");
            Long totalFee=Long.valueOf(totalFeeStr);
            Order order = orderMapper.selectByPrimaryKey(id);
            if (totalFee!=/*order.getActualPay()*/1){
                throw new LyException(ExceptionEnum.INVALID_PARAM);
            }
            //判断交易的状态
            /**
             * SUCCESS—支付成功
             *
             * REFUND—转入退款
             *
             * NOTPAY—未支付
             *
             * CLOSED—已关闭
             *
             * REVOKED—已撤销（刷卡支付）
             *
             * USERPAYING--用户支付中
             *
             * PAYERROR--支付失败(其他原因，如银行返回失败)
             */
            String tradeState = result.get("trade_state");
            //如果相同则支付成功 改支付的状态
            if ("SUCCESS".equals(tradeState)){
                OrderStatus orderStatus = new OrderStatus();
                orderStatus.setOrderId(id);
                orderStatus.setPaymentTime(new Date());
                orderStatus.setStatus(OrderStatusEnum.PAY_NOT_DELIVERD.value());
                int count = statusMapper.updateByPrimaryKeySelective(orderStatus);
                if (count!=1){
                    throw new LyException(ExceptionEnum.INVALID_PARAM);
                }
                return PayState.SUCCESS;
            }
            if (tradeState.equals("NOTPAY")||tradeState.equals("USERPAYING")){
                return PayState.NOTPAY;
            }
            return PayState.FAIL;

        }catch (Exception e){
            return PayState.NOTPAY;
        }
    }
}
