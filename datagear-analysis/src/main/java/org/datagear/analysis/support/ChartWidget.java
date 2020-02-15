/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.analysis.support;

import org.datagear.analysis.Chart;
import org.datagear.analysis.ChartDataSet;
import org.datagear.analysis.ChartDefinition;
import org.datagear.analysis.ChartPlugin;
import org.datagear.analysis.ChartPluginManager;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.RenderException;

/**
 * 图表部件。
 * <p>
 * 它可在{@linkplain RenderContext}中渲染自己所描述的{@linkplain Chart}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ChartWidget<T extends RenderContext> extends ChartDefinition
{
	private ChartPlugin<T> plugin;

	public ChartWidget()
	{
		super();
	}

	public ChartWidget(String id, String name, ChartDataSet[] chartDataSets, ChartPlugin<T> plugin)
	{
		super(id, name, chartDataSets);
		this.plugin = plugin;
	}

	public ChartPlugin<T> getPlugin()
	{
		return plugin;
	}

	public void setPlugin(ChartPlugin<T> plugin)
	{
		this.plugin = plugin;
	}

	/**
	 * 从{@linkplain ChartPluginManager}查找并设置{@linkplain #setPlugin(ChartPlugin)}。
	 * 
	 * @param chartPluginManager
	 * @param chartPluginId
	 * @return
	 */
	public void setPlugin(ChartPluginManager chartPluginManager, String chartPluginId)
	{
		ChartPlugin<T> chartPlugin = chartPluginManager.get(chartPluginId);
		setPlugin(chartPlugin);
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
		return this.plugin.renderChart(renderContext, buildChartDefinition(renderContext));
	}

	protected ChartDefinition buildChartDefinition(T renderContext) throws RenderException
	{
		ChartDefinition chartDefinition = new ChartDefinition();
		ChartDefinition.copy(this, chartDefinition);
		chartDefinition.setId(generateChartId(renderContext));
		return chartDefinition;
	}

	/**
	 * 生成图表ID。
	 * <p>
	 * 同一个渲染上下文内，允许多次使用同一个{@linkplain ChartWidget}，
	 * 为了保证{@linkplain Chart#getId()}不重复，所以这里要生成新的ID。
	 * </p>
	 * 
	 * @param renderContext
	 * @return
	 * @throws RenderException
	 */
	protected String generateChartId(T renderContext) throws RenderException
	{
		String key = "chartIndex-" + getId();
		Integer index = renderContext.getAttribute(key);
		if (index == null)
			index = 0;

		renderContext.setAttribute(key, index + 1);

		return getId() + "-" + index.toString();
	}
}
