/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support.html;

import java.io.IOException;

import org.datagear.analysis.RenderException;
import org.datagear.analysis.TplDashboardWidget;
import org.datagear.analysis.TplDashboardWidgetResManager;
import org.datagear.analysis.TplDashboardRenderContext;
import org.datagear.util.IOUtil;

/**
 * HTML {@linkplain TplDashboardWidget}。
 * <p>
 * 注意：此类的{@linkplain #render(TplDashboardRenderContext)}参数必须是{@linkplain HtmlTplDashboardRenderContext}实例。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class HtmlTplDashboardWidget extends TplDashboardWidget
{
	private HtmlTplDashboardWidgetRenderer renderer;

	private TplDashboardWidgetResManager resManager;

	public HtmlTplDashboardWidget()
	{
		super();
	}

	public HtmlTplDashboardWidget(String id, String template, HtmlTplDashboardWidgetRenderer renderer,
			TplDashboardWidgetResManager resManager)
	{
		super(id, template);
		this.renderer = renderer;
		this.resManager = resManager;
	}

	public HtmlTplDashboardWidget(String id, String[] templates, HtmlTplDashboardWidgetRenderer renderer,
			TplDashboardWidgetResManager resManager)
	{
		super(id, templates);
		this.renderer = renderer;
		this.resManager = resManager;
	}

	public HtmlTplDashboardWidgetRenderer getRenderer()
	{
		return renderer;
	}

	public void setRenderer(HtmlTplDashboardWidgetRenderer renderer)
	{
		this.renderer = renderer;
	}

	public TplDashboardWidgetResManager getResManager()
	{
		return resManager;
	}

	public void setResManager(TplDashboardWidgetResManager resManager)
	{
		this.resManager = resManager;
	}
	
	@Override
	public HtmlTplDashboard render(TplDashboardRenderContext renderContext) throws RenderException
	{
		if(!(renderContext instanceof HtmlTplDashboardRenderContext))
			throw new IllegalArgumentException("[renderContext] must be instance of " + HtmlTplDashboardRenderContext.class.getSimpleName());
		
		HtmlTplDashboardRenderContext rawRenderContext = (HtmlTplDashboardRenderContext)renderContext;
		HtmlTplDashboardRenderContext fullRenderContext = null;
		
		try
		{
			fullRenderContext = toFullRenderContext(rawRenderContext);
			return this.renderer.render(this, fullRenderContext);
		}
		catch(IOException e)
		{
			throw new RenderException(e);
		}
		finally
		{
			if(fullRenderContext != null && fullRenderContext.getTemplateReader() != renderContext.getTemplateReader())
				IOUtil.close(fullRenderContext.getTemplateReader());
		}
	}
	
	protected HtmlTplDashboardRenderContext toFullRenderContext(HtmlTplDashboardRenderContext renderContext) throws IOException
	{
		HtmlTplDashboardRenderContext full = new HtmlTplDashboardRenderContext(renderContext);
		
		if(!full.hasTemplateReader())
		{
			full.setTemplateReader(IOUtil.getBufferedReader(this.resManager.getReader(this, full.getTemplate())));
			full.setTemplateLastModified(this.resManager.lastModified(this.getId(), full.getTemplate()));
		}
		
		return full;
	}
}
