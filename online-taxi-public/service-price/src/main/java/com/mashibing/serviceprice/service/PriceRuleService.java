package com.mashibing.serviceprice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mashibing.internalcommon.constant.CommonStatusEnum;
import com.mashibing.internalcommon.dto.PriceRule;
import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.serviceprice.mapper.PriceRuleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zhangjj
 * @since 2024-01-08
 */
@Service
public class PriceRuleService {

    @Autowired
    private PriceRuleMapper priceRuleMapper;

    public ResponseResult add(PriceRule priceRule) {
        // 添加计价规则
        // 当计价规则存在时，报错，不存在时添加
        // 1、可能会查出多个值 2、需要查询出版本号最大的一个
        QueryWrapper<PriceRule>  queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("city_code",priceRule.getCityCode());
        queryWrapper.eq("vehicle_type",priceRule.getVehicleType());
        queryWrapper.orderByDesc("fare_version");
        List<PriceRule> priceRules = priceRuleMapper.selectList(queryWrapper);
        if (priceRules.size() > 0){
            // 计价规则已存在，不允许添加
            return ResponseResult.fail(CommonStatusEnum.PRICE_RULE_EXISTS);
        }
        String fareType = priceRule.getCityCode() + "$" + priceRule.getVehicleType();
        priceRule.setFareType(fareType);
        priceRule.setFareVersion(1);
        priceRuleMapper.insert(priceRule);

        return ResponseResult.success();
    }
}
