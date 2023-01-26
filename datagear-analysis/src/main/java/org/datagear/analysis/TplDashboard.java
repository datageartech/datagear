/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis;

/**
 * 模板{@linkplain Dashboard}。
 * 
 * @author datagear@163.com
 *
 */
public class TplDashboard extends Dashboard
{
	/** 模板 */
	private String template;

	public TplDashboard()
	{
		super();
	}

	public TplDashboard(String id, String template, RenderContext renderContext, TplDashboardWidget widget)
	{
		super(id, renderContext, widget);
		this.template = template;
	}

	public String getTemplate()
	{
		return template;
	}

	public void setTemplate(String template)
	{
		this.template = template;
	}

	@Override
	public TplDashboardWidget getWidget()
	{
		return (TplDashboardWidget) super.getWidget();
	}

	@Override
	public void setWidget(DashboardWidget widget)
	{
		if (widget != null && !(widget instanceof TplDashboardWidget))
			throw new IllegalArgumentException();

		super.setWidget(widget);
	}
}
