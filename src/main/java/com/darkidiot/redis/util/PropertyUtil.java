package com.darkidiot.redis.util;

import java.util.Enumeration;
import java.util.Properties;
/**
 * 
 * @author darkidiot
 */
public class PropertyUtil {

	public static int getValueAsInt(Properties props, String key, int defaultValue) {
		int value = defaultValue;
		String v = props.getProperty(key);
		if (v != null) {
			try {
				value = Integer.parseInt(v.trim());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return value;
	}

	public static boolean getValueAsBoolean(Properties props, String key, boolean defaultValue) {
		boolean value = defaultValue;
		String v = props.getProperty(key);
		if (v != null) {
			try {
				value = Boolean.parseBoolean(v.trim());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return value;
	}

	public static String getValueAsString(Properties props, String key, String defaultValue) {
		return props.getProperty(key, defaultValue);
	}

	public static int[] getValueAsIntList(Properties props, String key) {
		int[] value = null;
		String v = props.getProperty(key);
		if (v != null) {
			try {
				String[] splits = v.split(",");
				if (splits != null && splits.length > 0) {
					value = new int[splits.length];
					for (int i = 0; i < splits.length; i++) {
						value[i] = Integer.valueOf(splits[i].trim());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return value;
	}

	public static long getValueAsLong(Properties props, String key, long defaultValue) {
		long value = defaultValue;
		String v = props.getProperty(key);
		if (v != null) {
			try {
				value = Long.parseLong(v.trim());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return value;
	}

	public static int getValueAsInt(Properties props, String key) {
		return getValueAsInt(props, key, 0);
	}

	public static boolean getValueAsBoolean(Properties props, String key) {
		return getValueAsBoolean(props, key, false);
	}

	public static String getValueAsString(Properties props, String key) {
		return props.getProperty(key);
	}

	public static long getValueAsLong(Properties props, String key) {
		return getValueAsLong(props, key, 0L);
	}

	public static Enumeration<?> propertyNames(Properties props) {
		return props.propertyNames();
	}
	
	/**
	 * 根据前缀将properties配置文件分拆
	 * @param props
	 * @param daoName
	 * @return
	 */
	public static Properties splitPropertyByPrefix(Properties props, String daoName) {
		String prefix = daoName + ".";
		Enumeration<?> nameEnums = propertyNames(props);
		Properties innerProps = new Properties();
		while (nameEnums.hasMoreElements()) {
			String name = (String) nameEnums.nextElement();
			if (name != null && name.length() > 0 && name.startsWith(prefix)) {
				innerProps.put(name.substring(prefix.length()), getValueAsString(props, name));
			}
		}
		return new Properties(innerProps);
	}

}
