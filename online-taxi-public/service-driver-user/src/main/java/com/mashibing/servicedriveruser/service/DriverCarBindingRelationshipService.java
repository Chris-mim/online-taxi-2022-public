package com.mashibing.servicedriveruser.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mashibing.internalcommon.constant.CommonStatusEnum;
import com.mashibing.internalcommon.constant.DriverCarConstants;
import com.mashibing.internalcommon.dto.DriverCarBindingRelationship;
import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.servicedriveruser.mapper.DriverCarBindingRelationshipMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DriverCarBindingRelationshipService {

    @Autowired
    private DriverCarBindingRelationshipMapper driverCarBindingRelationshipMapper;

    public ResponseResult bind(DriverCarBindingRelationship driverCarBindingRelationship) {
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
}
