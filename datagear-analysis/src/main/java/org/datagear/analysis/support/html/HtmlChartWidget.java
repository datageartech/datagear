/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support.html;

import org.datagear.analysis.ChartPlugin;
import org.datagear.analysis.ChartPropertyValues;
import org.datagear.analysis.DataSetFactory;
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

	public HtmlChartWidget(String id, HtmlChartPlugin<T> chartPlugin, ChartPropertyValues chartPropertyValues,
			DataSetFactory... dataSetFactories)
	{
		super(id, chartPlugin, chartPropertyValues, dataSetFactories);
	}

	@Override
	public HtmlChartPlugin<T> getChartPlugin()
	{
		return (HtmlChartPlugin<T>) super.getChartPlugin();
	}

	@Override
	public void setChartPlugin(ChartPlugin<T> chartPlugin)
	{
		if (!(chartPlugin instanceof HtmlChartPlugin<?>))
			throw new IllegalArgumentException();

		super.setChartPlugin(chartPlugin);
	}

}
