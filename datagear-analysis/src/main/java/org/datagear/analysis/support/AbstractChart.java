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
	private ChartPlugin<?> plugin;

	public RenderContext renderContext;

	private ChartPropertyValues propertyValues;

	private DataSetFactory[] dataSetFactories;

	public AbstractChart()
	{
		super();
	}

	public AbstractChart(String id, ChartPlugin<?> plugin, RenderContext renderContext,
			ChartPropertyValues propertyValues, DataSetFactory[] dataSetFactories)
	{
		super(id);
		this.plugin = plugin;
		this.renderContext = renderContext;
		this.propertyValues = propertyValues;
		this.dataSetFactories = dataSetFactories;
	}

	@Override
	public ChartPlugin<?> getPlugin()
	{
		return plugin;
	}

	public void setPlugin(ChartPlugin<?> plugin)
	{
		this.plugin = plugin;
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
	public ChartPropertyValues getPropertyValues()
	{
		return propertyValues;
	}

	public void setPropertyValues(ChartPropertyValues propertyValues)
	{
		this.propertyValues = propertyValues;
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
