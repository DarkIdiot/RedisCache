package com.darkidiot.redis.lock.imp;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Constants {
    private static final String LOCK_PREFIX = "Lock:";

    static final String LOCK_UNLOCK = "Lock:unlock";

    /** 默认获取锁超时时间 (超时会打印警告日志) */
    static final long defaultAcquireLockTimeout = 5 * 1000L;
    /** 默认释放锁超时时间 (超时会打印警告日志) */
    static final long defaultReleaseLockTimeout = 3 * 1000L;
    /** 默认检查锁超时时间 (超时会打印警告日志) */
    static final long defaultCheckLockTimeout = 3 * 1000L;

    /** 默认锁超时时间 (超时会自动释放锁) */
    static final long defaultLockTimeout = 5 * 60L;
    /** 获取锁失败后挂起再试的时间间隔单位(实际按斐波那契数列递增) */
    static final long defaultWaitIntervalInMSUnit = 5L;

    static String createKey(String lockName) {
        return LOCK_PREFIX + lockName;
    }


    private static final String PKEY_LOCK_ENTRANCE_COUNT_SPLITTER = "-";
    private static final String PKEY_VALUE_COUNT = "%s" + PKEY_LOCK_ENTRANCE_COUNT_SPLITTER + "[%d]";
    private static final Pattern pattern = Pattern.compile("\\[(\\d+)\\]");

    public static String autoOverlayValue(String value) {
        validateParam(value);
        if (value.equals(LOCK_UNLOCK)) {
            return null;
        }
        if (!value.contains("[")) {
            return String.format(PKEY_VALUE_COUNT, value, 1);
        }
        Matcher matcher = pattern.matcher(value);
        String s = matcher.find() ? matcher.group(1) : "";
        return value.replaceFirst("\\[\\d+\\]", "[" + (Integer.parseInt(s) + 1) + "]");
    }

    private static void validateParam(String value) {
        if (value == null) {
            throw new IllegalArgumentException("the lock's value can not be null");
        }
    }

    public static String autoDepriveValue(String value) {
        validateParam(value);
        if (!value.contains("[")) {
            return LOCK_UNLOCK;
        }
        Matcher matcher = pattern.matcher(value);
        String s = matcher.find() ? matcher.group(1) : "";
        int count = Integer.parseInt(s) - 1;
        return value.replaceFirst(PKEY_LOCK_ENTRANCE_COUNT_SPLITTER+"\\[\\d+\\]", count == 0 ? "" : PKEY_LOCK_ENTRANCE_COUNT_SPLITTER+"[" + (count) + "]");
    }
}
