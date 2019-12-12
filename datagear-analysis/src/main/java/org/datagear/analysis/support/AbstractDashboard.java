/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import java.util.List;

import org.datagear.analysis.AbstractIdentifiable;
import org.datagear.analysis.Chart;
import org.datagear.analysis.Dashboard;
import org.datagear.analysis.DashboardWidget;
import org.datagear.analysis.RenderContext;

/**
 * 抽象{@linkplain Dashboard}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractDashboard extends AbstractIdentifiable implements Dashboard
{
	private DashboardWidget<?> dashboardWidget;

	private RenderContext renderContext;

	private List<Chart> charts;

	public AbstractDashboard()
	{
		super();
	}

	@SuppressWarnings("unchecked")
	public AbstractDashboard(String id, DashboardWidget<?> dashboardWidget, RenderContext renderContext,
			List<? extends Chart> charts)
	{
		super(id);
		this.dashboardWidget = dashboardWidget;
		this.renderContext = renderContext;
		this.charts = (List<Chart>) charts;
	}

	@Override
	public DashboardWidget<?> getDashboardWidget()
	{
		return dashboardWidget;
	}

	public void setDashboardWidget(DashboardWidget<?> dashboardWidget)
	{
		this.dashboardWidget = dashboardWidget;
	}

	@Override
	public RenderContext getRenderContext()
	{
		return this.renderContext;
	}

	public void setRenderContext(RenderContext renderContext)
	{
		this.renderContext = renderContext;
	}

	@Override
	public List<Chart> getCharts()
	{
		return charts;
	}

	@SuppressWarnings("unchecked")
	public void setCharts(List<? extends Chart> charts)
	{
		this.charts = (List<Chart>) charts;
	}

	@Override
	public Chart getChart(String id)
	{
		if (this.charts == null)
			return null;

		for (Chart chart : this.charts)
		{
			if (chart.getId().equals(id))
				return chart;
		}

		return null;
	}
}
