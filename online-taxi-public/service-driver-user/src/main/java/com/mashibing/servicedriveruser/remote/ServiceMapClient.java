package com.mashibing.servicedriveruser.remote;

import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.internalcommon.response.TerminalResponse;
import com.mashibing.internalcommon.response.TrackResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("service-map")
public interface ServiceMapClient {

    /**
     * 创建终端
     * @param name
     * @return
     */
    @PostMapping("/terminal/add")
    ResponseResult<TerminalResponse> addTerminal(@RequestParam String name, @RequestParam String desc);

    /**
     * 根据终端创建轨迹
     * @param tid
     * @return
     */
    @PostMapping("/track/add")
    ResponseResult<TrackResponse> addTrack(@RequestParam String tid);
}
