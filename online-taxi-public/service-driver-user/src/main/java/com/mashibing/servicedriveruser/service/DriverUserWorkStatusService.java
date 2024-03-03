package com.mashibing.servicedriveruser.service;

import com.mashibing.internalcommon.dto.DriverUserWorkStatus;
import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.servicedriveruser.mapper.DriverUserWorkStatusMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zhangjj
 * @since 2023-12-30
 */
@Service
public class DriverUserWorkStatusService{

    @Autowired
    private DriverUserWorkStatusMapper driverUserWorkStatusMapper;
    public ResponseResult changeWorkStatus(DriverUserWorkStatus driverUserWorkStatus) {
        Map<String,Object> queryMap = new HashMap<>();
        queryMap.put("driver_id", driverUserWorkStatus.getDriverId());
        List<DriverUserWorkStatus> driverUserWorkStatuses = driverUserWorkStatusMapper.selectByMap(queryMap);
        DriverUserWorkStatus originWorkStatus = driverUserWorkStatuses.get(0);
        originWorkStatus.setWorkStatus(driverUserWorkStatus.getWorkStatus());
        originWorkStatus.setGmtModified(LocalDateTime.now());
        driverUserWorkStatusMapper.updateById(originWorkStatus);
        return ResponseResult.success();
    }

    public ResponseResult<DriverUserWorkStatus> getWorkStatus(Long driverId) {
        Map<String,Object> queryMap = new HashMap<>();
        queryMap.put("driver_id",driverId);
        List<DriverUserWorkStatus> driverUserWorkStatuses = driverUserWorkStatusMapper.selectByMap(queryMap);
        DriverUserWorkStatus driverUserWorkStatus = driverUserWorkStatuses.get(0);

        return ResponseResult.success(driverUserWorkStatus);

    }
}
