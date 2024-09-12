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

package org.datagear.web.util;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.datagear.analysis.support.html.HtmlTplDashboard;
import org.datagear.analysis.support.html.HtmlTplDashboardImport;
import org.datagear.analysis.support.html.HtmlTplDashboardImportBuilder;
import org.datagear.analysis.support.html.HtmlTplDashboardRenderContext;
import org.datagear.analysis.support.html.HtmlTplDashboardWidgetHtmlRenderer;
import org.datagear.analysis.support.html.HtmlTplDashboardWidgetRenderer;
import org.datagear.util.Global;
import org.datagear.web.controller.ServerTimeJsController;

/**
 * Web {@linkplain HtmlTplDashboardImportBuilder}。
 * 
 * @author datagear@163.com
 *
 */
public class WebHtmlTplDashboardImportBuilder implements HtmlTplDashboardImportBuilder
{
	/**
	 * 内置看板资源名，不要修改它们，因为它们可能会在看板模板中通过{@linkplain HtmlTplDashboardWidgetHtmlRenderer#DEFAULT_ATTR_NAME_DASHBOARD_UNIMPORT}设置。
	 */
	public static final String BUILTIN_DASHBOARD_IMPORT_NAME_JQUERY = "jquery";
	public static final String BUILTIN_DASHBOARD_IMPORT_NAME_ECHARTS = "echarts";
	public static final String BUILTIN_DASHBOARD_IMPORT_NAME_ECHARTS_WORDCLOUD = "echarts-wordcloud";
	public static final String BUILTIN_DASHBOARD_IMPORT_NAME_ECHARTS_LIQUIDFILL = "echarts-liquidfill";
	public static final String BUILTIN_DASHBOARD_IMPORT_NAME_DATATABLES = "DataTables";
	public static final String BUILTIN_DASHBOARD_IMPORT_NAME_JQUERY_DATETIMEPICKER = "jquery-datetimepicker";
	public static final String BUILTIN_DASHBOARD_IMPORT_NAME_CHARTFACTORY = "chartFactory";
	public static final String BUILTIN_DASHBOARD_IMPORT_NAME_DASHBOARDFACTORY = "dashboardFactory";
	public static final String BUILTIN_DASHBOARD_IMPORT_NAME_SERVERTIME = "serverTime";
	public static final String BUILTIN_DASHBOARD_IMPORT_NAME_CHARTSUPPORT = "chartSupport";
	public static final String BUILTIN_DASHBOARD_IMPORT_NAME_CHARTSETTING = "chartSetting";
	public static final String BUILTIN_DASHBOARD_IMPORT_NAME_CHARTPLUGINMANAGER = "chartPluginManager";
	public static final String BUILTIN_DASHBOARD_IMPORT_NAME_DASHBOARDSTYLE = "dashboardStyle";
	public static final String BUILTIN_DASHBOARD_IMPORT_NAME_DASHBOARDEDITOR = "dashboardEditor";
	public static final String BUILTIN_DASHBOARD_IMPORT_NAME_FAVICON = "favicon";

	public static final String PATH_STATIC = "/static";

	public static final String PATH_LIB = "/static/lib";

	public static final String PATH_CSS = "/static/css";

	public static final String PATH_SCRIPT = "/static/script";

	/**
	 * 看板展示模式：展示
	 */
	public static final String MODE_SHOW = "SHOW";

	/**
	 * 看板展示模式：可视编辑
	 */
	public static final String MODE_EDIT = "EDIT";

	/** 请求 */
	private HttpServletRequest request;

	/** 模式 */
	private String mode;

	public WebHtmlTplDashboardImportBuilder()
	{
		super();
	}
	
	public WebHtmlTplDashboardImportBuilder(HttpServletRequest request, String mode)
	{
		super();
		this.request = request;
		this.mode = mode;
	}

	public HttpServletRequest getRequest()
	{
		return request;
	}

	public void setRequest(HttpServletRequest request)
	{
		this.request = request;
	}

	public String getMode()
	{
		return mode;
	}

	public void setMode(String mode)
	{
		this.mode = mode;
	}

	@Override
	public List<HtmlTplDashboardImport> build(HtmlTplDashboardRenderContext renderContext, HtmlTplDashboard dashboard)
	{
		return build(renderContext, dashboard, this.request, this.mode);
	}

	protected List<HtmlTplDashboardImport> build(HtmlTplDashboardRenderContext renderContext,
			HtmlTplDashboard dashboard, HttpServletRequest request, String mode)
	{
		String contextPath = WebUtils.getContextPath(request);
		String randomCode = "rc" + Long.toHexString(System.currentTimeMillis());

		return build(renderContext, dashboard, request, mode, contextPath, randomCode);
	}

	protected List<HtmlTplDashboardImport> build(HtmlTplDashboardRenderContext renderContext,
			HtmlTplDashboard dashboard, HttpServletRequest request, String mode, String contextPath,
			String randomCode)
	{
		List<HtmlTplDashboardImport> impts = new ArrayList<>();

		String libPrefix = contextPath + PATH_LIB;
		String cssPrefix = contextPath + PATH_CSS;
		String scriptPrefix = contextPath + PATH_SCRIPT;

		// favicon
		impts.add(new HtmlTplDashboardImport(BUILTIN_DASHBOARD_IMPORT_NAME_FAVICON,
				"<link type=\"images/x-icon\" href=\"" + contextPath + "/favicon.ico?v=" + Global.VERSION
						+ "\" rel=\"shortcut icon\" " + HtmlTplDashboardWidgetRenderer.DASHBOARD_IMPORT_ITEM_NAME_ATTR
						+ "=\"" + BUILTIN_DASHBOARD_IMPORT_NAME_FAVICON + "\" />"));

		// CSS
		impts.add(HtmlTplDashboardImport.valueOfLinkCss(BUILTIN_DASHBOARD_IMPORT_NAME_DATATABLES,
						libPrefix + "/DataTables-1.11.3/css/datatables.min.css"));
		impts.add(HtmlTplDashboardImport.valueOfLinkCss(BUILTIN_DASHBOARD_IMPORT_NAME_JQUERY_DATETIMEPICKER,
				libPrefix + "/jquery-datetimepicker-2.5.20/jquery.datetimepicker.min.css"));
		impts.add(HtmlTplDashboardImport.valueOfLinkCss(BUILTIN_DASHBOARD_IMPORT_NAME_DASHBOARDSTYLE,
				cssPrefix + "/analysis.css?v=" + Global.VERSION));
		
		if (isModelEdit(mode))
		{
			impts.add(HtmlTplDashboardImport.valueOfLinkCss(BUILTIN_DASHBOARD_IMPORT_NAME_DASHBOARDEDITOR,
					contextPath + "/static/css/dashboardEditor.css?v=" + Global.VERSION));
		}

		// JS
		impts.add(HtmlTplDashboardImport.valueOfJavaScript(BUILTIN_DASHBOARD_IMPORT_NAME_JQUERY,
				libPrefix + "/jquery-3.7.1/jquery-3.7.1.min.js"));
		impts.add(HtmlTplDashboardImport.valueOfJavaScript(BUILTIN_DASHBOARD_IMPORT_NAME_ECHARTS,
				libPrefix + "/echarts-5.4.3/echarts.min.js"));
		impts.add(HtmlTplDashboardImport.valueOfJavaScript(BUILTIN_DASHBOARD_IMPORT_NAME_ECHARTS_WORDCLOUD,
				libPrefix + "/echarts-wordcloud-2.0.0/echarts-wordcloud.min.js"));
		impts.add(HtmlTplDashboardImport.valueOfJavaScript(BUILTIN_DASHBOARD_IMPORT_NAME_ECHARTS_LIQUIDFILL,
				libPrefix + "/echarts-liquidfill-3.0.0/echarts-liquidfill.min.js"));
		impts.add(HtmlTplDashboardImport.valueOfJavaScript(BUILTIN_DASHBOARD_IMPORT_NAME_DATATABLES,
						libPrefix + "/DataTables-1.11.3/js/datatables.min.js"));
		impts.add(HtmlTplDashboardImport.valueOfJavaScript(BUILTIN_DASHBOARD_IMPORT_NAME_JQUERY_DATETIMEPICKER,
				libPrefix + "/jquery-datetimepicker-2.5.20/jquery.datetimepicker.full.min.js"));
		impts.add(HtmlTplDashboardImport.valueOfJavaScript(BUILTIN_DASHBOARD_IMPORT_NAME_CHARTFACTORY,
						scriptPrefix + "/chartFactory.js?v=" + Global.VERSION));
		impts.add(HtmlTplDashboardImport.valueOfJavaScript(BUILTIN_DASHBOARD_IMPORT_NAME_DASHBOARDFACTORY,
				scriptPrefix + "/dashboardFactory.js?v=" + Global.VERSION));
		impts.add(HtmlTplDashboardImport.valueOfJavaScript(BUILTIN_DASHBOARD_IMPORT_NAME_SERVERTIME,
				contextPath + ServerTimeJsController.SERVER_TIME_URL + "?v=" + randomCode));
		impts.add(HtmlTplDashboardImport.valueOfJavaScript(BUILTIN_DASHBOARD_IMPORT_NAME_CHARTSUPPORT,
						scriptPrefix + "/chartSupport.js?v=" + Global.VERSION));
		impts.add(HtmlTplDashboardImport.valueOfJavaScript(BUILTIN_DASHBOARD_IMPORT_NAME_CHARTSETTING,
						scriptPrefix + "/chartSetting.js?v=" + Global.VERSION));
		impts.add(HtmlTplDashboardImport.valueOfJavaScript(BUILTIN_DASHBOARD_IMPORT_NAME_CHARTPLUGINMANAGER,
				contextPath + "/chartPlugin/chartPluginManager.js?v=" + Global.VERSION));

		if (isModelEdit(mode))
		{
			impts.add(HtmlTplDashboardImport.valueOfJavaScript(BUILTIN_DASHBOARD_IMPORT_NAME_DASHBOARDEDITOR,
					scriptPrefix + "/dashboardEditor.js?v=" + Global.VERSION));
		}

		return impts;
	}

	protected boolean isModelEdit(String mode)
	{
		return MODE_EDIT.equals(mode);
	}
}
