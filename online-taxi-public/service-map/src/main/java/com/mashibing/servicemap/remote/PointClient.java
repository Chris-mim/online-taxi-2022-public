package com.mashibing.servicemap.remote;

import com.alibaba.fastjson.JSONObject;
import com.mashibing.internalcommon.constant.AmapConfigConstants;
import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.internalcommon.request.PointRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class PointClient {

    @Value("${amap.key}")
    private String amapKey;
    @Value("${amap.sid}")
    private String amapSid;

    @Autowired
    private RestTemplate restTemplate;



    public ResponseResult upload(PointRequest pointRequest) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        // 针对url参数，包含数组类型的可以这么处理。
        // 例如：https://tsapi.amap.com/v1/track/point/upload
        // ?key=e86216c5e908fff38e9b6270a0cb8df0
        // &sid=1011360&tid=816342810
        // &trid=40
        // &points=%5B++++{++++++++%22location%22%3A%22116.479983%2C39.99659%22%2C++++++++%22locatetime%22%3A1704002213359++++}%5D
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("key",amapKey);
        map.add("sid",amapSid);
        map.add("tid",pointRequest.getTid());
        map.add("trid",pointRequest.getTrid());
        map.add("points", JSONObject.toJSONString(pointRequest.getPoints()));

        HttpEntity<MultiValueMap<String,Object>> request = new HttpEntity<>(map,headers);

        ResponseEntity<String> entity = restTemplate.postForEntity(AmapConfigConstants.POINT_UPLOAD_URL, request, String.class);
        System.out.println("高德地图响应："+entity.getBody());

        return ResponseResult.success();
    }
}
