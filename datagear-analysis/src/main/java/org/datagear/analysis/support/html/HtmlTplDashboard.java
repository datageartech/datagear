/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support.html;

import org.datagear.analysis.DashboardWidget;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.TemplateDashboard;

/**
 * HTML模板看板。
 * 
 * @author datagear@163.com
 *
 */
public class HtmlTplDashboard extends TemplateDashboard
{
	private String varName;

	public HtmlTplDashboard()
	{
		super();
	}

	public HtmlTplDashboard(String id, String template, RenderContext renderContext,
			HtmlTplDashboardWidget dashboardWidget, String varName)
	{
		super(id, template, renderContext, dashboardWidget);
		this.varName = varName;
	}

	@Override
	public HtmlTplDashboardWidget getWidget()
	{
		return (HtmlTplDashboardWidget) super.getWidget();
	}

	@Override
	public void setWidget(DashboardWidget widget)
	{
		if (widget != null && !(widget instanceof HtmlTplDashboardWidget))
			throw new IllegalArgumentException();

		super.setWidget(widget);
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
