/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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

	public DashboardTheme(String name, String color, String backgroundColor, String actualBackgroundColor,
			ChartTheme chartTheme)
	{
		super(name, color, backgroundColor, actualBackgroundColor);
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
