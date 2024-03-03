package com.mashibing.apidriver.remote;

import com.mashibing.internalcommon.request.PushRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient("service-sse-push")
public interface ServiceSsePushClient {


    @RequestMapping(method = RequestMethod.POST,value = "/push")
    String push(@RequestBody PushRequest pushRequest);
}
