/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.persistence.support;

/**
 * UUID工具类。
 * 
 * @author datagear@163.com
 *
 */
public class UUID
{
	private UUID()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * 生成一个UUID字符串。
	 * <p>
	 * 此方法会移除{@linkplain UUID}中的所有'-'字符。
	 * </p>
	 * 
	 * @return
	 */
	public static String gen()
	{
		char[] uuid = java.util.UUID.randomUUID().toString().toCharArray();
		char[] chars = new char[uuid.length];

		int count = 0;
		for (int i = 0; i < uuid.length; i++)
		{
			if (uuid[i] != '-')
				chars[count++] = uuid[i];
		}

		return new String(chars, 0, count);
	}
}
