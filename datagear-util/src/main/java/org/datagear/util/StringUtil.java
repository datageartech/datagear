/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.util;

import java.util.Collection;
import java.util.Map;

/**
 * 字符串工具类。
 * 
 * @author datagear@163.com
 *
 */
public class StringUtil
{
	private StringUtil()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * 字符串是否为{@code null}、空格串。
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isBlank(String s)
	{
		if (s == null)
			return true;

		if (s.isEmpty())
			return true;

		if (s.trim().isEmpty())
			return true;

		return false;
	}

	/**
	 * 字符串是否为{@code null}、空。
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isEmpty(String s)
	{
		return (s == null || s.isEmpty());
	}

	/**
	 * 判断对象、字符串、数组、集合、Map是否为{@code null}、空、空元素。
	 * 
	 * @param obj
	 * @return
	 */
	public static boolean isEmpty(Object obj)
	{
		if (obj == null)
		{
			return true;
		}
		else if (obj instanceof String)
		{
			String str = (String) obj;
			return (str == null || str.isEmpty());
		}
		else if (obj instanceof Object[])
		{
			Object[] array = (Object[]) obj;

			return (array.length == 0);
		}
		else if (obj instanceof Collection<?>)
		{
			@SuppressWarnings("unchecked")
			Collection<Object> collection = (Collection<Object>) obj;

			return (collection.isEmpty());
		}
		else if (obj instanceof Map<?, ?>)
		{
			Map<?, ?> map = (Map<?, ?>) obj;

			return map.isEmpty();
		}
		else
			return false;
	}

	/**
	 * 将字符串第一个字符转为小写。
	 * 
	 * @param s
	 * @return
	 */
	public static String firstLowerCase(String s)
	{
		if (s == null || s.isEmpty())
			return s;

		StringBuilder sb = new StringBuilder(s.length());

		for (int i = 0; i < s.length(); i++)
		{
			char c = s.charAt(i);

			if (i == 0)
				sb.append(Character.toLowerCase(c));
			else
				sb.append(c);
		}

		return sb.toString();
	}
}
