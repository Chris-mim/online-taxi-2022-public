package com.mashibing.apidriver.service;

import com.mashibing.apidriver.remote.ServiceDriverUserClient;
import com.mashibing.apidriver.remote.ServiceOrderClient;
import com.mashibing.internalcommon.constant.CommonStatusEnum;
import com.mashibing.internalcommon.constant.IdentityConstants;
import com.mashibing.internalcommon.dto.DriverCarBindingRelationship;
import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.internalcommon.entity.OrderInfo;
import com.mashibing.internalcommon.request.DriverGrabRequest;
import com.mashibing.internalcommon.request.OrderRequest;
import com.mashibing.internalcommon.response.OrderDriverResponse;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApiDriverOrderInfoService {

    @Autowired
    private ServiceOrderClient serviceOrderClient;

    @Autowired
    ServiceDriverUserClient serviceDriverUserClient;

    public ResponseResult toPickUpPassenger(OrderRequest orderRequest) {
        return serviceOrderClient.toPickUpPassenger(orderRequest);
    }

    public ResponseResult arrivedDeparture(OrderRequest orderRequest) {
        return serviceOrderClient.arrivedDeparture(orderRequest);
    }

    public ResponseResult pickUpPassenger(OrderRequest orderRequest) {
        return serviceOrderClient.pickUpPassenger(orderRequest);
    }

    public ResponseResult passengerGetoff(OrderRequest orderRequest) {
        return serviceOrderClient.passengerGetoff(orderRequest);
    }

    public ResponseResult cancel(Long orderId) {
        return serviceOrderClient.cancel(orderId, IdentityConstants.DRIVER_IDENTITY);
    }

    public ResponseResult<OrderInfo> detail(Long orderId){
        return serviceOrderClient.detail(orderId);
    }

    public ResponseResult<OrderInfo> currentOrder(String phone , String identity){
        return serviceOrderClient.current(phone,identity);
    }

    /**
     * 司机抢单
     * @param driverPhone
     * @param orderId
     * @return
     */
    public ResponseResult grap(String driverPhone , Long orderId , String receiveOrderCarLongitude, String receiveOrderCarLatitude){
        // 根据 司机的电话，查询车辆信息
        ResponseResult<DriverCarBindingRelationship> driverCarRelationShipResponseResult = serviceDriverUserClient.getDriverCarRelationShip(driverPhone);

        if (driverCarRelationShipResponseResult == null){
            return ResponseResult.fail(CommonStatusEnum.DRIVER_CAR_BIND_NOT_EXISTS.getCode(),CommonStatusEnum.DRIVER_CAR_BIND_EXISTS.getValue());
        }
        DriverCarBindingRelationship driverCarBindingRelationship = driverCarRelationShipResponseResult.getData();
        Long carId = driverCarBindingRelationship.getCarId();

        ResponseResult<OrderDriverResponse> availableDriverResponseResult = serviceDriverUserClient.getAvailableDriver(carId);
        if (availableDriverResponseResult == null){
            return ResponseResult.fail(CommonStatusEnum.CAR_NOT_EXISTS.getCode(),CommonStatusEnum.CAR_NOT_EXISTS.getValue());
        }
        OrderDriverResponse orderDriverResponse = availableDriverResponseResult.getData();
        Long driverId = orderDriverResponse.getDriverId();
        String licenseId = orderDriverResponse.getLicenseId();
        String vehicleNo = orderDriverResponse.getVehicleNo();
        String vehicleType = orderDriverResponse.getVehicleType();

//        orderInfo.setOrderStatus(OrderConstants.DRIVER_RECEIVE_ORDER);

        // 执行抢单动作

        DriverGrabRequest driverGrabRequest = new DriverGrabRequest();
        driverGrabRequest.setOrderId(orderId);
        driverGrabRequest.setDriverId(driverId);
        driverGrabRequest.setCarId(carId);
        driverGrabRequest.setDriverPhone(driverPhone);
        driverGrabRequest.setLicenseId(licenseId);
        driverGrabRequest.setVehicleNo(vehicleNo);
        driverGrabRequest.setVehicleType(vehicleType);
        driverGrabRequest.setReceiveOrderCarLatitude(receiveOrderCarLatitude);
        driverGrabRequest.setReceiveOrderCarLongitude(receiveOrderCarLongitude);
        System.out.println(JSONObject.fromObject(driverGrabRequest));

        serviceOrderClient.driverGrab(driverGrabRequest);

        return ResponseResult.success();
    }
}
