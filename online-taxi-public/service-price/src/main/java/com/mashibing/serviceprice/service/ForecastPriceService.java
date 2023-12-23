package com.mashibing.serviceprice.service;

import com.mashibing.internalcommon.constant.CommonStatusEnum;
import com.mashibing.internalcommon.dto.PriceRule;
import com.mashibing.internalcommon.dto.ResponseResult;
import com.mashibing.internalcommon.request.ForecastPriceDTO;
import com.mashibing.internalcommon.response.DirectionResponse;
import com.mashibing.internalcommon.response.ForecastPriceResponse;
import com.mashibing.serviceprice.mapper.PriceRuleMapper;
import com.mashibing.serviceprice.remote.ServiceMapClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class ForecastPriceService {

    @Autowired
    private ServiceMapClient serviceMapClient;

    @Autowired
    private PriceRuleMapper priceRuleMapper;

    public ResponseResult<ForecastPriceResponse> forecastPrice(ForecastPriceDTO forecastPriceDTO) {
        log.info("出发地经度：" + forecastPriceDTO.getDepLongitude());
        log.info("出发地纬度：" + forecastPriceDTO.getDepLatitude());
        log.info("目的地经度：" + forecastPriceDTO.getDestLongitude());
        log.info("目的地纬度：" + forecastPriceDTO.getDestLatitude());

        log.info("调用地图服务，查询距离和时长");
        ResponseResult<DirectionResponse> direction = serviceMapClient.driving(forecastPriceDTO);
        Integer distance = direction.getData().getDistance();
        Integer duration = direction.getData().getDuration();
        log.info(("距离：" + distance + ",时长：" + duration));

        log.info("读取计价规则");
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("city_code", "110000");
        queryMap.put("vehicle_type", "1");
        List<PriceRule> priceRules = priceRuleMapper.selectByMap(queryMap);
        if (priceRules.isEmpty()) {
            return ResponseResult.fail(CommonStatusEnum.PRICE_RULE_NOT_EXIST);
        }
        PriceRule priceRule = priceRules.get(0);

        log.info("根据距离、时长、计价规则，计算价格");
        double price = getPrice(distance, duration, priceRule);


        ForecastPriceResponse forecastPriceResponse = new ForecastPriceResponse();
        forecastPriceResponse.setPrice(price);
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
