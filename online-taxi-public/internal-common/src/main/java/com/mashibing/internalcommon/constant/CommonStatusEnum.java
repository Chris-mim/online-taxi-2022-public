package com.mashibing.internalcommon.constant;


import lombok.Getter;

public enum CommonStatusEnum {

    /**
     * 验证码错误提示：1000-1099
     */
    VERIFICATION_CODE_ERR(1099,"验证码不正确"),
    /**
     * 验证码错误提示：1100-1199
     */
    TOKEN_ERR(1199,"token错误"),
    /**
     * 用户错误提示：1200-1299
     */
    USER_NOT_EXIST(1200,"用户不存在"),
    /**
     * 计价规则错误提示：1300-1399
     */
    PRICE_RULE_NOT_EXIST(1300,"计价规则不存在"),
    /**
     * 地图信息：1400-1499
     */
    MAP_DISTRICT_ERROR(1400,"请求地图错误"),

    /**
     * 司机和车辆：1500-1599
     */
    DRIVER_CAR_BIND_NOT_EXISTS(1500,"司机和车辆绑定关系不存在"),


    /**
     * 成功
     */
    SUCCESS(1,"success"),
    /**
     * 失败
     */
    FAIL(0,"fail")

    ;
    @Getter
    private int code;
    @Getter
    private String value;

    CommonStatusEnum(int code, String value) {
        this.code = code;
        this.value = value;
    }
}