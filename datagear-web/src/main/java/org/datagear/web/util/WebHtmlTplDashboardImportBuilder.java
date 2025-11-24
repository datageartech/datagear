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
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.datagear.analysis.support.html.HtmlTplDashboard;
import org.datagear.analysis.support.html.HtmlTplDashboardImport;
import org.datagear.analysis.support.html.HtmlTplDashboardImportBuilder;
import org.datagear.analysis.support.html.HtmlTplDashboardRenderContext;
import org.datagear.analysis.support.html.HtmlTplDashboardWidgetHtmlRenderer;
import org.datagear.analysis.support.html.HtmlTplDashboardWidgetRenderer;
import org.datagear.util.Global;
import org.datagear.web.analysis.DashboardApiVersion;
import org.datagear.web.controller.ChartPluginVisualResController;
import org.datagear.web.controller.ServerTimeJsController;
import org.datagear.web.util.ChartPluginManagerJsFactory.ChartPluginManagerJs;

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
	public static final String BUILTIN_DASHBOARD_IMPORT_NAME_CHARTLIBREGISTRY = "chartLibRegistry";
	public static final String BUILTIN_DASHBOARD_IMPORT_NAME_SERVERTIME = "serverTime";
	public static final String BUILTIN_DASHBOARD_IMPORT_NAME_CHARTSUPPORT = "chartSupport";
	public static final String BUILTIN_DASHBOARD_IMPORT_NAME_CHARTSETTING = "chartSetting";
	public static final String BUILTIN_DASHBOARD_IMPORT_NAME_CHARTTOOL = "chartTool";
	public static final String BUILTIN_DASHBOARD_IMPORT_NAME_CHARTPLUGINMANAGER = "chartPluginManager";
	public static final String BUILTIN_DASHBOARD_IMPORT_NAME_DASHBOARDSTYLE = "dashboardStyle";
	public static final String BUILTIN_DASHBOARD_IMPORT_NAME_DASHBOARDEDITOR = "dashboardEditor";
	public static final String BUILTIN_DASHBOARD_IMPORT_NAME_FAVICON = "favicon";
	public static final String BUILTIN_DASHBOARD_IMPORT_NAME_BUILTIN_MAP = "dashboardBuiltinMap";

	public static final String PATH_STATIC = "/static";

	public static final String PATH_LIB = "/static/analysislib";

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

	private ChartPluginManagerJsFactory chartPluginManagerJsFactory;

	public WebHtmlTplDashboardImportBuilder()
	{
		super();
	}
	
	public WebHtmlTplDashboardImportBuilder(HttpServletRequest request, String mode,
			ChartPluginManagerJsFactory chartPluginManagerJsFactory)
	{
		super();
		this.request = request;
		this.mode = mode;
		this.chartPluginManagerJsFactory = chartPluginManagerJsFactory;
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

	public ChartPluginManagerJsFactory getChartPluginManagerJsFactory()
	{
		return chartPluginManagerJsFactory;
	}

	public void setChartPluginManagerJsFactory(ChartPluginManagerJsFactory chartPluginManagerJsFactory)
	{
		this.chartPluginManagerJsFactory = chartPluginManagerJsFactory;
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
		String apiVersion = trimDashboardApiVersion(dashboard);
		boolean isV1 = DashboardApiVersion.isV1(apiVersion);
		Locale locale = WebUtils.getLocale(request);

		// favicon
		impts.add(new HtmlTplDashboardImport(BUILTIN_DASHBOARD_IMPORT_NAME_FAVICON,
				"<link type=\"images/x-icon\" href=\"" + contextPath + "/favicon.ico?v=" + Global.VERSION
						+ "\" rel=\"shortcut icon\" " + HtmlTplDashboardWidgetRenderer.DASHBOARD_LIB_NAME_ATTR
						+ "=\"" + BUILTIN_DASHBOARD_IMPORT_NAME_FAVICON + "\" />"));

		if (isV1)
		{
			String apiPrefix = getV1AnalysisPath(contextPath, dashboard);

			impts.add(HtmlTplDashboardImport.valueOfLinkCss(BUILTIN_DASHBOARD_IMPORT_NAME_JQUERY_DATETIMEPICKER,
					libPrefix + "/jquery-datetimepicker-2.5.20/jquery.datetimepicker.min.css"));
			impts.add(HtmlTplDashboardImport.valueOfLinkCss(BUILTIN_DASHBOARD_IMPORT_NAME_DASHBOARDSTYLE,
					apiPrefix + "/css/style.css?v=" + Global.VERSION));

			if (isEditMode(mode))
			{
				impts.add(HtmlTplDashboardImport.valueOfLinkCss(BUILTIN_DASHBOARD_IMPORT_NAME_DASHBOARDEDITOR,
						apiPrefix + "/css/dashboardEditor.css?v=" + Global.VERSION));
			}

			impts.add(HtmlTplDashboardImport.valueOfJavaScript(BUILTIN_DASHBOARD_IMPORT_NAME_JQUERY,
					libPrefix + "/jquery-3.7.1/jquery-3.7.1.min.js"));
			impts.add(HtmlTplDashboardImport.valueOfJavaScript(BUILTIN_DASHBOARD_IMPORT_NAME_ECHARTS,
					libPrefix + "/echarts-5.6.0/echarts.min.js"));
			impts.add(HtmlTplDashboardImport.valueOfJavaScript(BUILTIN_DASHBOARD_IMPORT_NAME_JQUERY_DATETIMEPICKER,
					libPrefix + "/jquery-datetimepicker-2.5.20/jquery.datetimepicker.full.min.js"));
			impts.add(HtmlTplDashboardImport.valueOfJavaScript(BUILTIN_DASHBOARD_IMPORT_NAME_JQUERY,
					libPrefix + "/jquery-3.7.1/jquery-3.7.1.min.js"));
			impts.add(HtmlTplDashboardImport.valueOfJavaScript(BUILTIN_DASHBOARD_IMPORT_NAME_ECHARTS,
					libPrefix + "/echarts-5.6.0/echarts.min.js"));
			impts.add(HtmlTplDashboardImport.valueOfJavaScript(BUILTIN_DASHBOARD_IMPORT_NAME_JQUERY_DATETIMEPICKER,
					libPrefix + "/jquery-datetimepicker-2.5.20/jquery.datetimepicker.full.min.js"));
			impts.add(HtmlTplDashboardImport.valueOfJavaScript(BUILTIN_DASHBOARD_IMPORT_NAME_CHARTFACTORY,
					apiPrefix + "/chartFactory.js?v=" + Global.VERSION));
			impts.add(HtmlTplDashboardImport.valueOfJavaScript(BUILTIN_DASHBOARD_IMPORT_NAME_DASHBOARDFACTORY,
					apiPrefix + "/dashboardFactory.js?v=" + Global.VERSION));
			impts.add(HtmlTplDashboardImport.valueOfJavaScript(BUILTIN_DASHBOARD_IMPORT_NAME_SERVERTIME,
					contextPath + ServerTimeJsController.SERVER_TIME_URL + "?v=" + randomCode));
			impts.add(HtmlTplDashboardImport.valueOfJavaScript(BUILTIN_DASHBOARD_IMPORT_NAME_CHARTSUPPORT,
					apiPrefix + "/chartSupport.js?v=" + Global.VERSION));
			impts.add(HtmlTplDashboardImport.valueOfJavaScript(BUILTIN_DASHBOARD_IMPORT_NAME_CHARTSETTING,
					apiPrefix + "/chartSetting.js?v=" + Global.VERSION));

			if (isEditMode(mode))
			{
				impts.add(HtmlTplDashboardImport.valueOfJavaScript(BUILTIN_DASHBOARD_IMPORT_NAME_DASHBOARDEDITOR,
						apiPrefix + "/dashboardEditor.js?v=" + Global.VERSION));
			}
		}
		else
		{
			String apiPrefix = getV2AnalysisPath(contextPath, dashboard);

			impts.add(HtmlTplDashboardImport.valueOfLinkCss(BUILTIN_DASHBOARD_IMPORT_NAME_DASHBOARDSTYLE,
					apiPrefix + "/css/style.css?v=" + Global.VERSION));

			if (isEditMode(mode))
			{
				impts.add(HtmlTplDashboardImport.valueOfLinkCss(BUILTIN_DASHBOARD_IMPORT_NAME_DASHBOARDEDITOR,
						apiPrefix + "/css/dashboardEditor.css?v=" + Global.VERSION));
			}

			impts.add(HtmlTplDashboardImport.valueOfJavaScript(BUILTIN_DASHBOARD_IMPORT_NAME_CHARTFACTORY,
					apiPrefix + "/chartFactory.js?v=" + Global.VERSION));
			impts.add(HtmlTplDashboardImport.valueOfJavaScript(BUILTIN_DASHBOARD_IMPORT_NAME_DASHBOARDFACTORY,
					apiPrefix + "/dashboardFactory.js?v=" + Global.VERSION));
			impts.add(HtmlTplDashboardImport.valueOfJavaScript(BUILTIN_DASHBOARD_IMPORT_NAME_CHARTLIBREGISTRY,
					apiPrefix + "/chartLibRegistry.js?v=" + Global.VERSION));
			impts.add(HtmlTplDashboardImport.valueOfJavaScript(BUILTIN_DASHBOARD_IMPORT_NAME_SERVERTIME,
					contextPath + ServerTimeJsController.SERVER_TIME_URL + "?v=" + randomCode));
			impts.add(HtmlTplDashboardImport.valueOfJavaScript(BUILTIN_DASHBOARD_IMPORT_NAME_CHARTSUPPORT,
					apiPrefix + "/chartSupport.js?v=" + Global.VERSION));
			impts.add(HtmlTplDashboardImport.valueOfJavaScript(BUILTIN_DASHBOARD_IMPORT_NAME_CHARTTOOL,
					apiPrefix + "/chartTool.js?v=" + Global.VERSION));
			impts.add(HtmlTplDashboardImport.valueOfJavaScript(BUILTIN_DASHBOARD_IMPORT_NAME_BUILTIN_MAP,
					apiPrefix + "/dashboardBuiltinMap.js?v=" + Global.VERSION));

			if (isEditMode(mode))
			{
				impts.add(HtmlTplDashboardImport.valueOfJavaScript(BUILTIN_DASHBOARD_IMPORT_NAME_DASHBOARDEDITOR,
						apiPrefix + "/dashboardEditor.js?v=" + Global.VERSION));
			}
		}

		addChartPluginManagerImport(impts, contextPath, locale, apiVersion);

		return impts;
	}

	protected void addChartPluginManagerImport(List<HtmlTplDashboardImport> impts, String contextPath, Locale locale,
			String apiVersion)
	{
		ChartPluginManagerJs managerJs = this.chartPluginManagerJsFactory.latest(locale, apiVersion);
		int scriptCount = managerJs.getScriptCount();
		String key = managerJs.getKey();
		
		for (int i = 0; i < scriptCount; i++)
		{
			impts.add(HtmlTplDashboardImport.valueOfJavaScript(BUILTIN_DASHBOARD_IMPORT_NAME_CHARTPLUGINMANAGER,
					contextPath + "/vres/plugin/chartPluginManager.js" //
							+ "?" + ChartPluginVisualResController.MANAGER_JS_KEY_PARAM + "=" + key //
							+ "&" + ChartPluginVisualResController.MANAGER_JS_BLOCK_PARAM + "=" + i //
							+ "&" + ChartPluginVisualResController.API_VERSION_PARAM + "=" + apiVersion //
							+ "&v=" + Global.VERSION));
		}
	}

	protected String getV1AnalysisPath(String contextPath, HtmlTplDashboard dashboard)
	{
		return contextPath + "/static/analysisapi/1.0";
	}

	protected String getV2AnalysisPath(String contextPath, HtmlTplDashboard dashboard)
	{
		return contextPath + "/static/analysisapi/2.0";
	}

	protected String getAnalysisPath(String contextPath, HtmlTplDashboard dashboard)
	{
		String apiVersion = trimDashboardApiVersion(dashboard);

		if (DashboardApiVersion.isV1(apiVersion))
		{
			return getV1AnalysisPath(contextPath, dashboard);
		}
		else
		{
			return getV2AnalysisPath(contextPath, dashboard);
		}
	}

	protected String trimDashboardApiVersion(HtmlTplDashboard dashboard)
	{
		// 默认版本应设为V1，以兼容旧版看板
		return DashboardApiVersion.trimVersionWithV1(dashboard.getApiVersion());
	}

	protected boolean isEditMode(String mode)
	{
		return MODE_EDIT.equals(mode);
	}
}
