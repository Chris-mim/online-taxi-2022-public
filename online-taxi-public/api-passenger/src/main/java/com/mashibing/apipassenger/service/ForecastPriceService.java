package com.mashibing.apipassenger.service;

import com.mashibing.apipassenger.remote.ServicePriceClient;
import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.internalcommon.request.ForecastPriceDTO;
import com.mashibing.internalcommon.response.ForecastPriceResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ForecastPriceService {

    @Autowired
    private ServicePriceClient servicePriceClient;

    public ResponseResult<ForecastPriceResponse> forecastPrice(ForecastPriceDTO forecastPriceDTO) {
        log.info("出发地经度：" + forecastPriceDTO.getDepLongitude());
        log.info("出发地纬度：" + forecastPriceDTO.getDepLatitude());
        log.info("目的地经度：" + forecastPriceDTO.getDestLongitude());
        log.info("目的地纬度：" + forecastPriceDTO.getDestLatitude());

        log.info("调用计价服务，计算价格");

        return servicePriceClient.forecastPrice(forecastPriceDTO);
    }

}
