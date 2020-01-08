/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support.html;

import java.util.List;

import org.datagear.analysis.Chart;
import org.datagear.analysis.Dashboard;
import org.datagear.analysis.DashboardWidget;
import org.datagear.analysis.RenderContext;

/**
 * HTML看板。
 * 
 * @author datagear@163.com
 *
 */
public class HtmlDashboard extends Dashboard
{
	private String varName;

	public HtmlDashboard()
	{
		super();
	}

	public HtmlDashboard(String id, HtmlRenderContext renderContext, DashboardWidget<?> dashboardWidget, String varName)
	{
		super(id, renderContext, dashboardWidget);
		this.varName = varName;
	}

	@Override
	public HtmlRenderContext getRenderContext()
	{
		return (HtmlRenderContext) super.getRenderContext();
	}

	@Override
	public void setRenderContext(RenderContext renderContext)
	{
		if (renderContext != null && !(renderContext instanceof HtmlRenderContext))
			throw new IllegalArgumentException();

		super.setRenderContext(renderContext);
	}

	@Override
	public List<Chart> getCharts()
	{
		// TODO Auto-generated method stub
		return super.getCharts();
	}

	@Override
	public void setCharts(List<Chart> charts)
	{
		// TODO Auto-generated method stub
		super.setCharts(charts);
	}

	public String getVarName()
	{
		return varName;
	}

	public void setVarName(String varName)
	{
		this.varName = varName;
	}
}
