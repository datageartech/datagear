/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */

package org.datagear.analysis;

import java.util.Map;

/**
 * 图表。
 * 
 * @author datagear@163.com
 *
 */
public class Chart extends AbstractIdentifiable
{
	public RenderContext renderContext;

	private ChartPlugin<?> plugin;

	private Map<String, ?> propertyValues;

	private ChartDataSetFactory[] chartDataSetFactories;

	public Chart()
	{
		super();
	}

	public Chart(String id)
	{
		super(id);
	}

	public Chart(String id, RenderContext renderContext, ChartPlugin<?> plugin, Map<String, ?> propertyValues,
			ChartDataSetFactory[] chartDataSetFactories)
	{
		super(id);
		this.renderContext = renderContext;
		this.plugin = plugin;
		this.propertyValues = propertyValues;
		this.chartDataSetFactories = chartDataSetFactories;
	}

	public RenderContext getRenderContext()
	{
		return renderContext;
	}

	public void setRenderContext(RenderContext renderContext)
	{
		this.renderContext = renderContext;
	}

	public ChartPlugin<?> getPlugin()
	{
		return plugin;
	}

	public void setPlugin(ChartPlugin<?> plugin)
	{
		this.plugin = plugin;
	}

	public Map<String, ?> getPropertyValues()
	{
		return propertyValues;
	}

	public void setPropertyValues(Map<String, ?> propertyValues)
	{
		this.propertyValues = propertyValues;
	}

	public ChartDataSetFactory[] getChartDataSetFactories()
	{
		return chartDataSetFactories;
	}

	public void setChartDataSetFactories(ChartDataSetFactory[] chartDataSetFactories)
	{
		this.chartDataSetFactories = chartDataSetFactories;
	}

	/**
	 * 获取数据集。
	 * 
	 * @param dataSetParamValues
	 * @return
	 * @throws DataSetException
	 */
	public DataSet[] getDataSets(Map<String, ?> dataSetParamValues) throws DataSetException
	{
		if (this.chartDataSetFactories == null || this.chartDataSetFactories.length == 0)
			return new DataSet[0];

		DataSet[] dataSets = new DataSet[this.chartDataSetFactories.length];

		for (int i = 0; i < this.chartDataSetFactories.length; i++)
		{
			DataSet dataSet = this.chartDataSetFactories[i].getDataSetFactory().getDataSet(dataSetParamValues);
			dataSets[i] = dataSet;
		}

		return dataSets;
	}
}
