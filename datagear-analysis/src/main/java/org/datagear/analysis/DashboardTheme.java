/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
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

	private ChartTheme chartTheme;

	public DashboardTheme()
	{
		super();
	}

	public DashboardTheme(String backgroundColor, String foregroundColor, ChartTheme chartTheme)
	{
		super(backgroundColor, foregroundColor);
		this.chartTheme = chartTheme;
	}

	public DashboardTheme(String backgroundColor, String foregroundColor, String chartBackgroundColor,
			String... chartGraphColors)
	{
		super(backgroundColor, foregroundColor);
		this.chartTheme = new ChartTheme(chartBackgroundColor, foregroundColor, chartGraphColors,
				new Theme(backgroundColor, foregroundColor));
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
