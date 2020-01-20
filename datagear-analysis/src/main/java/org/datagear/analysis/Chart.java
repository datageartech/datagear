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

	private ChartDataSet[] chartDataSets;

	public Chart()
	{
		super();
	}

	public Chart(String id)
	{
		super(id);
	}

	public Chart(String id, RenderContext renderContext, ChartPlugin<?> plugin, Map<String, ?> propertyValues,
			ChartDataSet[] chartDataSets)
	{
		super(id);
		this.renderContext = renderContext;
		this.plugin = plugin;
		this.propertyValues = propertyValues;
		this.chartDataSets = chartDataSets;
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

	public ChartDataSet[] getChartDataSets()
	{
		return chartDataSets;
	}

	public void setChartDataSets(ChartDataSet[] chartDataSets)
	{
		this.chartDataSets = chartDataSets;
	}

	/**
	 * 获取此图表的所有{@linkplain DataSetResult}。
	 * 
	 * @param dataSetParamValues
	 * @return
	 * @throws DataSetException
	 */
	public DataSetResult[] getDataSetResults(Map<String, ?> dataSetParamValues) throws DataSetException
	{
		if (this.chartDataSets == null || this.chartDataSets.length == 0)
			return new DataSetResult[0];

		DataSetResult[] dataSets = new DataSetResult[this.chartDataSets.length];

		for (int i = 0; i < this.chartDataSets.length; i++)
		{
			DataSetResult dataSetResult = this.chartDataSets[i].getDataSet()
					.getResult(dataSetParamValues);
			dataSets[i] = dataSetResult;
		}

		return dataSets;
	}
}
