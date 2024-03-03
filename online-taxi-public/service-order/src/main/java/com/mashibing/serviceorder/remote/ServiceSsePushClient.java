package com.mashibing.serviceorder.remote;

import com.mashibing.internalcommon.request.PushRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("service-sse-push")
public interface ServiceSsePushClient {

    /**
     * 发送消息
     * @return
     */
    @PostMapping(value = "/push")
    public String push(@RequestBody PushRequest pushRequest);

}
