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
 * 看板主题。
 * 
 * @author datagear@163.com
 *
 */
public class DashboardTheme extends Theme
{
	private static final long serialVersionUID = 1L;

	public static final String PROPERTY_CHART_THEME = "chartTheme";

	private ChartTheme chartTheme;

	public DashboardTheme()
	{
		super();
	}

	public DashboardTheme(String name, String color, String backgroundColor, ChartTheme chartTheme)
	{
		super(name, color, backgroundColor);
		this.chartTheme = chartTheme;
	}

	public ChartTheme getChartTheme()
	{
		return chartTheme;
	}

	public void setChartTheme(ChartTheme chartTheme)
	{
		this.chartTheme = chartTheme;
	}

}
