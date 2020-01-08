/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support.html;

import org.datagear.analysis.ChartPlugin;
import org.datagear.analysis.DataSetFactory;
import org.datagear.analysis.RenderException;
import org.datagear.analysis.support.ChartWidget;

/**
 * HTML {@linkplain ChartWidget}。
 * 
 * @author datagear@163.com
 *
 */
public class HtmlChartWidget<T extends HtmlRenderContext> extends ChartWidget<T>
{
	/** 更新间隔毫秒数 */
	private int updateInterval = -1;

	public HtmlChartWidget()
	{
		super();
	}

	public HtmlChartWidget(String id, HtmlChartPlugin<T> chartPlugin, DataSetFactory... dataSetFactories)
	{
		super(id, chartPlugin, dataSetFactories);
	}

	@Override
	public HtmlChartPlugin<T> getChartPlugin()
	{
		return (HtmlChartPlugin<T>) super.getChartPlugin();
	}

	@Override
	public void setChartPlugin(ChartPlugin<T> chartPlugin)
	{
		if (chartPlugin != null && !(chartPlugin instanceof HtmlChartPlugin<?>))
			throw new IllegalArgumentException();

		super.setChartPlugin(chartPlugin);
	}

	@Override
	public HtmlChart render(T renderContext) throws RenderException
	{
		HtmlRenderAttributes.setChartUpdateInterval(renderContext, this.updateInterval);
		HtmlChart chart = (HtmlChart) super.render(renderContext);
		HtmlRenderAttributes.removeChartUpdateInterval(renderContext);

		return chart;
	}

	/**
	 * 获取图表更新间隔毫秒数。
	 * 
	 * @return {@code <0}：不间隔更新；0 ：实时更新；{@code >0}：间隔更新毫秒数
	 */
	public int getUpdateInterval()
	{
		return updateInterval;
	}

	public void setUpdateInterval(int updateInterval)
	{
		this.updateInterval = updateInterval;
	}
}
