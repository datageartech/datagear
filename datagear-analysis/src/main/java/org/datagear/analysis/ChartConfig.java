/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis;

/**
 * 图表配置。
 * 
 * @author datagear@163.com
 *
 */
public class ChartConfig
{
	private ChartPropertyValues chartPropertyValues;

	private DataSetFactory[] dataSetFactories;

	public ChartConfig()
	{
	}

	public ChartConfig(ChartPropertyValues chartPropertyValues, DataSetFactory... dataSetFactories)
	{
		super();
		this.chartPropertyValues = chartPropertyValues;
		this.dataSetFactories = dataSetFactories;
	}

	public ChartPropertyValues getChartPropertyValues()
	{
		return chartPropertyValues;
	}

	public void setChartPropertyValues(ChartPropertyValues chartPropertyValues)
	{
		this.chartPropertyValues = chartPropertyValues;
	}

	public DataSetFactory[] getDataSetFactories()
	{
		return dataSetFactories;
	}

	public void setDataSetFactories(DataSetFactory... dataSetFactories)
	{
		this.dataSetFactories = dataSetFactories;
	}
}
