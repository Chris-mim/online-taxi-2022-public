package com.mashibing.servicemap.service;

import com.mashibing.internalcommon.constant.AmapConfigConstants;
import com.mashibing.internalcommon.constant.CommonStatusEnum;
import com.mashibing.internalcommon.dto.DicDistrict;
import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.internalcommon.response.DirectionResponse;
import com.mashibing.servicemap.mapper.DicDistrictMapper;
import com.mashibing.servicemap.remote.MapDicDistrictClient;
import lombok.Generated;
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
public class DicDistrictService {


    @Autowired
    private MapDicDistrictClient mapDicDistrictClient;
    @Autowired
    private DicDistrictMapper districtMapper;

    public ResponseResult initDicDistrict(String keywords) {
        // 调用地图
        String dicDistrictResult = mapDicDistrictClient.dicDistrict(keywords);
        JSONObject dicDistrictJSONObject = JSONObject.fromObject(dicDistrictResult);

        int status = dicDistrictJSONObject.getInt(AmapConfigConstants.STATUS);
        if (status != 1){
            return ResponseResult.fail(CommonStatusEnum.MAP_DISTRICT_ERROR);
        }
        JSONArray districtArray = dicDistrictJSONObject.getJSONArray(AmapConfigConstants.DISTRICTS);
        parseDicDistrict(districtArray,"0");

        return ResponseResult.success();
    }

    /**
     * parentAddressCode参数针对的是一个数组，不是一个对象，所以传参要用数组 + parent
     * @param districtArray
     * @param parentAddressCode
     */
    private void parseDicDistrict(JSONArray districtArray, String parentAddressCode) {
        if (districtArray == null || districtArray.isEmpty()){
            return;
        }
        for (int i = 0; i < districtArray.size(); i ++){
            JSONObject districtObject = districtArray.getJSONObject(i);
            if (AmapConfigConstants.STREET.equals(districtObject.getString(AmapConfigConstants.LEVEL))){
                continue;
            }
            String addressCode = districtObject.getString(AmapConfigConstants.ADCODE);
            String addressName = districtObject.getString(AmapConfigConstants.NAME);
            int level = generateLevel(districtObject.getString(AmapConfigConstants.LEVEL));
            // 插入数据库
            DicDistrict district = new DicDistrict();
            district.setAddressCode(addressCode);
            district.setAddressName(addressName);
            district.setLevel(level);
            district.setParentAddressCode(parentAddressCode);
            districtMapper.insert(district);
            if (districtObject.has(AmapConfigConstants.DISTRICTS)){
                parseDicDistrict(districtObject.getJSONArray(AmapConfigConstants.DISTRICTS), addressCode);
            }

        }
    }

    public int generateLevel(String level){
        int levelInt = 0;
        if (level.trim().equals("country")){
            levelInt = 0;
        }else if(level.trim().equals("province")){
            levelInt = 1;
        }else if(level.trim().equals("city")){
            levelInt = 2;
        }else if(level.trim().equals("district")){
            levelInt = 3;
        }
        return levelInt;
    }
}
