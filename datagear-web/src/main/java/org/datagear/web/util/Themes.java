/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
