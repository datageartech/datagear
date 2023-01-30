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

package org.datagear.web.util;

import java.util.Arrays;
import java.util.List;

/**
 * 系统支持的主题枚举。
 * 
 * @author datagear@163.com
 */
public class Themes
{
	/** 蓝色 */
	public static final String BLUE = "blue";

	/** 蓝色-暗黑模式 */
	public static final String BLUE_DARK = "blueDark";

	/** 所有主题列表 */
	public static final List<String> THEMES = Arrays.asList(BLUE, BLUE_DARK);

	/**
	 * 校验并规范主题。
	 * <p>
	 * 如果给定主题名合法，返回之；否则，返回{@linkplain #LIGHT}。
	 * </p>
	 * 
	 * @param theme
	 * @return
	 */
	public static String trimTheme(String theme)
	{
		for (String enumTheme : THEMES)
		{
			if (enumTheme.equalsIgnoreCase(theme))
			{
				return enumTheme;
			}
		}

		return BLUE;
	}
}
