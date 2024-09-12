/*
 * Copyright 2018-present datagear.tech
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

import java.io.Reader;
import java.io.Writer;
import java.util.Locale;

import org.datagear.analysis.DashboardTheme;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.TplDashboardRenderContext;
import org.datagear.util.Global;
import org.datagear.util.StringUtil;

/**
 * HTML{@linkplain TplDashboardRenderContext}。
 * 
 * @author datagear@163.com
 */
public class HtmlTplDashboardRenderContext extends TplDashboardRenderContext
{
	private static final long serialVersionUID = 1L;

	private HtmlTplDashboardImportBuilder importBuilder = null;
	
	private HtmlTitleHandler htmlTitleHandler = null;
	
	private DashboardTheme dashboardTheme = null;
	
	private Locale locale = null;
	
	public HtmlTplDashboardRenderContext()
	{
		super();
	}

	public HtmlTplDashboardRenderContext(String template, Writer writer)
	{
		super(template, writer);
	}

	public HtmlTplDashboardRenderContext(String template, long templateLastModified, Writer writer)
	{
		super(template, templateLastModified, writer);
	}

	public HtmlTplDashboardRenderContext(String template, Reader templateReader, Writer writer)
	{
		super(template, templateReader, writer);
	}

	public HtmlTplDashboardRenderContext(String template, Reader templateReader, long templateLastModified,
			Writer writer)
	{
		super(template, templateReader, templateLastModified, writer);
	}

	public HtmlTplDashboardRenderContext(HtmlTplDashboardRenderContext renderContext)
	{
		super(renderContext);
		this.importBuilder = renderContext.getImportBuilder();
		this.htmlTitleHandler = renderContext.getHtmlTitleHandler();
		this.dashboardTheme = renderContext.getDashboardTheme();
		this.locale = renderContext.getLocale();
	}

	public HtmlTplDashboardImportBuilder getImportBuilder()
	{
		return importBuilder;
	}

	public void setImportBuilder(HtmlTplDashboardImportBuilder importBuilder)
	{
		this.importBuilder = importBuilder;
	}

	public HtmlTitleHandler getHtmlTitleHandler()
	{
		return htmlTitleHandler;
	}

	public void setHtmlTitleHandler(HtmlTitleHandler htmlTitleHandler)
	{
		this.htmlTitleHandler = htmlTitleHandler;
	}

	public DashboardTheme getDashboardTheme()
	{
		return dashboardTheme;
	}

	public void setDashboardTheme(DashboardTheme dashboardTheme)
	{
		this.dashboardTheme = dashboardTheme;
	}
	
	public Locale getLocale()
	{
		return locale;
	}

	public void setLocale(Locale locale)
	{
		this.locale = locale;
	}

	/**
	 * 获取默认{@linkplain RenderContext}变量名。
	 * 
	 * @param suffix
	 * @return
	 */
	public String varNameOfRenderContext()
	{
		return varNameOfRenderContext(null);
	}

	/**
	 * 获取指定后缀的{@linkplain RenderContext}变量名。
	 * 
	 * @param suffix
	 * @return
	 */
	public String varNameOfRenderContext(String suffix)
	{
		return genIdentifier("RenderContext", suffix);
	}

	/**
	 * 获取默认图表插件变量名。
	 * 
	 * @return
	 */
	public String varNameOfChartPlugin()
	{
		return varNameOfChartPlugin(null);
	}

	/**
	 * 获取指定后缀的图表插件变量名。
	 * 
	 * @param suffix
	 *            允许为{@code null}
	 * @return
	 */
	public String varNameOfChartPlugin(String suffix)
	{
		return genIdentifier("ChartPlugin", suffix);
	}

	/**
	 * 获取默认图表HTML元素ID。
	 * 
	 * @return
	 */
	public String elementIdOfChart()
	{
		return elementIdOfChart(null);
	}

	/**
	 * 获取指定后缀的图表HTML元素ID。
	 * 
	 * @param suffix
	 *            允许为{@code null}
	 * @return
	 */
	public String elementIdOfChart(String suffix)
	{
		return genIdentifier("chartele", suffix);
	}

	/**
	 * 获取默认图表变量名。
	 * 
	 * @return
	 */
	public String varNameOfChart()
	{
		return varNameOfChart(null);
	}

	/**
	 * 获取指定后缀的图表变量名。
	 * 
	 * @param suffix
	 *            允许为{@code null}
	 * @return
	 */
	public String varNameOfChart(String suffix)
	{
		return genIdentifier("Chart", suffix);
	}

	/**
	 * 获取默认看板变量名。
	 * 
	 * @return
	 */
	public String varNameOfDashboard()
	{
		return varNameOfDashboard(null);
	}

	/**
	 * 获取指定后缀的看板变量名。
	 * 
	 * @param suffix
	 *            允许为{@code null}
	 * @return
	 */
	public String varNameOfDashboard(String suffix)
	{
		return genIdentifier("Dashboard", suffix);
	}

	/**
	 * 生成标识。
	 * 
	 * @param prefix
	 * @param suffix 允许为{@code null}
	 * @return
	 */
	protected String genIdentifier(String prefix, String suffix)
	{
		StringBuilder sb = new StringBuilder(Global.PRODUCT_NAME_EN_LC);
		
		sb.append(prefix);

		if (!StringUtil.isEmpty(suffix))
			sb.append(suffix);

		return sb.toString();
	}
}
