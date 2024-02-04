package com.mashibing.apipassenger.remote;

import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.internalcommon.request.PriceDTO;
import com.mashibing.internalcommon.response.ForecastPriceResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("service-price")
public interface ServicePriceClient {

    @PostMapping("/forecast-price")
    ResponseResult<ForecastPriceResponse> forecastPrice(@RequestBody PriceDTO priceDTO);
}
