package com.mashibing.serviceorder.service.impl;

import com.mashibing.internalcommon.constant.CommonStatusEnum;
import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.internalcommon.request.DriverGrabRequest;
import com.mashibing.serviceorder.service.GrabService;
import com.mashibing.serviceorder.service.OrderInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service("grabByRedisDiyService")
public class GrabByRedisDiyServiceImpl implements GrabService {

    @Autowired
    OrderInfoService orderInfoService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Override
    public ResponseResult grab(DriverGrabRequest driverGrabRequest) {
        ResponseResult grab = null;
        String orderId = driverGrabRequest.getOrderId()+"";
        String driverId = driverGrabRequest.getDriverId()+"";
        String key = orderId;
        // 设置加锁的key
        Boolean aBoolean = stringRedisTemplate.opsForValue().setIfAbsent(key, driverId,20, TimeUnit.SECONDS);

        if (aBoolean){
            System.out.println("开始锁redis diy");
            grab = orderInfoService.grab(driverGrabRequest);
            System.out.println("结束锁redis diy");

            stringRedisTemplate.delete(orderId);
        }else {
            grab = ResponseResult.fail(CommonStatusEnum.ORDER_GRABING.getCode(),CommonStatusEnum.ORDER_GRABING.getValue());
        }

        return grab;
    }
}
