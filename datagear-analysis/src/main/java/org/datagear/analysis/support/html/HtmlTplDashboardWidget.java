/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support.html;

import java.io.Writer;

import org.datagear.analysis.RenderContext;
import org.datagear.analysis.RenderException;
import org.datagear.analysis.TemplateDashboardWidget;

/**
 * HTML {@linkplain TemplateDashboardWidget}。
 * <p>
 * 此类将看板代码（HTML、JavaScript）输出至{@linkplain HtmlTplDashboardRenderAttr#getHtmlWriter(RenderContext)}。
 * </p>
 * <p>
 * 注意：此类{@linkplain #render(RenderContext)}、{@linkplain #render(RenderContext, String)}的{@linkplain RenderContext}
 * 必须符合{@linkplain HtmlTplDashboardRenderAttr#inflate(RenderContext, Writer)}规范。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class HtmlTplDashboardWidget extends TemplateDashboardWidget
{
	private static final long serialVersionUID = 1L;

	private transient HtmlTplDashboardWidgetRenderer renderer;

	public HtmlTplDashboardWidget()
	{
		super();
	}

	public HtmlTplDashboardWidget(String id, String template, HtmlTplDashboardWidgetRenderer renderer)
	{
		super(id, template);
		this.renderer = renderer;
	}

	public HtmlTplDashboardWidget(String id, String[] templates, HtmlTplDashboardWidgetRenderer renderer)
	{
		super(id, templates);
		this.renderer = renderer;
	}

	public HtmlTplDashboardWidgetRenderer getRenderer()
	{
		return renderer;
	}

	public void setRenderer(HtmlTplDashboardWidgetRenderer renderer)
	{
		this.renderer = renderer;
	}

	@Override
	public HtmlTplDashboard render(RenderContext renderContext) throws RenderException
	{
		return (HtmlTplDashboard) super.render(renderContext);
	}

	@Override
	public HtmlTplDashboard render(RenderContext renderContext, String template)
			throws RenderException, IllegalArgumentException
	{
		return (HtmlTplDashboard) super.render(renderContext, template);
	}

	@Override
	protected HtmlTplDashboard renderTemplate(RenderContext renderContext, String template) throws RenderException
	{
		return this.renderer.render(renderContext, this, template);
	}
}
