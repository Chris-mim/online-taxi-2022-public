package com.mashibing.internalcommon.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PriceRule {

    private String cityCode;

    private String vehicleType;

    private BigDecimal startFare;

    private Integer startMile;

    private BigDecimal unitPricePerMile;

    private BigDecimal unitPricePerMinute;
}
