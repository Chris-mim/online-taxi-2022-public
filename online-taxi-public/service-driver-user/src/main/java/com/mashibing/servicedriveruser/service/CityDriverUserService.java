package com.mashibing.servicedriveruser.service;

import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.servicedriveruser.mapper.DriverUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CityDriverUserService {
    @Autowired
    private DriverUserMapper driverUserMapper;

    /**
     * 判断当前城市是否存在可用的司机
     * @param cityCode cityCode
     * @return
     */
    public ResponseResult<Boolean> isAvailableDriver(String cityCode) {
        int count = driverUserMapper.selectAvailableDriverCount(cityCode);
        return ResponseResult.success(count > 0);
    }
}
