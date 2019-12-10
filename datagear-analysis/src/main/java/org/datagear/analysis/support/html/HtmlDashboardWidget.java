/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support.html;

import java.util.List;

import org.datagear.analysis.ChartWidget;
import org.datagear.analysis.Dashboard;
import org.datagear.analysis.DashboardWidget;
import org.datagear.analysis.RenderException;

/**
 * HTML {@linkplain DashboardWidget}。
 * <p>
 * 此类将看板代码（HTML、JavaScript）输出至{@linkplain HtmlRenderContext#getWriter()}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class HtmlDashboardWidget<T extends HtmlRenderContext> extends DashboardWidget<T>
{
	/** 看板HTML模板 */
	private String template;

	/** 换行符 */
	private String newLine = "\r\n";

	public HtmlDashboardWidget()
	{
		super();
	}

	public HtmlDashboardWidget(String id, List<? extends ChartWidget<T>> chartWidgets, String template)
	{
		super(id, chartWidgets);
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

	public String getNewLine()
	{
		return newLine;
	}

	public void setNewLine(String newLine)
	{
		this.newLine = newLine;
	}

	@Override
	public Dashboard render(T renderContext) throws RenderException
	{
		return null;
	}
}
