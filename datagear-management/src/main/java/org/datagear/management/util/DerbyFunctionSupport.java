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

	/**
	 * 取最大值。
	 * 
	 * @param v0
	 * @param v1
	 * @return
	 */
	public static Integer maxInt(Integer v0, Integer v1)
	{
		if (v0 == null)
			return v1;

		if (v1 == null)
			return v0;

		return Math.max(v0, v1);
	}

	/**
	 * 取余数。
	 * 
	 * @param valueNum
	 * @param divNum
	 * @return
	 */
	public static int modInt(int valueNum, int divNum)
	{
		return valueNum % divNum;
	}

	/**
	 * 取字符串长度。
	 * 
	 * @param str
	 * @return
	 */
	public static int lengthStr(String str)
	{
		return (str == null ? 0 : str.length());
	}
}
