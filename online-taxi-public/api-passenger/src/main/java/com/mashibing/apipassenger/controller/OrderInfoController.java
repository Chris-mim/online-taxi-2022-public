package com.mashibing.apipassenger.controller;

import com.mashibing.apipassenger.request.OrderRequest;
import com.mashibing.apipassenger.service.OrderInfoService;
import com.mashibing.internalcommon.constant.CommonStatusEnum;
import com.mashibing.internalcommon.constant.IdentityConstants;
import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.internalcommon.dto.TokenResult;
import com.mashibing.internalcommon.entity.OrderInfo;
import com.mashibing.internalcommon.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@RestController
@RequestMapping("/order")
@Validated
public class OrderInfoController {

    @Autowired
    private OrderInfoService orderInfoService;

    @PostMapping("/add")
    public ResponseResult add(@Validated @RequestBody OrderRequest orderRequest){
        return orderInfoService.add(orderRequest);
    }


    /**
     * 取消订单
     * @param orderId
     * @return
     */
    @PostMapping("/cancel")
    public ResponseResult cancel(@NotNull(message = "订单id不能为空") @Positive(message = "订单id格式不正确") @RequestParam Long orderId){
        return orderInfoService.cancel(orderId);
    }

    @GetMapping("/detail")
    public ResponseResult<OrderInfo> detail(@NotNull(message = "订单id不能为空") @Positive Long orderId){
        return orderInfoService.detail(orderId);
    }

    @GetMapping("/current")
    public ResponseResult<OrderInfo> currentOrder(HttpServletRequest httpServletRequest){
        String authorization = httpServletRequest.getHeader("Authorization");
        TokenResult tokenResult = JwtUtils.parseToken(authorization);
        String identity = tokenResult.getIdentity();
        if (!identity.equals(IdentityConstants.PASSENGER_IDENTITY)){
            return ResponseResult.fail(CommonStatusEnum.TOKEN_ERR);
        }
        String phone = tokenResult.getPhone();

        return orderInfoService.currentOrder(phone,IdentityConstants.PASSENGER_IDENTITY);
    }
}
