package com.mashibing.servicedriveruser.service;

import com.mashibing.internalcommon.dto.Car;
import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.internalcommon.response.TerminalResponse;
import com.mashibing.internalcommon.response.TrackResponse;
import com.mashibing.servicedriveruser.mapper.CarMapper;
import com.mashibing.servicedriveruser.remote.ServiceMapClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CarService {

    @Autowired
    private CarMapper carMapper;

    @Autowired
    private ServiceMapClient terminalClient;

    public ResponseResult testGetCar() {
        Car car = carMapper.selectById(new Long(1584359540577861633L));
        return ResponseResult.success(car);
    }

    public ResponseResult addCar(Car car) {
        LocalDateTime now = LocalDateTime.now();
        car.setGmtCreate(now);
        car.setGmtModified(now);

        carMapper.insert(car);

        // 获取车辆的终端id：tid
        ResponseResult<TerminalResponse> terminal = terminalClient.addTerminal(car.getVehicleNo(), car.getId() + "");
        String tid = terminal.getData().getTid();
        car.setTid(tid);

        // 获取车辆的轨迹id：trid
        ResponseResult<TrackResponse> track = terminalClient.addTrack(tid);
        String trid = track.getData().getTrid();
        String trname = track.getData().getTrname();
        car.setTrid(trid);
        car.setTrname(trname);

        carMapper.updateById(car);

        return ResponseResult.success();
    }

    public ResponseResult<Car> getCatById(Long carId) {
        Car car = carMapper.selectById(carId);
        return ResponseResult.success(car);
    }
}
