package com.mashibing.serviceorder.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mashibing.internalcommon.constant.CommonStatusEnum;
import com.mashibing.internalcommon.constant.OrderConstants;
import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.internalcommon.entity.OrderInfo;
import com.mashibing.internalcommon.request.OrderRequest;
import com.mashibing.serviceorder.mapper.OrderInfoMapper;
import com.mashibing.serviceorder.remote.ServicePriceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author zhangjj
 * @since 2024-01-04
 */
@Service
@Slf4j
public class OrderInfoService {

    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private ServicePriceClient servicePriceClient;


    public ResponseResult add(OrderRequest orderRequest) {
        // 获取最新计价规则，判断是否有变化
        ResponseResult<Boolean> isNew = servicePriceClient.isNew(orderRequest.getFareType(), orderRequest.getFareVersion());
        if (!isNew.getData()) {
            return ResponseResult.fail(CommonStatusEnum.PRICE_RULE_CHANGED);
        }

        if(isOrderGoingOn(orderRequest) > 0){
            return ResponseResult.fail(CommonStatusEnum.ORDER_GOING_ON);
        }

        OrderInfo orderInfo = new OrderInfo();
        BeanUtils.copyProperties(orderRequest, orderInfo);

        LocalDateTime now = LocalDateTime.now();
        orderInfo.setGmtCreate(now);
        orderInfo.setGmtModified(now);
        orderInfo.setOrderStatus(OrderConstants.ORDER_START);

        orderInfoMapper.insert(orderInfo);
        log.info("===============");
        return ResponseResult.success();

    }

    /**
     * 校验乘客是否存在正在进行的订单
     * @param orderRequest
     * @return
     */
    private Integer isOrderGoingOn(OrderRequest orderRequest) {
        // 校验乘客是否存在正在进行的订单
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("passenger_id", orderRequest.getPassengerId());
        queryWrapper.in("order_status",
                Arrays.asList(OrderConstants.ORDER_START,
                        OrderConstants.DRIVER_RECEIVE_ORDER,
                        OrderConstants.DRIVER_TO_PICK_UP_PASSENGER,
                        OrderConstants.DRIVER_ARRIVED_DEPARTURE,
                        OrderConstants.PICK_UP_PASSENGER,
                        OrderConstants.PASSENGER_GETOFF,
                        OrderConstants.TO_START_PAY
                ));
        Integer validOrderStatus = orderInfoMapper.selectCount(queryWrapper);
        return validOrderStatus;
    }

    public OrderRequest testMapper() {
        OrderRequest orderRequest = new OrderRequest();
        OrderInfo orderInfo = orderInfoMapper.selectById(1584370883330600970L);

        BeanUtils.copyProperties(orderInfo, orderRequest);
        return orderRequest;

    }
}
