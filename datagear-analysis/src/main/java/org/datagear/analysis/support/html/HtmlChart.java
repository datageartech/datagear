/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
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
	private String elementId;

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
