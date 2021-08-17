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
public class TemplateDashboard extends Dashboard
{
	private static final long serialVersionUID = 1L;

	/** 模板 */
	private String template;

	public TemplateDashboard()
	{
		super();
	}

	public TemplateDashboard(String id, String template, RenderContext renderContext, TemplateDashboardWidget widget)
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
	public TemplateDashboardWidget getWidget()
	{
		return (TemplateDashboardWidget) super.getWidget();
	}

	@Override
	public void setWidget(DashboardWidget widget)
	{
		if (widget != null && !(widget instanceof TemplateDashboardWidget))
			throw new IllegalArgumentException();

		super.setWidget(widget);
	}
}
