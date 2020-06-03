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
import java.util.Map;

import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetResult;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.RenderException;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * {@linkplain HtmlTplDashboard} JS对象输出流。
 * 
 * @author datagear@163.com
 *
 */
public class HtmlTplDashboardScriptObjectWriter extends AbstractHtmlScriptObjectWriter
{
	public HtmlTplDashboardScriptObjectWriter()
	{
		super();
	}

	/**
	 * 将{@linkplain HtmlTplDashboard}的JS脚本对象写入输出流。
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
	public void write(Writer out, HtmlTplDashboard dashboard, String renderContextVarName) throws IOException
	{
		JsonHtmlTplDashboard jsonHtmlTplDashboard = new JsonHtmlTplDashboard(dashboard, renderContextVarName);

		out.write("var " + dashboard.getVarName() + "=");
		writeNewLine(out);
		writeJsonObject(out, jsonHtmlTplDashboard);
		out.write(";");
		writeNewLine(out);
	}

	/**
	 * 可输出JSON的{@linkplain HtmlTplDashboard}。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static class JsonHtmlTplDashboard extends HtmlTplDashboard
	{
		@SuppressWarnings("unchecked")
		public JsonHtmlTplDashboard(HtmlTplDashboard dashboard, String renderContextVarName)
		{
			super(dashboard.getId(), dashboard.getTemplate(), new RefRenderContext(renderContextVarName),
					new JsonTemplateDashboardWidget(dashboard.getWidget()), dashboard.getVarName());

			setCharts(Collections.EMPTY_LIST);
		}

		@JsonIgnore
		@Override
		public Map<String, DataSetResult[]> getDataSetResults() throws DataSetException
		{
			throw new UnsupportedOperationException();
		}

	}

	protected static class JsonTemplateDashboardWidget extends HtmlTplDashboardWidget
	{
		public JsonTemplateDashboardWidget(HtmlTplDashboardWidget dashboardWidget)
		{
			super(dashboardWidget.getId(), dashboardWidget.getTemplates(), null);
		}

		@JsonIgnore
		@Override
		public String getFirstTemplate()
		{
			throw new UnsupportedOperationException();
		}

		@JsonIgnore
		@Override
		public int getTemplateCount()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		protected HtmlTplDashboard renderTemplate(RenderContext renderContext, String template) throws RenderException
		{
			throw new UnsupportedOperationException();
		}
	}
}
