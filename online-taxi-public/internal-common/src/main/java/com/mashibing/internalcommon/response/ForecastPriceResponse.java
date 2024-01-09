package com.mashibing.internalcommon.response;

import lombok.Data;

@Data
public class ForecastPriceResponse {
    private double price;
    private String cityCode;

    private String vehicleType;
    /**
     * 版本，默认1，修改往上增。
     */
    private Integer fareVersion;

    /**
     * 运价类型编码
     */
    private String fareType;
}
