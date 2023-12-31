package com.mashibing.servicemap.controller;

import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.internalcommon.request.PointRequest;
import com.mashibing.servicemap.service.PointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/point")
public class PointController {

    @Autowired
    private PointService service;

    /**
     * 轨迹点上传（单点、批量）
     * @return
     */
    @PostMapping("/upload")
    public ResponseResult upload(@RequestBody PointRequest pointRequest){
        return service.upload(pointRequest);
    }
}
