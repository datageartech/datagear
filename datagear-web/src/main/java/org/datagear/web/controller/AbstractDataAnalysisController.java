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

package org.datagear.web.controller;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.analysis.ChartDefinition;
import org.datagear.analysis.ChartQuery;
import org.datagear.analysis.DashboardQuery;
import org.datagear.analysis.DashboardResult;
import org.datagear.analysis.DashboardTheme;
import org.datagear.analysis.DashboardThemeSource;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.SimpleDashboardQueryHandler;
import org.datagear.analysis.support.ChartWidget;
import org.datagear.analysis.support.html.HtmlChartWidget;
import org.datagear.analysis.support.html.HtmlTitleHandler;
import org.datagear.analysis.support.html.HtmlTplDashboardImportBuilder;
import org.datagear.analysis.support.html.HtmlTplDashboardRenderContext;
import org.datagear.analysis.support.html.HtmlTplDashboardWidgetRenderer;
import org.datagear.management.domain.User;
import org.datagear.util.StringUtil;
import org.datagear.web.analysis.AnalysisUser;
import org.datagear.web.analysis.RenderContextAttrs;
import org.datagear.web.analysis.SessionDashboardInfoSupport;
import org.datagear.web.analysis.SessionDashboardInfoSupport.DashboardInfo;
import org.datagear.web.analysis.WebDashboardQueryConverter;
import org.datagear.web.analysis.WebHtmlTplDashboardImportBuilder;
import org.datagear.web.analysis.WebHtmlTplDashboardImportBuilderFactory;
import org.datagear.web.util.ThemeSpec;
import org.datagear.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 抽象数据分析控制器。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractDataAnalysisController extends AbstractController
{
	/**
	 * 看板展示URL的请求参数名：系统主题。
	 */
	public static final String DASHBOARD_SHOW_PARAM_THEME_NAME = ChartDefinition.BUILTIN_NAME_PREFIX + "THEME";

	/**
	 * 看板展示URL的请求参数值：自动设置系统主题。
	 */
	public static final String DASHBOARD_SHOW_PARAM_VALUE_AUTO_THEME = "auto";

	/** 看板心跳URL后缀 */
	public static final String HEARTBEAT_TAIL_URL = "/heartbeat";

	/** 看板卸载URL后缀 */
	public static final String UNLOAD_TAIL_URL = "/unload";

	/** 看板心跳参数：看板ID */
	public static final String HEARTBEAT_PARAM_DASHBOARD_ID = "dashboardId";

	/** 看板卸载参数：看板ID */
	public static final String UNLOAD_PARAM_DASHBOARD_ID = "dashboardId";

	@Autowired
	private WebDashboardQueryConverter webDashboardQueryConverter;

	@Autowired
	private DashboardThemeSource dashboardThemeSource;
	
	@Autowired
	private WebHtmlTplDashboardImportBuilderFactory webHtmlTplDashboardImportBuilderFactory;
	
	@Autowired
	private ThemeSpec themeSpec;

	@Autowired
	private SessionDashboardInfoSupport sessionDashboardInfoSupport;

	public AbstractDataAnalysisController()
	{
		super();
	}

	public WebDashboardQueryConverter getWebDashboardQueryConverter()
	{
		return webDashboardQueryConverter;
	}

	public void setWebDashboardQueryConverter(WebDashboardQueryConverter webDashboardQueryConverter)
	{
		this.webDashboardQueryConverter = webDashboardQueryConverter;
	}

	public DashboardThemeSource getDashboardThemeSource()
	{
		return dashboardThemeSource;
	}

	public void setDashboardThemeSource(DashboardThemeSource dashboardThemeSource)
	{
		this.dashboardThemeSource = dashboardThemeSource;
	}

	public WebHtmlTplDashboardImportBuilderFactory getWebHtmlTplDashboardImportBuilderFactory()
	{
		return webHtmlTplDashboardImportBuilderFactory;
	}

	public void setWebHtmlTplDashboardImportBuilderFactory(
			WebHtmlTplDashboardImportBuilderFactory webHtmlTplDashboardImportBuilderFactory)
	{
		this.webHtmlTplDashboardImportBuilderFactory = webHtmlTplDashboardImportBuilderFactory;
	}

	public ThemeSpec getThemeSpec()
	{
		return themeSpec;
	}

	public void setThemeSpec(ThemeSpec themeSpec)
	{
		this.themeSpec = themeSpec;
	}

	public SessionDashboardInfoSupport getSessionDashboardInfoSupport()
	{
		return sessionDashboardInfoSupport;
	}

	public void setSessionDashboardInfoSupport(SessionDashboardInfoSupport sessionDashboardInfoSupport)
	{
		this.sessionDashboardInfoSupport = sessionDashboardInfoSupport;
	}

	/**
	 * 处理看板心跳。
	 * <p>
	 * 看板页面有停留较长时间再操作的场景，此时可能会因为会话超时导致操作失败，应添加心跳请求，避免会话超时。
	 * </p>
	 * 
	 * @param request
	 * @param response
	 * @param dashboardId
	 * @return
	 * @throws Throwable
	 */
	protected Map<String, Object> handleHeartbeat(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(HEARTBEAT_PARAM_DASHBOARD_ID) String dashboardId) throws Throwable
	{
		long time = System.currentTimeMillis();

		Map<String, Object> data = new HashMap<>();
		data.put("heartbeat", true);
		data.put("dashboardId", dashboardId);
		data.put("time", time);

		return data;
	}

	/**
	 * 看板卸载。
	 * <p>
	 * 看板页面关闭后，应卸载后台看板数据。
	 * </p>
	 * 
	 * @param request
	 * @param response
	 * @param dashboardId
	 * @return
	 * @throws Throwable
	 */
	protected Map<String, Object> handleUnloadDashboard(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(UNLOAD_PARAM_DASHBOARD_ID) String dashboardId) throws Throwable
	{
		Map<String, Object> data = new HashMap<>();
		data.put("unload", true);
		data.put("dashboardId", dashboardId);

		getSessionDashboardInfoSupport().removeDashboardInfo(request, dashboardId);

		return data;
	}

	protected HtmlTplDashboardRenderContext createRenderContext(HttpServletRequest request, HttpServletResponse response,
			String template, Writer responseWriter, HtmlTplDashboardImportBuilder importBuilder,
			HtmlTitleHandler titleHandler) throws IOException
	{
		HtmlTplDashboardRenderContext renderContext = new HtmlTplDashboardRenderContext(template, responseWriter);
		
		Map<String, ?> paramValues = resolveDashboardShowParamValues(request);
		DashboardTheme dashboardTheme = resolveDashboardTheme(request);
		AnalysisUser analysisUser = getWebDashboardQueryConverter().toAnalysisUser(getCurrentUser().cloneNoPassword());
		
		renderContext.putAll(paramValues);
		renderContext.put(RenderContextAttrs.DASHBOARD_THEME, dashboardTheme);
		renderContext.put(RenderContextAttrs.USER, analysisUser);
		
		renderContext.setImportBuilder(importBuilder);
		renderContext.setDashboardTheme(dashboardTheme);
		renderContext.setHtmlTitleHandler(titleHandler);

		return renderContext;
	}

	/**
	 * 构建看板导入列表：展示。
	 * 
	 * @param request
	 * @return
	 */
	protected WebHtmlTplDashboardImportBuilder buildWebHtmlTplDashboardImportBuilderForShow(HttpServletRequest request)
	{
		return this.webHtmlTplDashboardImportBuilderFactory.getBuilder(request,
				WebHtmlTplDashboardImportBuilder.MODE_SHOW);
	}

	/**
	 * 构建看板导入列表：可视编辑。
	 * 
	 * @param request
	 * @return
	 */
	protected WebHtmlTplDashboardImportBuilder buildWebHtmlTplDashboardImportBuilderForEdit(HttpServletRequest request)
	{
		return this.webHtmlTplDashboardImportBuilderFactory.getBuilder(request,
				WebHtmlTplDashboardImportBuilder.MODE_EDIT);
	}

	/**
	 * 填充Web上下文信息至{@linkplain RenderContext}。
	 * 
	 * @param request
	 * @param renderContext
	 * @return
	 */
	protected void inflateWebRenderContext(HttpServletRequest request, RenderContext renderContext)
	{
		renderContext.put(RenderContextAttrs.CONTEXT_PATH, WebUtils.getContextPath(request));
	}

	/**
	 * 解析看板展示请求参数映射表。
	 * 
	 * @param request
	 * @return
	 */
	protected Map<String, ?> resolveDashboardShowParamValues(HttpServletRequest request)
	{
		Map<String, Object> paramValues = new HashMap<>();

		Map<String, String[]> origin = request.getParameterMap();

		for (Map.Entry<String, String[]> entry : origin.entrySet())
		{
			String name = entry.getKey();
			String[] values = entry.getValue();

			if (values == null || values.length == 0)
				continue;

			if (values.length == 1)
				paramValues.put(name, values[0]);
			else
				paramValues.put(name, values);
		}

		return paramValues;
	}

	/**
	 * 转义要放入{@linkplain RenderContext}的看板模板内容。
	 * <p>
	 * 它可能包含"</script>"子串，传回浏览器端时会导致页面解析出错，需转义为："<\/script>"。
	 * </p>
	 * 
	 * @param templateContent
	 * @return
	 */
	protected Map<String, String> escapeDashboardRenderContextValue(Map<String, String> value)
	{
		if (value == null || value.isEmpty())
			return value;

		Map<String, String> re = new HashMap<String, String>(value.size());

		for (Map.Entry<String, String> entry : value.entrySet())
			re.put(entry.getKey(), escapeDashboardRenderContextValue(entry.getValue()));

		return re;
	}

	/**
	 * 转义要放入{@linkplain RenderContext}的看板模板内容。
	 * <p>
	 * 它可能包含"</script>"子串，传回浏览器端时会导致页面解析出错，需转义为："<\/script>"。
	 * </p>
	 * 
	 * @param templateContent
	 * @return
	 */
	protected String escapeDashboardRenderContextValue(String value)
	{
		return (value == null ? null : value.replace("</", "<\\/"));
	}

	/**
	 * 解析看板展示主题。
	 * 
	 * @param request
	 * @return
	 */
	protected DashboardTheme resolveDashboardTheme(HttpServletRequest request)
	{
		String theme = request.getParameter(DASHBOARD_SHOW_PARAM_THEME_NAME);
		DashboardTheme dashboardTheme = null;
		
		if (isDashboardThemeAuto(request, theme))
		{
			theme = WebUtils.getTheme(request);
			dashboardTheme = this.themeSpec.dashboardTheme(theme);
		}
		
		if(dashboardTheme == null)
			dashboardTheme = (theme == null ? null : this.dashboardThemeSource.getDashboardTheme(theme));

		if (dashboardTheme == null)
			dashboardTheme = this.dashboardThemeSource.getDashboardTheme();

		return dashboardTheme;
	}
	
	/**
	 * 是否看板主题自动匹配系统主题。
	 * <p>
	 * 默认是：
	 * </p>
	 * <p>
	 * 只有参数明确了自动设置看板展示主题才执行，因为看板展示主题应由制作者决定，不应随当前系统主题而改变
	 * </p>
	 * 
	 * @param request
	 * @param theme 参数中的主题名，可能为{@code null}
	 * @return
	 */
	protected boolean isDashboardThemeAuto(HttpServletRequest request, String theme)
	{
		return DASHBOARD_SHOW_PARAM_VALUE_AUTO_THEME.equalsIgnoreCase(theme);
	}
	
	/**
	 * 获取看板结果。
	 * 
	 * @param request
	 * @param response
	 * @param form
	 * @param renderer
	 * @return
	 */
	protected DashboardResult getDashboardResult(HttpServletRequest request, HttpServletResponse response,
			DashboardQueryForm form, HtmlTplDashboardWidgetRenderer renderer)
	{
		if (StringUtil.isEmpty(form.getDashboardId()))
			throw new IllegalInputException();

		DashboardInfo dashboardInfo = getSessionDashboardInfoSupport().getDashboardInfo(request,
				form.getDashboardId());

		if (dashboardInfo == null)
			throw new IllegalInputException();

		DashboardQuery dashboardQuery = form.getDashboardQuery();
		Map<String, HtmlChartWidget> chartWidgets = getChartWidgets(dashboardInfo, form.getDashboardQuery(), renderer);

		DashboardQuery queriesConverted = convertDashboardQuery(dashboardQuery, chartWidgets, getCurrentUser());

		SimpleDashboardQueryHandler dqh = new SimpleDashboardQueryHandler(chartWidgets);

		return dqh.getResult(queriesConverted);
	}

	/**
	 * 获取【图表ID-图表部件】映射表。
	 * 
	 * @param dashboardInfo
	 * @param query
	 * @param renderer
	 * @return
	 */
	protected Map<String, HtmlChartWidget> getChartWidgets(DashboardInfo dashboardInfo,DashboardQuery query,
			HtmlTplDashboardWidgetRenderer renderer)
	{
		Map<String, ChartQuery> chartQueries = query.getChartQueries();

		Map<String, HtmlChartWidget> chartWidgets = new HashMap<String, HtmlChartWidget>(chartQueries.size());

		for (String chartId : chartQueries.keySet())
		{
			String chartWidgetId = dashboardInfo.getChartWidgetId(chartId);
			HtmlChartWidget chartWidget = renderer.getHtmlChartWidget(chartWidgetId);

			// 忽略未找到的ChartWidget
			if (chartWidget != null)
				chartWidgets.put(chartId, chartWidget);
		}

		return chartWidgets;
	}

	protected DashboardQuery convertDashboardQuery(DashboardQuery query,
			Map<String, ? extends ChartWidget> chartWidgets, User user)
	{
		return getWebDashboardQueryConverter().convert(query, chartWidgets, user);
	}

	protected void addFetchDataUrlValue(HttpServletRequest request, RenderContext renderContext, String fetchDataURL)
	{
		renderContext.put(RenderContextAttrs.FETCH_DATA_URL, fetchDataURL);
	}

	protected void addLoadChartUrlValue(HttpServletRequest request, RenderContext renderContext, String loadChartURL)
	{
		renderContext.put(RenderContextAttrs.LOAD_CHART_URL, loadChartURL);
	}

	protected void addHeartBeatUrlValue(HttpServletRequest request, RenderContext renderContext, String heartbeatURL)
	{
		renderContext.put(RenderContextAttrs.HEARTBEAT_URL, heartbeatURL);
	}

	protected void addUnloadUrlValue(HttpServletRequest request, RenderContext renderContext, String unloadURL)
	{
		renderContext.put(RenderContextAttrs.UNLOAD_URL, unloadURL);
	}

	protected void addPluginResUrlPrefixValue(HttpServletRequest request, RenderContext renderContext,
			String pluginResUrlPrefix)
	{
		renderContext.put(RenderContextAttrs.PLUGIN_RES_URL_PREFIX, pluginResUrlPrefix);
	}

	/**
	 * 解析插件资源路径前缀。
	 * 
	 * @param request
	 * @return
	 */
	protected String resolvePluginResPathPrefix(HttpServletRequest request)
	{
		return "/vres/plugin/resource";
	}

	/**
	 * 看板查询表单。
	 *
	 */
	public static class DashboardQueryForm
	{
		/**更新数据的看板ID*/
		private String dashboardId;
		
		/** 看板查询 */
		private DashboardQuery dashboardQuery;

		public DashboardQueryForm()
		{
			super();
		}

		public String getDashboardId()
		{
			return dashboardId;
		}

		public void setDashboardId(String dashboardId)
		{
			this.dashboardId = dashboardId;
		}

		public DashboardQuery getDashboardQuery()
		{
			return dashboardQuery;
		}

		public void setDashboardQuery(DashboardQuery dashboardQuery)
		{
			this.dashboardQuery = dashboardQuery;
		}
	}
}
