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

package org.datagear.web.util;

import java.util.Arrays;
import java.util.List;

import org.datagear.analysis.DashboardTheme;
import org.datagear.analysis.support.SimpleDashboardThemeSource;

/**
 * 系统支持的主题规范。
 * 
 * @author datagear@163.com
 */
public class ThemeSpec
{
	/** 蓝色 */
	public static final String BLUE = "blue";

	/** 蓝色-暗黑模式 */
	public static final String BLUE_DARK = "blueDark";

	/** 默认主题 */
	private String defaultTheme = BLUE;
	
	/** 主题列表 */
	private List<String> themes = Arrays.asList(BLUE, BLUE_DARK);
	
	public ThemeSpec()
	{
		super();
	}
	
	/**
	 * 获取默认主题。
	 * 
	 * @return
	 */
	public String getDefaultTheme()
	{
		return defaultTheme;
	}

	public void setDefaultTheme(String defaultTheme)
	{
		this.defaultTheme = defaultTheme;
	}

	/**
	 * 获取所有主题，其中应包含{@linkplain #getDefaultTheme()}。
	 * 
	 * @return
	 */
	public List<String> getThemes()
	{
		return themes;
	}
	
	protected void setThemes(List<String> themes)
	{
		this.themes = themes;
	}

	/**
	 * 校验并规范主题。
	 * <p>
	 * 如果给定主题名合法，返回之；否则，返回{@linkplain #defaultTheme()}。
	 * </p>
	 * 
	 * @param theme
	 * @return
	 */
	public String trimTheme(String theme)
	{
		List<String> themes = getThemes();
		
		for (String enumTheme : themes)
		{
			if (enumTheme.equalsIgnoreCase(theme))
			{
				return enumTheme;
			}
		}
		
		return getDefaultTheme();
	}
	
	/**
	 * 获取系统主题对应的{@linkplain DashboardTheme}。
	 * 
	 * @param theme
	 * @return
	 */
	public DashboardTheme dashboardTheme(String theme)
	{
		theme = trimTheme(theme);
		return toDashboardTheme(theme);
	}

	/**
	 * 获取系统主题对应的{@linkplain DashboardTheme}。
	 * 
	 * @param theme
	 * @return
	 */
	protected DashboardTheme toDashboardTheme(String theme)
	{
		DashboardTheme dh = null;

		if (BLUE.equalsIgnoreCase(theme))
			dh = SimpleDashboardThemeSource.THEME_LIGHT;
		else if (BLUE_DARK.equalsIgnoreCase(theme))
			dh = SimpleDashboardThemeSource.THEME_DARK;

		return dh;
	}

	/**
	 * 是否浅色主题。
	 * 
	 * @param theme
	 * @return
	 */
	public boolean isLightTheme(String theme)
	{
		return BLUE.equals(theme);
	}

	/**
	 * 是否暗色主题。
	 * 
	 * @param theme
	 * @return
	 */
	public boolean isDarkTheme(String theme)
	{
		return BLUE_DARK.equals(theme);
	}
}
