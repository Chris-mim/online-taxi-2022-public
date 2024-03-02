package com.mashibing.servicedriveruser.controller;

import com.mashibing.internalcommon.dto.DriverCarBindingRelationship;
import com.mashibing.internalcommon.dto.DriverUser;
import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.internalcommon.response.DriverUserExistsResponse;
import com.mashibing.internalcommon.response.OrderDriverResponse;
import com.mashibing.servicedriveruser.service.DriverCarBindingRelationshipService;
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

    /**
     * 检查手机号对应的司机是否存在
     * @param driverPhone
     * @return
     */
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


    /**
     * 根据车辆Id查询订单需要的司机信息
     * @param carId
     * @return
     */
    @GetMapping("/get-available-driver/{carId}")
    public ResponseResult<OrderDriverResponse> getAvailableDriver(@PathVariable("carId") Long carId){
        return driverUserService.getAvailableDriver(carId);
    }

    @Autowired
    DriverCarBindingRelationshipService driverCarBindingRelationshipService;

    /**
     * 根据司机手机号查询司机和车辆绑定关系
     * @param driverPhone
     * @return
     */
    @GetMapping("/driver-car-binding-relationship")
    public ResponseResult<DriverCarBindingRelationship> getDriverCarRelationShip(@RequestParam String driverPhone){
        return driverCarBindingRelationshipService.getDriverCarRelationShipByDriverPhone(driverPhone);
    }

}
