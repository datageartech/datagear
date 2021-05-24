/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support.html;

import java.io.Writer;

import org.datagear.analysis.ChartDataSet;
import org.datagear.analysis.ChartDefinition;
import org.datagear.analysis.ChartPlugin;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.RenderException;
import org.datagear.analysis.support.ChartWidget;
import org.datagear.analysis.support.html.HtmlChartRenderAttr.HtmlChartRenderOption;

/**
 * HTML {@linkplain ChartWidget}。
 * <p>
 * 注意：此类{@linkplain #render(RenderContext)}的{@linkplain RenderContext}必须符合{@linkplain HtmlChartRenderAttr#inflate(RenderContext, Writer, HtmlChartRenderOption)}规范。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class HtmlChartWidget extends ChartWidget
{
	public HtmlChartWidget()
	{
		super();
	}

	public HtmlChartWidget(String id, String name, ChartDataSet[] chartDataSets, HtmlChartPlugin plugin)
	{
		super(id, name, chartDataSets, plugin);
	}

	public HtmlChartWidget(ChartDefinition chartDefinition, HtmlChartPlugin plugin)
	{
		super(chartDefinition, plugin);
	}

	@Override
	public HtmlChartPlugin getPlugin()
	{
		return (HtmlChartPlugin) super.getPlugin();
	}

	@Override
	public void setPlugin(ChartPlugin plugin)
	{
		if (plugin != null && !(plugin instanceof HtmlChartPlugin))
			throw new IllegalArgumentException();

		super.setPlugin(plugin);
	}

	@Override
	public HtmlChart render(RenderContext renderContext) throws RenderException
	{
		HtmlChart chart = (HtmlChart) super.render(renderContext);

		return chart;
	}
}
