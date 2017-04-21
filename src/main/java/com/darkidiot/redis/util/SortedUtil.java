package com.darkidiot.redis.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 排序工具类
 *
 * @author darkidiot
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SortedUtil {


    /**
     * 排序List<String> 每个String中的":" 后面的数字作为排序的标准.
     *
     * @param list
     * @return
     */
    public static List<String> sortedString(List<String> list) {
        Collections.sort(list, new Comparator<String>() {

            @Override
            public int compare(String s1, String s2) {
                Integer s1Number = Integer.parseInt(s1.substring(s1.indexOf(":") + 1, s1.length()));
                Integer s2Number = Integer.parseInt(s2.substring(s1.indexOf(":") + 1, s1.length()));
                if (s1Number < s2Number) {
                    return -1;
                } else if (s1Number > s2Number) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
        return list;
    }
}