package com.leyou.order.web;

import com.leyou.order.dto.OrderDTO;
import com.leyou.order.pojo.Order;
import com.leyou.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@Slf4j
@RestController
@RequestMapping("order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<Long>createOrder(@RequestBody OrderDTO orderDTO){
        return ResponseEntity.ok(orderService.createOrder(orderDTO));
    }

    @GetMapping("{orderId}")
    public ResponseEntity<Order>queryOrderById(@PathVariable("orderId")Long id){
        return ResponseEntity.ok(orderService.queryOrderById(id));
    }
    @GetMapping("url/{id}")
    public ResponseEntity<String>createPayUrl(@PathVariable("id")Long orderId){
        log.info("[进入了 createPayUrl 方法]");
        return ResponseEntity.ok(orderService.createPayUrl(orderId));
    }
    @GetMapping("state/{id}")
    public ResponseEntity<Integer>queryOrderState(@PathVariable("id")Long id){
        return ResponseEntity.ok(orderService.queryOrderState(id).getValue());
    }



}
