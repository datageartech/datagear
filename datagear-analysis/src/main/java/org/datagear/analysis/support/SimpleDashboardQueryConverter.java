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

package org.datagear.analysis.support;

import java.util.Map;

import org.datagear.analysis.ChartDefinition;

/**
 * 简单{@linkplain DashboardQueryConverter}。
 * 
 * @author datagear@163.com
 *
 */
public class SimpleDashboardQueryConverter extends DashboardQueryConverter
{
	private Map<String, ? extends ChartDefinition> chartDefinitions;

	public SimpleDashboardQueryConverter()
	{
		super();
	}

	public SimpleDashboardQueryConverter(DataSetParamValueConverter dataSetParamValueConverter,
			Map<String, ? extends ChartDefinition> chartDefinitions)
	{
		super(dataSetParamValueConverter);
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
		return this.chartDefinitions.get(chartId);
	}
}
