package com.mashibing.internalcommon.request;

import lombok.Data;

@Data
public class PriceDTO {

    /**
     * 出发地经度
     */
    private String depLongitude;
    /**
     * 出发地纬度
     */
    private String depLatitude;
    /**
     * 目的地经度
     */
    private String destLongitude;
    /**
     * 目的地纬度
     */
    private String destLatitude;

    private String cityCode;

    private String vehicleType;

    /**
     * 距离
     */
    private Integer distance;
    /**
     * 时长
     */
    private Integer duration;
}
