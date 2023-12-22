package com.mashibing.servicemap.service;

import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.internalcommon.request.ForecastPriceDTO;
import com.mashibing.internalcommon.response.DirectionResponse;
import com.mashibing.servicemap.remote.MapDirectionClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DirectionService {

    @Autowired
    private MapDirectionClient mapDirectionClient;

    /**
     * 根据起点经纬度和终点经纬获取距离（米）和时长（分钟）
     * @param forecastPriceDTO forecastPriceDTO
     * @return 距离（米）和时长（分钟）
     */
    public ResponseResult<DirectionResponse> driving(ForecastPriceDTO forecastPriceDTO){
        // 调用地图服务接口
        DirectionResponse direction = mapDirectionClient.direction(forecastPriceDTO);


        return ResponseResult.success(direction);
    }
}
