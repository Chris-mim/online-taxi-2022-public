package com.mashibing.servicemap.remote;

import com.mashibing.internalcommon.constant.AmapConfigConstants;
import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.internalcommon.response.ServiceResponse;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ServiceClient {

    @Value("${amap.key}")
    private String amapKey;

    @Autowired
    private RestTemplate restTemplate;



    public ResponseResult add(String name) {

        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(AmapConfigConstants.SERVICE_ADD_URL);
        urlBuilder.append("?");
        urlBuilder.append("key=" + amapKey);
        urlBuilder.append("&");
        urlBuilder.append("name=" + name);


        ResponseEntity<String> serviceEntity = restTemplate.postForEntity(urlBuilder.toString(),null, String.class);
        String serviceString = serviceEntity.getBody();
        // 解析接口
        JSONObject serviceResponse = JSONObject.fromObject(serviceString);
        JSONObject data = serviceResponse.getJSONObject("data");
        String sid = data.getString("sid");
        ServiceResponse response = new ServiceResponse();
        response.setSid(sid);
        return ResponseResult.success(response);
    }
}
