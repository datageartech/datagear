/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support.html;

import java.util.List;

import org.datagear.analysis.DashboardWidget;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.support.AbstractDashboard;

/**
 * HTML看板。
 * 
 * @author datagear@163.com
 *
 */
public class HtmlDashboard extends AbstractDashboard
{
	private String varName;

	public HtmlDashboard()
	{
		super();
	}

	public HtmlDashboard(String id, DashboardWidget<?> dashboardWidget, HtmlRenderContext renderContext,
			List<? extends HtmlChart> charts, String varName)
	{
		super(id, dashboardWidget, renderContext, charts);
		this.varName = varName;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<? extends HtmlChart> getCharts()
	{
		return (List<? extends HtmlChart>) super.getCharts();
	}

	@Override
	public HtmlRenderContext getRenderContext()
	{
		return (HtmlRenderContext) super.getRenderContext();
	}

	@Override
	public void setRenderContext(RenderContext renderContext)
	{
		if (!(renderContext instanceof HtmlRenderContext))
			throw new IllegalArgumentException();

		super.setRenderContext(renderContext);
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
