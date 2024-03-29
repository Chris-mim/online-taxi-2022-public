package com.mashibing.apidriver.remote;

import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.internalcommon.entity.OrderInfo;
import com.mashibing.internalcommon.request.DriverGrabRequest;
import com.mashibing.internalcommon.request.OrderRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient("service-order")
public interface ServiceOrderClient {

    /**
     * 去接乘客
     * @param orderRequest
     * @return
     */
    @PostMapping("/order/to-pick-up-passenger")
    ResponseResult toPickUpPassenger(@RequestBody OrderRequest orderRequest);

    /**
     * 到达乘客上车点
     * @param orderRequest
     * @return
     */
    @PostMapping("/order/arrived-departure")
    ResponseResult arrivedDeparture(@RequestBody OrderRequest orderRequest);

    /**
     * 司机接到乘客
     * @param orderRequest
     * @return
     */
    @PostMapping("/order/pick-up-passenger")
    ResponseResult pickUpPassenger(@RequestBody OrderRequest orderRequest);

    /**
     * 乘客到达目的地，行程终止
     * @param orderRequest
     * @return
     */
    @PostMapping("/order/passenger-getoff")
    ResponseResult passengerGetoff(@RequestBody OrderRequest orderRequest);

    /**
     * 订单取消
     * @param orderId
     * @param identity
     * @return
     */
    @PostMapping("/order/cancel")
    ResponseResult cancel(@RequestParam Long orderId, @RequestParam String identity);

    @PostMapping("/order/push-pay-info")
    ResponseResult pushPayInfo(@RequestBody OrderRequest orderRequest);

    @RequestMapping(method = RequestMethod.GET, value = "/order/detail")
    ResponseResult<OrderInfo> detail(@RequestParam Long orderId);

    @RequestMapping(method = RequestMethod.GET, value = "/order/current")
    ResponseResult current(@RequestParam String phone ,@RequestParam String identity);

    @PostMapping(value = "/order/grab"  )
    public ResponseResult driverGrab(@RequestBody DriverGrabRequest driverGrabRequest);
}
