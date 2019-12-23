/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support.html;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;

import org.datagear.analysis.AbstractIdentifiable;
import org.datagear.analysis.Dashboard;
import org.datagear.analysis.DashboardWidget;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.RenderException;

/**
 * {@linkplain HtmlDashboard}脚本对象输出流。
 * 
 * @author datagear@163.com
 *
 */
public class HtmlDashboardScriptObjectWriter extends AbstractHtmlScriptObjectWriter
{
	public HtmlDashboardScriptObjectWriter()
	{
		super();
	}

	/**
	 * 将{@linkplain HtmlDashboard}以脚本对象格式（“<code>{...}</code>”）写入输出流。
	 * 
	 * @param out
	 * @param dashboard
	 * @throws IOException
	 */
	public void write(Writer out, HtmlDashboard dashboard) throws IOException
	{
		write(out, dashboard, false);
	}

	/**
	 * 将{@linkplain HtmlDashboard}以脚本对象格式（“<code>{...}</code>”）写入输出流。
	 * 
	 * @param out
	 * @param dashboard
	 * @param renderContextEmpty
	 *            {@linkplain HtmlDashboard#getRenderContext()}是否只输出空对象。
	 * @throws IOException
	 */
	public void write(Writer out, HtmlDashboard dashboard, boolean renderContextEmpty) throws IOException
	{
		JsonHtmlDashboard jsonHtmlDashboard = new JsonHtmlDashboard(dashboard, renderContextEmpty);

		writeScriptObject(out, jsonHtmlDashboard);
	}

	/**
	 * 仅用于JSON输出的{@linkplain HtmlDashboard}。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static class JsonHtmlDashboard extends HtmlDashboard
	{
		public JsonHtmlDashboard()
		{
			super();
		}

		@SuppressWarnings("unchecked")
		public JsonHtmlDashboard(HtmlDashboard dashboard, boolean renderContextEmpty)
		{
			super(dashboard.getId(), new IdDashboardWidget(dashboard.getWidget()),
					(renderContextEmpty ? new OnlyContextPathHtmlRenderContext(dashboard.getRenderContext())
							: new AttributesHtmlRenderContext(dashboard.getRenderContext())),
					Collections.EMPTY_LIST, dashboard.getVarName());
		}
	}

	protected static class IdDashboardWidget extends AbstractIdentifiable implements DashboardWidget<RenderContext>
	{
		public IdDashboardWidget()
		{
			super();
		}

		public IdDashboardWidget(DashboardWidget<?> dashboardWidget)
		{
			super(dashboardWidget.getId());
		}

		@Override
		public Dashboard render(RenderContext renderContext) throws RenderException
		{
			throw new UnsupportedOperationException();
		}
	}
}
