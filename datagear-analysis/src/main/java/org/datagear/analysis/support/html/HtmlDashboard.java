/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support.html;

import java.util.List;

import org.datagear.analysis.Chart;
import org.datagear.analysis.DashboardWidget;
import org.datagear.analysis.support.AbstractDashboard;

/**
 * HTML看板。
 * 
 * @author datagear@163.com
 *
 */
public class HtmlDashboard extends AbstractDashboard
{
	private String dashboardVarName;

	public HtmlDashboard()
	{
		super();
	}

	public HtmlDashboard(String id, DashboardWidget<?> dashboardWidget, HtmlRenderContext renderContext,
			List<Chart> charts, String dashboardVarName)
	{
		super(id, dashboardWidget, renderContext, charts);
		this.dashboardVarName = dashboardVarName;
	}

	@Override
	public HtmlRenderContext getRenderContext()
	{
		return (HtmlRenderContext) super.getRenderContext();
	}

	public String getDashboardVarName()
	{
		return dashboardVarName;
	}

	public void setDashboardVarName(String dashboardVarName)
	{
		this.dashboardVarName = dashboardVarName;
	}
}
