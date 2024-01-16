package com.mashibing.servicemap.remote;

import com.mashibing.internalcommon.constant.AmapConfigConstants;
import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.internalcommon.response.TerminalResponse;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class TerminalClient {

    @Value("${amap.key}")
    private String amapKey;
    @Value("${amap.sid}")
    private String amapSid;

    @Autowired
    private RestTemplate restTemplate;



    public ResponseResult<TerminalResponse> add(String name, String desc) {

        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(AmapConfigConstants.TERMINAL_ADD_URL);
        urlBuilder.append("?");
        urlBuilder.append("key=" + amapKey);
        urlBuilder.append("&");
        urlBuilder.append("sid=" + amapSid);
        urlBuilder.append("&");
        urlBuilder.append("name=" + name);
        urlBuilder.append("&");
        urlBuilder.append("desc=" + desc);


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
    /**
     * 周边搜索终端
     * @param center 经纬度，string类型。格式为：纬度,经度
     * @param radius int类型。单位：米，取值范围[1,5000]
     * @return
     */
    public ResponseResult<List<TerminalResponse>> aroundSearch(String center, Integer radius) {

        StringBuilder url = new StringBuilder();
        url.append(AmapConfigConstants.TERMINAL_AROUNDSEARCH_URL);
        url.append("?");
        url.append("key=" + amapKey);
        url.append("&");
        url.append("sid=" + amapSid);
        url.append("&");
        url.append("center=" + center);
        url.append("&");
        url.append("radius=" + radius);

        log.info("周边搜索终端请求"+ url);
        ResponseEntity<String> entity = restTemplate.postForEntity(url.toString(),null, String.class);
        String body = entity.getBody();
        // 解析接口
        JSONObject responseJson = JSONObject.fromObject(body);
        log.info("周边搜索终端响应 :"+ responseJson);
        JSONObject data = responseJson.getJSONObject("data");
        JSONArray results = data.getJSONArray("results");
        List<TerminalResponse> res = new ArrayList<>();
        for (int i = 0; i < results.size(); i ++){
            JSONObject jsonObject = results.getJSONObject(i);
            TerminalResponse terminal = new TerminalResponse();
            terminal.setTid(jsonObject.getString("tid"));
            String desc = jsonObject.getString("desc");
            terminal.setCarId(Long.parseLong(desc));
            JSONObject location = jsonObject.getJSONObject("location");
            terminal.setLongitude(location.getString("longitude"));
            terminal.setLatitude(location.getString("latitude"));
            res.add(terminal);
        }

        return ResponseResult.success(res);
    }
}
