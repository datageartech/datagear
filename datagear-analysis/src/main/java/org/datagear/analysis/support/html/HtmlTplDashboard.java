/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
	private static final long serialVersionUID = 1L;

	/** 看板JS对象变量名 */
	private String varName;
	
	private LoadableChartWidgetsPattern loadableChartWidgetsPattern = null;

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
	
	public LoadableChartWidgetsPattern getLoadableChartWidgetsPattern()
	{
		return loadableChartWidgetsPattern;
	}

	public void setLoadableChartWidgetsPattern(LoadableChartWidgetsPattern loadableChartWidgetsPattern)
	{
		this.loadableChartWidgetsPattern = loadableChartWidgetsPattern;
	}
}
