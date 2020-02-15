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

	public HtmlChartWidget(String id, String name, ChartDataSet[] chartDataSets, HtmlChartPlugin<T> plugin)
	{
		super(id, name, chartDataSets, plugin);
	}

	@Override
	public HtmlChartPlugin<T> getPlugin()
	{
		return (HtmlChartPlugin<T>) super.getPlugin();
	}

	@Override
	public void setPlugin(ChartPlugin<T> plugin)
	{
		if (plugin != null && !(plugin instanceof HtmlChartPlugin<?>))
			throw new IllegalArgumentException();

		super.setPlugin(plugin);
	}

	@Override
	public HtmlChart render(T renderContext) throws RenderException
	{
		HtmlChart chart = (HtmlChart) super.render(renderContext);

		return chart;
	}
}
