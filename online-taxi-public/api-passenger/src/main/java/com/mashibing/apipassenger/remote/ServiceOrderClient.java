package com.mashibing.apipassenger.remote;

import com.mashibing.apipassenger.request.OrderRequest;
import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.internalcommon.entity.OrderInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient("service-order")
public interface ServiceOrderClient {

    @PostMapping("/order/add")
    ResponseResult add(@RequestBody OrderRequest orderRequest);

    @RequestMapping(method = RequestMethod.POST, value = "/order/book")
    public ResponseResult book(@RequestBody OrderRequest orderRequest);

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
    ResponseResult<OrderInfo> detail(@RequestParam Long orderId);

    @RequestMapping(method = RequestMethod.GET, value = "/order/current")
    ResponseResult current(@RequestParam String phone ,@RequestParam String identity);
}
