package com.mashibing.servicemap.service;

import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.internalcommon.request.ForecastPriceDTO;
import com.mashibing.internalcommon.response.DirectionResponse;
import org.springframework.stereotype.Service;

@Service
public class DirectionService {

    /**
     * 根据起点经纬度和终点经纬获取距离（米）和时长（分钟）
     * @param forecastPriceDTO forecastPriceDTO
     * @return 距离（米）和时长（分钟）
     */
    public ResponseResult driving(ForecastPriceDTO forecastPriceDTO){


        DirectionResponse directionResponse = new DirectionResponse();
        directionResponse.setDistance(123);
        directionResponse.setDuration(11);
        return ResponseResult.success(directionResponse);
    }
}
