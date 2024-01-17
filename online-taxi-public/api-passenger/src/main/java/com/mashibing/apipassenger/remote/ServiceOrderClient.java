package com.mashibing.apipassenger.remote;

import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.internalcommon.request.OrderRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("service-order")
public interface ServiceOrderClient {

    @PostMapping("/order/add")
    ResponseResult add(@RequestBody OrderRequest orderRequest);


    @GetMapping("/order/test-dispatch-real-time-order")
    String dispatchRealTimeOrder(@RequestParam Long orderId);
}
