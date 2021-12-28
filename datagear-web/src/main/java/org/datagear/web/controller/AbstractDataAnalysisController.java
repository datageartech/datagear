/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.controller;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.datagear.analysis.Chart;
import org.datagear.analysis.ChartDataSet;
import org.datagear.analysis.ChartQuery;
import org.datagear.analysis.ChartTheme;
import org.datagear.analysis.Dashboard;
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
import org.datagear.analysis.support.DefaultRenderContext;
import org.datagear.analysis.support.SimpleDashboardThemeSource;
import org.datagear.analysis.support.html.HtmlChartWidget;
import org.datagear.analysis.support.html.HtmlTplDashboardImport;
import org.datagear.analysis.support.html.HtmlTplDashboardRenderAttr;
import org.datagear.analysis.support.html.HtmlTplDashboardRenderAttr.HtmlTitleHandler;
import org.datagear.analysis.support.html.HtmlTplDashboardRenderAttr.WebContext;
import org.datagear.analysis.support.html.HtmlTplDashboardWidgetRenderer;
import org.datagear.management.domain.Role;
import org.datagear.management.domain.User;
import org.datagear.util.Global;
import org.datagear.util.StringUtil;
import org.datagear.web.util.WebUtils;

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
	public static final String DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_PREFIX = "DG_";

	/**
	 * 看板内置渲染上下文属性名：{@linkplain HtmlTplDashboardRenderAttr#setWebContextName(String)}。
	 * <p>
	 * 注意：谨慎重构此常量值，因为它可能已被用于系统已创建的看板中，重构它将导致这些看板展示页面出错。
	 * </p>
	 */
	public static final String DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_WEB_CONTEXT = DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_PREFIX
			+ "WEB_CONTEXT";

	/**
	 * 看板内置渲染上下文属性名：{@linkplain HtmlTplDashboardRenderAttr#setDashboardThemeName(String)}。
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
	 * 看板内置渲染上下文属性名：{@linkplain HtmlTplDashboardRenderAttr#setHtmlTitleHandlerName(String)}。
	 */
	public static final String DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_HTML_TITLE_HANDLER = DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_PREFIX
			+ "HTML_TITLE_HANDLER";

	/**
	 * 看板内置渲染上下文属性名：{@linkplain HtmlTplDashboardRenderAttr#setImportListName(String)}。
	 */
	public static final String DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_IMPORT_LIST = DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_PREFIX
			+ "IMPORT_LIST";

	/**
	 * 看板内置渲染上下文属性名：{@linkplain HtmlTplDashboardRenderAttr#setHtmlWriterName(String)}。
	 */
	public static final String DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_HTML_WRITER = DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_PREFIX
			+ "HTML_WRITER";

	/**
	 * 看板内置渲染上下文属性名：{@linkplain HtmlTplDashboardRenderAttr#setLocaleName(String)}。
	 */
	public static final String DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_LOCALE = DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_PREFIX
			+ "LOCALE";

	/**
	 * 看板内置渲染上下文属性名：{@linkplain HtmlTplDashboardRenderAttr#setIgnoreRenderAttrsName(String)}。
	 */
	public static final String DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_IGNORE = DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_PREFIX
			+ "IGNORE_RENDER_ATTRS_NAME";

	/**
	 * 看板展示URL的请求参数名：系统主题。
	 */
	public static final String DASHBOARD_SHOW_PARAM_THEME_NAME = DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_PREFIX + "THEME";

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

	/** 看板更新数据URL名 */
	public static final String DASHBOARD_UPDATE_URL_NAME = "updateDashboardURL";

	/** 看板加载图表URL名 */
	public static final String DASHBOARD_LOAD_CHART_URL_NAME = "loadChartURL";

	/** 看板心跳URL名 */
	public static final String DASHBOARD_HEARTBEAT_URL_NAME = "heartbeatURL";

	private DataSetParamValueConverter dataSetParamValueConverter = new DataSetParamValueConverter();

	private DashboardThemeSource dashboardThemeSource = new SimpleDashboardThemeSource();

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

	protected RenderContext createHtmlRenderContext(HttpServletRequest request, HttpServletResponse response,
			Writer responseWriter, WebContext webContext, HtmlTitleHandler htmlTitleHandler) throws IOException
	{
		RenderContext renderContext = new DefaultRenderContext(resolveDashboardShowParamValues(request));

		HtmlTplDashboardRenderAttr renderAttr = createHtmlTplDashboardRenderAttr();

		List<HtmlTplDashboardImport> importList = buildHtmlTplDashboardImports(request);
		DashboardTheme dashboardTheme = resolveDashboardTheme(request);
		User user = WebUtils.getUser(request, response).cloneNoPassword();

		inflateHtmlRenderContext(request, renderContext, renderAttr, responseWriter, webContext, dashboardTheme,
				importList, htmlTitleHandler, AnalysisUser.valueOf(user));

		return renderContext;
	}

	protected void inflateHtmlRenderContext(HttpServletRequest request, RenderContext renderContext,
			HtmlTplDashboardRenderAttr renderAttr, Writer responseWriter, WebContext webContext,
			DashboardTheme dashboardTheme, List<HtmlTplDashboardImport> importList, HtmlTitleHandler htmlTitleHandler,
			AnalysisUser analysisUser)
	{
		renderAttr.inflate(renderContext, responseWriter);
		renderAttr.setWebContext(renderContext, webContext);
		renderAttr.setImportList(renderContext, importList);
		renderAttr.setDashboardTheme(renderContext, dashboardTheme);
		renderContext.setAttribute(DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_USER, analysisUser);
		renderAttr.setHtmlTitleHandler(renderContext, htmlTitleHandler);
		renderAttr.setIgnoreRenderAttrs(renderContext,
				Arrays.asList(renderAttr.getHtmlWriterName(), renderAttr.getImportListName(),
						renderAttr.getHtmlTitleHandlerName(),
						renderAttr.getIgnoreRenderAttrsName(), HtmlTplDashboardRenderAttr.ATTR_NAME));
	}

	protected HtmlTplDashboardRenderAttr createHtmlTplDashboardRenderAttr()
	{
		HtmlTplDashboardRenderAttr renderAttr = new HtmlTplDashboardRenderAttr();
		renderAttr.setHtmlWriterName(DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_HTML_WRITER);
		renderAttr.setLocaleName(DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_LOCALE);
		renderAttr.setIgnoreRenderAttrsName(DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_IGNORE);
		renderAttr.setImportListName(DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_IMPORT_LIST);
		renderAttr.setWebContextName(DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_WEB_CONTEXT);
		renderAttr.setDashboardThemeName(DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_DASHBOARD_THEME);
		renderAttr.setHtmlTitleHandlerName(DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_HTML_TITLE_HANDLER);

		return renderAttr;
	}

	/**
	 * 构建看板导入列表。
	 * 
	 * @param request
	 * @return
	 */
	protected List<HtmlTplDashboardImport> buildHtmlTplDashboardImports(HttpServletRequest request)
	{
		List<HtmlTplDashboardImport> impts = new ArrayList<>();

		String contextPath = WebUtils.getContextPath(request);
		String rp = "rc" + Long.toHexString(System.currentTimeMillis());

		String staticPrefix = contextPath + "/static";
		String libPrefix = staticPrefix + "/lib";
		String cssPrefix = staticPrefix + "/css";
		String scriptPrefix = staticPrefix + "/script";

		// CSS
		impts.add(HtmlTplDashboardImport.valueOfLinkCss("dataTable",
						libPrefix + "/DataTables-1.11.3/css/datatables.min.css"));
		impts.add(HtmlTplDashboardImport.valueOfLinkCss("datetimepicker",
				libPrefix + "/jquery-datetimepicker-2.5.20/jquery.datetimepicker.min.css"));
		impts.add(HtmlTplDashboardImport.valueOfLinkCss("dashboardStyle",
				cssPrefix + "/analysis.css?v=" + Global.VERSION));

		// JS
		impts.add(HtmlTplDashboardImport.valueOfJavaScript("jquery", libPrefix + "/jquery-3.6.0/jquery-3.6.0.min.js"));
		impts.add(HtmlTplDashboardImport.valueOfJavaScript("echarts", libPrefix + "/echarts-5.2.2/echarts.min.js"));
		impts.add(HtmlTplDashboardImport.valueOfJavaScript("wordcloud",
				libPrefix + "/echarts-wordcloud-2.0.0/echarts-wordcloud.min.js"));
		impts.add(HtmlTplDashboardImport.valueOfJavaScript("liquidfill",
				libPrefix + "/echarts-liquidfill-3.0.0/echarts-liquidfill.min.js"));
		impts.add(HtmlTplDashboardImport.valueOfJavaScript("dataTable",
						libPrefix + "/DataTables-1.11.3/js/datatables.min.js"));
		impts.add(HtmlTplDashboardImport.valueOfJavaScript("datetimepicker",
				libPrefix + "/jquery-datetimepicker-2.5.20/jquery.datetimepicker.full.min.js"));
		impts.add(HtmlTplDashboardImport.valueOfJavaScript("chartFactory",
						scriptPrefix + "/chartFactory.js?v=" + Global.VERSION));
		impts.add(HtmlTplDashboardImport.valueOfJavaScript("dashboardFactory",
				scriptPrefix + "/dashboardFactory.js?v=" + Global.VERSION));
		impts.add(HtmlTplDashboardImport.valueOfJavaScript("serverTime",
						contextPath + "/dashboard/serverTime.js?v=" + rp));
		impts.add(HtmlTplDashboardImport.valueOfJavaScript("chartSupport",
						scriptPrefix + "/chartSupport.js?v=" + Global.VERSION));
		impts.add(HtmlTplDashboardImport.valueOfJavaScript("chartSetting",
						scriptPrefix + "/chartSetting.js?v=" + Global.VERSION));
		impts.add(HtmlTplDashboardImport.valueOfJavaScript("chartPluginManager",
				contextPath + "/chartPlugin/chartPluginManager.js?v=" + Global.VERSION));

		return impts;
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

		// 转义自定义模板内容参数，它可能包含"</script>"子串，传回浏览器端时会导致页面解析出错，需转义为："<\/script>"
		String templateContent = (String) paramValues.get(DASHBOARD_SHOW_PARAM_TEMPLATE_CONTENT);
		if (templateContent != null)
		{
			templateContent = templateContent.replace("</", "<\\/");
			paramValues.put(DASHBOARD_SHOW_PARAM_TEMPLATE_CONTENT, templateContent);
		}

		return paramValues;
	}

	protected boolean isDashboardShowForEdit(HttpServletRequest request)
	{
		String editTemplate = request.getParameter(DASHBOARD_SHOW_PARAM_EDIT_TEMPLATE);
		return ("true".equalsIgnoreCase(editTemplate) || "1".equals(editTemplate));
	}

	protected DashboardTheme resolveDashboardTheme(HttpServletRequest request)
	{
		String theme = request.getParameter(DASHBOARD_SHOW_PARAM_THEME_NAME);

		if (StringUtil.isEmpty(theme))
			theme = WebUtils.getTheme(request);

		DashboardTheme dashboardTheme = this.dashboardThemeSource.getDashboardTheme(theme);
		if (dashboardTheme == null)
			dashboardTheme = this.dashboardThemeSource.getDashboardTheme();

		return dashboardTheme;
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

		SessionDashboardInfoManager dashboardInfoManager = getSessionDashboardInfoManagerNotNull(request);
		DashboardInfo dashboardInfo = dashboardInfoManager.get(form.getDashboardId());

		if (dashboardInfo == null)
			throw new IllegalInputException();

		AnalysisUser analysisUser = AnalysisUser.valueOf(WebUtils.getUser(request, response));

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

			if (chartWidgetId == null)
				throw new IllegalInputException("Chart '" + chartId + "' not found");

			HtmlChartWidget chartWidget = renderer.getHtmlChartWidget(chartWidgetId);

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

			if (chartWidget == null)
				throw new IllegalInputException("Chart '" + chartId + "' not found");

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
					dataSetQueryRe = getDataSetParamValueConverter().convert(dataSetQueryRe, chartDataSets[j].getDataSet(), true);
					analysisUser.setParamValue(dataSetQueryRe, analysisRoleNames);

					dataSetQueriesRe.add(dataSetQueryRe);
				}

				chartQueryRe = chartQuery.copy();
				chartQueryRe.setDataSetQueries(dataSetQueriesRe);
			}

			chartQueriesRe.put(chartId, chartQueryRe);
		}
		
		DashboardQuery queryRe = query.copy();
		queryRe.setChartQueries(chartQueriesRe);

		return queryRe;
	}

	protected void addHeartBeatValue(HttpServletRequest request, WebContext webContext)
	{
		String heartbeatURL = "/dashboard" + DashboardController.HEARTBEAT_TAIL_URL;
		heartbeatURL = addJsessionidParam(heartbeatURL, request.getSession().getId());

		webContext.addAttribute(DASHBOARD_HEARTBEAT_URL_NAME, heartbeatURL);
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
	protected String addJsessionidParam(String url, String sessionId)
	{
		return WebUtils.addJsessionidParam(url, sessionId);
	}

	protected SessionDashboardInfoManager getSessionDashboardInfoManagerNotNull(HttpServletRequest request)
	{
		HttpSession session = request.getSession();

		SessionDashboardInfoManager dashboardManager = (SessionDashboardInfoManager) session
				.getAttribute(SessionDashboardInfoManager.class.getName());

		synchronized (session)
		{
			if (dashboardManager == null)
			{
				dashboardManager = new SessionDashboardInfoManager();
				session.setAttribute(SessionDashboardInfoManager.class.getName(), dashboardManager);
			}
		}

		return dashboardManager;
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

	protected static class SessionDashboardInfoManager implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private Map<String, DashboardInfo> dashboardInfos = null;

		public SessionDashboardInfoManager()
		{
			super();
		}

		public synchronized DashboardInfo get(String dashboardId)
		{
			if (this.dashboardInfos == null)
				return null;

			return this.dashboardInfos.get(dashboardId);
		}

		public synchronized void put(DashboardInfo dashboardInfo)
		{
			if (this.dashboardInfos == null)
				this.dashboardInfos = new HashMap<>();

			this.dashboardInfos.put(dashboardInfo.getDashboardId(), dashboardInfo);
		}
	}

	protected static class DashboardInfo implements Serializable
	{
		private static final long serialVersionUID = 1L;

		/**看板ID*/
		private final String dashboardId;
		
		/**看板部件ID*/
		private final String dashboardWidgetId;

		/** 图表ID-图表部件ID映射表 */
		private final Map<String, String> chartIdToChartWidgetIds = new HashMap<String, String>();

		public DashboardInfo(String dashboardId, String dashboardWidgetId)
		{
			super();
			this.dashboardId = dashboardId;
			this.dashboardWidgetId = dashboardWidgetId;
		}

		public DashboardInfo(Dashboard dashboard)
		{
			this.dashboardId = dashboard.getId();
			this.dashboardWidgetId = dashboard.getWidget().getId();

			if (dashboard.hasChart())
			{
				List<Chart> charts = dashboard.getCharts();
				for (Chart chart : charts)
					this.chartIdToChartWidgetIds.put(chart.getId(), ChartWidget.getChartWidget(chart));
			}
		}

		public String getDashboardId()
		{
			return dashboardId;
		}

		public String getDashboardWidgetId()
		{
			return dashboardWidgetId;
		}

		public synchronized Map<String, String> getChartIdToChartWidgetIds()
		{
			return Collections.unmodifiableMap(chartIdToChartWidgetIds);
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
		public static final String DATA_SET_PARAM_NAME_CURRENT_USER = DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_PREFIX
				+ "USER";

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
		public static final String DATA_SET_PARAM_NAME_CURRENT_ROLE_NAMES = DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_PREFIX
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
}
