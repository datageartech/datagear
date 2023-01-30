/*
 * Copyright 2018-2023 datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package org.datagear.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
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
	 * 判断两个对象是否相等。
	 * 
	 * @param a
	 *            允许为{@code null}
	 * @param b
	 *            允许为{@code null}
	 * @return
	 */
	public static boolean isEquals(Object a, Object b)
	{
		if (a == null)
			return (b == null);
		else
			return a.equals(b);
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

		StringBuilder sb = new StringBuilder(s.length());

		char[] cs = s.toCharArray();

		for (int i=0; i<cs.length; i++)
		{
			char c = cs[i];
			
			switch(c)
			{
				case '<':
				{
					sb.append("&lt;");
					break;
				}
				case '>':
				{
					sb.append("&gt;");
					break;
				}
				case '"':
				{
					sb.append("&quot;");
					break;
				}
				case '&':
				{
					sb.append("&amp;");
					break;
				}
				default:
				{
					sb.append(c);
				}
			}
		}

		return sb.toString();
	}

	/**
	 * 转换为JavaScript字符串，首尾将添加双引号。
	 * 
	 * @param s
	 * @return
	 */
	public static String toJavaScriptString(String s)
	{
		return toJavaScriptString(s, true);
	}

	/**
	 * 转换为JavaScript字符串。
	 * 
	 * @param s
	 * @param quote
	 *            是否首位添加双引号
	 * @return
	 */
	public static String toJavaScriptString(String s, boolean quote)
	{
		if (s == null)
			return "null";

		StringBuilder sb = new StringBuilder(s.length());

		if (quote)
			sb.append("\"");

		char[] cs = s.toCharArray();

		for (int i=0; i<cs.length; i++)
		{
			char c = cs[i];
			
			switch(c)
			{
				case '\\':
				{
					sb.append("\\\\");
					break;
				}
				case '\'':
				{
					sb.append("\\\'");
					break;
				}
				case '"':
				{
					sb.append("\\\"");
					break;
				}
				case '\t':
				{
					sb.append("\\\t");
					break;
				}
				case '\n':
				{
					sb.append("\\\n");
					break;
				}
				case '\r':
				{
					sb.append("\\\r");
					break;
				}
				default:
				{
					sb.append(c);
				}
			}
		}

		if (quote)
			sb.append("\"");

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

	/**
	 * 在数组中查找元素索引。
	 * 
	 * @param array
	 *            数组，允许为{@code null}
	 * @param element
	 *            查找元素，允许为{@code null}
	 * @return 数组索引，{@code -1}表示未找到
	 */
	public static int search(Object[] array, Object element)
	{
		if (array == null)
			return -1;

		for (int i = 0; i < array.length; i++)
		{
			if (element == null)
			{
				if (array[i] == null)
					return i;
			}
			else if (element.equals(array[i]))
				return i;
		}

		return -1;
	}

	/**
	 * 脱敏处理字符串，生成类似{@code "abc****def"}的字符串。
	 * 
	 * @param str
	 * @param prefixCount
	 * @param suffixCount
	 * @param maskCount
	 * @return
	 */
	public static String mask(String str, int prefixCount, int suffixCount, int maskCount)
	{
		prefixCount = (prefixCount < 0 ? 0 : prefixCount);
		suffixCount = (suffixCount < 0 ? 0 : suffixCount);
		maskCount = (maskCount < 0 ? 0 : maskCount);

		String prefix = "", suffix = "";

		int len = (str == null ? 0 : str.length());

		if (prefixCount == 0 || len == 0)
			prefix = "";
		else if (len > prefixCount)
			prefix = str.substring(0, prefixCount);
		else
			prefix = str;

		if (suffixCount == 0 || len == 0)
			suffix = "";
		else if (len >= (prefixCount + suffixCount))
			suffix = str.substring(len - suffixCount);
		else if (len > prefixCount)
			suffix = str.substring(prefixCount);
		else
			suffix = "";

		StringBuilder sb = new StringBuilder(prefix);
		for (int i = 0; i < maskCount; i++)
			sb.append("*");
		sb.append(suffix);

		return sb.toString();
	}

	/**
	 * 解码URL。
	 * 
	 * @param url
	 * @param encoding
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String decodeURL(String url, String encoding) throws UnsupportedEncodingException
	{
		if (url == null)
			return null;

		return URLDecoder.decode(url, encoding);
	}

	/**
	 * 编码URL。
	 * 
	 * @param url
	 * @param encoding
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String encodeURL(String url, String encoding) throws UnsupportedEncodingException
	{
		if (url == null)
			return null;

		return URLEncoder.encode(url, encoding);
	}

	/**
	 * 编码路径URL。
	 * <p>
	 * 将字符串中除了'/'的字符都进行URL编码。
	 * </p>
	 * 
	 * @param url
	 * @param encoding
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String encodePathURL(String url, String encoding) throws UnsupportedEncodingException
	{
		if (url == null)
			return null;

		StringBuilder re = new StringBuilder();

		StringBuilder node = new StringBuilder();
		char[] cs = url.toCharArray();

		for (char c : cs)
		{
			if (c == '/')
			{
				if (node.length() > 0)
				{
					re.append(encodeURL(node.toString(), encoding));
					node.delete(0, node.length());
				}

				re.append(c);
			}
			else
			{
				node.append(c);
			}
		}

		if (node.length() > 0)
		{
			re.append(encodeURL(node.toString(), encoding));
		}

		return re.toString();
	}
}
