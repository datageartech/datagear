/*
 * Copyright 2018-2023 datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
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
	private static final long serialVersionUID = 1L;

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
