package com.mashibing.apidriver.remote;

import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.internalcommon.request.PointRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("service-map")
public interface ServiceMapClient {

    /**
     * 轨迹点上传
     * @param pointRequest
     * @return
     */
    @PostMapping("/point/upload")
    ResponseResult uploadPoint(@RequestBody PointRequest pointRequest);
}
