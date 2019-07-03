package com.leyou.order.web;

import com.leyou.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("notify")
public class NotifyController {

    @Autowired
    private OrderService orderService;

    @PostMapping(value = "pay",produces = "application/xml")
    public Map<String,String> pay(@RequestBody Map<String,String> result){
        log.info("[进入了 pay 方法]");
        //处理通知
        orderService.handlerNotify(result);
        Map<String,String> msg=new HashMap<>();
        msg.put("return_code","SUCCESS");
        msg.put("return_msg","OK");
        return msg;
    }
}
