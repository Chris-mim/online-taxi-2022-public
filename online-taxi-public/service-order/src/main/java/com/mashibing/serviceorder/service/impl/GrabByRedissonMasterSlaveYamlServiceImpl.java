package com.mashibing.serviceorder.service.impl;

import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.internalcommon.request.DriverGrabRequest;
import com.mashibing.serviceorder.service.GrabService;
import com.mashibing.serviceorder.service.OrderInfoService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("grabByRedissonMasterSlaveYamlService")
public class GrabByRedissonMasterSlaveYamlServiceImpl implements GrabService {

    @Autowired
    OrderInfoService orderInfoService;

    @Autowired
    @Qualifier("redissonMasterSlaveClient")
    RedissonClient redissonClient;

    @Override
    public ResponseResult grab(DriverGrabRequest driverGrabRequest) {

        String orderId = driverGrabRequest.getOrderId()+"";

        String key = orderId;

        RLock lock = redissonClient.getLock(key);
        lock.lock();

        System.out.println("开始锁redis redisson master-slave yaml");

        ResponseResult grab = orderInfoService.grab(driverGrabRequest);
        System.out.println("结束锁redis redisson master-slave yaml");
        lock.unlock();

        return grab;
    }
}
