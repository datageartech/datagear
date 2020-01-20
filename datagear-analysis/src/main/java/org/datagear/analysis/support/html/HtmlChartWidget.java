/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support.html;

import org.datagear.analysis.ChartDataSet;
import org.datagear.analysis.ChartPlugin;
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
	public HtmlChartWidget()
	{
		super();
	}

	public HtmlChartWidget(String id, String name, HtmlChartPlugin<T> chartPlugin,
			ChartDataSet... chartDataSets)
	{
		super(id, name, chartPlugin, chartDataSets);
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
		HtmlChart chart = (HtmlChart) super.render(renderContext);

		return chart;
	}
}
