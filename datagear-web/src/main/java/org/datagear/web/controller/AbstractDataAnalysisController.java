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
import org.datagear.analysis.DataSetQuery;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.SimpleDashboardQueryHandler;
import org.datagear.analysis.support.ChartWidget;
import org.datagear.analysis.support.DataSetParamValueConverter;
import org.datagear.analysis.support.DefaultRenderContext;
import org.datagear.analysis.support.SimpleDashboardThemeSource;
import org.datagear.analysis.support.html.HtmlChartWidget;
import org.datagear.analysis.support.html.HtmlTplDashboardRenderAttr;
import org.datagear.analysis.support.html.HtmlTplDashboardRenderAttr.WebContext;
import org.datagear.analysis.support.html.HtmlTplDashboardWidgetRenderer;
import org.datagear.analysis.support.html.HtmlTplDashboardWidgetRenderer.HtmlTitleHandler;
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
	 * 看板内置渲染上下文属性名：{@linkplain WebContext}
	 */
	public static final String DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_WEB_CONTEXT = DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_PREFIX
			+ "WEB_CONTEXT";

	/**
	 * 看板内置渲染上下文属性名：{@linkplain DashboardTheme}
	 */
	public static final String DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_DASHBOARD_THEME = DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_PREFIX
			+ "DASHBOARD_THEME";

	/**
	 * 看板内置渲染上下文属性名：{@linkplain ChartTheme}
	 */
	public static final String DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_CHART_THEME = DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_PREFIX
			+ "CHART_THEME";

	/**
	 * 看板内置渲染上下文属性名：{@linkplain HtmlTitleHandler}
	 */
	public static final String DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_HTML_TITLE_HANDLER = DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_PREFIX
			+ "HTML_TITLE_HANDLER";

	/**
	 * 看板展示URL的系统主题请求参数名。
	 */
	public static final String DASHBOARD_THEME_NAME_PARAM = DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_PREFIX + "THEME";

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
			HtmlTplDashboardRenderAttr renderAttr, WebContext webContext,
			HtmlTplDashboardWidgetRenderer htmlTplDashboardWidgetRenderer) throws IOException
	{
		RenderContext renderContext = new DefaultRenderContext(resolveParamValues(request));

		Writer out = response.getWriter();
		DashboardTheme dashboardTheme = resolveDashboardTheme(request);
		renderAttr.inflate(renderContext, out, webContext, dashboardTheme);

		renderAttr.setIgnoreRenderAttrs(renderContext,
				Arrays.asList(renderAttr.getHtmlWriterName(), renderAttr.getHtmlTitleHandlerName(),
						renderAttr.getIgnoreRenderAttrsName(), HtmlTplDashboardRenderAttr.ATTR_NAME));

		return renderContext;
	}

	protected HtmlTplDashboardRenderAttr createHtmlTplDashboardRenderAttr()
	{
		HtmlTplDashboardRenderAttr renderAttr = new HtmlTplDashboardRenderAttr();
		renderAttr.setWebContextName(DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_WEB_CONTEXT);
		renderAttr.setDashboardThemeName(DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_DASHBOARD_THEME);
		renderAttr.setHtmlTitleHandlerName(DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_HTML_TITLE_HANDLER);

		return renderAttr;
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

	protected Map<String, ?> resolveParamValues(HttpServletRequest request)
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

	protected DashboardTheme resolveDashboardTheme(HttpServletRequest request)
	{
		String theme = request.getParameter(DASHBOARD_THEME_NAME_PARAM);

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
	 * @param form
	 * @param renderer
	 * @return
	 */
	protected DashboardResult getDashboardResult(HttpServletRequest request, DashboardQueryForm form,
			HtmlTplDashboardWidgetRenderer renderer)
	{
		if (StringUtil.isEmpty(form.getDashboardId()))
			throw new IllegalInputException();

		SessionDashboardInfoManager dashboardInfoManager = getSessionDashboardInfoManagerNotNull(request);
		DashboardInfo dashboardInfo = dashboardInfoManager.get(form.getDashboardId());

		if (dashboardInfo == null)
			throw new IllegalInputException();

		DashboardQuery dashboardQuery = form.getDashboardQuery();
		Map<String, HtmlChartWidget> chartWidgets = getChartWidgets(form.getDashboardQuery(), dashboardInfo, renderer);

		DashboardQuery queriesConverted = convertDashboardQuery(dashboardQuery, chartWidgets);

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
			Map<String, ? extends ChartWidget> chartWidgets)
	{
		if (query == null)
			return new DashboardQuery();
		
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
}
