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

package org.datagear.web.controller;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.analysis.Chart;
import org.datagear.analysis.ChartDataSet;
import org.datagear.analysis.ChartDefinition;
import org.datagear.analysis.ChartQuery;
import org.datagear.analysis.ChartTheme;
import org.datagear.analysis.DashboardQuery;
import org.datagear.analysis.DashboardResult;
import org.datagear.analysis.DashboardTheme;
import org.datagear.analysis.DashboardThemeSource;
import org.datagear.analysis.DataSet;
import org.datagear.analysis.DataSetQuery;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.SimpleDashboardQueryHandler;
import org.datagear.analysis.support.ChartWidget;
import org.datagear.analysis.support.DataSetParamValueConverter;
import org.datagear.analysis.support.html.HtmlChartWidget;
import org.datagear.analysis.support.html.HtmlTitleHandler;
import org.datagear.analysis.support.html.HtmlTplDashboard;
import org.datagear.analysis.support.html.HtmlTplDashboardImport;
import org.datagear.analysis.support.html.HtmlTplDashboardRenderContext;
import org.datagear.analysis.support.html.HtmlTplDashboardWidgetRenderer;
import org.datagear.analysis.support.html.LoadableChartWidgets;
import org.datagear.management.domain.Role;
import org.datagear.management.domain.User;
import org.datagear.util.StringUtil;
import org.datagear.web.util.ExpiredSessionAttrManager;
import org.datagear.web.util.ExpiredSessionAttrManager.ExpiredSessionAttr;
import org.datagear.web.util.HtmlTplDashboardImportResolver;
import org.datagear.web.util.SessionIdParamResolver;
import org.datagear.web.util.ThemeSpec;
import org.datagear.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 抽象数据分析控制器。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractDataAnalysisController extends AbstractController
{
	/**
	 * 看板内置渲染上下文属性名前缀。
	 * <p>
	 * 由于看板展示URL的请求参数会添加至渲染上下文属性中，为了避免名字冲突，所有内置属性名都应采用此前缀。
	 * </p>
	 */
	public static final String DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_PREFIX = ChartDefinition.BUILTIN_ATTR_PREFIX;

	/**
	 * 看板内置渲染上下文属性名。
	 * <p>
	 * 注意：谨慎重构此常量值，因为它可能已被用于系统已创建的看板中，重构它将导致这些看板展示页面出错。
	 * </p>
	 */
	public static final String DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_WEB_CONTEXT = DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_PREFIX
			+ "WEB_CONTEXT";

	/**
	 * 看板内置渲染上下文属性名。
	 * <p>
	 * 注意：谨慎重构此常量值，因为它可能已被用于系统已创建的看板中，重构它将导致这些看板展示页面出错。
	 * </p>
	 */
	public static final String DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_DASHBOARD_THEME = DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_PREFIX
			+ "DASHBOARD_THEME";

	/**
	 * 看板内置渲染上下文属性名：{@linkplain ChartTheme}。
	 * <p>
	 * 注意：谨慎重构此常量值，因为它可能已被用于系统已创建的看板中，重构它将导致这些看板展示页面出错。
	 * </p>
	 */
	public static final String DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_CHART_THEME = DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_PREFIX
			+ "CHART_THEME";

	/**
	 * 看板内置渲染上下文属性名：{@linkplain User}。
	 * <p>
	 * 注意：谨慎重构此常量值，因为它可能已被用于系统已创建的看板中，重构它将导致这些看板展示页面出错。
	 * </p>
	 */
	public static final String DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_USER = DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_PREFIX
			+ "USER";

	/**
	 * 看板内置渲染上下文属性名。
	 */
	public static final String DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_LOCALE = DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_PREFIX
			+ "LOCALE";

	/**
	 * 看板展示URL的请求参数名：系统主题。
	 */
	public static final String DASHBOARD_SHOW_PARAM_THEME_NAME = DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_PREFIX + "THEME";

	/**
	 * 看板展示URL的请求参数值：自动设置系统主题。
	 */
	public static final String DASHBOARD_SHOW_PARAM_VALUE_AUTO_THEME = "auto";

	/**
	 * 看板展示URL的请求参数名：编辑模板。仅用于可视化编辑看板模板功能。
	 */
	public static final String DASHBOARD_SHOW_PARAM_EDIT_TEMPLATE = DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_PREFIX
			+ "EDIT_TEMPLATE";

	/**
	 * 看板展示URL的请求参数名：自定义模板内容。仅用于可视化编辑看板模板功能。
	 */
	public static final String DASHBOARD_SHOW_PARAM_TEMPLATE_CONTENT = DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_PREFIX
			+ "TEMPLATE_CONTENT";

	/** 看板展示页{@linkplain WebContext}属性名：更新数据URL名 */
	public static final String DASHBOARD_UPDATE_URL_NAME = "updateDashboardURL";

	/** 看板展示页{@linkplain WebContext}属性名：加载图表URL名 */
	public static final String DASHBOARD_LOAD_CHART_URL_NAME = "loadChartURL";

	/** 看板展示页{@linkplain WebContext}属性名：心跳URL名 */
	public static final String DASHBOARD_HEARTBEAT_URL_NAME = "heartbeatURL";

	/** 看板展示页{@linkplain WebContext}属性名：会话名 */
	public static final String DASHBOARD_SESSION_NAME_NAME = "sessionName";

	/** 看板展示页{@linkplain WebContext}属性名：会话值 */
	public static final String DASHBOARD_SESSION_VALUE_NAME = "sessionValue";

	/** 看板心跳URL后缀 */
	public static final String HEARTBEAT_TAIL_URL = "/heartbeat";

	/** 看板心跳频率：5分钟 */
	public static final long HEARTBEAT_INTERVAL_MS = 1000 * 60 * 5;

	/**
	 * 看板展示URL的请求参数名：启用安全会话。
	 * <p>
	 * 当看板嵌入不同源的iframe时，cookie会被浏览器禁用，会导致无法保持会话，需要设置此参数启用安全会话。
	 * </p>
	 * <p>
	 * 注意：这里的启用安全会话功能无法支持看板展示页面内的链接，如果需要这些链接支持安全会话，
	 * 需要定义看板时使用JS为这些链接添加启用安全会话参数、会话信息参数（可通过{@linkplain #DASHBOARD_SESSION_NAME_NAME}、{@linkplain #DASHBOARD_SESSION_VALUE_NAME}获取会话信息），示例：
	 * </p>
	 * <p>
	 * <code>
	 * &lt;a href="content.html;jsessionid=xxxxx?DG_SAFE_SESSION=1"&gt;看板超链接&lt;/a&gt;
	 * <br>
	 * &lt;script src="res/common.js;jsessionid=xxxxx"&gt;&lt;/script&gt;
	 * </code>
	 * </p>
	 */
	public static final String DASHBOARD_SHOW_PARAM_SAFE_SESSION = DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_PREFIX
			+ "SAFE_SESSION";

	/**
	 * {@linkplain #DASHBOARD_SHOW_PARAM_SAFE_SESSION}的参数值规范：{@code 1}。
	 */
	public static final String DASHBOARD_SHOW_PARAM_SAFE_SESSION_VALUE_1 = "1";

	/**
	 * {@linkplain #DASHBOARD_SHOW_PARAM_SAFE_SESSION}的参数值规范：{@code true}。
	 */
	public static final String DASHBOARD_SHOW_PARAM_SAFE_SESSION_VALUE_TRUE = "true";

	/**
	 * {@linkplain #DASHBOARD_SHOW_PARAM_SAFE_SESSION}的参数值规范：{@code 0}。
	 */
	public static final String DASHBOARD_SHOW_PARAM_SAFE_SESSION_VALUE_0 = "0";

	/**
	 * {@linkplain #DASHBOARD_SHOW_PARAM_SAFE_SESSION}的参数值规范：{@code false}。
	 */
	public static final String DASHBOARD_SHOW_PARAM_SAFE_SESSION_VALUE_FALSE = "false";

	@Autowired
	private DataSetParamValueConverter dataSetParamValueConverter;

	@Autowired
	private DashboardThemeSource dashboardThemeSource;
	
	@Autowired
	private HtmlTplDashboardImportResolver htmlTplDashboardImportResolver;
	
	@Autowired
	private ThemeSpec themeSpec;

	@Autowired
	private ExpiredSessionAttrManager expiredSessionAttrManager;

	@Autowired
	private SessionIdParamResolver sessionIdParamResolver;

	public AbstractDataAnalysisController()
	{
		super();
	}

	public DataSetParamValueConverter getDataSetParamValueConverter()
	{
		return dataSetParamValueConverter;
	}

	public void setDataSetParamValueConverter(DataSetParamValueConverter dataSetParamValueConverter)
	{
		this.dataSetParamValueConverter = dataSetParamValueConverter;
	}

	public DashboardThemeSource getDashboardThemeSource()
	{
		return dashboardThemeSource;
	}

	public void setDashboardThemeSource(DashboardThemeSource dashboardThemeSource)
	{
		this.dashboardThemeSource = dashboardThemeSource;
	}

	public HtmlTplDashboardImportResolver getHtmlTplDashboardImportResolver()
	{
		return htmlTplDashboardImportResolver;
	}

	public void setHtmlTplDashboardImportResolver(HtmlTplDashboardImportResolver htmlTplDashboardImportResolver)
	{
		this.htmlTplDashboardImportResolver = htmlTplDashboardImportResolver;
	}

	public ThemeSpec getThemeSpec()
	{
		return themeSpec;
	}

	public void setThemeSpec(ThemeSpec themeSpec)
	{
		this.themeSpec = themeSpec;
	}

	public ExpiredSessionAttrManager getExpiredSessionAttrManager()
	{
		return expiredSessionAttrManager;
	}

	public void setExpiredSessionAttrManager(ExpiredSessionAttrManager expiredSessionAttrManager)
	{
		this.expiredSessionAttrManager = expiredSessionAttrManager;
	}

	public SessionIdParamResolver getSessionIdParamResolver()
	{
		return sessionIdParamResolver;
	}

	public void setSessionIdParamResolver(SessionIdParamResolver sessionIdParamResolver)
	{
		this.sessionIdParamResolver = sessionIdParamResolver;
	}

	protected HtmlTplDashboardRenderContext createRenderContext(HttpServletRequest request, HttpServletResponse response,
			String template, Writer responseWriter, WebContext webContext, List<HtmlTplDashboardImport> importList,
			HtmlTitleHandler htmlTitleHandler) throws IOException
	{
		HtmlTplDashboardRenderContext renderContext = new HtmlTplDashboardRenderContext(template, responseWriter);
		
		Map<String, ?> paramValues = resolveDashboardShowParamValues(request);
		DashboardTheme dashboardTheme = resolveDashboardTheme(request);
		AnalysisUser analysisUser = AnalysisUser.valueOf(getCurrentUser().cloneNoPassword());
		
		renderContext.putAttributes(paramValues);
		renderContext.setAttribute(DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_WEB_CONTEXT, webContext);
		renderContext.setAttribute(DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_DASHBOARD_THEME, dashboardTheme);
		renderContext.setAttribute(DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_USER, analysisUser);
		
		renderContext.setImportList(importList);
		renderContext.setDashboardTheme(dashboardTheme);
		renderContext.setHtmlTitleHandler(htmlTitleHandler);

		return renderContext;
	}

	/**
	 * 构建看板导入列表：展示。
	 * 
	 * @param request
	 * @return
	 */
	protected List<HtmlTplDashboardImport> buildHtmlTplDashboardImportsForShow(HttpServletRequest request)
	{
		return this.htmlTplDashboardImportResolver.resolve(request, HtmlTplDashboardImportResolver.MODE_SHOW);
	}

	/**
	 * 构建看板导入列表：可视编辑。
	 * 
	 * @param request
	 * @return
	 */
	protected List<HtmlTplDashboardImport> buildHtmlTplDashboardImportsForEdit(HttpServletRequest request)
	{
		return this.htmlTplDashboardImportResolver.resolve(request, HtmlTplDashboardImportResolver.MODE_EDIT);
	}

	/**
	 * 创建初始{@linkplain WebContext}。
	 * 
	 * @param request
	 * @return
	 */
	protected WebContext createInitWebContext(HttpServletRequest request)
	{
		WebContext webContext = new WebContext(WebUtils.getContextPath(request));

		// 如果是启用安全会话请求，则将会话信息返回给前端，前端需要构建安全会话链接时可能需要
		if (isSafeSessionRequest(request))
		{
			webContext.addAttribute(DASHBOARD_SESSION_NAME_NAME, this.sessionIdParamResolver.getSessionIdParamName());
			webContext.addAttribute(DASHBOARD_SESSION_VALUE_NAME, this.sessionIdParamResolver.getSessionId(request));
		}

		return webContext;
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
	protected Map<String, String> escapeDashboardRenderContextAttrValue(Map<String, String> value)
	{
		if (value == null || value.isEmpty())
			return value;

		Map<String, String> re = new HashMap<String, String>(value.size());

		for (Map.Entry<String, String> entry : value.entrySet())
			re.put(entry.getKey(), escapeDashboardRenderContextAttrValue(entry.getValue()));

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
	protected String escapeDashboardRenderContextAttrValue(String value)
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

		DashboardInfo dashboardInfo = getSessionDashboardInfo(request, form.getDashboardId());

		if (dashboardInfo == null)
			throw new IllegalInputException();

		AnalysisUser analysisUser = AnalysisUser.valueOf(getCurrentUser());

		DashboardQuery dashboardQuery = form.getDashboardQuery();
		Map<String, HtmlChartWidget> chartWidgets = getChartWidgets(form.getDashboardQuery(), dashboardInfo, renderer);

		DashboardQuery queriesConverted = convertDashboardQuery(dashboardQuery, chartWidgets, analysisUser);

		SimpleDashboardQueryHandler dqh = new SimpleDashboardQueryHandler(chartWidgets);

		return dqh.getResult(queriesConverted);
	}

	/**
	 * 获取【图表ID-图表部件】映射表。
	 * 
	 * @param query
	 * @param dashboardInfo
	 * @param renderer
	 * @return
	 */
	protected Map<String, HtmlChartWidget> getChartWidgets(DashboardQuery query, DashboardInfo dashboardInfo,
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
			Map<String, ? extends ChartWidget> chartWidgets, AnalysisUser analysisUser)
	{
		if (query == null)
			return new DashboardQuery();
		
		List<String> analysisRoleNames = analysisUser.getEnabledRoleNames();

		Map<String, ChartQuery> chartQueries = query.getChartQueries();
		Map<String, ChartQuery> chartQueriesRe = new HashMap<String, ChartQuery>(chartQueries.size());

		for (Map.Entry<String, ChartQuery> entry : chartQueries.entrySet())
		{
			String chartId = entry.getKey();
			ChartQuery chartQuery = entry.getValue();
			ChartWidget chartWidget = chartWidgets.get(chartId);

			// 忽略未找到的ChartWidget
			if (chartWidget != null)
			{
				ChartDataSet[] chartDataSets = chartWidget.getChartDataSets();
				List<DataSetQuery> dataSetQueries = chartQuery.getDataSetQueries();

				ChartQuery chartQueryRe = null;

				if (chartDataSets == null || chartDataSets.length == 0 || dataSetQueries == null
						|| dataSetQueries.isEmpty())
				{
					chartQueryRe = chartQuery;
				}
				else
				{
					List<DataSetQuery> dataSetQueriesRe = new ArrayList<DataSetQuery>(dataSetQueries.size());

					for (int j = 0; j < chartDataSets.length; j++)
					{
						DataSetQuery dataSetQueryRe = dataSetQueries.get(j);
						dataSetQueryRe = getDataSetParamValueConverter().convert(dataSetQueryRe,
								chartDataSets[j].getDataSet(), true);
						analysisUser.setParamValue(dataSetQueryRe, analysisRoleNames);

						dataSetQueriesRe.add(dataSetQueryRe);
					}

					chartQueryRe = chartQuery.copy();
					chartQueryRe.setDataSetQueries(dataSetQueriesRe);
				}

				chartQueriesRe.put(chartId, chartQueryRe);
			}
		}
		
		DashboardQuery queryRe = query.copy();
		queryRe.setChartQueries(chartQueriesRe);

		return queryRe;
	}

	protected void addHeartBeatValue(HttpServletRequest request, WebContext webContext)
	{
		String heartbeatURL = "/dashboard" + HEARTBEAT_TAIL_URL;
		heartbeatURL = addSessionIdParamIfNotExplicitDisable(heartbeatURL, request);

		webContext.addAttribute(DASHBOARD_HEARTBEAT_URL_NAME, heartbeatURL);
	}

	/**
	 * 如果是启用安全会话请求，则为URL添加会话ID参数；否则，直接返回{@code url}。
	 * 
	 * @param url
	 * @param request
	 * @return
	 */
	protected String addSessionIdParamIfNeed(String url, HttpServletRequest request)
	{
		if (isSafeSessionRequest(request))
		{
			return addSessionIdParam(url, request);
		}
		else
			return url;
	}

	/**
	 * 如果没有明确禁用安全会话请求，则为URL添加会话ID参数；否则，直接返回{@code url}。
	 * <p>
	 * {@linkplain #DASHBOARD_SHOW_PARAM_SAFE_SESSION}是在4.7.0版本新加的特性，
	 * 在之前版本，为了iframe嵌入，某些URL默认添加了会话ID参数，为了兼容，应仅在明确禁用安全会话时才移除这些URL中的会话ID参数。
	 * </p>
	 * 
	 * @param url
	 * @param request
	 * @return
	 */
	protected String addSessionIdParamIfNotExplicitDisable(String url, HttpServletRequest request)
	{
		if (!isExplicitDisableSafeSessionRequest((request)))
		{
			return addSessionIdParam(url, request);
		}
		else
			return url;
	}

	/**
	 * 如果是启用安全会话请求，则为URL添加{@linkplain #DASHBOARD_SHOW_PARAM_SAFE_SESSION}参数；否则，直接返回{@code url}。
	 * 
	 * @param url
	 * @param request
	 * @return
	 */
	protected String addSafeSessionParamIfNeed(String url, HttpServletRequest request)
	{
		if (isSafeSessionRequest(request))
			return WebUtils.addUrlParam(url, DASHBOARD_SHOW_PARAM_SAFE_SESSION, DASHBOARD_SHOW_PARAM_SAFE_SESSION_VALUE_TRUE);
		else
			return url;
	}

	/**
	 * 是否是启用安全会话请求。
	 * 
	 * @param request
	 * @return
	 */
	protected boolean isSafeSessionRequest(HttpServletRequest request)
	{
		String value = request.getParameter(DASHBOARD_SHOW_PARAM_SAFE_SESSION);

		if (value == null)
			return false;

		return (DASHBOARD_SHOW_PARAM_SAFE_SESSION_VALUE_1.equals(value)
				|| DASHBOARD_SHOW_PARAM_SAFE_SESSION_VALUE_TRUE.equalsIgnoreCase(value));
	}

	/**
	 * 是否是明确禁用安全会话请求。
	 * 
	 * @param request
	 * @return
	 */
	protected boolean isExplicitDisableSafeSessionRequest(HttpServletRequest request)
	{
		String value = request.getParameter(DASHBOARD_SHOW_PARAM_SAFE_SESSION);

		if (value == null)
			return false;

		return (DASHBOARD_SHOW_PARAM_SAFE_SESSION_VALUE_0.equals(value)
				|| DASHBOARD_SHOW_PARAM_SAFE_SESSION_VALUE_FALSE.equalsIgnoreCase(value));
	}

	/**
	 * 为指定URL添加会话ID参数。
	 * <p>
	 * 图表、看板展示页可能会以&lt;iframe&gt;的方式嵌入外部网页中，当在跨域场景时，某些浏览器会禁用&lt;iframe&gt;内的cookie，导致会话无法保持，
	 * 从而引起图表、看板内的ajax请求失效，此方法可以解决上述问题。
	 * </p>
	 * 
	 * @param url
	 * @param sessionId
	 * @return
	 */
	protected String addSessionIdParam(String url, String sessionId)
	{
		return this.sessionIdParamResolver.addSessionId(url, sessionId);
	}

	/**
	 * 为指定URL添加会话参数。
	 * 
	 * @param url
	 * @param session
	 * @return
	 */
	protected String addSessionIdParam(String url, HttpServletRequest request)
	{
		return this.sessionIdParamResolver.addSessionId(url, request);
	}

	/**
	 * 在会话中存储{@linkplain DashboardInfo}。
	 * 
	 * @param request
	 * @param di
	 */
	protected void setSessionDashboardInfo(HttpServletRequest request, DashboardInfo di)
	{
		String name = toSessionNameForDashboardInfo(request, di.getDashboardId());
		getExpiredSessionAttrManager().setAttr(request, name, di);
	}

	/**
	 * 获取会话中存储的{@linkplain DashboardInfo}。
	 * 
	 * @param request
	 * @param dashboardId
	 * @return 返回{@code null}表示没有
	 */
	protected DashboardInfo getSessionDashboardInfo(HttpServletRequest request, String dashboardId)
	{
		String name = toSessionNameForDashboardInfo(request, dashboardId);
		return getExpiredSessionAttrManager().getAttr(request, name);
	}

	/**
	 * 为会话中的{@linkplain DashboardInfo}添加图表信息。
	 * 
	 * @param request
	 * @param di
	 * @param chartIdToChartWidgetIds
	 */
	protected void addSessionDashboardInfoCharts(HttpServletRequest request, DashboardInfo di,
			Map<String, String> chartIdToChartWidgetIds)
	{
		di.putChartWidgetIds(chartIdToChartWidgetIds);

		// 无论di是否已存储至会话，这里都应再执行存储，以为可能扩展的分布式会话提供支持
		String name = toSessionNameForDashboardInfo(request, di.getDashboardId());
		getExpiredSessionAttrManager().setAttr(request, name, di);
	}

	/**
	 * 更新会话中的{@linkplain DashboardInfo}的访问时间。
	 * 
	 * @param request
	 * @param dashboardId
	 * @param time
	 */
	protected void updateSessionDashboardInfoAccess(HttpServletRequest request, String dashboardId, long time)
	{
		String name = toSessionNameForDashboardInfo(request, dashboardId);
		getExpiredSessionAttrManager().updateAccessTime(request, name, time);
	}

	/**
	 * 移除会话中过期的看板信息。
	 * <p>
	 * 需定时清理，防止会话中存储过多已过期的看板信息
	 * </p>
	 * 
	 * @param request
	 */
	protected void removeSessionDashboardInfoExpired(HttpServletRequest request)
	{
		getExpiredSessionAttrManager().removeExpired(request, HEARTBEAT_INTERVAL_MS * 3, DashboardInfo.class);
	}

	protected String toSessionNameForDashboardInfo(HttpServletRequest request, String dashboardId)
	{
		return DashboardInfo.class.getSimpleName() + "-" + dashboardId;
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

	protected static class DashboardInfo implements ExpiredSessionAttr
	{
		private static final long serialVersionUID = 1L;

		/**看板ID*/
		private final String dashboardId;
		
		/**看板部件ID*/
		private final String dashboardWidgetId;

		private final LoadableChartWidgets loadableChartWidgets;
		
		/** 图表ID-图表部件ID映射表 */
		private final Map<String, String> chartIdToChartWidgetIds = new HashMap<String, String>();
		
		private final boolean showForEdit;

		private volatile long lastAccessTime = 0;

		public DashboardInfo(HtmlTplDashboard dashboard, boolean showForEdit)
		{
			this.dashboardId = dashboard.getId();
			this.dashboardWidgetId = dashboard.getWidget().getId();
			this.loadableChartWidgets = dashboard.getLoadableChartWidgets();
			
			if (dashboard.hasChart())
			{
				List<Chart> charts = dashboard.getCharts();
				for (Chart chart : charts)
					this.chartIdToChartWidgetIds.put(chart.getId(), ChartWidget.getChartWidgetId(chart));
			}

			this.showForEdit = showForEdit;
			this.lastAccessTime = System.currentTimeMillis();
		}

		public String getDashboardId()
		{
			return dashboardId;
		}

		public String getDashboardWidgetId()
		{
			return dashboardWidgetId;
		}
		
		/**
		 * @return 可能为{@code null}
		 */
		public LoadableChartWidgets getLoadableChartWidgets()
		{
			return loadableChartWidgets;
		}

		public synchronized Map<String, String> getChartIdToChartWidgetIds()
		{
			return Collections.unmodifiableMap(chartIdToChartWidgetIds);
		}

		public boolean isShowForEdit()
		{
			return showForEdit;
		}

		public synchronized String getChartWidgetId(String chartId)
		{
			return this.chartIdToChartWidgetIds.get(chartId);
		}

		public synchronized void putChartWidgetId(String chartId, String chartWidgetId)
		{
			this.chartIdToChartWidgetIds.put(chartId, chartWidgetId);
		}

		public synchronized void putChartWidgetIds(Map<String, String> chartIdToChartWidgetIds)
		{
			this.chartIdToChartWidgetIds.putAll(chartIdToChartWidgetIds);
		}

		@Override
		public long getLastAccessTime()
		{
			return this.lastAccessTime;
		}

		@Override
		public void setLastAccessTime(long time)
		{
			this.lastAccessTime = time;
		}
	}

	/**
	 * 数据分析用户。
	 * <p>
	 * 看板展示页面渲染上下文中的当前用户。
	 * </p>
	 * <p>
	 * 这里不直接使用{@linkplain User}，因为数据分析用户不应因{@linkplain User}的改变而改变。
	 * </p>
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class AnalysisUser implements Serializable
	{
		private static final long serialVersionUID = 1L;

		/**
		 * 内置数据集参数：当前用户。
		 * <p>
		 * 注意：谨慎重构此常量值，因为它可能已被用于系统已创建的数据集中，重构它将导致这些数据集执行出错。
		 * </p>
		 */
		public static final String DATA_SET_PARAM_NAME_CURRENT_USER = DataSetQuery.BUILTIN_PARAM_PREFIX + "USER";

		/**
		 * 内置数据集参数：当前角色名集。
		 * <p>
		 * 在数据集的参数化语境内，虽然可以通过{@code DG_USERS.ROLES}获取角色名集，但是语法较为繁琐，
		 * 考虑到角色名集可能使用较频繁，所以单独定义。
		 * </p>
		 * <p>
		 * 注意：谨慎重构此常量值，因为它可能已被用于系统已创建的数据集中，重构它将导致这些数据集执行出错。
		 * </p>
		 */
		public static final String DATA_SET_PARAM_NAME_CURRENT_ROLE_NAMES = DataSetQuery.BUILTIN_PARAM_PREFIX
				+ "ROLE_NAMES";

		/** ID */
		private String id;

		/** 用户名 */
		private String name;

		/** 姓名 */
		private String realName;

		/** 是否管理员 */
		private boolean admin = false;

		/** 是否是匿名用户 */
		private boolean anonymous = false;

		/** 角色集 */
		private List<AnalysisRole> roles = Collections.emptyList();

		public AnalysisUser(String id, String name, String realName, boolean admin, boolean anonymous,
				List<AnalysisRole> roles)
		{
			super();
			this.id = id;
			this.name = name;
			this.realName = realName;
			this.admin = admin;
			this.anonymous = anonymous;
			this.roles = roles;
		}

		public AnalysisUser(User user)
		{
			this(user.getId(), user.getName(), user.getRealName(), user.isAdmin(), user.isAnonymous(),
					AnalysisRole.valueOf(user.getRoles()));
		}

		public String getId()
		{
			return id;
		}

		public void setId(String id)
		{
			this.id = id;
		}

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		public String getRealName()
		{
			return realName;
		}

		public void setRealName(String realName)
		{
			this.realName = realName;
		}

		public boolean isAdmin()
		{
			return admin;
		}

		public void setAdmin(boolean admin)
		{
			this.admin = admin;
		}

		public boolean isAnonymous()
		{
			return anonymous;
		}

		public void setAnonymous(boolean anonymous)
		{
			this.anonymous = anonymous;
		}

		public List<AnalysisRole> getRoles()
		{
			return roles;
		}

		public void setRoles(List<AnalysisRole> roles)
		{
			this.roles = roles;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((id == null) ? 0 : id.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			AnalysisUser other = (AnalysisUser) obj;
			if (id == null)
			{
				if (other.id != null)
					return false;
			}
			else if (!id.equals(other.id))
				return false;
			return true;
		}

		@Override
		public String toString()
		{
			return getClass().getSimpleName() + " [id=" + id + ", name=" + name + ", realName=" + realName + ", admin="
					+ admin + ", anonymous=" + anonymous + ", roles=" + roles + "]";
		}

		/**
		 * 将此{@linkplain AnalysisUser}以{@linkplain #DATA_SET_PARAM_NAME_CURRENT_USER}名、
		 * {@linkplain #getEnabledRoleNames(AnalysisUser)}以{@linkplain #DATA_SET_PARAM_NAME_CURRENT_ROLE_NAMES}
		 * 名加入{@linkplain DataSetQuery#getParamValues()}。
		 * <p>
		 * 使得参数化数据集{@linkplain DataSet#getResult(DataSetQuery)}可支持根据当前数据分析用户、角色返回不同的数据。
		 * </p>
		 * 
		 * @param dataSetQuery
		 */
		public void setParamValue(DataSetQuery dataSetQuery)
		{
			setParamValue(dataSetQuery, getEnabledRoleNames());
		}

		/**
		 * 将此{@linkplain AnalysisUser}以{@linkplain #DATA_SET_PARAM_NAME_CURRENT_USER}名、
		 * {@code analysisRoleNames}以{@linkplain #DATA_SET_PARAM_NAME_CURRENT_ROLE_NAMES}
		 * 名加入{@linkplain DataSetQuery#getParamValues()}。
		 * <p>
		 * 使得参数化数据集{@linkplain DataSet#getResult(DataSetQuery)}可支持根据当前数据分析用户、角色返回不同的数据。
		 * </p>
		 * 
		 * @param dataSetQuery
		 * @param analysisRoleNames
		 */
		public void setParamValue(DataSetQuery dataSetQuery, List<String> analysisRoleNames)
		{
			if (analysisRoleNames == null)
				throw new IllegalArgumentException("[analysisRoleNames] required");

			dataSetQuery.setParamValue(DATA_SET_PARAM_NAME_CURRENT_USER, this);
			dataSetQuery.setParamValue(DATA_SET_PARAM_NAME_CURRENT_ROLE_NAMES, analysisRoleNames);
		}

		/**
		 * 获取{@linkplain AnalysisUser#getRoles()}列表中已启用的{@linkplain AnalysisRole#getName()}列表。
		 * 
		 * @return 不会为{@code null}
		 */
		public List<String> getEnabledRoleNames()
		{
			List<String> roleNames = new ArrayList<String>();

			if (this.roles != null)
			{
				for (AnalysisRole role : this.roles)
				{
					if (role.isEnabled())
						roleNames.add(role.getName());
				}
			}

			return roleNames;
		}

		/**
		 * 构建{@linkplain AnalysisUser}。
		 * 
		 * @param user
		 * @return
		 */
		public static AnalysisUser valueOf(User user)
		{
			return new AnalysisUser(user);
		}
	}

	/**
	 * 数据分析角色。
	 * <p>
	 * 这里不直接使用{@linkplain Role}，因为数据分析角色不应因{@linkplain Role}的改变而改变。
	 * </p>
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class AnalysisRole implements Serializable
	{
		private static final long serialVersionUID = 1L;

		/** ID */
		private String id;

		/** 名称 */
		private String name;

		/** 是否启用 */
		private boolean enabled = true;

		public AnalysisRole(String id, String name, boolean enabled)
		{
			super();
			this.id = id;
			this.name = name;
			this.enabled = enabled;
		}

		public AnalysisRole(Role role)
		{
			this(role.getId(), role.getName(), role.isEnabled());
		}

		public String getId()
		{
			return id;
		}

		public void setId(String id)
		{
			this.id = id;
		}

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		public boolean isEnabled()
		{
			return enabled;
		}

		public void setEnabled(boolean enabled)
		{
			this.enabled = enabled;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((id == null) ? 0 : id.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			AnalysisRole other = (AnalysisRole) obj;
			if (id == null)
			{
				if (other.id != null)
					return false;
			}
			else if (!id.equals(other.id))
				return false;
			return true;
		}

		@Override
		public String toString()
		{
			return getClass().getSimpleName() + " [id=" + id + ", name=" + name + ", enabled=" + enabled + "]";
		}

		/**
		 * 构建{@linkplain AnalysisRole}列表。
		 * 
		 * @param roles 允许为{@code null}
		 * @return 不会为{@code null}
		 */
		public static List<AnalysisRole> valueOf(Collection<Role> roles)
		{
			if (roles == null)
				return Collections.emptyList();

			List<AnalysisRole> analysisRoles = new ArrayList<AnalysisRole>(roles.size());

			for (Role role : roles)
				analysisRoles.add(new AnalysisRole(role));

			return analysisRoles;
		}
	}
	
	/**
	 * Web上下文信息。
	 * <p>
	 * 这些信息将输出至客户端，提供看板交互支持。
	 * </p>
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class WebContext
	{
		/** 上下文路径 */
		private String contextPath;

		/** Web属性集 */
		private Map<String, ?> attributes = new HashMap<String, Object>();

		public WebContext()
		{
			super();
		}

		public WebContext(String contextPath)
		{
			super();
			this.contextPath = contextPath;
		}

		public String getContextPath()
		{
			return contextPath;
		}

		public void setContextPath(String contextPath)
		{
			this.contextPath = contextPath;
		}

		public Map<String, ?> getAttributes()
		{
			return attributes;
		}

		public void setAttributes(Map<String, ?> attributes)
		{
			this.attributes = attributes;
		}

		@SuppressWarnings("unchecked")
		public void addAttribute(String name, Object value)
		{
			((Map<String, Object>) this.attributes).put(name, value);
		}

		@Override
		public String toString()
		{
			return getClass().getSimpleName() + " [contextPath=" + contextPath + ", attributes=" + attributes + "]";
		}
	}
}
