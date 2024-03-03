package com.mashibing.apipassenger.remote;

import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.internalcommon.entity.OrderInfo;
import com.mashibing.internalcommon.request.OrderRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient("service-order")
public interface ServiceOrderClient {

    @PostMapping("/order/add")
    ResponseResult add(@RequestBody OrderRequest orderRequest);


    @GetMapping("/order/test-dispatch-real-time-order")
    String dispatchRealTimeOrder(@RequestParam Long orderId);

    /**
     * 订单取消
     * @param orderId
     * @param identity
     * @return
     */
    @PostMapping("/order/cancel")
    ResponseResult cancel(@RequestParam Long orderId, @RequestParam String identity);

    @RequestMapping(method = RequestMethod.GET, value = "/order/detail")
    public ResponseResult<OrderInfo> detail(@RequestParam Long orderId);
}
