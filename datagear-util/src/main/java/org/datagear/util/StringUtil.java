/*
 * Copyright 2018-present datagear.tech
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
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

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
	 * 数组是否为{@code null}、空。
	 * 
	 * @param obj
	 * @return
	 */
	public static boolean isEmpty(Object[] obj)
	{
		return (obj == null || obj.length == 0);
	}

	/**
	 * 集合是否为{@code null}、空。
	 * 
	 * @param obj
	 * @return
	 */
	public static boolean isEmpty(Collection<?> obj)
	{
		return (obj == null || obj.isEmpty());
	}

	/**
	 * 映射表是否为{@code null}、空。
	 * 
	 * @param obj
	 * @return
	 */
	public static boolean isEmpty(Map<?, ?> obj)
	{
		return (obj == null || obj.isEmpty());
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
			return isEmpty((String) obj);
		}
		else if (obj instanceof Object[])
		{
			return isEmpty((Object[]) obj);
		}
		else if (obj instanceof Collection<?>)
		{
			return isEmpty((Collection<?>) obj);
		}
		else if (obj instanceof Map<?, ?>)
		{
			return isEmpty((Map<?, ?>) obj);
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
	 * 简单拆分字符串，空片段将被忽略。
	 * 
	 * @param text
	 *            允许{@code null}
	 * @param splitter
	 * @param trim
	 * @return
	 */
	public static String[] split(String text, String splitter, boolean trim)
	{
		List<String> re = splitOfList(text, splitter, trim);
		return re.toArray(new String[re.size()]);
	}

	/**
	 * 拆分字符串，空片段将被忽略，并删除元素两边的空格。
	 * <p>
	 * 如果{@code s}为{@code null}，返回空列表。
	 * </p>
	 * 
	 * @param str
	 * @param splitter
	 * @return
	 */
	public static List<String> splitWithTrim(String str, String splitter)
	{
		return splitOfList(str, splitter, true);
	}

	protected static List<String> splitOfList(String text, String splitter, boolean trim)
	{
		if (isEmpty(text))
			return Collections.emptyList();

		StringTokenizer st = new StringTokenizer(text, splitter);
		List<String> tokens = new ArrayList<>();

		while (st.hasMoreTokens())
		{
			String token = st.nextToken();
			if (trim)
				token = token.trim();

			// 忽略空片段
			if (!token.isEmpty())
			{
				tokens.add(token);
			}
		}

		return tokens;
	}

	/**
	 * 分隔字符串同时处理'\'转义，空片段将被忽略。
	 * <p>
	 * 如果{@code s}为{@code null}、{@code ""}，返回空列表。
	 * </p>
	 * 
	 * @param str
	 *            允许{@code null}
	 * @param splitter
	 *            分隔符
	 * @param trim
	 *            是否删除片段首尾空格
	 * @return
	 */
	public static List<String> splitWithEscape(String str, char splitter, boolean trim)
	{
		if (splitter == '\\')
			throw new IllegalArgumentException("Splitter must not be '\\'");

		if (isEmpty(str))
			return Collections.emptyList();

		List<String> re = new ArrayList<>();

		char[] cs = str.toCharArray();
		StringBuilder segment = new StringBuilder();
		boolean escapeMode = false;

		for (int i = 0; i < cs.length; i++)
		{
			char c = cs[i];

			if (escapeMode)
			{
				segment.append(c);
				escapeMode = false;
			}
			else if (c == '\\')
			{
				escapeMode = true;
			}
			else if (c == splitter)
			{
				String sv = segment.toString();
				if (trim)
					sv = sv.trim();

				if (!sv.isEmpty())
					re.add(sv);

				segment.setLength(0);
			}
			else
			{
				segment.append(c);
			}
		}

		String sv = segment.toString();
		if (trim)
			sv = sv.trim();

		if (!sv.isEmpty())
			re.add(sv);

		return re;
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
	 *            允许{@code null}
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
	 * 脱敏处理邮箱字符串中的用户名部分（{@linkplain '@'}之前），生成类似{@code "abc****def"}的字符串。
	 * 
	 * @param email
	 *            允许{@code null}
	 * @param prefixCount
	 * @param suffixCount
	 * @param maskCount
	 * @return
	 */
	public static String maskEmail(String email, int prefixCount, int suffixCount, int maskCount)
	{
		if (email == null)
			return mask(email, prefixCount, suffixCount, maskCount);

		String prefix = email;
		String suffix = "";
		
		int idx = email.indexOf('@');
		if(idx > 0)
		{
			prefix = email.substring(0, idx);
			suffix = email.substring(idx);
		}

		prefix = mask(prefix, prefixCount, suffixCount, maskCount);

		return prefix + suffix;
	}

	/**
	 * 脱敏处理JDBC URL字符串。
	 * 
	 * @param url
	 *            允许{@code null}
	 * @param prefixCount
	 * @param suffixCount
	 * @param maskCount
	 * @return
	 */
	public static String maskJdbcUrl(String url, int prefixCount, int suffixCount, int maskCount)
	{
		if (url == null)
			return mask(url, prefixCount, suffixCount, maskCount);

		String prefix = "";
		String suffix = url;

		// JDBC规范格式：jdbc:<subprotocol>:<subname>
		// 这里仅对<subname>脱敏处理

		int idx = url.indexOf(':');

		if (idx > 0)
		{
			int idx2 = url.indexOf(':', idx + 1);

			if (idx2 > idx)
				idx = idx2;
		}

		if (idx > 0 && idx < url.length())
		{
			prefix = url.substring(0, idx + 1);
			suffix = url.substring(idx + 1);
		}

		suffix = mask(suffix, prefixCount, suffixCount, maskCount);

		return prefix + suffix;
	}

	/**
	 * 解码URL。
	 * 
	 * @param url
	 *            允许{@code null}
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
	 * 解码URL。
	 * <p>
	 * 此方法内部出现的{@linkplain UnsupportedEncodingException}异常将被包裹为{@linkplain UnsupportedOperationException}。
	 * </p>
	 * 
	 * @param url
	 *            允许{@code null}
	 * @param encoding
	 * @return
	 * @throws UnsupportedOperationException
	 */
	public static String decodeURLUnchecked(String url, String encoding) throws UnsupportedOperationException
	{
		try
		{
			return decodeURL(url, encoding);
		}
		catch (UnsupportedEncodingException e)
		{
			throw new UnsupportedOperationException(e);
		}
	}

	/**
	 * 编码URL。
	 * 
	 * @param url
	 *            允许{@code null}
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
	 * 编码URL。
	 * <p>
	 * 此方法内部出现的{@linkplain UnsupportedEncodingException}异常将被包裹为{@linkplain UnsupportedOperationException}。
	 * </p>
	 * 
	 * @param url
	 *            允许{@code null}
	 * @param encoding
	 * @return
	 * @throws UnsupportedOperationException
	 */
	public static String encodeURLUnchecked(String url, String encoding) throws UnsupportedOperationException
	{
		try
		{
			return encodeURL(url, encoding);
		}
		catch (UnsupportedEncodingException e)
		{
			throw new UnsupportedOperationException(e);
		}
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

	/**
	 * 编码路径URL。
	 * <p>
	 * 将字符串中除了'/'的字符都进行URL编码。
	 * </p>
	 * <p>
	 * 此方法内部出现的{@linkplain UnsupportedEncodingException}异常将被包裹为{@linkplain UnsupportedOperationException}。
	 * </p>
	 * 
	 * @param url
	 *            允许{@code null}
	 * @param encoding
	 * @return
	 * @throws UnsupportedOperationException
	 */
	public static String encodePathURLUnchecked(String url, String encoding) throws UnsupportedOperationException
	{
		try
		{
			return encodePathURL(url, encoding);
		}
		catch (UnsupportedEncodingException e)
		{
			throw new UnsupportedOperationException(e);
		}
	}

	/**
	 * 将指定URI转换为RFC 2396规范编码的URI。
	 * <p>
	 * 例如：
	 * </p>
	 * <p>
	 * <code>http://中文.def.com/中文一/ghi?param1=中文二&amp;param2=b#中文三</code>
	 * </p>
	 * <p>
	 * 将被转换为：
	 * </p>
	 * <p>
	 * <code>http://%E4%B8%AD%E6%96%87.def.com/%E4%B8%AD%E6%96%87%E4%B8%80/ghi?param1=%E4%B8%AD%E6%96%87%E4%BA%8C&amp;param2=b#%E4%B8%AD%E6%96%87%E4%B8%89</code>
	 * </p>
	 * 
	 * @param uri
	 * @return
	 */
	public static String toAsciiURI(String uri)
	{
		return URI.create(uri).toASCIIString();
	}

	/**
	 * 字符串转换为布尔值。
	 * <p>
	 * 返回{@code true}的字符串（忽略大小写）：
	 * </p>
	 * <p>
	 * {@code "true"}、{@code "1"}、{@code "y"}、{@code "yes"}、{@code "on"}、{@code "是"}
	 * </p>
	 * <p>
	 * 其他情况都将返回{@code false}。
	 * </p>
	 * 
	 * @param v
	 * @return
	 */
	public static boolean toBoolean(String v)
	{
		if (v == null)
			return false;

		return ("true".equalsIgnoreCase(v) || "1".equals(v) || "y".equalsIgnoreCase(v) || "yes".equalsIgnoreCase(v)
				|| "on".equalsIgnoreCase(v) || "是".equals(v));
	}

	/**
	 * 数值转换为布尔值。
	 * <p>
	 * 数值大于{@code 0}时返回{@code true}，否则返回{@code true}。
	 * </p>
	 * 
	 * @param v
	 * @return
	 */
	public static boolean toBoolean(Number v)
	{
		if (v == null)
			return false;

		return (v.intValue() > 0);
	}

	/**
	 * 转换为字符串。
	 * 
	 * @param o
	 * @return 当{@code o}为{@code null}时将返回{@code null}
	 * @see {@linkplain #toString(Object, String)}
	 */
	public static String toString(Object o)
	{
		return toString(o, null);
	}

	/**
	 * 转换为字符串。
	 * <p>
	 * 如果不是{@linkplain String}类型，将调用{@linkplain Object#toString()}。
	 * </p>
	 * 
	 * @param o
	 * @param nullValue
	 *            当{@code o}为{@code null}时的返回值
	 * @return
	 */
	public static String toString(Object o, String nullValue)
	{
		if (o == null)
		{
			return nullValue;
		}
		else if (o instanceof String)
		{
			return (String) o;
		}
		else
		{
			return o.toString();
		}
	}
}
