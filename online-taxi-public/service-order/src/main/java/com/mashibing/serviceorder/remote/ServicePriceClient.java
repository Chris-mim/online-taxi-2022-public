package com.mashibing.serviceorder.remote;

import com.mashibing.internalcommon.dto.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("service-price")
public interface ServicePriceClient {

    /**
     * 判断规则是否是最新
     * @param fareType
     * @param fareVersion
     * @return
     */
    @GetMapping("/price-rule/is-new")
    ResponseResult<Boolean> isNew(@RequestParam String fareType, @RequestParam Integer fareVersion);


}
