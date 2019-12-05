/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis;

/**
 * 图表模板。
 * <p>
 * 它可在{@linkplain RenderContext}中绘制自己所描述的{@linkplain Chart}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ChartTemplate<T extends RenderContext> extends AbstractIdentifiable
{
	private ChartPlugin<T> chartPlugin;

	private ChartPropertyValues chartPropertyValues;

	private DataSetFactory[] dataSetFactories;

	public ChartTemplate()
	{
		super();
	}

	public ChartTemplate(String id, ChartPlugin<T> chartPlugin, ChartPropertyValues chartPropertyValues,
			DataSetFactory... dataSetFactories)
	{
		super(id);
		this.chartPlugin = chartPlugin;
		this.chartPropertyValues = chartPropertyValues;
		this.dataSetFactories = dataSetFactories;
	}

	public ChartPlugin<T> getChartPlugin()
	{
		return chartPlugin;
	}

	public void setChartPlugin(ChartPlugin<T> chartPlugin)
	{
		this.chartPlugin = chartPlugin;
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

	/**
	 * 渲染{@linkplain Chart}。
	 * 
	 * @param renderContext
	 * @return
	 * @throws RenderException
	 */
	public Chart render(T renderContext) throws RenderException
	{
		return this.chartPlugin.renderChart(renderContext, this.chartPropertyValues, this.dataSetFactories);
	}
}
