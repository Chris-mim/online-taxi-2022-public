package com.mashibing.internalcommon.util;

public class RedisPrefixUtils {

    // 乘客验证码的前缀
    public static String verificationCodePrefix = "verification-code-";

    // token存储的前缀
    public static String tokenPrefix = "token-";

    // 设备号的前缀
    public static String blackDeviceCodePrefix = "black-device-";

    /**
     * 根据手机号，生成key
     *
     * @param phone
     * @return
     */
    public static String getPhoneKey(String phone, String identity) {
        return verificationCodePrefix + phone + "-" + identity ;
    }

    /**
     * 根据手机号和身份标识，生成token
     *
     * @param phone
     * @param identity
     * @param tokenType
     * @return
     */
    public static String generatorTokenKey(String phone, String identity, String tokenType) {
        return tokenPrefix + phone + "-" + identity + "-" + tokenType;
    }

    /**
     * 根据设备号，生成key
     *
     * @param deviceCode
     * @return
     */
    public static String generatorBlackDeviceCodeKey(String deviceCode) {
        return blackDeviceCodePrefix + deviceCode ;
    }
}
