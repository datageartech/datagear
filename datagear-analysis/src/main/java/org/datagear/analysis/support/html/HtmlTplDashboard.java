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

package org.datagear.analysis.support.html;

import org.datagear.analysis.RenderContext;
import org.datagear.analysis.TplDashboard;
import org.datagear.analysis.TplDashboardWidget;

/**
 * HTML模板看板。
 * 
 * @author datagear@163.com
 *
 */
public class HtmlTplDashboard extends TplDashboard
{
	private static final long serialVersionUID = 1L;

	/** 看板JS对象变量名 */
	private String varName;
	
	private LoadableChartWidgets loadableChartWidgets = null;

	public HtmlTplDashboard()
	{
		super();
	}

	public HtmlTplDashboard(String id, RenderContext renderContext, String template,
			HtmlTplDashboardWidget dashboardWidget, String varName)
	{
		super(id, renderContext, template, dashboardWidget);
		this.varName = varName;
	}

	@Override
	public HtmlTplDashboardWidget getWidget()
	{
		return (HtmlTplDashboardWidget) super.getWidget();
	}

	@Override
	public void setWidget(TplDashboardWidget widget)
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

	public LoadableChartWidgets getLoadableChartWidgets()
	{
		return loadableChartWidgets;
	}

	public void setLoadableChartWidgets(LoadableChartWidgets loadableChartWidgets)
	{
		this.loadableChartWidgets = loadableChartWidgets;
	}
}
