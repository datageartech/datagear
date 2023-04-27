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

package org.datagear.web.util;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.datagear.analysis.support.html.HtmlTplDashboardImport;
import org.datagear.analysis.support.html.HtmlTplDashboardWidgetHtmlRenderer;
import org.datagear.util.Global;

/**
 * 看板展示请求的{@linkplain HtmlTplDashboardImport}加载列表处理器。
 * 
 * @author datagear@163.com
 *
 */
public class HtmlTplDashboardImportResolver
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

	/**
	 * 看板展示模式：展示
	 */
	public static final String MODE_SHOW = "SHOW";

	/**
	 * 看板展示模式：可视编辑
	 */
	public static final String MODE_EDIT = "EDIT";

	public HtmlTplDashboardImportResolver()
	{
		super();
	}
	
	/**
	 * 获取执行请求的{@linkplain HtmlTplDashboardImport}加载列表。
	 * 
	 * @param request
	 * @param mode
	 *            参考{@linkplain #MODE_SHOW}、{@linkplain #MODE_EDIT}
	 * @return
	 */
	public List<HtmlTplDashboardImport> resolve(HttpServletRequest request, String mode)
	{
		List<HtmlTplDashboardImport> impts = new ArrayList<>();

		String contextPath = WebUtils.getContextPath(request);
		String rp = "rc" + Long.toHexString(System.currentTimeMillis());

		String staticPrefix = contextPath + "/static";
		String libPrefix = staticPrefix + "/lib";
		String cssPrefix = staticPrefix + "/css";
		String scriptPrefix = staticPrefix + "/script";

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
					WebUtils.getContextPath(request) + "/static/css/dashboardEditor.css?v=" + Global.VERSION));
		}

		// JS
		impts.add(HtmlTplDashboardImport.valueOfJavaScript(BUILTIN_DASHBOARD_IMPORT_NAME_JQUERY,
				libPrefix + "/jquery-3.6.0/jquery-3.6.0.min.js"));
		impts.add(HtmlTplDashboardImport.valueOfJavaScript(BUILTIN_DASHBOARD_IMPORT_NAME_ECHARTS,
				libPrefix + "/echarts-5.4.2/echarts.min.js"));
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
						contextPath + "/dashboard/serverTime.js?v=" + rp));
		impts.add(HtmlTplDashboardImport.valueOfJavaScript(BUILTIN_DASHBOARD_IMPORT_NAME_CHARTSUPPORT,
						scriptPrefix + "/chartSupport.js?v=" + Global.VERSION));
		impts.add(HtmlTplDashboardImport.valueOfJavaScript(BUILTIN_DASHBOARD_IMPORT_NAME_CHARTSETTING,
						scriptPrefix + "/chartSetting.js?v=" + Global.VERSION));
		impts.add(HtmlTplDashboardImport.valueOfJavaScript(BUILTIN_DASHBOARD_IMPORT_NAME_CHARTPLUGINMANAGER,
				contextPath + "/chartPlugin/chartPluginManager.js?v=" + Global.VERSION));

		if (isModelEdit(mode))
		{
			impts.add(HtmlTplDashboardImport.valueOfJavaScript(BUILTIN_DASHBOARD_IMPORT_NAME_DASHBOARDEDITOR,
					WebUtils.getContextPath(request) + "/static/script/dashboardEditor.js?v=" + Global.VERSION));
		}

		return impts;
	}

	protected boolean isModelEdit(String mode)
	{
		return MODE_EDIT.equals(mode);
	}
}
