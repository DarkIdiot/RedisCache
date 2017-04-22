package com.darkidiot.redis.util;

import com.google.common.io.BaseEncoding;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;

/**
 * Byte数组与Object之间的转换工具类
 *
 * @author darkidiot
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ByteObjectConvertUtil {
    /**
     * byte[]数组转成object
     *
     * @param objBytes
     * @return
     */
    public static Object getObjectFromBytes(byte[] objBytes) {
        if (objBytes == null || objBytes.length == 0) {
            return null;
        }
        byte[] decode = Base64.getDecoder().decode(objBytes);

        try (ByteArrayInputStream bi = new ByteArrayInputStream(decode);
             ObjectInputStream oi = new ObjectInputStream(bi)) {
            return oi.readObject();
        } catch (Exception e) {
            log.error("getObjectFromBytes error: {}.", e);
            return null;
        }
    }

    /**
     * object转成byte[]数组
     *
     * @param obj
     * @return
     */
    public static byte[] getBytesFromObject(Object obj) {
        try (ByteArrayOutputStream bo = new ByteArrayOutputStream();
             ObjectOutputStream oo = new ObjectOutputStream(bo)) {
            oo.writeObject(obj);
            byte[] byteArray = bo.toByteArray();
            byte[] encode = Base64.getEncoder().encode(byteArray);
            return encode;
        } catch (Exception e) {
            log.error("getBytesFromObject error: {}.", e);
            return null;
        }
    }

    /**
     * string转成object
     *
     * @param str
     * @return
     */
    public static Object getObjectFromBytes2(String str) {
        byte[] decode = BaseEncoding.base16().decode(str);

        try (ByteArrayInputStream bi = new ByteArrayInputStream(decode);
             ObjectInputStream oi = new ObjectInputStream(bi)) {
            return oi.readObject();
        } catch (Exception e) {
            log.error("getObjectFromBytes error: {}.", e);
            return null;
        }
    }

    /**
     * object转成String
     *
     * @param obj
     * @return
     */
    public static String getBytesFromObject2(Object obj) {
        try (ByteArrayOutputStream bo = new ByteArrayOutputStream();
             ObjectOutputStream oo = new ObjectOutputStream(bo)) {
            oo.writeObject(obj);
            byte[] byteArray = bo.toByteArray();
            return BaseEncoding.base16().encode(byteArray);
        } catch (Exception e) {
            log.error("getBytesFromObject error: {}.", e);
            return null;
        }
    }
}
