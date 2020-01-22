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
 * {@linkplain HtmlDashboard} JS对象输出流。
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
	 * 将{@linkplain HtmlDashboard}的JS脚本对象写入输出流。
	 * <p>
	 * 格式为：
	 * </p>
	 * <code>
	 * <pre>
	 * var [varName]=
	 * {
	 * 	...,
	 * 	renderContext : [renderContextVarName],
	 * 	widget : { "id" : "...." },
	 * 	charts : [],
	 * 	...
	 * };
	 * <pre>
	 * </code>
	 * 
	 * @param out
	 * @param dashboard
	 * @param renderContextVarName
	 * @throws IOException
	 */
	public void write(Writer out, HtmlDashboard dashboard, String renderContextVarName) throws IOException
	{
		JsonHtmlDashboard jsonHtmlDashboard = new JsonHtmlDashboard(dashboard, renderContextVarName);

		out.write("var " + dashboard.getVarName() + "=");
		writeNewLine(out);
		writeJsonObject(out, jsonHtmlDashboard);
		out.write(";");
		writeNewLine(out);
	}

	/**
	 * 可输出JSON的{@linkplain HtmlDashboard}。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static class JsonHtmlDashboard extends HtmlDashboard
	{
		@SuppressWarnings("unchecked")
		public JsonHtmlDashboard(HtmlDashboard dashboard, String renderContextVarName)
		{
			super(dashboard.getId(), new RefHtmlRenderContext(renderContextVarName),
					new IdDashboardWidget(dashboard.getWidget()), dashboard.getVarName());

			setCharts(Collections.EMPTY_LIST);
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
