/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis;

import java.util.Map;

/**
 * 简单{@linkplain DashboardQueryHandler}。
 * 
 * @author datagear@163.com
 *
 */
public class SimpleDashboardQueryHandler extends DashboardQueryHandler
{
	private Map<String, ? extends ChartDefinition> chartDefinitions;

	public SimpleDashboardQueryHandler()
	{
		super();
	}

	public SimpleDashboardQueryHandler(Map<String, ? extends ChartDefinition> chartDefinitions)
	{
		super();
		this.chartDefinitions = chartDefinitions;
	}

	public Map<String, ? extends ChartDefinition> getChartDefinitions()
	{
		return chartDefinitions;
	}

	public void setChartDefinitions(Map<String, ? extends ChartDefinition> chartDefinitions)
	{
		this.chartDefinitions = chartDefinitions;
	}

	@Override
	protected ChartDefinition getChartDefinition(String chartId)
	{
		return (this.chartDefinitions == null ? null : this.chartDefinitions.get(chartId));
	}
}
