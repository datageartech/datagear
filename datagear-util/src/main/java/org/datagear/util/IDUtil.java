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

	/**
	 * 生成一个基于时间的且20个字符长度的随机ID。
	 * <p>
	 * 此ID可作为数据库存储实体的ID，因为有数据库ID约束保证不会重复。
	 * </p>
	 * 
	 * @return
	 */
	public static String randomIdOnTime20()
	{
		String timeStr = Long.toHexString(System.currentTimeMillis());
		String randomStr = random(20 - timeStr.length());

		return randomStr + timeStr;
	}

	/**
	 * 生成指定长度的随机字符串。
	 * 
	 * @param len
	 * @return
	 */
	public static String random(int len)
	{
		StringBuilder sb = new StringBuilder(len);

		for (int i = 0; i < len; i++)
		{
			int index = (int) (Math.random() * RANDOM_CODE.length);
			sb.append(RANDOM_CODE[index]);
		}

		return sb.toString();
	}

	private static final char[] RANDOM_CODE = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
			'e', 'f' };
}
