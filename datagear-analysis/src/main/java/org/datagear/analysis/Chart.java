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
public class Chart extends ChartDefinition
{
	private ChartPlugin<?> plugin;

	private RenderContext renderContext;

	public Chart()
	{
		super();
	}

	public Chart(String id, String name, ChartDataSet[] chartDataSets, ChartPlugin<?> plugin,
			RenderContext renderContext)
	{
		super(id, name, chartDataSets);
		this.plugin = plugin;
		this.renderContext = renderContext;
	}

	public Chart(ChartDefinition chartDefinition, ChartPlugin<?> plugin, RenderContext renderContext)
	{
		super(chartDefinition.getId(), chartDefinition.getName(), chartDefinition.getChartDataSets());
		setChartParamValues(chartDefinition.getChartParamValues());
		setDataSetParamValues(chartDefinition.getDataSetParamValues());
		setUpdateInterval(chartDefinition.getUpdateInterval());

		this.plugin = plugin;
		this.renderContext = renderContext;
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

	/**
	 * 是否已准备齐全执行{@linkplain #getDataSetResults(Map)}的参数。
	 * 
	 * @param dataSetParamValues
	 * @return
	 */
	public boolean isReadyForDataSetResults(Map<String, ?> dataSetParamValues)
	{
		ChartDataSet[] chartDataSets = getChartDataSets();

		if (chartDataSets == null || chartDataSets.length == 0)
			return true;

		for (ChartDataSet chartDataSet : chartDataSets)
		{
			if (!chartDataSet.getDataSet().isReady(dataSetParamValues))
				return false;
		}

		return true;
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
		ChartDataSet[] chartDataSets = getChartDataSets();

		if (chartDataSets == null || chartDataSets.length == 0)
			return new DataSetResult[0];

		DataSetResult[] dataSets = new DataSetResult[chartDataSets.length];

		for (int i = 0; i < chartDataSets.length; i++)
		{
			DataSetResult dataSetResult = chartDataSets[i].getDataSet().getResult(dataSetParamValues);
			dataSets[i] = dataSetResult;
		}

		return dataSets;
	}
}
