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
