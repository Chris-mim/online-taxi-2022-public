package com.mashibing.apidriver.remote;

import com.mashibing.internalcommon.dto.*;
import com.mashibing.internalcommon.response.DriverUserExistsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient("service-driver-user")
public interface ServiceDriverUserClient {

    @PutMapping("/user")
    ResponseResult updateUser(@RequestBody DriverUser driverUser);

    @GetMapping("/check-driver/{driverPhone}")
    ResponseResult<DriverUserExistsResponse> getUser(@PathVariable("driverPhone") String driverPhone);

    @GetMapping("/car")
    ResponseResult<Car> getCatById(@RequestParam Long carId);

    @PostMapping("/driver-user-work-status")
    ResponseResult changeWorkStatus(@RequestBody DriverUserWorkStatus driverUserWorkStatus);

    @GetMapping("/driver-car-binding-relationship")
    ResponseResult<DriverCarBindingRelationship> getDriverCarRelationShip(@RequestParam String driverPhone);

    @GetMapping("/work-status")
    public ResponseResult<DriverUserWorkStatus> getWorkStatus(@RequestParam Long driverId);
}
