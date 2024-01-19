package com.mashibing.internalcommon.util;

public class SsePrefixUtils {

    public static final String SEPARATOR = "$";

    public static String generatorSseKey(Long userId, String identity) {
        return userId + SEPARATOR + identity;
    }
}
