/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support.html;

import org.datagear.analysis.Chart;
import org.datagear.analysis.ChartPlugin;
import org.datagear.analysis.ChartPropertyValues;
import org.datagear.analysis.DataSetFactory;
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

	public HtmlChart(String id, HtmlRenderContext renderContext, ChartPlugin<?> chartPlugin,
			ChartPropertyValues chartPropertyValues, DataSetFactory[] dataSetFactories, String elementId,
			String varName)
	{
		super(id, renderContext, chartPlugin, chartPropertyValues, dataSetFactories);
		this.elementId = elementId;
		this.varName = varName;
	}

	@Override
	public HtmlRenderContext getRenderContext()
	{
		return (HtmlRenderContext) super.getRenderContext();
	}

	@Override
	public void setRenderContext(RenderContext renderContext)
	{
		if (renderContext != null && !(renderContext instanceof HtmlRenderContext))
			throw new IllegalArgumentException();

		super.setRenderContext(renderContext);
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
