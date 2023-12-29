package com.mashibing.servicedriveruser.controller;

import com.mashibing.internalcommon.dto.DriverUser;
import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.internalcommon.response.DriverUserExistsResponse;
import com.mashibing.servicedriveruser.service.DriverUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    @Autowired
    private DriverUserService driverUserService;

    @PostMapping("/user")
    public ResponseResult addUser(@RequestBody DriverUser driverUser){
        return driverUserService.addDriverUser(driverUser);
    }
    @PutMapping("/user")
    public ResponseResult updateUser(@RequestBody DriverUser driverUser){
        return driverUserService.updateDriverUser(driverUser);
    }

    @GetMapping("/check-driver/{driverPhone}")
    public ResponseResult<DriverUserExistsResponse> getUser(@PathVariable("driverPhone") String driverPhone){
        DriverUser driverUserByPhone = driverUserService.getDriverUserByPhone(driverPhone);
        DriverUserExistsResponse driverUserExistsResponse = new DriverUserExistsResponse();
        driverUserExistsResponse.setDriverPhone(driverPhone);
        if(driverUserByPhone == null){
            driverUserExistsResponse.setIfExists(0);
        }else{
            driverUserExistsResponse.setIfExists(1);
        }
        return ResponseResult.success(driverUserExistsResponse);
    }

}
