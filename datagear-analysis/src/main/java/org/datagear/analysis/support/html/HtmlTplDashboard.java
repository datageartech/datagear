/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
