package com.gome.supermain.redis.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Byte数组与Object之间的转换工具类
 * @author darkidiot
 */
public final class ByteObjectConvertUtil {
	private static final Logger LOG = LoggerFactory.getLogger("rediscache");

	private ByteObjectConvertUtil() {
	}

	/**
	 * byte[]数组转成object
	 * @param objBytes
	 * @return
	 */
	public static Object getObjectFromBytes(byte[] objBytes) {
		if (objBytes == null || objBytes.length == 0) {
			return null;
		}
		ByteArrayInputStream bi = null;
		ObjectInputStream oi = null;
		try {
			byte[] decode = Base64.getDecoder().decode(objBytes);
			bi = new ByteArrayInputStream(decode);
			oi = new ObjectInputStream(bi);

			return oi.readObject();
		} catch (Exception e) {
			LOG.error("getObjectFromBytes error: {}.", e);
			return null;
		} finally {
			if (oi != null) {
				try {
					oi.close();
				} catch (Exception e) {
				}
			}
			if (bi != null) {
				try {
					bi.close();
				} catch (Exception e) {
				}
			}
		}
	}

	/**
	 * object转成byte[]数组
	 * @param obj
	 * @return
	 */
	public static byte[] getBytesFromObject(Object obj) {
		ByteArrayOutputStream bo = null;
		ObjectOutputStream oo = null;
		try {
			bo = new ByteArrayOutputStream();
			oo = new ObjectOutputStream(bo);
			oo.writeObject(obj);
			byte[] byteArray = bo.toByteArray();
			byte[] encode = Base64.getEncoder().encode(byteArray);
			return encode;
		} catch (Exception e) {
			LOG.error("getBytesFromObject error: {}.", e);
			return null;
		} finally {
			if (bo != null) {
				try {
					bo.close();
				} catch (Exception e) {
				}
			}
			if (oo != null) {
				try {
					oo.close();
				} catch (Exception e) {
				}
			}
		}
	}
}
