/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

/**
 * 
 */
package org.datagear.web.config.support;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.web.util.Themes;
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
	public static final String COOKIE_THEME_NAME = "THEME";

	public EnumCookieThemeResolver()
	{
		setCookieName(COOKIE_THEME_NAME);
		super.setDefaultThemeName(Themes.LIGHT);
		super.setCookieMaxAge(60 * 60 * 24 * 365);
	}

	@Override
	public String resolveThemeName(HttpServletRequest request)
	{
		String themeName = super.resolveThemeName(request);

		// 确保已设置的主题名是合法的
		String trimThemeName = Themes.trimTheme(themeName);
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
		themeName = Themes.trimTheme(themeName);

		super.setThemeName(request, response, themeName);
	}
}
