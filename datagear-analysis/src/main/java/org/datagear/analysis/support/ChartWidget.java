/*
 * Copyright 2018-2023 datagear.tech
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

package org.datagear.analysis.support;

import org.datagear.analysis.AbstractIdentifiable;
import org.datagear.analysis.Chart;
import org.datagear.analysis.ChartDataSet;
import org.datagear.analysis.ChartDefinition;
import org.datagear.analysis.ChartPlugin;
import org.datagear.analysis.ChartPluginManager;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.RenderException;
import org.datagear.util.IDUtil;
import org.datagear.util.StringUtil;

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
	private static final long serialVersionUID = 1L;

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
		return render(renderContext, null);
	}

	/**
	 * 渲染{@linkplain Chart}。
	 * 
	 * @param renderContext
	 * @param chartId 允许为{@code null}
	 * @return
	 * @throws RenderException
	 */
	public Chart render(RenderContext renderContext, String chartId) throws RenderException
	{
		if(StringUtil.isEmpty(chartId))
			chartId = generateChartId(renderContext);
		
		return this.plugin.renderChart(buildChartDefinition(chartId), renderContext);
	}

	protected ChartDefinition buildChartDefinition(String id) throws RenderException
	{
		ChartDefinition chartDefinition = new ChartDefinition(this);
		chartDefinition.setId(id);
		
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
	public static class ChartWidgetId extends AbstractIdentifiable
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
