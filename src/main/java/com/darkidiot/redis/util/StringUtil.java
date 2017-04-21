package com.darkidiot.redis.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 字符串工具类
 *
 * @author: darkidiot
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StringUtil {

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static String nullToEmpty(String str) {
        if (str == null)
            return "";
        return str;
    }
}
