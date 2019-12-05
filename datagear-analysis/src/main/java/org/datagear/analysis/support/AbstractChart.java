/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import org.datagear.analysis.AbstractIdentifiable;
import org.datagear.analysis.Chart;
import org.datagear.analysis.ChartPlugin;
import org.datagear.analysis.ChartPropertyValues;
import org.datagear.analysis.DataSetFactory;
import org.datagear.analysis.RenderContext;

/**
 * 抽象{@linkplain Chart}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractChart extends AbstractIdentifiable implements Chart
{
	private ChartPlugin<?> chartPlugin;

	public RenderContext renderContext;

	private ChartPropertyValues chartPropertyValues;

	private DataSetFactory[] dataSetFactories;

	public AbstractChart()
	{
		super();
	}

	public AbstractChart(String id, ChartPlugin<?> chartPlugin, RenderContext renderContext,
			ChartPropertyValues chartPropertyValues, DataSetFactory[] dataSetFactories)
	{
		super(id);
		this.chartPlugin = chartPlugin;
		this.renderContext = renderContext;
		this.chartPropertyValues = chartPropertyValues;
		this.dataSetFactories = dataSetFactories;
	}

	@Override
	public ChartPlugin<?> getChartPlugin()
	{
		return chartPlugin;
	}

	public void setChartPlugin(ChartPlugin<?> chartPlugin)
	{
		this.chartPlugin = chartPlugin;
	}

	@Override
	public RenderContext getRenderContext()
	{
		return renderContext;
	}

	public void setRenderContext(RenderContext renderContext)
	{
		this.renderContext = renderContext;
	}

	@Override
	public ChartPropertyValues getChartPropertyValues()
	{
		return chartPropertyValues;
	}

	public void setChartPropertyValues(ChartPropertyValues chartPropertyValues)
	{
		this.chartPropertyValues = chartPropertyValues;
	}

	@Override
	public DataSetFactory[] getDataSetFactories()
	{
		return dataSetFactories;
	}

	public void setDataSetFactories(DataSetFactory[] dataSetFactories)
	{
		this.dataSetFactories = dataSetFactories;
	}
}
