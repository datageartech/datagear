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
import org.datagear.util.IDUtil;

/**
 * 图表部件。
 * <p>
 * 它可在{@linkplain RenderContext}中渲染自己所描述的{@linkplain Chart}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ChartWidget extends ChartDefinition
{
	private ChartPlugin plugin;

	public ChartWidget()
	{
		super();
	}

	public ChartWidget(String id, String name, ChartDataSet[] chartDataSets, ChartPlugin plugin)
	{
		super(id, name, chartDataSets);
		this.plugin = plugin;
	}

	public ChartPlugin getPlugin()
	{
		return plugin;
	}

	public void setPlugin(ChartPlugin plugin)
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
		ChartPlugin chartPlugin = chartPluginManager.get(chartPluginId);
		setPlugin(chartPlugin);
	}

	/**
	 * 渲染{@linkplain Chart}。
	 * 
	 * @param renderContext
	 * @return
	 * @throws RenderException
	 */
	public Chart render(RenderContext renderContext) throws RenderException
	{
		return this.plugin.renderChart(renderContext, buildChartDefinition(renderContext));
	}

	protected ChartDefinition buildChartDefinition(RenderContext renderContext) throws RenderException
	{
		ChartDefinition chartDefinition = new ChartDefinition();
		ChartDefinition.copy(this, chartDefinition);
		chartDefinition.setId(generateChartId(renderContext));
		return chartDefinition;
	}

	/**
	 * 生成图表ID。
	 * 
	 * @param renderContext
	 * @return
	 * @throws RenderException
	 */
	protected String generateChartId(RenderContext renderContext) throws RenderException
	{
		return IDUtil.uuid();
	}
}
