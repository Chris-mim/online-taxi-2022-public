package com.mashibing.serviceorder.controller;

import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.internalcommon.entity.OrderInfo;
import com.mashibing.internalcommon.request.OrderRequest;
import com.mashibing.serviceorder.mapper.OrderInfoMapper;
import com.mashibing.serviceorder.service.OrderInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderInfoController {

    @Autowired
    private OrderInfoService orderInfoService;

    @PostMapping("/add")
    public ResponseResult add(@RequestBody OrderRequest orderRequest, HttpServletRequest httpServletRequest) {
//        String deviceCode = httpServletRequest.getHeader(HeaderParamConstants.DEVICE_CODE);
//        orderRequest.setDeviceCode(deviceCode);
//        log.info("deviceCode: {}", deviceCode);

        return orderInfoService.add(orderRequest);
    }

    @RequestMapping("/testMapper")
    public OrderRequest testMapper() {
        return orderInfoService.testMapper();
    }


    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Value("${server.port}")
    private String port;

    @GetMapping("/test-dispatch-real-time-order")
    public String dispatchRealTimeOrder(@RequestParam Long orderId) {
        log.info("并发测试 端口: " + port + " orderId: " + orderId);
        OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
        orderInfoService.dispatchRealTimeOrder(orderInfo);
        return "dispatch-real-time-order";
    }
}
