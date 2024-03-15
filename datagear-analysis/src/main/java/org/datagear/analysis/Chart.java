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

/**
 * 图表。
 * 
 * @author datagear@163.com
 *
 */
public class Chart extends ChartDefinition
{
	private static final long serialVersionUID = 1L;

	private ChartPlugin plugin;

	private RenderContext renderContext;

	public Chart()
	{
		super();
	}

	public Chart(String id, String name, ChartDataSet[] chartDataSets, ChartPlugin plugin, RenderContext renderContext)
	{
		super(id, name, chartDataSets);
		this.plugin = plugin;
		this.renderContext = renderContext;
	}

	public Chart(ChartDefinition chartDefinition, ChartPlugin plugin, RenderContext renderContext)
	{
		super(chartDefinition);
		this.plugin = plugin;
		this.renderContext = renderContext;
	}

	public Chart(Chart chart)
	{
		this(chart, chart.plugin, chart.renderContext);
	}

	public RenderContext getRenderContext()
	{
		return renderContext;
	}

	public void setRenderContext(RenderContext renderContext)
	{
		this.renderContext = renderContext;
	}

	public ChartPlugin getPlugin()
	{
		return plugin;
	}

	public void setPlugin(ChartPlugin plugin)
	{
		this.plugin = plugin;
	}
}
