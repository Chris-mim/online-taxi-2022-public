package com.mashibing.serviceorder.remote;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("service-sse-push")
public interface ServiceSsePushClient {

    /**
     * 发送消息
     * @param userId 用户id
     * @param identity 身份
     * @param content 内容
     * @return
     */
    @GetMapping("/push")
    String push(@RequestParam Long userId, @RequestParam String identity, @RequestParam String content);
}
