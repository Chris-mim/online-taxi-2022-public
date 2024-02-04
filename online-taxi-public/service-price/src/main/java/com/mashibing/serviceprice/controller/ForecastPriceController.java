package com.mashibing.serviceprice.controller;

import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.internalcommon.request.PriceDTO;
import com.mashibing.internalcommon.response.ForecastPriceResponse;
import com.mashibing.serviceprice.service.PriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ForecastPriceController {

    @Autowired
    private PriceService priceService;

    @PostMapping("/forecast-price")
    public ResponseResult<ForecastPriceResponse> forecastPrice(@RequestBody PriceDTO priceDTO){
        return priceService.forecastPrice(priceDTO);
    }

    /**
     * 计算实际价格
     * @param priceDTO
     * @return
     */
    @PostMapping("/calculate-price")
    public ResponseResult calculatePrice(@RequestBody PriceDTO priceDTO){
        return priceService.calculatePrice(priceDTO);
    }
}
