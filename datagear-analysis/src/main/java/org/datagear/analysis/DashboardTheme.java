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

	public DashboardTheme(String backgroundColor, String foregroundColor, String borderColor, ChartTheme chartTheme)
	{
		super(backgroundColor, foregroundColor, borderColor);
		this.chartTheme = chartTheme;
	}

	public DashboardTheme(String backgroundColor, String foregroundColor, String borderColor,
			String... chartGraphColors)
	{
		super(backgroundColor, foregroundColor, borderColor);
		this.chartTheme = new ChartTheme(backgroundColor, foregroundColor, borderColor, chartGraphColors,
				new Theme(backgroundColor, foregroundColor, borderColor));
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
