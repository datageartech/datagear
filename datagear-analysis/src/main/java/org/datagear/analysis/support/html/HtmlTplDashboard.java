/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support.html;

import org.datagear.analysis.RenderContext;
import org.datagear.analysis.TemplateDashboard;
import org.datagear.analysis.TemplateDashboardWidget;

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

	public HtmlTplDashboard(String id, String template, HtmlRenderContext renderContext,
			TemplateDashboardWidget<?> dashboardWidget, String varName)
	{
		super(id, template, renderContext, dashboardWidget);
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

	public String getVarName()
	{
		return varName;
	}

	public void setVarName(String varName)
	{
		this.varName = varName;
	}
}
