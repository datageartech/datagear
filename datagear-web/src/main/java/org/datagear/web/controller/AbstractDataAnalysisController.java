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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.datagear.analysis.Chart;
import org.datagear.analysis.ChartDataSet;
import org.datagear.analysis.ChartQuery;
import org.datagear.analysis.Dashboard;
import org.datagear.analysis.DashboardQuery;
import org.datagear.analysis.DashboardResult;
import org.datagear.analysis.DashboardTheme;
import org.datagear.analysis.DashboardThemeSource;
import org.datagear.analysis.DataSetQuery;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.support.DataSetParamValueConverter;
import org.datagear.analysis.support.DefaultRenderContext;
import org.datagear.analysis.support.SimpleDashboardThemeSource;
import org.datagear.analysis.support.html.HtmlTplDashboard;
import org.datagear.analysis.support.html.HtmlTplDashboardRenderAttr;
import org.datagear.analysis.support.html.HtmlTplDashboardRenderAttr.WebContext;
import org.datagear.analysis.support.html.HtmlTplDashboardWidgetRenderer;
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
	public static final String DASHBOARD_THEME_NAME_PARAM = "theme";

	public static final String WEB_CONTEXT_CONTEXT_PATH_PARAM = "";

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
	 * @param response
	 * @param model
	 * @param webContext
	 * @param form
	 * @return
	 * @throws Exception
	 */
	protected DashboardResult getDashboardResult(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model, WebContext webContext, DashboardQueryForm form) throws Exception
	{
		String dashboardId = form.getDashboardId();
		DashboardQuery dashboardQuery = form.getDashboardQuery();
		
		if (StringUtil.isEmpty(dashboardId))
			throw new IllegalInputException();
		
		SessionHtmlTplDashboardManager dashboardManager = getSessionHtmlTplDashboardManagerNotNull(request);
		HtmlTplDashboard dashboard = dashboardManager.get(dashboardId);

		if (dashboard == null)
			throw new RecordNotFoundException();

		DashboardQuery queriesConverted = convertDashboardQuery(dashboard, dashboardQuery);

		return dashboard.getResult(queriesConverted);
	}

	protected DashboardQuery convertDashboardQuery(Dashboard dashboard, DashboardQuery query)
	{
		if (query == null)
			return new DashboardQuery();
		
		Map<String, ChartQuery> chartQueries = query.getChartQueries();
		Map<String, ChartQuery> chartQueriesRe = new HashMap<String, ChartQuery>(chartQueries.size());

		for (Map.Entry<String, ChartQuery> entry : chartQueries.entrySet())
		{
			String chartId = entry.getKey();
			ChartQuery chartQuery = entry.getValue();
			Chart chart = dashboard.getChart(chartId);

			if (chart == null)
				throw new IllegalInputException("Chart '" + chartId + "' not found");

			ChartDataSet[] chartDataSets = chart.getChartDataSets();
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

	protected SessionHtmlTplDashboardManager getSessionHtmlTplDashboardManagerNotNull(HttpServletRequest request)
	{
		HttpSession session = request.getSession();

		SessionHtmlTplDashboardManager dashboardManager = (SessionHtmlTplDashboardManager) session
				.getAttribute(SessionHtmlTplDashboardManager.class.getName());

		synchronized (session)
		{
			if (dashboardManager == null)
			{
				dashboardManager = new SessionHtmlTplDashboardManager();
				session.setAttribute(SessionHtmlTplDashboardManager.class.getName(), dashboardManager);
			}
		}

		return dashboardManager;
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

	protected static class SessionHtmlTplDashboardManager implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private transient Map<String, HtmlTplDashboard> htmlTplDashboards;

		public SessionHtmlTplDashboardManager()
		{
			super();
		}

		public synchronized HtmlTplDashboard get(String htmlTplDashboardId)
		{
			if (this.htmlTplDashboards == null)
				return null;

			return this.htmlTplDashboards.get(htmlTplDashboardId);
		}

		public synchronized void put(HtmlTplDashboard dashboard)
		{
			if (this.htmlTplDashboards == null)
				this.htmlTplDashboards = new HashMap<>();

			this.htmlTplDashboards.put(dashboard.getId(), dashboard);
		}
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
