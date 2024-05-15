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

import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

/**
 * 基于星号模式（{@code *}，表示任意个任意字符）的匹配器。
 * <p>
 * 此类同时支持星号匹配模式、正则匹配模式，以{@linkplain #PATTERN_PREFIX_REGEX}开头的是正则匹配模式，
 * 以{@linkplain #PATTERN_PREFIX_ASTERISK}开头的是星号匹配模式，其他，则都作为星号匹配模式。
 * </p>
 * <p>
 * 星号匹配规则如下：
 * </p>
 * <ul>
 * <li>{@code *}<br>
 * 匹配任意字符串</li>
 * <li>{@code *abc}<br>
 * 匹配以{@code abc}结尾的字符串</li>
 * <li>{@code abc*}<br>
 * 匹配以{@code abc}开头的字符串</li>
 * <li>{@code abc*def}<br>
 * 匹配以{@code abc}开头、以{@code def}结尾的字符串</li>
 * <li>{@code *abc*def*}<br>
 * 匹配依次包含{@code abc}、{@code def}的字符串</li>
 * </ul>
 * 
 * @author datagear@163.com
 *
 */
public class AsteriskPatternMatcher
{
	public static final char ASTERISK = '*';

	/**
	 * 星号匹配模式前缀
	 */
	public static final String PATTERN_PREFIX_ASTERISK = "asterisk:";

	/**
	 * 正则匹配模式前缀
	 */
	public static final String PATTERN_PREFIX_REGEX = "regex:";

	/**
	 * 能够匹配任意非{@code null}字符串的星号匹配模式。
	 */
	public static final String ALL_PATTERN = new StringBuilder().append(ASTERISK).toString();

	/** 是否忽略大小写 */
	private boolean ignoreCase = false;

	/**
	 * 【匹配模式字符串 - 正则表达式对象】映射缓存
	 */
	private ConcurrentMap<String, WeakReference<Pattern>> patternRegexCache = new ConcurrentHashMap<>();

	public AsteriskPatternMatcher()
	{
		super();
	}

	public AsteriskPatternMatcher(boolean ignoreCase)
	{
		super();
		this.ignoreCase = ignoreCase;
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
	 * 是否匹配。
	 * 
	 * @param pattern
	 *            允许{@code null}、{@code ""}，星号匹配模式/正则匹配模式（需以{@linkplain #PATTERN_PREFIX_REGEX}开头）字符串，
	 *            {@code null}只能匹配{@code null}，{@code ""}、{@linkplain #PATTERN_PREFIX_ASTERISK}、{@linkplain #PATTERN_PREFIX_REGEX}只能匹配{@code ""}
	 * @param text
	 *            允许{@code null}、{@code ""}，要匹配的字符串，{@code null}只能被{@code null}匹配，{@code ""}可以被{@code ""}、{@code "*"}匹配
	 * @return
	 */
	public boolean matches(String pattern, String text)
	{
		if (pattern == null)
			return (text == null);

		if (pattern.isEmpty() || PATTERN_PREFIX_ASTERISK.equals(pattern)
				|| PATTERN_PREFIX_REGEX.equals(pattern))
			return (text != null && text.isEmpty());

		if (text == null)
			return (pattern == null);

		Pattern rp = getRegexPattern(pattern);
		return rp.matcher(text).matches();
	}

	protected Pattern getRegexPattern(String pattern)
	{
		WeakReference<Pattern> ref = this.patternRegexCache.get(pattern);
		Pattern p = (ref == null ? null : ref.get());

		if (p == null)
		{
			p = buildPattern(pattern);
			this.patternRegexCache.put(pattern, new WeakReference<Pattern>(p));
		}

		return p;
	}

	protected Pattern buildPattern(String pattern)
	{
		if(pattern.startsWith(PATTERN_PREFIX_REGEX))
		{
			pattern = pattern.substring(PATTERN_PREFIX_REGEX.length());

			if (isIgnoreCase())
				return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
			else
				return Pattern.compile(pattern);
		}
		else
		{
			if(pattern.startsWith(PATTERN_PREFIX_ASTERISK))
				pattern = pattern.substring(PATTERN_PREFIX_ASTERISK.length());

			return buildAsteriskPattern(pattern);
		}
	}

	/**
	 * 将星号模式字符串转换为正则字符串。
	 * 
	 * @param asteriskPattern
	 * @return
	 */
	protected Pattern buildAsteriskPattern(String asteriskPattern)
	{
		StringBuilder pb = new StringBuilder();

		pb.append('^');

		char[] cs = asteriskPattern.toCharArray();
		StringBuilder lb = new StringBuilder();
		for (int i = 0; i < cs.length; i++)
		{
			char c = cs[i];

			if (c == ASTERISK)
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
}
