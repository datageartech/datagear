/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.analysis.support.html;

import org.datagear.analysis.Dashboard;
import org.datagear.analysis.RenderException;
import org.datagear.analysis.TemplateDashboardWidget;
import org.datagear.analysis.support.ChartWidget;
import org.datagear.analysis.support.ChartWidgetSource;

/**
 * HTML {@linkplain TemplateDashboardWidget}。
 * <p>
 * 此类将看板代码（HTML、JavaScript）输出至{@linkplain HtmlRenderContext#getWriter()}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class HtmlTemplateDashboardWidget<T extends HtmlRenderContext> extends TemplateDashboardWidget<T>
{
	private ChartWidgetSource chartWidgetSource;

	public HtmlTemplateDashboardWidget()
	{
		super();
	}

	public HtmlTemplateDashboardWidget(String id, String name, String template, ChartWidgetSource chartWidgetSource)
	{
		super(id, name, template);
		this.chartWidgetSource = chartWidgetSource;
	}

	public ChartWidgetSource getChartWidgetSource()
	{
		return chartWidgetSource;
	}

	public void setChartWidgetSource(ChartWidgetSource chartWidgetSource)
	{
		this.chartWidgetSource = chartWidgetSource;
	}

	@Override
	public Dashboard render(T renderContext) throws RenderException
	{
		return null;
	}

	/**
	 * 获取指定ID的{@linkplain ChartWidget}。
	 * 
	 * @param id
	 * @return
	 */
	protected ChartWidget<T> getChartWidget(String id)
	{
		return this.chartWidgetSource.getChartWidget(id);
	}
}
