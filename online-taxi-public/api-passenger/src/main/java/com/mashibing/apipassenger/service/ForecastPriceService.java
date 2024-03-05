package com.mashibing.apipassenger.service;

import com.mashibing.apipassenger.remote.ServicePriceClient;
import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.apipassenger.request.ForecastPriceDTO;
import com.mashibing.internalcommon.response.ForecastPriceResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ForecastPriceService {

    @Autowired
    private ServicePriceClient servicePriceClient;

    public ResponseResult<ForecastPriceResponse> forecastPrice(ForecastPriceDTO priceDTO) {
        log.info("出发地经度：" + priceDTO.getDepLongitude());
        log.info("出发地纬度：" + priceDTO.getDepLatitude());
        log.info("目的地经度：" + priceDTO.getDestLongitude());
        log.info("目的地纬度：" + priceDTO.getDestLatitude());

        log.info("调用计价服务，计算价格");

        return servicePriceClient.forecastPrice(priceDTO);
    }

}
