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

package org.datagear.web.config.support;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.util.Global;
import org.datagear.web.util.ThemeSpec;
import org.springframework.web.servlet.theme.CookieThemeResolver;

/**
 * 枚举{@linkplain CookieThemeResolver}。
 * <p>
 * 限定主题名枚举，防止无效的主题导致系统出错。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class EnumCookieThemeResolver extends CookieThemeResolver
{
	public static final String COOKIE_THEME_NAME = Global.NAME_SHORTCUT_UC_PREFIX + "THEME";
	
	private ThemeSpec themeSpec;

	public EnumCookieThemeResolver(ThemeSpec themeSpec)
	{
		this.themeSpec = themeSpec;
		
		setCookieName(COOKIE_THEME_NAME);
		super.setDefaultThemeName(themeSpec.getDefaultTheme());
		super.setCookieMaxAge(60 * 60 * 24 * 365);
	}

	public ThemeSpec getThemeSpec()
	{
		return themeSpec;
	}

	public void setThemeSpec(ThemeSpec themeSpec)
	{
		this.themeSpec = themeSpec;
	}

	@Override
	public String resolveThemeName(HttpServletRequest request)
	{
		String themeName = super.resolveThemeName(request);

		// 确保已设置的主题名是合法的
		String trimThemeName = this.themeSpec.trimTheme(themeName);
		if (!trimThemeName.equals(themeName))
		{
			themeName = trimThemeName;
			request.setAttribute(THEME_REQUEST_ATTRIBUTE_NAME, themeName);
		}

		return themeName;
	}

	@Override
	public void setThemeName(HttpServletRequest request, HttpServletResponse response, String themeName)
	{
		themeName = this.themeSpec.trimTheme(themeName);
		super.setThemeName(request, response, themeName);
	}
}
