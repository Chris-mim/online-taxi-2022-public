package com.mashibing.serviceprice.remote;

import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.internalcommon.request.PriceDTO;
import com.mashibing.internalcommon.response.DirectionResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("service-map")
public interface ServiceMapClient {

    @GetMapping("/direction/driving")
    ResponseResult<DirectionResponse> driving(@RequestBody PriceDTO priceDTO);


}
