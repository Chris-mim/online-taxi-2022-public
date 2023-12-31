package com.mashibing.servicedriveruser.service;

import com.mashibing.internalcommon.dto.Car;
import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.internalcommon.response.TerminalResponse;
import com.mashibing.servicedriveruser.mapper.CarMapper;
import com.mashibing.servicedriveruser.remote.TerminalClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CarService {

    @Autowired
    private CarMapper carMapper;

    @Autowired
    private TerminalClient terminalClient;

    public ResponseResult testGetCar() {
        Car car = carMapper.selectById(new Long(1584359540577861633L));
        return ResponseResult.success(car);
    }

    public ResponseResult addCar(Car car) {
        LocalDateTime now = LocalDateTime.now();
        car.setGmtCreate(now);
        car.setGmtModified(now);
        // 获取车辆的终端id：tid
        ResponseResult<TerminalResponse> terminal = terminalClient.add(car.getVehicleNo());
        String tid = terminal.getData().getTid();
        car.setTid(tid);

        // 获取车辆的轨迹id：trid

        carMapper.insert(car);
        return ResponseResult.success();
    }
}
