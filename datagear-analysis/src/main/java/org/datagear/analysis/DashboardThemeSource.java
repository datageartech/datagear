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
