/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.analysis.support.html;

import org.datagear.analysis.Dashboard;
import org.datagear.analysis.RenderException;
import org.datagear.analysis.TemplateDashboardWidget;

/**
 * HTML {@linkplain TemplateDashboardWidget}。
 * <p>
 * 此类将看板代码（HTML、JavaScript）输出至{@linkplain HtmlRenderContext#getWriter()}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class HtmlFreemarkerDashboardWidget<T extends HtmlRenderContext> extends TemplateDashboardWidget<T>
{
	private HtmlFreemarkerDashboardWidgetRenderer<T> renderer;

	public HtmlFreemarkerDashboardWidget()
	{
		super();
	}

	public HtmlFreemarkerDashboardWidget(String id, String template, HtmlFreemarkerDashboardWidgetRenderer<T> renderer)
	{
		super(id, template);
		this.renderer = renderer;
	}

	public HtmlFreemarkerDashboardWidgetRenderer<T> getRenderer()
	{
		return renderer;
	}

	public void setRenderer(HtmlFreemarkerDashboardWidgetRenderer<T> renderer)
	{
		this.renderer = renderer;
	}

	@Override
	public Dashboard render(T renderContext) throws RenderException
	{
		return this.renderer.render(renderContext, this);
	}
}
