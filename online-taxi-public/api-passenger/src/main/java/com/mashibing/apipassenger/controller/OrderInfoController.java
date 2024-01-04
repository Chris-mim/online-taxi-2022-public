package com.mashibing.apipassenger.controller;

import com.mashibing.apipassenger.service.OrderInfoService;
import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.internalcommon.request.OrderRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
public class OrderInfoController {

    @Autowired
    private OrderInfoService orderInfoService;

    @PostMapping("/add")
    public ResponseResult add(@RequestBody OrderRequest orderRequest){
        return orderInfoService.add(orderRequest);
    }



}
