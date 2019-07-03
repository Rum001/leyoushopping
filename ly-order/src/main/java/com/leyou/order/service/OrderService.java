package com.leyou.order.service;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.common.dto.CartDTO;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.IdWorker;
import com.leyou.item.pojo.Sku;
import com.leyou.order.client.AddressClient;
import com.leyou.order.client.GoodsClient;
import com.leyou.order.config.PayHelper;
import com.leyou.order.dto.AddressDTO;
import com.leyou.order.dto.OrderDTO;
import com.leyou.order.enums.OrderStatusEnum;
import com.leyou.order.enums.PayState;
import com.leyou.order.intetceptor.UserInterceptor;
import com.leyou.order.mapper.OrderDetailMapper;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderDetail;
import com.leyou.order.pojo.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper detailMapper;

    @Autowired
    private OrderStatusMapper statusMapper;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private PayHelper payHelper;

    @Transactional
    public Long createOrder(OrderDTO orderDTO) {
        //生存订单的Id
        IdWorker idWorker = new IdWorker();
        Long orderId=idWorker.nextId();

        //1 添加订单
        //1.1 订单的基本信息
        Order order = new Order();
        order.setOrderId(orderId);
        order.setCreateTime(new Date());
        order.setPostFee("0");
        order.setPaymentType(orderDTO.getPaymentType());

        //1.2用户的信息
        UserInfo user = UserInterceptor.getUser();
        order.setUserId(user.getId());
        order.setBuyerNick(user.getUsername());
        order.setBuyerRate(false);
        //1.3收货人信息
        AddressDTO address = AddressClient.findById(orderDTO.getAddressId());
        order.setReceiver(address.getName());
        order.setReceiverAddress(address.getAddress());
        order.setReceiverCity(address.getCity());
        order.setReceiverDistrict(address.getDistrict());
        order.setReceiverZip(address.getZipCode());
        order.setReceiverState(address.getState());
        order.setReceiverMobile(address.getPhone());
        //1.4总金额的计算
        Map<Long, Integer> mapCart = orderDTO.getCarts().stream().collect(Collectors.toMap(CartDTO::getSkuId, CartDTO::getNum));
        Set<Long> ids = mapCart.keySet();
        List<Sku> skus = goodsClient.querySkuListById(new ArrayList<>(ids));
        Long totalPay=0L;
        List<OrderDetail>details=new ArrayList<>();
        for (Sku sku : skus) {
            totalPay+=sku.getPrice()*mapCart.get(sku.getId());
            OrderDetail detail = new OrderDetail();
            detail.setImage(StringUtils.substringBefore(sku.getImages(),","));
            detail.setNum(mapCart.get(sku.getId()));
            detail.setOrderId(orderId);
            detail.setOwnSpec(sku.getOwnSpec());
            detail.setPrice(sku.getPrice());
            detail.setSkuId(sku.getId());
            detail.setTitle(sku.getTitle());
            details.add(detail);
        }
        //总金额
        order.setTotalPay(totalPay);
        //实际金额  总金额 +邮费 +优惠金额
        order.setActualPay(1L);
        int count= orderMapper.insertSelective(order);
        if (count!=1){
            throw new LyException(ExceptionEnum.ORDER_SAVE_ERROR);
        }
        //2 添加订单详情
        count=detailMapper.insertList(details);
        if (count!=details.size()){
            throw new LyException(ExceptionEnum.ORDERDETAIL_SAVE_ERROR);
        }
        //3 添加订单状态
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setCreateTime(order.getCreateTime());
        orderStatus.setOrderId(orderId);
        orderStatus.setStatus(OrderStatusEnum.UNPAY.value());
        count = statusMapper.insertSelective(orderStatus);
        if (count!=1){
            throw new LyException(ExceptionEnum.ORDER_STATUS_SAVE_ERROR);
        }
        //4减库存
        goodsClient.stockDecrease(orderDTO.getCarts());
        return orderId;
    }

    public Order queryOrderById(Long id) {
        //查询出订单
        Order order = orderMapper.selectByPrimaryKey(id);
        if (order == null) {
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }
        //订单详情
        OrderDetail detail = new OrderDetail();
        detail.setOrderId(id);
        List<OrderDetail> details = detailMapper.select(detail);
        if (CollectionUtils.isEmpty(details)){
            throw new LyException(ExceptionEnum.ORDER_DETAIL_NOT_FOUND);
        }
        order.setOrderDetails(details);
        //订单状态
        OrderStatus status = statusMapper.selectByPrimaryKey(id);
        if (status == null) {
            throw new LyException(ExceptionEnum.ORDER_STATUS_NOT_FOUND);
        }
        order.setStatus(status.getStatus());
        return order;
    }

    public String createPayUrl(Long orderId) {
        //查询订单
        Order order = queryOrderById(orderId);
        OrderDetail orderDetail = order.getOrderDetails().get(0);
//        if (order.getStatus().equals(OrderStatusEnum.UNPAY)){
//            throw new LyException(ExceptionEnum.ORDER_STATUS_ERROR);
//        }
        return payHelper.createOrder(orderId, order.getActualPay(), orderDetail.getTitle());
    }

    public void handlerNotify(Map<String, String> result) {
        //校验数据
        payHelper.isSuccess(result);
        //校验签名
        payHelper.checkSign(result);
        //校验金额
        String totalFeeStr = result.get("total_fee");
        Long totalFee=Long.valueOf(totalFeeStr);
        //获取到 订单的ID 查询出订单
        String tradeNo = result.get("out_trade_no");
        Long orderId=Long.valueOf(tradeNo);
        Order order = queryOrderById(orderId);
        if (totalFee!=/*order.getActualPay()*/1){
            throw new LyException(ExceptionEnum.INVALID_PARAM);
        }
        //修改订单状态
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setStatus(OrderStatusEnum.PAY_NOT_DELIVERD.value());
        orderStatus.setOrderId(orderId);
        orderStatus.setPaymentTime(new Date());
        int count = statusMapper.updateByPrimaryKeySelective(orderStatus);
        if (count!=1){
            throw new LyException(ExceptionEnum.INVALID_PARAM);
        }
        log.info("[订单回调成功]  订单编号:{}",orderId);
    }

    public PayState queryOrderState(Long id) {
        //查询出订单的状态
        OrderStatus orderStatus = statusMapper.selectByPrimaryKey(id);
        //如果 订单的状态不等于 1就已经支付
        if (orderStatus.getStatus()!=OrderStatusEnum.UNPAY.value()){
            return PayState.SUCCESS;
        }
        // 用户未支付不一定就是未支付 可能回调还没有去执行 所以查询微信的 支付状态
       return payHelper.orderQuery(id);
    }
}
