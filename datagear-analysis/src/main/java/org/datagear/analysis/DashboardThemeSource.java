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
	 * 获取默认{@linkplain DashboardTheme}。
	 * 
	 * @return
	 */
	DashboardTheme getDashboardTheme();

	/**
	 * 获取指定名称的{@linkplain DashboardTheme}，没有则返回{@code null}。
	 * 
	 * @param themeName
	 * @return
	 */
	DashboardTheme getDashboardTheme(String themeName);
}
