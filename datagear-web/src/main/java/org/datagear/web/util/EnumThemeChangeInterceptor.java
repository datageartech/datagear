/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.util;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ThemeResolver;
import org.springframework.web.servlet.theme.ThemeChangeInterceptor;

/**
 * 枚举{@linkplain ThemeChangeInterceptor}。
 * <p>
 * 限定主题名枚举，防止无效的主题参数导致系统出错。
 * </p>
 * 
 * @author datagear@163.com
 * @deprecated 使用了{@linkplain EnumCookieThemeResolver}后，这里不再需要校验主题名
 */
@Deprecated
public class EnumThemeChangeInterceptor extends ThemeChangeInterceptor
{
	public EnumThemeChangeInterceptor()
	{
		super();
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws ServletException
	{
		ThemeResolver themeResolver = WebUtils.getThemeResolver(request);

		String newTheme = request.getParameter(getParamName());
		if (newTheme != null)
		{
			// 新增此行，其他都从父类中拷贝
			newTheme = Themes.trimTheme(newTheme);
			// 新增此行，其他都从父类中拷贝

			themeResolver.setThemeName(request, response, newTheme);
		}
		// Proceed in any case.
		return true;
	}
}
