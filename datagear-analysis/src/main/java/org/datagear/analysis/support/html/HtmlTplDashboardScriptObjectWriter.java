/*
 * Copyright 2018-2023 datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package org.datagear.analysis.support.html;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;

import org.datagear.analysis.RenderException;
import org.datagear.analysis.TplDashboardRenderContext;

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
		private static final long serialVersionUID = 1L;

		@SuppressWarnings("unchecked")
		public HtmlTplDashboardJson(HtmlTplDashboard dashboard, String renderContextVarName)
		{
			super(dashboard.getId(), new RefRenderContext(renderContextVarName), dashboard.getTemplate(),
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
		private static final long serialVersionUID = 1L;

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
		public HtmlTplDashboard render(TplDashboardRenderContext renderContext) throws RenderException
		{
			throw new UnsupportedOperationException();
		}
	}
}
