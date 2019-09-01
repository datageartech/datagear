/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.management.util;

/**
 * Derby数据库自定义函数支持类。
 * 
 * @author datagear@163.com
 *
 */
public class DerbyFunctionSupport
{
	private DerbyFunctionSupport()
	{

	}

	/**
	 * 字符串替换。
	 * 
	 * @param source
	 * @param oldStr
	 * @param newStr
	 * @return
	 */
	public static String replace(String source, String oldStr, String newStr)
	{
		if (source == null)
			return null;

		return source.replace(oldStr, newStr);
	}
}
