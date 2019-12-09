/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support.html;

import org.datagear.analysis.ChartPlugin;
import org.datagear.analysis.ChartPropertyValues;
import org.datagear.analysis.DataSetFactory;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.support.AbstractChart;

/**
 * HTML图表。
 * 
 * @author datagear@163.com
 *
 */
public class HtmlChart extends AbstractChart
{
	private String chartElementId;

	private String chartVarName;

	public HtmlChart()
	{
		super();
	}

	public HtmlChart(String id, ChartPlugin<?> chartPlugin, RenderContext renderContext,
			ChartPropertyValues chartPropertyValues, DataSetFactory[] dataSetFactories, String chartElementId,
			String chartVarName)
	{
		super(id, chartPlugin, renderContext, chartPropertyValues, dataSetFactories);
		this.chartElementId = chartElementId;
		this.chartVarName = chartVarName;
	}

	public String getChartElementId()
	{
		return chartElementId;
	}

	public void setChartElementId(String chartElementId)
	{
		this.chartElementId = chartElementId;
	}

	public String getChartVarName()
	{
		return chartVarName;
	}

	public void setChartVarName(String chartVarName)
	{
		this.chartVarName = chartVarName;
	}
}
