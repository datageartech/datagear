/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
	 * 拆分字符串。
	 * 
	 * @param text
	 * @param splitter
	 * @param trim
	 * @return
	 */
	public static String[] split(String text, String splitter, boolean trim)
	{
		if (trim)
		{
			text = text.trim();

			if (text.isEmpty())
				return new String[0];
		}

		if (text.startsWith(splitter))
			text = text.substring(splitter.length());
		if (text.endsWith(splitter))
			text = text.substring(0, text.length() - splitter.length());

		String[] array = text.split(splitter);
		for (int i = 0; i < array.length; i++)
			array[i] = (trim ? array[i].trim() : array[i]);

		return array;
	}

	/**
	 * 将文本数组以分隔符合并为一个字符串。
	 * 
	 * @param text
	 * @param splitter
	 * @return
	 */
	public static String concat(String[] texts, String splitter)
	{
		if (texts == null)
			return "";

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < texts.length; i++)
		{
			if (i > 0)
				sb.append(splitter);

			sb.append(texts[i]);
		}

		return sb.toString();
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

	/**
	 * 将字符串第一个字符转为大写。
	 * 
	 * @param s
	 * @return
	 */
	public static String firstUpperCase(String s)
	{
		if (s == null || s.isEmpty())
			return s;

		StringBuilder sb = new StringBuilder(s.length());

		for (int i = 0; i < s.length(); i++)
		{
			char c = s.charAt(i);

			if (i == 0)
				sb.append(Character.toUpperCase(c));
			else
				sb.append(c);
		}

		return sb.toString();
	}

	/**
	 * 转义HTML字符串。
	 * 
	 * @param s
	 * @return
	 */
	public static String escapeHtml(String s)
	{
		if (s == null || s.isEmpty())
			return s;

		StringBuilder sb = new StringBuilder();

		char[] cs = s.toCharArray();

		for (char c : cs)
		{
			if (c == '<')
				sb.append("&lt;");
			else if (c == '>')
				sb.append("&gt;");
			else if (c == '&')
				sb.append("&amp;");
			else if (c == '"')
				sb.append("&quot;");
			else
				sb.append(c);
		}

		return sb.toString();
	}

	/**
	 * 转换为JavaScript语法的字符串。
	 * 
	 * @param s
	 * @return
	 */
	public static String escapeJavaScriptStringValue(String s)
	{
		if (s == null)
			return "";

		StringBuilder sb = new StringBuilder();

		char[] cs = s.toCharArray();

		for (char c : cs)
		{
			if (c == '\\')
				sb.append("\\\\");
			else if (c == '\'')
				sb.append("\\\'");
			else if (c == '"')
				sb.append("\\\"");
			else if (c == '\t')
				sb.append("\\\t");
			else if (c == '\n')
				sb.append("\\\n");
			else if (c == '\r')
				sb.append("\\\r");
			else
				sb.append(c);
		}

		return sb.toString();
	}

	/**
	 * 拆分字符串，并删除元素两边的空格。
	 * <p>
	 * 如果{@code s}为{@code null}，返回空列表。
	 * </p>
	 * 
	 * @param str
	 * @param splitter
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<String> splitWithTrim(String str, String splitter)
	{
		if (str == null)
			return Collections.EMPTY_LIST;

		String[] strs = str.split(splitter);

		for (int i = 0; i < strs.length; i++)
			strs[i] = strs[i].trim();

		return Arrays.asList(strs);
	}
}
