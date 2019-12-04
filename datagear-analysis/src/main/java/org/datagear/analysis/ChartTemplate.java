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
public class ChartTemplate<T extends RenderContext> extends ChartConfig implements Identifiable
{
	private String id;

	private ChartPlugin<T> chartPlugin;

	public ChartTemplate()
	{
		super();
	}

	public ChartTemplate(String id, ChartPlugin<T> chartPlugin, ChartPropertyValues chartPropertyValues,
			DataSetFactory... dataSetFactories)
	{
		super(chartPropertyValues, dataSetFactories);
		this.id = id;
		this.chartPlugin = chartPlugin;
	}

	@Override
	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public ChartPlugin<T> getChartPlugin()
	{
		return chartPlugin;
	}

	public void setChartPlugin(ChartPlugin<T> chartPlugin)
	{
		this.chartPlugin = chartPlugin;
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
		return this.chartPlugin.renderChart(renderContext, this);
	}
}
