package com.mashibing.servicedriveruser.remote;

import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.internalcommon.response.TerminalResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("service-map")
public interface TerminalClient {

    /**
     * 创建终端
     * @param name
     * @return
     */
    @PostMapping("/terminal/add")
    ResponseResult<TerminalResponse> add(@RequestParam String name);
}
