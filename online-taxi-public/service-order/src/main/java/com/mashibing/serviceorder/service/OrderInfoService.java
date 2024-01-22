package com.mashibing.serviceorder.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mashibing.internalcommon.constant.CommonStatusEnum;
import com.mashibing.internalcommon.constant.IdentityConstants;
import com.mashibing.internalcommon.constant.OrderConstants;
import com.mashibing.internalcommon.dto.Car;
import com.mashibing.internalcommon.dto.PriceRule;
import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.internalcommon.entity.OrderInfo;
import com.mashibing.internalcommon.request.OrderRequest;
import com.mashibing.internalcommon.response.OrderDriverResponse;
import com.mashibing.internalcommon.response.TerminalResponse;
import com.mashibing.internalcommon.util.RedisPrefixUtils;
import com.mashibing.serviceorder.mapper.OrderInfoMapper;
import com.mashibing.serviceorder.remote.ServiceDriverUserClient;
import com.mashibing.serviceorder.remote.ServiceMapClient;
import com.mashibing.serviceorder.remote.ServicePriceClient;
import com.mashibing.serviceorder.remote.ServiceSsePushClient;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
    @Autowired
    private ServiceDriverUserClient serviceDriverUserClient;
    @Autowired
    private ServiceMapClient serviceMapClient;
    @Autowired
    private ServiceSsePushClient serviceSsePushClient;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    public ResponseResult add(OrderRequest orderRequest) {
        // 需要判断 下单的设备是否是 黑名单设备
        if (isBlackDevice(orderRequest.getDeviceCode())) {
            return ResponseResult.fail(CommonStatusEnum.DEVICE_IS_BLACK);
        }

        // 判断当前城市的司机是否可用
        ResponseResult<Boolean> isAvailableDriver = serviceDriverUserClient.isAvailableDriver(orderRequest.getAddress());
        log.info("测试城市是否有司机结果：" + isAvailableDriver.getData());
        if (!isAvailableDriver.getData()) {
            return ResponseResult.fail(CommonStatusEnum.CITY_DRIVER_EMPTY);
        }

        // 根据城市编码和车型查询计价规则是否存在
        if (!isPriceRuleExists(orderRequest)) {
            return ResponseResult.fail(CommonStatusEnum.CITY_SERVICE_NOT_SERVICE);
        }

        // 获取最新计价规则，判断是否有变化
        ResponseResult<Boolean> isNew = servicePriceClient.isNew(orderRequest.getFareType(), orderRequest.getFareVersion());
        if (!isNew.getData()) {
            return ResponseResult.fail(CommonStatusEnum.PRICE_RULE_CHANGED);
        }
        if (isPassengerOrderGoingOn(orderRequest.getPassengerId())) {
            return ResponseResult.fail(CommonStatusEnum.ORDER_GOING_ON);
        }

        OrderInfo orderInfo = new OrderInfo();
        BeanUtils.copyProperties(orderRequest, orderInfo);

        LocalDateTime now = LocalDateTime.now();
        orderInfo.setGmtCreate(now);
        orderInfo.setGmtModified(now);
        orderInfo.setOrderStatus(OrderConstants.ORDER_START);

        orderInfoMapper.insert(orderInfo);

        // 派单 dispatchRealTimeOrder
        dispatchRealTimeOrder(orderInfo);
        return ResponseResult.success();

    }

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 调用终端周边搜索接口，派发司机车辆订单
     *
     * @param orderInfo
     */
    public void dispatchRealTimeOrder(OrderInfo orderInfo) {
//        // 因为方法执行时间太短，使用synchronized无法复现集群并发问题，所以加长方法执行时间，成功复现
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
        List<Integer> radiusList = new ArrayList<>();
        radiusList.add(2000);
        radiusList.add(4000);
        radiusList.add(5000);
        String center = orderInfo.getDepLatitude() + "," + orderInfo.getDepLongitude();

        radius:
        for (int i = 0; i < radiusList.size(); i++) {
            Integer radius = radiusList.get(i);
            ResponseResult<List<TerminalResponse>> responseResult = serviceMapClient.aroundSearch(center, radius);
            log.info("在半径为" + radius + "的范围内，寻找车辆,结果：" + JSONArray.fromObject(responseResult.getData()).toString());
            // 获得终端
            List<TerminalResponse> terminalResponses = responseResult.getData();

            // 根据解析出来的终端，查询车辆信息
            for (int j = 0; j < terminalResponses.size(); j++) {
                TerminalResponse terminalResponse = terminalResponses.get(j);
                Long carId = terminalResponse.getCarId();
                // 查询是否有对于的可派单司机
                ResponseResult<OrderDriverResponse> availableDriver = serviceDriverUserClient.getAvailableDriver(carId);
                if (availableDriver.getCode() == CommonStatusEnum.AVAILABLE_DRIVER_EMPTY.getCode()) {
                    log.info("没有车辆ID：" + carId + ",对于的司机");
                    // 继续找符合条件的司机
                    continue;
                } else {
                    log.info("车辆ID：" + carId + "找到了正在出车的司机");
                    OrderDriverResponse driverResponse = availableDriver.getData();
                    // 判断司机是否有正在进行的订单
                    Long driverId = driverResponse.getDriverId();
                    // 单机锁司机ID小技巧:如果driverId在常量池内没有，则放入常量池 如果driverId字符串在常量池有则返回常量池里的地址，这里就锁住了同一个地址
//                    synchronized ((driverId+"").intern()){
                    String lockKey = driverId + "";
                    RLock lock = redissonClient.getLock(lockKey);

                    lock.lock();
                    // 有接着遍历下一个
                    if (isDriverOrderGoingOn(driverId)) {
                        lock.unlock();
                        log.info("司机有正在处理的订单");
                        continue;
                    }

                    // 匹配订单和司机

                    orderInfo.setDriverId(driverId);
                    orderInfo.setDriverPhone(driverResponse.getDriverPhone());
                    orderInfo.setCarId(carId);
                    // 从地图中来 接订单的经纬度
                    orderInfo.setReceiveOrderCarLongitude(terminalResponse.getLongitude());
                    orderInfo.setReceiveOrderCarLatitude(terminalResponse.getLatitude());

                    orderInfo.setReceiveOrderTime(LocalDateTime.now());
                    orderInfo.setLicenseId(driverResponse.getLicenseId());
                    orderInfo.setVehicleNo(driverResponse.getVehicleNo());
                    orderInfo.setOrderStatus(OrderConstants.DRIVER_RECEIVE_ORDER);

                    orderInfoMapper.updateById(orderInfo);
//                    }

                    // 给司机发送消息
                    JSONObject driverContent = new JSONObject();
                    driverContent.put("passengerId", orderInfo.getPassengerId());
                    driverContent.put("passengerPhone", orderInfo.getPassengerPhone());
                    driverContent.put("departure", orderInfo.getDeparture());
                    driverContent.put("depLongitude", orderInfo.getDepLongitude());
                    driverContent.put("depLatitude", orderInfo.getDepLatitude());

                    driverContent.put("destination", orderInfo.getDestination());
                    driverContent.put("destLongitude", orderInfo.getDestLongitude());
                    driverContent.put("destLatitude", orderInfo.getDestLatitude());
                    serviceSsePushClient.push(driverId, IdentityConstants.DRIVER_IDENTITY, driverContent.toString());

                    // 通知乘客
                    JSONObject passengerContent = new  JSONObject();
                    passengerContent.put("driverId",orderInfo.getDriverId());
                    passengerContent.put("driverPhone",orderInfo.getDriverPhone());
                    passengerContent.put("vehicleNo",orderInfo.getVehicleNo());
                    // 车辆信息，调用车辆服务
                    ResponseResult<Car> carById = serviceDriverUserClient.getCarById(carId);
                    Car carRemote = carById.getData();

                    passengerContent.put("brand", carRemote.getBrand());
                    passengerContent.put("model",carRemote.getModel());
                    passengerContent.put("vehicleColor",carRemote.getVehicleColor());

                    passengerContent.put("receiveOrderCarLongitude",orderInfo.getReceiveOrderCarLongitude());
                    passengerContent.put("receiveOrderCarLatitude",orderInfo.getReceiveOrderCarLatitude());

                    serviceSsePushClient.push(orderInfo.getPassengerId(), IdentityConstants.PASSENGER_IDENTITY,passengerContent.toString());

                    lock.unlock();

                    // 跳出最外层循环
                    break radius;
                }

            }

            // 找到符合的车辆，进行派单

            // 如果派单成功，则退出循环

        }
    }

    private Boolean isPriceRuleExists(OrderRequest orderRequest) {
        String fareType = orderRequest.getFareType();
        int index = fareType.indexOf("$");
        String cityCode = fareType.substring(0, index);
        String vehicleType = fareType.substring(index + 1);
        PriceRule priceRule = new PriceRule();
        priceRule.setCityCode(cityCode);
        priceRule.setVehicleType(vehicleType);
        ResponseResult<Boolean> booleanResponseResult = this.servicePriceClient.ifPriceExists(priceRule);
        return booleanResponseResult.getData();
    }

    private boolean isBlackDevice(String deviceCode) {
        String deviceCodeKey = RedisPrefixUtils.generatorBlackDeviceCodeKey(deviceCode);
        Boolean hasKey = stringRedisTemplate.hasKey(deviceCodeKey);
        if (hasKey) {
            String keyStr = stringRedisTemplate.opsForValue().get(deviceCodeKey);
            int count = Integer.parseInt(keyStr);
            if (count >= 2) {
                // 该设备超过下单次数
                return true;
            } else {
                stringRedisTemplate.opsForValue().increment(deviceCodeKey);
            }
        } else {
            stringRedisTemplate.opsForValue().set(deviceCodeKey, "1", 1, TimeUnit.HOURS);
        }
        return false;
    }

    /**
     * 校验乘客是否存在正在进行的订单
     *
     * @param passengerId
     * @return
     */
    private boolean isPassengerOrderGoingOn(Long passengerId) {
        // 校验乘客是否存在正在进行的订单
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("passenger_id", passengerId);
        queryWrapper.in("order_status",
                Arrays.asList(OrderConstants.ORDER_START,
                        OrderConstants.DRIVER_RECEIVE_ORDER,
                        OrderConstants.DRIVER_TO_PICK_UP_PASSENGER,
                        OrderConstants.DRIVER_ARRIVED_DEPARTURE,
                        OrderConstants.PICK_UP_PASSENGER,
                        OrderConstants.PASSENGER_GETOFF,
                        OrderConstants.TO_START_PAY
                ));
        return orderInfoMapper.selectCount(queryWrapper) > 0;
    }

    /**
     * 校验司机是否存在正在进行的订单
     *
     * @param driverId
     * @return
     */
    private boolean isDriverOrderGoingOn(Long driverId) {
        // 校验乘客是否存在正在进行的订单
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("driver_id", driverId);
        queryWrapper.in("order_status",
                Arrays.asList(
                        OrderConstants.DRIVER_RECEIVE_ORDER,
                        OrderConstants.DRIVER_TO_PICK_UP_PASSENGER,
                        OrderConstants.DRIVER_ARRIVED_DEPARTURE,
                        OrderConstants.PICK_UP_PASSENGER
                ));
        return orderInfoMapper.selectCount(queryWrapper) > 0;
    }

    public OrderRequest testMapper() {
        OrderRequest orderRequest = new OrderRequest();
        OrderInfo orderInfo = orderInfoMapper.selectById(1584370883330600970L);

        BeanUtils.copyProperties(orderInfo, orderRequest);
        return orderRequest;

    }
}
