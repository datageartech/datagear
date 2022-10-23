/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support;

import java.io.Serializable;

import org.datagear.analysis.AbstractIdentifiable;
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
 * <p>
 * 由此部件渲染的图表可以通过{@linkplain #getChartWidgetId(Chart)}获取此部件ID。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ChartWidget extends ChartDefinition
{
	/** 图表部件渲染时的部件信息属性名 */
	public static final String ATTR_CHART_WIDGET = BUILTIN_ATTR_PREFIX + "CHART_WIDGET";

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

	public ChartWidget(ChartDefinition chartDefinition, ChartPlugin plugin)
	{
		super(chartDefinition);
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
		ChartDefinition chartDefinition = new ChartDefinition(this);
		chartDefinition.setId(generateChartId(renderContext));
		
		// 添加图表对应的部件信息
		chartDefinition.setAttrValue(ATTR_CHART_WIDGET, new ChartWidgetId(this.getId()));

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

	/**
	 * 获取由{@linkplain ChartWidget#render(RenderContext)}渲染的{@linkplain Chart}所关联的{@linkplain ChartWidget#getId()}。
	 * 
	 * @param chart
	 * @return 可能返回{@code null}
	 */
	public static String getChartWidgetId(Chart chart)
	{
		if (chart == null)
			return null;
		
		ChartWidgetId cwi = (ChartWidgetId)chart.getAttrValue(ATTR_CHART_WIDGET);
		return (cwi == null ? null : cwi.getId());
	}
	
	/**
	 * {@linkplain ChartWidget#getId()}描述类。
	 * 
	 * @author datagear@163.com
	 */
	public static class ChartWidgetId extends AbstractIdentifiable implements Serializable
	{
		private static final long serialVersionUID = 1L;

		public ChartWidgetId()
		{
			super();
		}

		public ChartWidgetId(String id)
		{
			super(id);
		}
	}
}
