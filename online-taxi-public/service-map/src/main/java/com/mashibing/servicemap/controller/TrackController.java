package com.mashibing.servicemap.controller;

import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.internalcommon.response.TrackResponse;
import com.mashibing.servicemap.service.TrackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/track")
public class TrackController {

    @Autowired
    private TrackService service;

    /**
     * 根据终端创建轨迹
     * @param tid
     * @return
     */
    @PostMapping("/add")
    public ResponseResult<TrackResponse> add(@RequestParam String tid){
        return service.add(tid);
    }
}
