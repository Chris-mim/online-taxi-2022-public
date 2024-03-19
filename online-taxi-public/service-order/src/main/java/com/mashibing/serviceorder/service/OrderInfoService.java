package com.mashibing.serviceorder.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mashibing.internalcommon.constant.CommonStatusEnum;
import com.mashibing.internalcommon.constant.IdentityConstants;
import com.mashibing.internalcommon.constant.OrderConstants;
import com.mashibing.internalcommon.dto.Car;
import com.mashibing.internalcommon.dto.PriceRule;
import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.internalcommon.entity.OrderInfo;
import com.mashibing.internalcommon.request.DriverGrabRequest;
import com.mashibing.internalcommon.request.OrderRequest;
import com.mashibing.internalcommon.request.PriceDTO;
import com.mashibing.internalcommon.request.PushRequest;
import com.mashibing.internalcommon.response.OrderDriverResponse;
import com.mashibing.internalcommon.response.TerminalResponse;
import com.mashibing.internalcommon.response.TrsearchResponse;
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
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
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

    /**
     * 新建订单
     * @param orderRequest
     * @return
     */
    public ResponseResult add(OrderRequest orderRequest) {
        // 需要判断 下单的设备是否是 黑名单设备
//        if (isBlackDevice(orderRequest.getDeviceCode())) {
//            return ResponseResult.fail(CommonStatusEnum.DEVICE_IS_BLACK);
//        }

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


        // 定时任务的处理
        for (int i =0;i<6;i++) {
            // 派单 dispatchRealTimeOrder
            int result = dispatchRealTimeOrder(orderInfo);
            if (result == 1) {
                break;
            }
            if (i == 5) {
                // 订单无效
                orderInfo.setOrderStatus(OrderConstants.ORDER_INVALID);
                orderInfoMapper.updateById(orderInfo);
            } else {
                // 等待20s
                try {
                    Thread.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

        return ResponseResult.success();

    }

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 调用终端周边搜索接口，派发司机车辆订单
     * 如果返回1：派单成功
     * @param orderInfo
     */
    public int dispatchRealTimeOrder(OrderInfo orderInfo) {
//        // 因为方法执行时间太短，使用synchronized无法复现集群并发问题，所以加长方法执行时间，成功复现
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }

        log.info("循环一次");
        List<Integer> radiusList = new ArrayList<>();
        radiusList.add(2000);
        radiusList.add(4000);
        radiusList.add(5000);
        String center = orderInfo.getDepLatitude() + "," + orderInfo.getDepLongitude();

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

                    String vehicleTypeFromCar = driverResponse.getVehicleType();

                    // 判断车辆的车型是否符合？
                    String vehicleType = orderInfo.getVehicleType();
                    if (!vehicleType.trim().equals(vehicleTypeFromCar.trim())){
                        log.info("车型不符合");
                        continue ;
                    }

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
                    driverContent.put("orderId", orderInfo.getId());
                    driverContent.put("passengerId", orderInfo.getPassengerId());
                    driverContent.put("passengerPhone", orderInfo.getPassengerPhone());
                    driverContent.put("departure", orderInfo.getDeparture());
                    driverContent.put("depLongitude", orderInfo.getDepLongitude());
                    driverContent.put("depLatitude", orderInfo.getDepLatitude());

                    driverContent.put("destination", orderInfo.getDestination());
                    driverContent.put("destLongitude", orderInfo.getDestLongitude());
                    driverContent.put("destLatitude", orderInfo.getDestLatitude());

                    PushRequest pushRequest = new PushRequest();
                    pushRequest.setUserId(driverId);
                    pushRequest.setIdentity(IdentityConstants.DRIVER_IDENTITY);
                    pushRequest.setContent(driverContent.toString());

                    serviceSsePushClient.push(pushRequest);
                    // 通知乘客
                    JSONObject passengerContent = new  JSONObject();
                    passengerContent.put("orderId", orderInfo.getId());
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

                    PushRequest pushRequest1 = new PushRequest();
                    pushRequest1.setUserId(orderInfo.getPassengerId());
                    pushRequest1.setIdentity(IdentityConstants.PASSENGER_IDENTITY);
                    pushRequest1.setContent(passengerContent.toString());

                    serviceSsePushClient.push(pushRequest1);

                    lock.unlock();

                    // 分配到司机
                    return 1;

                }

            }

        }
        return 0;

    }
    /**
     * 计价规则是否存在
     * @param orderRequest
     * @return
     */
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
    /**
     * 是否是黑名单
     * @param deviceCode
     * @return
     */
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

    /**
     * 去接乘客
     * @param orderRequest
     * @return
     */
    public ResponseResult toPickUpPassenger(OrderRequest orderRequest){
        Long orderId = orderRequest.getOrderId();
        LocalDateTime toPickUpPassengerTime = orderRequest.getToPickUpPassengerTime();
        String toPickUpPassengerLongitude = orderRequest.getToPickUpPassengerLongitude();
        String toPickUpPassengerLatitude = orderRequest.getToPickUpPassengerLatitude();
        String toPickUpPassengerAddress = orderRequest.getToPickUpPassengerAddress();
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id",orderId);
        OrderInfo orderInfo = orderInfoMapper.selectOne(queryWrapper);

        orderInfo.setToPickUpPassengerAddress(toPickUpPassengerAddress);
        orderInfo.setToPickUpPassengerLatitude(toPickUpPassengerLatitude);
        orderInfo.setToPickUpPassengerLongitude(toPickUpPassengerLongitude);
        orderInfo.setToPickUpPassengerTime(LocalDateTime.now());
        orderInfo.setOrderStatus(OrderConstants.DRIVER_TO_PICK_UP_PASSENGER);

        orderInfoMapper.updateById(orderInfo);

        return ResponseResult.success();

    }
    /**
     * 到达乘客上车点
     * @param orderRequest
     * @return
     */
    public ResponseResult arrivedDeparture(OrderRequest orderRequest) {
        Long orderId = orderRequest.getOrderId();
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", orderId);
        OrderInfo orderInfo = orderInfoMapper.selectOne(queryWrapper);
        orderInfo.setOrderStatus(OrderConstants.DRIVER_ARRIVED_DEPARTURE);
        orderInfo.setDriverArrivedDepartureTime(LocalDateTime.now());
        orderInfoMapper.updateById(orderInfo);
        return ResponseResult.success();

    }

    /**
     * 司机接到乘客
     * @param orderRequest
     * @return
     */
    public ResponseResult pickUpPassenger(OrderRequest orderRequest) {
        Long orderId = orderRequest.getOrderId();

        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", orderId);
        OrderInfo orderInfo = orderInfoMapper.selectOne(queryWrapper);

        orderInfo.setPickUpPassengerLongitude(orderRequest.getPickUpPassengerLongitude());
        orderInfo.setPickUpPassengerLatitude(orderRequest.getPickUpPassengerLatitude());
        orderInfo.setPickUpPassengerTime(LocalDateTime.now());
        orderInfo.setOrderStatus(OrderConstants.PICK_UP_PASSENGER);

        orderInfoMapper.updateById(orderInfo);
        return ResponseResult.success();
    }

    /**
     * 乘客下车到达目的地，行程终止
     * @param orderRequest
     * @return
     */
    public ResponseResult passengerGetoff(OrderRequest orderRequest){
        Long orderId = orderRequest.getOrderId();

        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id",orderId);
        OrderInfo orderInfo = orderInfoMapper.selectOne(queryWrapper);

        orderInfo.setPassengerGetoffTime(LocalDateTime.now());
        orderInfo.setPassengerGetoffLongitude(orderRequest.getPassengerGetoffLongitude());
        orderInfo.setPassengerGetoffLatitude(orderRequest.getPassengerGetoffLatitude());

        orderInfo.setOrderStatus(OrderConstants.PASSENGER_GETOFF);
        // 订单行驶的路程和时间,调用 service-map
        ResponseResult<Car> carById = serviceDriverUserClient.getCarById(orderInfo.getCarId());
        Long starttime = orderInfo.getPickUpPassengerTime().toInstant(ZoneOffset.of("+8")).toEpochMilli();
        Long endtime = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();
        System.out.println("开始时间："+starttime);
        System.out.println("结束时间："+endtime);
        ResponseResult<TrsearchResponse> responseResult = serviceMapClient.trsearch(carById.getData().getTid(), starttime,endtime);
        TrsearchResponse data = responseResult.getData();
        orderInfo.setDriveMile(data.getDriveMile());
        orderInfo.setDriveTime(data.getDriveTime());


        // 获取价格
        String address = orderInfo.getAddress();

        PriceDTO priceDTO = new PriceDTO();
        priceDTO.setCityCode(address);
        priceDTO.setDistance(data.getDriveMile().intValue());
        priceDTO.setDuration(data.getDriveTime().intValue());
        priceDTO.setVehicleType(orderInfo.getVehicleType());

        ResponseResult<Double> doubleResponseResult = servicePriceClient.calculatePrice(priceDTO);
        Double price = doubleResponseResult.getData();
        orderInfo.setPrice(price);

        orderInfoMapper.updateById(orderInfo);
        return ResponseResult.success();
    }

    /**
     * 支付
     * @param orderRequest
     * @return
     */
    public ResponseResult pay(OrderRequest orderRequest) {
        OrderInfo orderInfo = orderInfoMapper.selectById(orderRequest.getOrderId());
        orderInfo.setOrderStatus(OrderConstants.SUCCESS_PAY);
        orderInfoMapper.updateById(orderInfo);
        return ResponseResult.success();
    }

    /**
     * 订单取消
     * @param orderId 订单Id
     * @param identity  身份：1：乘客，2：司机
     * @return
     */
    public ResponseResult cancel(Long orderId, String identity) {
        // 查询订单当前状态
        OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
        Integer orderStatus = orderInfo.getOrderStatus();

        LocalDateTime cancelTime = LocalDateTime.now();
        Integer cancelOperator = null;
        Integer cancelTypeCode = null;

        // 正常取消
        int cancelType = 1;

        // 更新订单的取消状态
        // 如果是乘客取消
        if(IdentityConstants.PASSENGER_IDENTITY.equals(identity.trim())){
            switch (orderStatus){
                // 订单开始
                case OrderConstants.ORDER_START:
                    cancelTypeCode = OrderConstants.CANCEL_PASSENGER_BEFORE;
                    break;
                // 司机接到订单
                case OrderConstants.DRIVER_RECEIVE_ORDER:
                    LocalDateTime receiveOrderTime = orderInfo.getReceiveOrderTime();
                    long between = ChronoUnit.MINUTES.between(receiveOrderTime, cancelTime);
                    if (between > 1){
                        // 超过一分钟就是违约取消
                        cancelTypeCode = OrderConstants.CANCEL_PASSENGER_ILLEGAL;
                    }else {
                        cancelTypeCode = OrderConstants.CANCEL_PASSENGER_BEFORE;
                    }
                    break;
                // 司机去接乘客
                case OrderConstants.DRIVER_TO_PICK_UP_PASSENGER:
                // 司机到达乘客起点
                case OrderConstants.DRIVER_ARRIVED_DEPARTURE:
                    cancelTypeCode = OrderConstants.CANCEL_PASSENGER_ILLEGAL;
                    break;
                default:
                    log.info("乘客取消失败");
                    cancelType = 0;
                    break;
            }

        }

        // 如果是司机取消  1、正常取消  2、接单超过1分钟取消属于违约取消
        if (identity.trim().equals(IdentityConstants.DRIVER_IDENTITY)){
            switch (orderStatus){
                // 订单开始
                // 司机接到乘客
                case OrderConstants.DRIVER_RECEIVE_ORDER:
                case OrderConstants.DRIVER_TO_PICK_UP_PASSENGER:
                case OrderConstants.DRIVER_ARRIVED_DEPARTURE:
                    LocalDateTime receiveOrderTime = orderInfo.getReceiveOrderTime();
                    long between = ChronoUnit.MINUTES.between(receiveOrderTime, cancelTime);
                    if (between > 1){
                        cancelTypeCode = OrderConstants.CANCEL_DRIVER_ILLEGAL;
                    }else {
                        cancelTypeCode = OrderConstants.CANCEL_DRIVER_BEFORE;
                    }
                    break;

                default:
                    log.info("司机取消失败");
                    cancelType = 0;
                    break;
            }
        }

        if (cancelType == 0){
            return ResponseResult.fail(CommonStatusEnum.ORDER_CANCEL_ERROR);
        }

        orderInfo.setCancelTypeCode(cancelTypeCode);
        orderInfo.setCancelTime(cancelTime);
        orderInfo.setCancelOperator(Integer.parseInt(identity));
        orderInfo.setOrderStatus(OrderConstants.ORDER_CANCEL);

        orderInfoMapper.updateById(orderInfo);
        return ResponseResult.success();
    }

    public ResponseResult pushPayInfo(OrderRequest orderRequest) {

        Long orderId = orderRequest.getOrderId();

        OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
        orderInfo.setOrderStatus(OrderConstants.TO_START_PAY);
        orderInfoMapper.updateById(orderInfo);
        return ResponseResult.success();

    }

    public ResponseResult<OrderInfo> detail(Long orderId){
        OrderInfo orderInfo =  orderInfoMapper.selectById(orderId);
        return ResponseResult.success(orderInfo);
    }


    public ResponseResult<OrderInfo> current(String phone, String identity){
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();

        if (identity.equals(IdentityConstants.DRIVER_IDENTITY)){
            queryWrapper.eq("driver_phone",phone);

            queryWrapper.and(wrapper->wrapper
                    .eq("order_status",OrderConstants.DRIVER_RECEIVE_ORDER)
                    .or().eq("order_status",OrderConstants.DRIVER_TO_PICK_UP_PASSENGER)
                    .or().eq("order_status",OrderConstants.DRIVER_ARRIVED_DEPARTURE)
                    .or().eq("order_status",OrderConstants.PICK_UP_PASSENGER)

            );
        }
        if (identity.equals(IdentityConstants.PASSENGER_IDENTITY)){
            queryWrapper.eq("passenger_phone",phone);
            queryWrapper.and(wrapper->wrapper.eq("order_status",OrderConstants.ORDER_START)
                    .or().eq("order_status",OrderConstants.DRIVER_RECEIVE_ORDER)
                    .or().eq("order_status",OrderConstants.DRIVER_TO_PICK_UP_PASSENGER)
                    .or().eq("order_status",OrderConstants.DRIVER_ARRIVED_DEPARTURE)
                    .or().eq("order_status",OrderConstants.PICK_UP_PASSENGER)
                    .or().eq("order_status",OrderConstants.PASSENGER_GETOFF)
                    .or().eq("order_status",OrderConstants.TO_START_PAY)
            );
        }

        OrderInfo orderInfo = orderInfoMapper.selectOne(queryWrapper);
        return ResponseResult.success(orderInfo);
    }


    /**
     * 新建订单
     * @param orderRequest
     * @return
     */
    public ResponseResult book(OrderRequest orderRequest) {

//        // 测试当前城市是否有可用的司机
//        ResponseResult<Boolean> availableDriver = serviceDriverUserClient.isAvailableDriver(orderRequest.getAddress());
//        log.info("测试城市是否有司机结果："+availableDriver.getData());
//        if (!availableDriver.getData()){
//            return ResponseResult.fail(CommonStatusEnum.CITY_DRIVER_EMPTY.getCode(),CommonStatusEnum.CITY_DRIVER_EMPTY.getValue());
//        }

//        // 需要判断计价规则的版本是否为最新
//        PriceRuleIsNewRequest priceRuleIsNewRequest = new PriceRuleIsNewRequest();
//        priceRuleIsNewRequest.setFareType(orderRequest.getFareType());
//        priceRuleIsNewRequest.setFareVersion(orderRequest.getFareVersion());
//        ResponseResult<Boolean> aNew = servicePriceClient.isNew(priceRuleIsNewRequest);
//        if (!(aNew.getData())){
//            return ResponseResult.fail(CommonStatusEnum.PRICE_RULE_CHANGED.getCode(),CommonStatusEnum.PRICE_RULE_CHANGED.getValue());
//        }

        // 需要判断 下单的设备是否是 黑名单设备
//        if (isBlackDevice(orderRequest)) {
//            return ResponseResult.fail(CommonStatusEnum.DEVICE_IS_BLACK.getCode(), CommonStatusEnum.DEVICE_IS_BLACK.getValue());
//        }

//        // 判断：下单的城市和计价规则是否正常
//        if(!isPriceRuleExists(orderRequest)){
//            return ResponseResult.fail(CommonStatusEnum.CITY_SERVICE_NOT_SERVICE.getCode(),CommonStatusEnum.CITY_SERVICE_NOT_SERVICE.getValue());
//        }


//        // 判断乘客 是否有进行中的订单
//        if (isPassengerOrderGoingon(orderRequest.getPassengerId()) > 0){
//            return ResponseResult.fail(CommonStatusEnum.ORDER_GOING_ON.getCode(),CommonStatusEnum.ORDER_GOING_ON.getValue());
//        }

        // 创建订单
        OrderInfo orderInfo = new OrderInfo();

        BeanUtils.copyProperties(orderRequest,orderInfo);

        orderInfo.setOrderStatus(OrderConstants.ORDER_START);

        LocalDateTime now = LocalDateTime.now();
        orderInfo.setGmtCreate(now);
        orderInfo.setGmtModified(now);

        orderInfoMapper.insert(orderInfo);

        // 定时任务的处理
        for (int i =0;i<6;i++){
            // 派单 dispatchRealTimeOrder
            int result = dispatchBookOrder(orderInfo);
            if (result == 1){
                break;
            }

            // 等待20s
            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return ResponseResult.success();
    }

    /**
     * 预约订单-分发订单
     * 如果返回1：派单成功
     * @param orderInfo
     */
    public int dispatchBookOrder(OrderInfo orderInfo){
        log.info("循环一次");
        int result = 0;

        //2km
        String depLatitude = orderInfo.getDepLatitude();
        String depLongitude = orderInfo.getDepLongitude();

        String center = depLatitude+","+depLongitude;

        List<Integer> radiusList = new ArrayList<>();
        radiusList.add(2000);
        radiusList.add(4000);
        radiusList.add(5000);
        // 搜索结果
        ResponseResult<List<TerminalResponse>> listResponseResult = null;
        // goto是为了测试。
        radius:
        for (int i=0;i<radiusList.size();i++){
            Integer radius = radiusList.get(i);
            listResponseResult = serviceMapClient.aroundSearch(center,radius );

            log.info("在半径为"+radius+"的范围内，寻找车辆,结果："+ JSONArray.fromObject(listResponseResult.getData()).toString());

            // 获得终端  [{"carId":1578641048288702465,"tid":"584169988"}]

            // 解析终端
//            List<TerminalResponse> data = listResponseResult.getData();
            // 测试：为了测试是否从地图上获取到司机
            TerminalResponse testTerminal = new TerminalResponse();
            testTerminal.setCarId(3l);
            testTerminal.setTid("850548522");
            testTerminal.setLatitude("39.89");
            testTerminal.setLongitude("116.32");
            List<TerminalResponse> data = new ArrayList<>();
            data.add(testTerminal);

            // 为了测试是否从地图上获取到司机
//            List<TerminalResponse> data = new ArrayList<>();
            for (int j=0;j<data.size();j++){
                TerminalResponse terminalResponse = data.get(j);
                Long carId = terminalResponse.getCarId();

                String longitude = terminalResponse.getLongitude();
                String latitude = terminalResponse.getLatitude();

                // 查询是否有对应的可派单司机
                ResponseResult<OrderDriverResponse> availableDriver = serviceDriverUserClient.getAvailableDriver(carId);
                if(availableDriver.getCode() == CommonStatusEnum.AVAILABLE_DRIVER_EMPTY.getCode()){
                    log.info("没有车辆ID："+carId+",对于的司机");
                    continue;
                }else {
                    log.info("车辆ID："+carId+"找到了正在出车的司机");

                    OrderDriverResponse orderDriverResponse = availableDriver.getData();
                    Long driverId = orderDriverResponse.getDriverId();
                    String driverPhone = orderDriverResponse.getDriverPhone();
                    String licenseId = orderDriverResponse.getLicenseId();
                    String vehicleNo = orderDriverResponse.getVehicleNo();
                    String vehicleTypeFromCar = orderDriverResponse.getVehicleType();

                    // 判断车辆的车型是否符合？
                    String vehicleType = orderInfo.getVehicleType();
                    if (!vehicleType.trim().equals(vehicleTypeFromCar.trim())){
                        System.out.println("车型不符合");
                        continue ;
                    }

                    // 通知司机
                    JSONObject driverContent = new  JSONObject();

                    driverContent.put("orderId",orderInfo.getId());
                    driverContent.put("passengerId",orderInfo.getPassengerId());
                    driverContent.put("passengerPhone",orderInfo.getPassengerPhone());
                    driverContent.put("departure",orderInfo.getDeparture());
                    driverContent.put("depLongitude",orderInfo.getDepLongitude());
                    driverContent.put("depLatitude",orderInfo.getDepLatitude());

                    driverContent.put("destination",orderInfo.getDestination());
                    driverContent.put("destLongitude",orderInfo.getDestLongitude());
                    driverContent.put("destLatitude",orderInfo.getDestLatitude());

                    PushRequest pushRequest = new PushRequest();
                    pushRequest.setUserId(driverId);
                    pushRequest.setIdentity(IdentityConstants.DRIVER_IDENTITY);
                    pushRequest.setContent(driverContent.toString());

                    serviceSsePushClient.push(pushRequest);

                    // 退出，不在进行 司机的查找.如果派单成功，则退出循环
                    return 1;
                }

            }

        }

        return  result;

    }

    /**
     * 司机抢单
     * @param driverGrabRequest
     * @return
     */
    public  ResponseResult grab(DriverGrabRequest driverGrabRequest){
        System.out.println("请求来了："+driverGrabRequest.getDriverId());

        Long orderId = driverGrabRequest.getOrderId();
        // 不能直接锁orderId，如果数字在 -128 （包含）到127（包含）之间，是从数字缓冲池中拿的，是同一个对象，如果在这之外，是重新new一个对象的，synchronized是锁不住的。

        // 单机锁司机ID小技巧:如果driverId在常量池内没有，则放入常量池 如果driverId字符串在常量池有则返回常量池里的地址，这里就锁住了同一个地址

        String orderIdStr = (orderId+"").intern();

        synchronized(orderIdStr) {
            OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
            // 订单不存在报错
            if (orderInfo == null) {
                return ResponseResult.fail(CommonStatusEnum.ORDER_NOT_EXISTS.getCode(), CommonStatusEnum.ORDER_NOT_EXISTS.getValue());
            }

            int orderStatus = orderInfo.getOrderStatus();
            // 订单状态不为草稿，已经被其他人抢了，报错
            if (orderStatus != OrderConstants.ORDER_START) {
                return ResponseResult.fail(CommonStatusEnum.ORDER_CAN_NOT_GRAB.getCode(), CommonStatusEnum.ORDER_CAN_NOT_GRAB.getValue());
            }
            // 为了测试，休眠10毫秒
            try {
                TimeUnit.MICROSECONDS.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Long driverId = driverGrabRequest.getDriverId();
            Long carId = driverGrabRequest.getCarId();
            String licenseId = driverGrabRequest.getLicenseId();
            String vehicleNo = driverGrabRequest.getVehicleNo();
            String receiveOrderCarLatitude = driverGrabRequest.getReceiveOrderCarLatitude();
            String receiveOrderCarLongitude = driverGrabRequest.getReceiveOrderCarLongitude();
            String vehicleType = driverGrabRequest.getVehicleType();
            String driverPhone = driverGrabRequest.getDriverPhone();

            orderInfo.setDriverId(driverId);
            orderInfo.setDriverPhone(driverPhone);
            orderInfo.setCarId(carId);

            orderInfo.setReceiveOrderCarLongitude(receiveOrderCarLongitude);
            orderInfo.setReceiveOrderCarLatitude(receiveOrderCarLatitude);
            orderInfo.setReceiveOrderTime(LocalDateTime.now());

            orderInfo.setLicenseId(licenseId);
            orderInfo.setVehicleNo(vehicleNo);

            orderInfo.setVehicleType(vehicleType);

            orderInfo.setOrderStatus(OrderConstants.DRIVER_RECEIVE_ORDER);

            orderInfoMapper.updateById(orderInfo);
        }

        return  ResponseResult.success();

    }
}
