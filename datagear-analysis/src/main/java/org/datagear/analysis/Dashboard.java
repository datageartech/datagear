/*
 * Copyright 2018-2024 datagear.tech
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
	public static final String PROPERTY_CHARTS = "charts";

	private String id;

	private RenderContext renderContext;

	private List<Chart> charts = Collections.emptyList();

	public Dashboard()
	{
		super();
	}

	public Dashboard(String id, RenderContext renderContext)
	{
		super();
		this.id = id;
		this.renderContext = renderContext;
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
