package com.mashibing.apipassenger.controller;

import com.mashibing.apipassenger.remote.ServiceOrderClient;
import com.mashibing.internalcommon.dto.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class TestController {


    @GetMapping("/test")
    public String test(){
        return "test api passenger";
    }

    @GetMapping("/authTest")
    public ResponseResult authTest(){
        return ResponseResult.success("auth test");
    }
    @GetMapping("/noauthTest")
    public ResponseResult noauthTest(){
        return ResponseResult.success("noauth test");
    }

    @Autowired
    private ServiceOrderClient serviceOrderClient;
    @GetMapping("/order/test-dispatch-real-time-order")
    public String dispatchRealTimeOrder(@RequestParam Long orderId) {
        log.info("并发测试 api-passenger orderId: "+orderId);
        serviceOrderClient.dispatchRealTimeOrder(orderId);
        return "dispatch-real-time-order";
    }




}
