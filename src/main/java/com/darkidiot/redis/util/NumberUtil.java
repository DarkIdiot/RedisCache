package com.darkidiot.redis.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 数字工具类
 *
 * @author darkidiot
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NumberUtil {


    /**
     * 判断是否是奇数
     *
     * @param number
     * @return
     */
    public static boolean isOdd(Number number) {
        return number.intValue() % 2 == 1;
    }

    /**
     * 判断是否是偶数
     *
     * @param number
     * @return
     */
    public static boolean isEven(Number number) {
        return !isOdd(number);
    }
}
