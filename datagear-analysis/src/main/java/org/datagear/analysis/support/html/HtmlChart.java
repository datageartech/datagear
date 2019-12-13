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
	private String elementId;

	private String varName;

	public HtmlChart()
	{
		super();
	}

	public HtmlChart(String id, ChartPlugin<?> chartPlugin, RenderContext renderContext,
			ChartPropertyValues chartPropertyValues, DataSetFactory[] dataSetFactories, String elementId,
			String varName)
	{
		super(id, chartPlugin, renderContext, chartPropertyValues, dataSetFactories);
		this.elementId = elementId;
		this.varName = varName;
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
