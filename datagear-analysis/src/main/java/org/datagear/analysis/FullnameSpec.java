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

package org.datagear.analysis;

import org.datagear.util.StringUtil;

/**
 * 全名规范。
 * 
 * @author datagear@163.com
 *
 */
public class FullnameSpec
{
	/**
	 * 全名分隔符。
	 */
	public static final char SEPARATOR = '.';

	/**
	 * 转义符。
	 */
	public static final char ESCAPER = '\\';

	/**
	 * 保留字符。
	 * <p>
	 * 这些字符会在转换为全名时转义。
	 * </p>
	 */
	public static final char[] RESERVED_CHARS = new char[] {
			SEPARATOR, ESCAPER
	};

	/**
	 * 转换为全名。
	 * 
	 * @param name
	 * @param parentFullname
	 *            允许{@code null}
	 * @return
	 */
	public static String toFullname(String name, String parentFullname)
	{
		if (StringUtil.isEmpty(name))
			throw new IllegalArgumentException("[name] requied");

		name = escapeName(name);

		if (!StringUtil.isEmpty(parentFullname))
			name = parentFullname + SEPARATOR + name;

		return name;
	}

	/**
	 * 是否顶层全名。
	 * 
	 * @param fullname
	 * @param name
	 * @return
	 */
	public static boolean isTopFullname(String fullname, String name)
	{
		if (StringUtil.isEmpty(fullname))
			throw new IllegalArgumentException("[fullname] requied");

		if (StringUtil.isEmpty(name))
			throw new IllegalArgumentException("[name] requied");

		return fullname.equals(toFullname(name, null));
	}

	protected static String escapeName(String name)
	{
		StringBuilder re = new StringBuilder();

		for (int i = 0, len = name.length(); i < len; i++)
		{
			char c = name.charAt(i);

			if (isReserved(c))
				re.append(ESCAPER);

			re.append(c);
		}

		return re.toString();
	}

	/**
	 * 是否保留字符。
	 * 
	 * @param c
	 * @return
	 */
	protected static boolean isReserved(char c)
	{
		for (int i = 0; i < RESERVED_CHARS.length; i++)
		{
			if (c == RESERVED_CHARS[i])
				return true;
		}

		return false;
	}
}
