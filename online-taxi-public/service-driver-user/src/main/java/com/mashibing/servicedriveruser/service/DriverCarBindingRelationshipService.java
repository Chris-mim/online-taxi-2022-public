package com.mashibing.servicedriveruser.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mashibing.internalcommon.constant.CommonStatusEnum;
import com.mashibing.internalcommon.constant.DriverCarConstants;
import com.mashibing.internalcommon.dto.DriverCarBindingRelationship;
import com.mashibing.internalcommon.dto.DriverUser;
import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.servicedriveruser.mapper.DriverCarBindingRelationshipMapper;
import com.mashibing.servicedriveruser.mapper.DriverUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@Service
public class DriverCarBindingRelationshipService {

    @Autowired
    private DriverCarBindingRelationshipMapper driverCarBindingRelationshipMapper;

    @Autowired
    DriverUserMapper driverUserMapper;

    public ResponseResult bind(DriverCarBindingRelationship driverCarBindingRelationship) {
        // 查询数据库是否存在司机车辆绑定关系，存在不允许再次绑定
        QueryWrapper<DriverCarBindingRelationship> queryWrapper = new QueryWrapper();
        queryWrapper.eq("driver_id",driverCarBindingRelationship.getDriverId());
        queryWrapper.eq("car_id",driverCarBindingRelationship.getCarId());
        queryWrapper.eq("bind_state",DriverCarConstants.DRIVER_CAR_BIND);
        Integer count = driverCarBindingRelationshipMapper.selectCount(queryWrapper);
        if (count > 0){
            // 司机和车辆绑定关系已存在，请勿重复绑定
            return ResponseResult.fail(CommonStatusEnum.DRIVER_CAR_BIND_EXISTS);
        }

        // 司机已经被绑定了，请勿重复绑定
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("driver_id",driverCarBindingRelationship.getDriverId());
        queryWrapper.eq("bind_state",DriverCarConstants.DRIVER_CAR_BIND);
         count = driverCarBindingRelationshipMapper.selectCount(queryWrapper);
        if (count > 0){
            // 司机已经被绑定了，请勿重复绑定
            return ResponseResult.fail(CommonStatusEnum.DRIVER_BIND_EXISTS);
        }
        // 车辆已经被绑定了，请勿重复绑定
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("car_id",driverCarBindingRelationship.getCarId());
        queryWrapper.eq("bind_state",DriverCarConstants.DRIVER_CAR_BIND);
        count = driverCarBindingRelationshipMapper.selectCount(queryWrapper);
        if (count > 0){
            // 车辆已经被绑定了，请勿重复绑定
            return ResponseResult.fail(CommonStatusEnum.CAR_BIND_EXISTS);
        }

        driverCarBindingRelationship.setBindingTime(LocalDateTime.now());
        driverCarBindingRelationship.setBindState(DriverCarConstants.DRIVER_CAR_BIND);
        driverCarBindingRelationshipMapper.insert(driverCarBindingRelationship);
        return ResponseResult.success();
    }

    public ResponseResult unbind(DriverCarBindingRelationship driverCarBindingRelationship) {
        // 查询数据库是否存在司机车辆绑定关系
        QueryWrapper<DriverCarBindingRelationship> queryWrapper = new QueryWrapper();
        queryWrapper.eq("driver_id",driverCarBindingRelationship.getDriverId());
        queryWrapper.eq("car_id",driverCarBindingRelationship.getCarId());
        queryWrapper.eq("bind_state",DriverCarConstants.DRIVER_CAR_BIND);
        DriverCarBindingRelationship relationship = driverCarBindingRelationshipMapper.selectOne(queryWrapper);
        if(relationship == null){
            return ResponseResult.fail(CommonStatusEnum.DRIVER_CAR_BIND_NOT_EXISTS);
        }
        relationship.setUnBindingTime(LocalDateTime.now());
        relationship.setBindState(DriverCarConstants.DRIVER_CAR_UNBIND);
        driverCarBindingRelationshipMapper.updateById(relationship);

        return ResponseResult.success();
    }


    public ResponseResult<DriverCarBindingRelationship> getDriverCarRelationShipByDriverPhone(@RequestParam String driverPhone){
        QueryWrapper<DriverUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("driver_phone",driverPhone);

        DriverUser driverUser = driverUserMapper.selectOne(queryWrapper);
        Long driverId = driverUser.getId();

        QueryWrapper<DriverCarBindingRelationship> driverCarBindingRelationshipQueryWrapper = new QueryWrapper<>();
        driverCarBindingRelationshipQueryWrapper.eq("driver_id",driverId);
        driverCarBindingRelationshipQueryWrapper.eq("bind_state",DriverCarConstants.DRIVER_CAR_BIND);

        DriverCarBindingRelationship driverCarBindingRelationship = driverCarBindingRelationshipMapper.selectOne(driverCarBindingRelationshipQueryWrapper);
        return ResponseResult.success(driverCarBindingRelationship);

    }
}
