/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 看板。
 * 
 * @author datagear@163.com
 *
 */
public class Dashboard extends DashboardQueryHandler implements Identifiable, Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String PROPERTY_ID = "id";
	public static final String PROPERTY_RENDER_CONTEXT = "renderContext";
	public static final String PROPERTY_WIDGET = "widget";
	public static final String PROPERTY_CHARTS = "charts";

	private String id;

	private transient RenderContext renderContext;

	private DashboardWidget widget;

	private List<Chart> charts = Collections.emptyList();

	public Dashboard()
	{
		super();
	}

	public Dashboard(String id, RenderContext renderContext, DashboardWidget widget)
	{
		super();
		this.id = id;
		this.renderContext = renderContext;
		this.widget = widget;
	}

	@Override
	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public RenderContext getRenderContext()
	{
		return renderContext;
	}

	public void setRenderContext(RenderContext renderContext)
	{
		this.renderContext = renderContext;
	}

	public DashboardWidget getWidget()
	{
		return widget;
	}

	public void setWidget(DashboardWidget widget)
	{
		this.widget = widget;
	}

	/**
	 * 是否包含图表。
	 * 
	 * @return
	 */
	public boolean hasChart()
	{
		return (this.charts != null && !this.charts.isEmpty());
	}

	public List<Chart> getCharts()
	{
		return charts;
	}

	public void setCharts(List<Chart> charts)
	{
		this.charts = charts;
	}

	/**
	 * 获取指定ID的{@linkplain Chart}。
	 * 
	 * @param chartId
	 * @return 返回{@code null}表示没有找到
	 */
	public Chart getChart(String chartId)
	{
		if (this.charts == null)
			return null;

		for (Chart chart : this.charts)
		{
			if (chart.getId().equals(chartId))
				return chart;
		}

		return null;
	}

	@Override
	protected ChartDefinition getChartDefinition(String chartId)
	{
		return getChart(chartId);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Dashboard other = (Dashboard) obj;
		if (id == null)
		{
			if (other.id != null)
				return false;
		}
		else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [id=" + id + "]";
	}
}
