package com.mashibing.serviceprice.service;

import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.internalcommon.request.ForecastPriceDTO;
import com.mashibing.internalcommon.response.ForecastPriceResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ForecastPriceService {

    public ResponseResult forecastPrice(ForecastPriceDTO forecastPriceDTO) {
        log.info("出发地经度：" + forecastPriceDTO.getDepLongitude());
        log.info("出发地纬度：" + forecastPriceDTO.getDepLatitude());
        log.info("目的地经度：" + forecastPriceDTO.getDestLongitude());
        log.info("目的地纬度：" + forecastPriceDTO.getDestLatitude());

        log.info("调用地图服务，查询距离和时长");

        log.info("读取计价规则");

        log.info("根据距离、时长、计价规则，计算价格");


        ForecastPriceResponse forecastPriceResponse = new ForecastPriceResponse();
        forecastPriceResponse.setPrice(34);
        return ResponseResult.success(forecastPriceResponse);
    }

}
