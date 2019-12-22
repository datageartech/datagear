/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.analysis.support.html;

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
public class HtmlTplDashboardWidget<T extends HtmlRenderContext> extends TemplateDashboardWidget<T>
{
	private HtmlTplDashboardWidgetRenderer<T> renderer;

	public HtmlTplDashboardWidget()
	{
		super();
	}

	public HtmlTplDashboardWidget(String id, String template, HtmlTplDashboardWidgetRenderer<T> renderer)
	{
		super(id, template);
		this.renderer = renderer;
	}

	public HtmlTplDashboardWidgetRenderer<T> getRenderer()
	{
		return renderer;
	}

	public void setRenderer(HtmlTplDashboardWidgetRenderer<T> renderer)
	{
		this.renderer = renderer;
	}

	@Override
	public HtmlDashboard render(T renderContext) throws RenderException
	{
		return this.renderer.render(renderContext, this);
	}
}
