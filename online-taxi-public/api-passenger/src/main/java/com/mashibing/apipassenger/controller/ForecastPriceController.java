package com.mashibing.apipassenger.controller;

import com.mashibing.apipassenger.service.ForecastPriceService;
import com.mashibing.internalcommon.request.ForecastPriceDTO;
import com.mashibing.internalcommon.dto.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ForecastPriceController {

    @Autowired
    private ForecastPriceService forecastPriceService;

    @PostMapping("/forecast-price")
    public ResponseResult forecastPrice(@RequestBody ForecastPriceDTO forecastPriceDTO){
        return forecastPriceService.forecastPrice(forecastPriceDTO);
    }
}
