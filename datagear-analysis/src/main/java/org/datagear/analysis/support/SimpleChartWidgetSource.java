/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 简单{@linkplain ChartWidgetSource}。
 * 
 * @author datagear@163.com
 *
 */
public class SimpleChartWidgetSource implements ChartWidgetSource
{
	private Set<ChartWidget> chartWidgets = new HashSet<>();

	public SimpleChartWidgetSource()
	{
		super();
	}

	public SimpleChartWidgetSource(ChartWidget... chartWidgets)
	{
		super();
		this.chartWidgets.addAll(Arrays.asList(chartWidgets));
	}

	public Set<ChartWidget> getChartWidgets()
	{
		return chartWidgets;
	}

	public void setChartWidgets(Set<ChartWidget> chartWidgets)
	{
		this.chartWidgets = chartWidgets;
	}

	@Override
	public ChartWidget getChartWidget(String id)
	{
		for (ChartWidget chartWidget : this.chartWidgets)
		{
			if (chartWidget.getId().equals(id))
				return chartWidget;
		}

		return null;
	}
}
