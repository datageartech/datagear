/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis;

/**
 * 图表插件。
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public interface ChartPlugin<T extends ChartContext>
{
	String getName();

	String getLabel();

	String getIcon();

	ChartProperties getChartProperties();

	Chart getChart(T chartContext, ChartOptions chartOptions, DataSet... dataSet);
}
