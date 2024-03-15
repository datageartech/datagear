/*
 * Copyright 2018-present datagear.tech
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
