/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.util;

/**
 * ID工具类。
 * 
 * @author datagear@163.com
 *
 */
public class IDUtil
{
	private IDUtil()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * 生成一个UUID字符串。
	 * 
	 * @return
	 */
	public static String uuid()
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
