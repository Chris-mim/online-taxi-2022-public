package com.mashibing.serviceprice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mashibing.internalcommon.constant.CommonStatusEnum;
import com.mashibing.internalcommon.dto.PriceRule;
import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.internalcommon.request.PriceDTO;
import com.mashibing.internalcommon.response.DirectionResponse;
import com.mashibing.internalcommon.response.ForecastPriceResponse;
import com.mashibing.serviceprice.mapper.PriceRuleMapper;
import com.mashibing.serviceprice.remote.ServiceMapClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Slf4j
public class PriceService {

    @Autowired
    private ServiceMapClient serviceMapClient;

    @Autowired
    private PriceRuleMapper priceRuleMapper;

    public ResponseResult<ForecastPriceResponse> forecastPrice(PriceDTO param) {
        log.info("出发地经度：" + param.getDepLongitude());
        log.info("出发地纬度：" + param.getDepLatitude());
        log.info("目的地经度：" + param.getDestLongitude());
        log.info("目的地纬度：" + param.getDestLatitude());

        log.info("调用地图服务，查询距离和时长");
        ResponseResult<DirectionResponse> direction = serviceMapClient.driving(param);
        Integer distance = direction.getData().getDistance();
        Integer duration = direction.getData().getDuration();
        log.info(("距离：" + distance + ",时长：" + duration));

        log.info("读取计价规则");
        QueryWrapper<PriceRule> queryMap = new QueryWrapper<>();
        queryMap.eq("city_code", param.getCityCode());
        queryMap.eq("vehicle_type", param.getVehicleType());
        queryMap.orderByDesc("fare_version");
        List<PriceRule> priceRules = priceRuleMapper.selectList(queryMap);
        if (priceRules.isEmpty()) {
            return ResponseResult.fail(CommonStatusEnum.PRICE_RULE_NOT_EXIST);
        }
        PriceRule priceRule = priceRules.get(0);

        log.info("根据距离、时长、计价规则，计算价格");
        double price = getPrice(distance, duration, priceRule);


        ForecastPriceResponse forecastPriceResponse = new ForecastPriceResponse();
        forecastPriceResponse.setPrice(price);
        forecastPriceResponse.setCityCode(param.getCityCode());
        forecastPriceResponse.setVehicleType(param.getVehicleType());
        forecastPriceResponse.setFareType(priceRule.getFareType());
        forecastPriceResponse.setFareVersion(priceRule.getFareVersion());
        return ResponseResult.success(forecastPriceResponse);
    }

    private static double getPrice(Integer distance, Integer duration, PriceRule priceRule) {
        BigDecimal price = BigDecimal.ZERO;
        // 起步价
        price = price.add(priceRule.getStartFare());

        // 里程价 = 里程公里价 * 大于起步路程的公里数

        // 需要计算的里程数
        BigDecimal distanceMileDecimal = new BigDecimal(distance)
                .divide(new BigDecimal(1000), 2, RoundingMode.HALF_UP)
                .subtract(BigDecimal.valueOf(priceRule.getStartMile()));

        BigDecimal mile = distanceMileDecimal.compareTo(BigDecimal.ZERO) > 0 ? distanceMileDecimal : BigDecimal.ZERO;

        BigDecimal mileFare = mile
                .multiply(priceRule.getUnitPricePerMile())
                .setScale(2, RoundingMode.HALF_UP);

        price = price.add(mileFare);

        // 时长价 = 每分钟时长价 * 分钟数

        BigDecimal timeFare = new BigDecimal(duration)
                .multiply(priceRule.getUnitPricePerMinute())
                .divide(new BigDecimal(60), 2, RoundingMode.HALF_UP);
        price = price.add(timeFare);
        return price.doubleValue();
    }

    public ResponseResult calculatePrice(PriceDTO priceDTO) {
        // 查询计价规则
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("city_code", priceDTO.getCityCode());
        queryWrapper.eq("vehicle_type", priceDTO.getVehicleType());
        queryWrapper.orderByDesc("fare_version");

        List<PriceRule> priceRules = priceRuleMapper.selectList(queryWrapper);
        if (priceRules.size() == 0){
            return ResponseResult.fail(CommonStatusEnum.PRICE_RULE_NOT_EXIST);
        }

        PriceRule priceRule = priceRules.get(0);

        log.info("根据距离、时长和计价规则，计算价格");

        double price = getPrice(priceDTO.getDistance(), priceDTO.getDuration(), priceRule);
        return ResponseResult.success(price);
    }

//    public static void main(String[] args) {
//        PriceRule priceRule = new PriceRule();
//        priceRule.setUnitPricePerMile(1.8);
//        priceRule.setUnitPricePerMinute(0.5);
//        priceRule.setStartFare(10.0);
//        priceRule.setStartMile(3);
//
//        System.out.println(getPrice(6500, 1800, priceRule));
//    }

}
