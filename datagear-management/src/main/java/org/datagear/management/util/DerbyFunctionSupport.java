/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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

	/**
	 * 字符串正则表达式替换。
	 * 
	 * @param source
	 * @param regex
	 * @param target
	 * @return
	 */
	public static String replaceRegex(String source, String regex, String target)
	{
		if (source == null)
			return null;

		return source.replaceAll(regex, target);
	}
}
