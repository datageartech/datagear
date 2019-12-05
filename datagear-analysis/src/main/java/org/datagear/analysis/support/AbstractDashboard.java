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
import org.datagear.analysis.RenderContext;

/**
 * 抽象{@linkplain Dashboard}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractDashboard extends AbstractIdentifiable implements Dashboard
{
	private RenderContext renderContext;

	private List<Chart> charts;

	public AbstractDashboard()
	{
		super();
	}

	public AbstractDashboard(String id, RenderContext renderContext, List<Chart> charts)
	{
		super(id);
		this.renderContext = renderContext;
		this.charts = charts;
	}

	@Override
	public RenderContext getRenderContext()
	{
		return renderContext;
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

	public void setCharts(List<Chart> charts)
	{
		this.charts = charts;
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
