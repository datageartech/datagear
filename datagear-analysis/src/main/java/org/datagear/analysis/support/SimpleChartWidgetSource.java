/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.datagear.analysis.RenderContext;

/**
 * @author datagear@163.com
 *
 */
public class SimpleChartWidgetSource implements ChartWidgetSource
{
	private Set<ChartWidget<?>> chartWidgets = new HashSet<ChartWidget<?>>();

	public SimpleChartWidgetSource()
	{
		super();
	}

	public SimpleChartWidgetSource(ChartWidget<?>... chartWidgets)
	{
		super();
		this.chartWidgets.addAll(Arrays.asList(chartWidgets));
	}

	public Set<ChartWidget<?>> getChartWidgets()
	{
		return chartWidgets;
	}

	public void setChartWidgets(Set<ChartWidget<?>> chartWidgets)
	{
		this.chartWidgets = chartWidgets;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends RenderContext> ChartWidget<T> getChartWidget(String id)
	{
		for (ChartWidget<?> chartWidget : this.chartWidgets)
		{
			if (chartWidget.getId().equals(id))
				return (ChartWidget<T>) chartWidget;
		}

		return null;
	}
}
