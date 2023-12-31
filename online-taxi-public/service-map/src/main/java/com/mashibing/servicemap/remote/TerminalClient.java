package com.mashibing.servicemap.remote;

import com.mashibing.internalcommon.constant.AmapConfigConstants;
import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.internalcommon.response.TerminalResponse;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TerminalClient {

    @Value("${amap.key}")
    private String amapKey;
    @Value("${amap.sid}")
    private String amapSid;

    @Autowired
    private RestTemplate restTemplate;



    public ResponseResult add(String name) {

        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(AmapConfigConstants.TERMINAL_ADD_URL);
        urlBuilder.append("?");
        urlBuilder.append("key=" + amapKey);
        urlBuilder.append("&");
        urlBuilder.append("sid=" + amapSid);
        urlBuilder.append("&");
        urlBuilder.append("name=" + name);


        ResponseEntity<String> entity = restTemplate.postForEntity(urlBuilder.toString(),null, String.class);
        String body = entity.getBody();
        /*
         * {
         *     "errcode": 10000,
         *     "errmsg": "OK",
         *     "data": {
         *         "name": "车辆2",
         *         "tid": 816403813,
         *         "sid": 1011360
         *     }
         * }
         */
        // 解析接口
        JSONObject responseJson = JSONObject.fromObject(body);
        JSONObject data = responseJson.getJSONObject("data");
        String tid = data.getString("tid");
        TerminalResponse response = new TerminalResponse();
        response.setTid(tid);
        return ResponseResult.success(response);
    }
}
