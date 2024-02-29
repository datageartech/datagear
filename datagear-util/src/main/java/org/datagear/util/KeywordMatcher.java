/*
 * Copyright 2018-2024 datagear.tech
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 关键字查找工具类。
 * <p>
 * 用于支持以{@code '%'}作为关键字通配符的查找。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class KeywordMatcher
{
	/** 是否忽略大小写 */
	private boolean ignoreCase = true;

	public KeywordMatcher()
	{
		super();
	}

	public boolean isIgnoreCase()
	{
		return ignoreCase;
	}

	public void setIgnoreCase(boolean ignoreCase)
	{
		this.ignoreCase = ignoreCase;
	}

	/**
	 * 查找关键字匹配的项。
	 * 
	 * @param list
	 * @param keyword
	 *            包含的{@code%}作为通配符
	 * @param matchValue
	 * @return
	 */
	public <T> List<T> match(List<T> list, String keyword, MatchValue<T> matchValue)
	{
		Pattern pattern = toPattern(keyword);

		if (pattern == null)
			return list;

		List<T> result = new ArrayList<>();

		for (T obj : list)
		{
			if (match(pattern, obj, matchValue))
				result.add(obj);
		}

		return result;
	}

	/**
	 * 查找关键字匹配的项。
	 * 
	 * @param array
	 * @param keyword
	 *            包含的{@code%}作为通配符
	 * @param matchValue
	 * @return
	 */
	public <T> List<T> match(T[] array, String keyword, MatchValue<T> matchValue)
	{
		Pattern pattern = toPattern(keyword);

		if (pattern == null)
			return Arrays.asList(array);

		List<T> result = new ArrayList<>();

		for (T obj : array)
		{
			if (match(pattern, obj, matchValue))
				result.add(obj);
		}

		return result;
	}

	protected <T> boolean match(Pattern regex, T obj, MatchValue<T> matchValue)
	{
		String[] values = matchValue.get(obj);
		return match(regex, values);
	}

	protected boolean match(Pattern regex, String[] values)
	{
		if (values == null)
			return false;

		for (String value : values)
		{
			if (value != null && regex.matcher(value).matches())
				return true;
		}

		return false;
	}

	/**
	 * 将查询关键字解析为{@linkplain Pattern}。
	 * 
	 * @param keyword
	 * @return 当{@code keyword}为{@code null}、{@code ""}时返回{@code null}
	 */
	protected Pattern toPattern(String keyword)
	{
		if (keyword == null || keyword.isEmpty())
			return null;

		if (!keyword.startsWith("%") && !keyword.endsWith("%"))
			keyword = "%" + keyword + "%";

		StringBuilder pb = new StringBuilder();

		pb.append('^');

		char[] cs = keyword.toCharArray();
		StringBuilder lb = new StringBuilder();
		for (int i = 0; i < cs.length; i++)
		{
			char c = cs[i];

			if (c == '%')
			{
				if (lb.length() > 0)
				{
					pb.append(Pattern.quote(lb.toString()));
					lb.delete(0, lb.length());
				}

				pb.append(".*");
			}
			else
				lb.append(c);
		}

		if (lb.length() > 0)
			pb.append(Pattern.quote(lb.toString()));

		pb.append('$');

		String regex = pb.toString();

		if (isIgnoreCase())
			return Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		else
			return Pattern.compile(regex);
	}

	/**
	 * 用于提取关键字匹配值的接口类。
	 * 
	 * @author datagear@163.com
	 *
	 * @param <T>
	 */
	public static interface MatchValue<T>
	{
		/**
		 * 提取关键字匹配值。
		 * 
		 * @param t
		 * @return 返回{@code null}表示不能匹配
		 */
		String[] get(T t);
	}
}
