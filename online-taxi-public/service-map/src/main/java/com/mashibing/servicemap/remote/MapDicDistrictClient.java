package com.mashibing.servicemap.remote;

import com.mashibing.internalcommon.constant.AmapConfigConstants;
import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.internalcommon.response.DirectionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class MapDicDistrictClient {

    @Value("${amap.key}")
    private String amapKey;

    @Autowired
    private RestTemplate restTemplate;

    public String dicDistrict(String keywords) {
        // 调用获取行政区域接口初始化数据
        StringBuilder url = new StringBuilder();
        url.append(AmapConfigConstants.DIC_DISTRICT_URL);
        url.append("?");
        url.append("keywords=" + keywords);
        url.append("&");
        url.append("subdistrict=3");
        url.append("&");
        url.append("key=" + amapKey);
        log.info("行政区域查询url：" + url.toString());

        ResponseEntity<String> dicDistrictEntity = restTemplate.getForEntity(url.toString(), String.class);
        String dicDistrictString = dicDistrictEntity.getBody();
        log.info("高德地图：行政区域，返回信息：" + dicDistrictString);
        // 解析结果

        return dicDistrictString;

    }
}
