package com.mashibing.servicemap.remote;

import com.mashibing.internalcommon.constant.AmapConfigConstants;
import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.internalcommon.response.TrackResponse;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TrackClient {

    @Value("${amap.key}")
    private String amapKey;
    @Value("${amap.sid}")
    private String amapSid;

    @Autowired
    private RestTemplate restTemplate;



    public ResponseResult<TrackResponse> add(String tid) {

        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(AmapConfigConstants.TRACK_ADD_URL);
        urlBuilder.append("?");
        urlBuilder.append("key=" + amapKey);
        urlBuilder.append("&");
        urlBuilder.append("sid=" + amapSid);
        urlBuilder.append("&");
        urlBuilder.append("tid=" + tid);


        ResponseEntity<String> entity = restTemplate.postForEntity(urlBuilder.toString(),null, String.class);
        String body = entity.getBody();
        /*
         * {
         *     "errcode": 10000,
         *     "errmsg": "OK",
         *     "data": {
         *         "trid": 80
         *     }
         * }
         */
        // 解析接口
        JSONObject responseJson = JSONObject.fromObject(body);
        JSONObject data = responseJson.getJSONObject("data");
        String trid = data.getString("trid");
        TrackResponse response = new TrackResponse();
        response.setTrid(trid);
        return ResponseResult.success(response);
    }
}
