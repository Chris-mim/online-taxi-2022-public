package com.mashibing.apipassenger.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class ForecastPriceDTO {

    /**
     * 出发地经度
     * -180~180之间
     * -100~+100 不含 100
     */
    @NotBlank(message = "起点经度不能为空")
    @Pattern(regexp = "^[\\-+]?(0?\\d{1,2}\\.\\d{1,6}|1[0-7]\\d{1}\\.\\d{1,6}|180\\.0{1,6})$",message = "请输入正确的起点经度")
    private String depLongitude;
    /**
     * 出发地纬度
     * -90~+90
     */
    @NotBlank(message = "起点纬度不能为空")
    @Pattern(regexp = "^[\\-+]?([0-8]?\\d{1}\\.\\d{1,6}|90\\.0{1,6})$",message = "请输入正确的起点纬度")
    private String depLatitude;
    /**
     * 目的地经度
     */
    @NotBlank(message = "终点经度不能为空")
    @Pattern(regexp = "^[\\-+]?(0?\\d{1,2}\\.\\d{1,6}|1[0-7]\\d{1}\\.\\d{1,6}|180\\.0{1,6})$",message = "请输入正确的终点经度")
    private String destLongitude;
    /**
     * 目的地纬度
     */
    @NotBlank(message = "终点纬度不能为空")
    @Pattern(regexp = "^[\\-+]?([0-8]?\\d{1}\\.\\d{1,6}|90\\.0{1,6})$",message = "请输入正确的终点纬度")
    private String destLatitude;

    @NotBlank(message = "城市码不能为空")
    @Pattern(regexp = "^\\d{6}$",message = "请输入正确的城市码")
    private String cityCode;

    @NotBlank(message = "车辆类型不能为空")
    @Pattern(regexp = "^1$",message = "请输入正确的车辆类型")
    private String vehicleType;


}