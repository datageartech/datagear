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
	public static final String PROPERTY_TEMPLATE = "template";
	public static final String PROPERTY_WIDGET = "widget";
	
	private String template;
	
	private TplDashboardWidget widget;

	public TplDashboard()
	{
		super();
	}

	public TplDashboard(String id, RenderContext renderContext, String template, TplDashboardWidget widget)
	{
		super(id, renderContext);
		this.template = template;
		this.widget = widget;
	}

	public String getTemplate()
	{
		return template;
	}

	public void setTemplate(String template)
	{
		this.template = template;
	}

	public TplDashboardWidget getWidget()
	{
		return this.widget;
	}

	public void setWidget(TplDashboardWidget widget)
	{
		this.widget = widget;
	}
}
