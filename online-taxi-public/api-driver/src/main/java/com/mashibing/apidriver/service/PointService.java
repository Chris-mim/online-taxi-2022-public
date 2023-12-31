package com.mashibing.apidriver.service;

import com.mashibing.apidriver.remote.ServiceDriverUserClient;
import com.mashibing.apidriver.remote.ServiceMapClient;
import com.mashibing.internalcommon.dto.Car;
import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.internalcommon.request.PointRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PointService {


    @Autowired
    private ServiceDriverUserClient serviceDriverUserClient;
    @Autowired
    private ServiceMapClient serviceMapClient;
    public ResponseResult upload(PointRequest pointRequest) {
        // 1、根据carId，查询车辆对应的终端tid和轨迹trid
        ResponseResult<Car> carResponseResult = serviceDriverUserClient.getCatById(pointRequest.getCarId());
        Car car = carResponseResult.getData();
        String tid = car.getTid();
        String trid = car.getTrid();

        // 2、上传轨迹点
        pointRequest.setTid(tid);
        pointRequest.setTrid(trid);

        return serviceMapClient.uploadPoint(pointRequest);
    }
}
