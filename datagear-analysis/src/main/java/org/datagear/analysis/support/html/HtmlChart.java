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

package org.datagear.analysis.support.html;

import org.datagear.analysis.Chart;
import org.datagear.analysis.ChartDataSet;
import org.datagear.analysis.ChartDefinition;
import org.datagear.analysis.ChartPlugin;
import org.datagear.analysis.RenderContext;

/**
 * HTML图表。
 * 
 * @author datagear@163.com
 *
 */
public class HtmlChart extends Chart
{
	private static final long serialVersionUID = 1L;

	/** 图表的HTML元素ID */
	private String elementId;

	/** 图表JS变量名 */
	private String varName;

	public HtmlChart()
	{
		super();
	}

	public HtmlChart(String id, String name, ChartDataSet[] chartDataSets, ChartPlugin plugin,
			RenderContext renderContext, String elementId, String varName)
	{
		super(id, name, chartDataSets, plugin, renderContext);
		this.elementId = elementId;
		this.varName = varName;
	}

	public HtmlChart(ChartDefinition chartDefinition, ChartPlugin plugin, RenderContext renderContext, String elementId,
			String varName)
	{
		super(chartDefinition, plugin, renderContext);
		this.elementId = elementId;
		this.varName = varName;
	}

	public HtmlChart(HtmlChart chart)
	{
		this(chart, chart.getPlugin(), chart.getRenderContext(), chart.elementId, chart.varName);
	}

	@Override
	public HtmlChartPlugin getPlugin()
	{
		return (HtmlChartPlugin) super.getPlugin();
	}

	@Override
	public void setPlugin(ChartPlugin plugin)
	{
		if (plugin != null && !(plugin instanceof HtmlChartPlugin))
			throw new IllegalArgumentException();

		super.setPlugin(plugin);
	}

	public String getElementId()
	{
		return elementId;
	}

	public void setElementId(String elementId)
	{
		this.elementId = elementId;
	}

	public String getVarName()
	{
		return varName;
	}

	public void setVarName(String varName)
	{
		this.varName = varName;
	}
}
