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

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 基于星号模式（{@code *}，表示任意个任意字符）的匹配支持类。
 * 
 * @author datagear@163.com
 *
 */
public class AsteriskPatternSupport
{
	private AsteriskPatternMatcher matcher;

	public AsteriskPatternSupport()
	{
		super();
	}

	public AsteriskPatternSupport(AsteriskPatternMatcher matcher)
	{
		super();
		this.matcher = matcher;
	}

	public AsteriskPatternMatcher getMatcher()
	{
		return matcher;
	}

	public void setMatcher(AsteriskPatternMatcher matcher)
	{
		this.matcher = matcher;
	}

	/**
	 * 查找第一个映射表关键字匹配给定任一参数的值。
	 * 
	 * @param <V>
	 * @param map
	 * @param params
	 * @param paramPattern
	 *            {@code true} 将{@code params}作为匹配模式，{@code false}
	 *            将{@code map}的关键字作为匹配模式
	 * @return 没有匹配时返回{@code null}
	 */
	public <V> V findKeyMatched(Map<String, V> map, Collection<String> params, boolean paramPattern)
	{
		if (map == null || map.isEmpty() || params == null || params.isEmpty())
			return null;

		for (String param : params)
		{
			V found = findKeyMatched(map, param, paramPattern);

			if (found != null)
				return found;
		}

		return null;
	}

	/**
	 * 查找第一个映射表关键字匹配给定参数的值。
	 * 
	 * @param <T>
	 * @param map
	 * @param param
	 * @param paramPattern
	 *            {@code true} 将{@code param}作为匹配模式，{@code false}
	 *            将{@code map}的关键字作为匹配模式
	 * @return 没有匹配时返回{@code null}
	 */
	public <V> V findKeyMatched(Map<String, V> map, String param, boolean paramPattern)
	{
		if (map == null || map.isEmpty())
			return null;

		for (Map.Entry<String, V> entry : map.entrySet())
		{
			String myKey = entry.getKey();

			if (StringUtil.isEquals(myKey, param))
				return entry.getValue();

			if (myKey == null)
				continue;

			boolean match = false;

			if (paramPattern)
				match = this.matcher.matches(param, myKey);
			else
				match = this.matcher.matches(myKey, param);

			if (match)
				return entry.getValue();
		}

		return null;
	}

	/**
	 * 查找第一个{@linkplain KeyValuePair#getKey()}匹配给定任一参数的{@linkplain KeyValuePair#getValue()}。
	 * 
	 * @param list
	 * @param params
	 * @param paramPattern
	 *            {@code true} 将{@code params}作为匹配模式，{@code false}
	 *            将{@code list}的{@linkplain KeyValuePair#getKey()}作为匹配模式
	 * @return 没有匹配时返回{@code null}
	 */
	public <V> V findKeyMatched(List<? extends KeyValuePair<String, V>> list, Collection<String> params,
			boolean paramPattern)
	{
		if (list == null || list.isEmpty() || params == null || params.isEmpty())
			return null;

		for (String param : params)
		{
			V found = findKeyMatched(list, param, paramPattern);

			if (found != null)
				return found;
		}

		return null;
	}

	/**
	 * 查找第一个{@linkplain KeyValuePair#getKey()}匹配给定参数的{@linkplain KeyValuePair#getValue()}。
	 * 
	 * @param list
	 * @param param
	 * @param paramPattern
	 *            {@code true} 将{@code param}作为匹配模式，{@code false}
	 *            将{@code list}的{@linkplain KeyValuePair#getKey()}作为匹配模式
	 * @return 没有匹配时返回{@code null}
	 */
	public <V> V findKeyMatched(List<? extends KeyValuePair<String, V>> list, String param, boolean paramPattern)
	{
		if (list == null || list.isEmpty())
			return null;

		for (KeyValuePair<String, V> entry : list)
		{
			String myKey = entry.getKey();

			if (StringUtil.isEquals(myKey, param))
				return entry.getValue();

			if (myKey == null)
				continue;

			boolean match = false;

			if (paramPattern)
				match = this.matcher.matches(param, myKey);
			else
				match = this.matcher.matches(myKey, param);

			if (match)
				return entry.getValue();
		}

		return null;
	}
}
