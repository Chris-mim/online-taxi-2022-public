package com.mashibing.servicemap.remote;

import com.mashibing.internalcommon.constant.AmapConfigConstants;
import com.mashibing.internalcommon.request.PriceDTO;
import com.mashibing.internalcommon.response.DirectionResponse;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class MapDirectionClient {

    @Value("${amap.key}")
    private String amapKey;

    @Autowired
    private RestTemplate restTemplate;
    /**
     * 根据起点经纬度和终点经纬获取距离（米）和时长（分钟）
     * @return 距离（米）和时长（分钟）
     */
    public DirectionResponse direction(PriceDTO param) {
        /**
         *  https://restapi.amap.com/v3/direction/driving
         *  ?origin=116.481028,39.989643
         *  &destination=116.465302,40.004717
         *  &extensions=base
         *  &output=xml
         *  &key=e86216c5e908fff38e9b6270a0cb8df0
         */
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(AmapConfigConstants.DIRECTION_URL);
        urlBuilder.append("?");
        urlBuilder.append("origin=" + param.getDepLongitude() + "," + param.getDepLatitude());
        urlBuilder.append("&");
        urlBuilder.append("destination=" + param.getDestLongitude() + "," + param.getDestLatitude());
        urlBuilder.append("&");
        urlBuilder.append("extensions=base");
        urlBuilder.append("&");
        urlBuilder.append("output=json");
        urlBuilder.append("&");
        urlBuilder.append("key=" + amapKey);
        log.info("direction url :" + urlBuilder.toString());

        ResponseEntity<String> directionEntity = restTemplate.getForEntity(urlBuilder.toString(), String.class);
        String directionString = directionEntity.getBody();
        log.info("高德地图：路径规划，返回信息：" + directionString);
        // 解析接口
        DirectionResponse directionResponse = parseDirectionEntity(directionString);
        return directionResponse;
    }

    private DirectionResponse parseDirectionEntity(String directionString) {
        DirectionResponse directionResponse = null;
        try {
            JSONObject result = JSONObject.fromObject(directionString);
            if (result.has(AmapConfigConstants.STATUS)) {
                int status = result.getInt(AmapConfigConstants.STATUS);
                if (status == 1) {
                    if (result.has((AmapConfigConstants.ROUTE))) {
                        JSONObject routeObject = result.getJSONObject(AmapConfigConstants.ROUTE);
                        JSONArray pathsArray = routeObject.getJSONArray(AmapConfigConstants.PATHS);
                        JSONObject pathObject = pathsArray.getJSONObject(0);
                        directionResponse = new DirectionResponse();
                        if (pathObject.has(AmapConfigConstants.DISTANCE)) {
                            int distance = pathObject.getInt(AmapConfigConstants.DISTANCE);
                            directionResponse.setDistance(distance);
                        }
                        if (pathObject.has(AmapConfigConstants.DURATION)) {
                            int duration = pathObject.getInt(AmapConfigConstants.DURATION);
                            directionResponse.setDuration(duration);
                        }

                    }

                }
            }
        } catch (Exception e) {

        }
        return directionResponse;
    }
}
