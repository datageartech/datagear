/*
 * Copyright 2018-present datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package org.datagear.analysis.support.html;

import org.datagear.analysis.DataSetBind;
import org.datagear.analysis.ChartDefinition;
import org.datagear.analysis.ChartPlugin;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.RenderException;
import org.datagear.analysis.support.ChartWidget;

/**
 * HTML {@linkplain ChartWidget}。
 * <p>
 * 注意：此类{@linkplain #render(RenderContext)}和{@linkplain #render(RenderContext, String)}的{@linkplain RenderContext}参数必须是{@linkplain HtmlChartRenderContext}实例。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class HtmlChartWidget extends ChartWidget
{
	private static final long serialVersionUID = 1L;

	public HtmlChartWidget()
	{
		super();
	}

	public HtmlChartWidget(String id, String name, DataSetBind[] dataSetBinds, HtmlChartPlugin plugin)
	{
		super(id, name, dataSetBinds, plugin);
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
		return (HtmlChart) super.render(renderContext);
	}

	@Override
	public HtmlChart render(RenderContext renderContext, String chartId) throws RenderException
	{
		return (HtmlChart)super.render(renderContext, chartId);
	}
}
