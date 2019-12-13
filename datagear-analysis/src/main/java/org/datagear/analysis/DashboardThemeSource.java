/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis;

/**
 * {@linkplain DashboardTheme}源。
 * 
 * @author datagear@163.com
 *
 */
public interface DashboardThemeSource
{
	/**
	 * 获取指定{@linkplain RenderStyle}的{@linkplain DashboardTheme}，没有则返回{@code null}。
	 * 
	 * @param renderStyle
	 * @return
	 */
	DashboardTheme getDashboardTheme(RenderStyle renderStyle);
}
