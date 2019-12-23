/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.util;

import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ThemeResolver;
import org.springframework.web.servlet.theme.ThemeChangeInterceptor;

/**
 * 枚举{@linkplain ThemeChangeInterceptor}。
 * <p>
 * 限定主题名枚举，防止无效的主题参数导致页面错乱。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class EnumThemeChangeInterceptor extends ThemeChangeInterceptor
{
	private List<String> themes;

	public EnumThemeChangeInterceptor()
	{
		super();
	}

	public EnumThemeChangeInterceptor(List<String> themes)
	{
		super();
		this.themes = themes;
	}

	public List<String> getThemes()
	{
		return themes;
	}

	public void setThemes(List<String> themes)
	{
		this.themes = themes;
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
			newTheme = getThemeInEnum(newTheme);
			// 新增此行，其他都从父类中拷贝

			themeResolver.setThemeName(request, response, newTheme);
		}
		// Proceed in any case.
		return true;
	}

	/**
	 * 获取合法的枚举主题名。
	 * 
	 * @param theme
	 * @return
	 */
	protected String getThemeInEnum(String theme)
	{
		if (this.themes == null || this.themes.isEmpty())
			return theme;

		for (String enumTheme : this.themes)
		{
			if (enumTheme.equalsIgnoreCase(theme))
			{
				return enumTheme;
			}
		}

		return this.themes.get(0);
	}
}
