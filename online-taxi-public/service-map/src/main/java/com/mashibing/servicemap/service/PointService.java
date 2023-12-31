package com.mashibing.servicemap.service;

import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.internalcommon.request.PointRequest;
import com.mashibing.servicemap.remote.PointClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PointService {

    @Autowired
    private PointClient pointClient;

    /**
     * 轨迹点上传
     * @param pointRequest
     * @return
     */
    public ResponseResult upload(PointRequest pointRequest) {
        return pointClient.upload(pointRequest);
    }
}
