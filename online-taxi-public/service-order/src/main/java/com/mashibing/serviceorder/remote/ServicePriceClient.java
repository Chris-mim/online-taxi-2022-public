package com.mashibing.serviceorder.remote;

import com.mashibing.internalcommon.dto.PriceRule;
import com.mashibing.internalcommon.dto.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    /**
     * 根据城市编码和车型查询计价规则是否存在
     * @param priceRule
     * @return
     */
    @GetMapping("/price-rule/if-exists")
    ResponseResult<Boolean> ifPriceExists(@RequestBody PriceRule priceRule);
}
