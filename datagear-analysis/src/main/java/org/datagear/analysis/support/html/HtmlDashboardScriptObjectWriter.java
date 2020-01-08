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
	 * @param renderContextNoAttrs
	 *            {@linkplain HtmlDashboard#getRenderContext()}是否不输出{@linkplain HtmlRenderContext#getAttributes()}。
	 * @throws IOException
	 */
	public void write(Writer out, HtmlDashboard dashboard, boolean renderContextNoAttrs) throws IOException
	{
		JsonHtmlDashboard jsonHtmlDashboard = new JsonHtmlDashboard(dashboard, renderContextNoAttrs);

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
		public JsonHtmlDashboard(HtmlDashboard dashboard, boolean renderContextNoAttrs)
		{
			super(dashboard.getId(),
					(renderContextNoAttrs ? new NoAttributesHtmlRenderContext(dashboard.getRenderContext())
							: new AttributesHtmlRenderContext(dashboard.getRenderContext())),
					new IdDashboardWidget(dashboard.getWidget()), dashboard.getVarName());

			super.setCharts(Collections.EMPTY_LIST);
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
