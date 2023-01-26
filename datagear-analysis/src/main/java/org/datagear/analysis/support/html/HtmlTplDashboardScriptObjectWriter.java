/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support.html;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;

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
		HtmlTplDashboardJson htmlTplDashboardJson = new HtmlTplDashboardJson(dashboard, renderContextVarName);

		out.write("var " + dashboard.getVarName() + "=");
		writeNewLine(out);
		writeJsonObject(out, htmlTplDashboardJson);
		out.write(";");
		writeNewLine(out);
	}

	/**
	 * 用于输出JSON的{@linkplain HtmlTplDashboard}。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static class HtmlTplDashboardJson extends HtmlTplDashboard
	{
		@SuppressWarnings("unchecked")
		public HtmlTplDashboardJson(HtmlTplDashboard dashboard, String renderContextVarName)
		{
			super(dashboard.getId(), dashboard.getTemplate(), new RefRenderContext(renderContextVarName),
					new TplDashboardWidgetJson(dashboard.getWidget()), dashboard.getVarName());

			setCharts(Collections.EMPTY_LIST);
			
			LoadableChartWidgets lcws = dashboard.getLoadableChartWidgets();
			setLoadableChartWidgets(lcws == null ? null : new LoadableChartWidgetsJson(lcws));
		}
	}
	
	protected static class LoadableChartWidgetsJson extends LoadableChartWidgets
	{
		private static final long serialVersionUID = 1L;

		public LoadableChartWidgetsJson(LoadableChartWidgets pattern)
		{
			super(pattern.getPattern());
			setChartWidgetIds(pattern.getChartWidgetIds());
		}

		@JsonIgnore
		@Override
		public boolean isPatternAll()
		{
			return super.isPatternAll();
		}

		@JsonIgnore
		@Override
		public boolean isPatternNone()
		{
			return super.isPatternNone();
		}

		@JsonIgnore
		@Override
		public boolean isPatternPermitted()
		{
			return super.isPatternPermitted();
		}

		@JsonIgnore
		@Override
		public boolean isPatternList()
		{
			return super.isPatternList();
		}
	}

	/**
	 * 用于输出JSON的{@linkplain HtmlTplDashboardWidget}。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static class TplDashboardWidgetJson extends HtmlTplDashboardWidget
	{
		public TplDashboardWidgetJson(HtmlTplDashboardWidget dashboardWidget)
		{
			super(dashboardWidget.getId(), dashboardWidget.getTemplates(), null, null);
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
