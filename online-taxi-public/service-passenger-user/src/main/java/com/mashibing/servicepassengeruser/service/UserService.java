package com.mashibing.servicepassengeruser.service;

import com.mashibing.internalcommon.constant.ResponseResult;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    public ResponseResult loginOrRegister(String passengerPhone) {
        System.out.println("user service被调用，手机号："+passengerPhone);

        // 根据手机号查询用户信息

        // 判断用户信息是否存在

        // 如果不存在，插入用户信息

        return ResponseResult.success();
    }
}
