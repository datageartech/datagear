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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.datagear.analysis.Chart;
import org.datagear.analysis.ChartDataSet;
import org.datagear.analysis.Dashboard;
import org.datagear.analysis.DashboardTheme;
import org.datagear.analysis.DashboardThemeSource;
import org.datagear.analysis.DataSetQuery;
import org.datagear.analysis.DataSetResult;
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
	 * 获取看板数据。
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @param webContext
	 * @param form
	 * @return
	 * @throws Exception
	 */
	protected Map<String, DataSetResult[]> getDashboardData(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model, WebContext webContext, DashboardUpdateDataForm form) throws Exception
	{
		String dashboardId = form.getDashboardId();
		List<String> chartIds = form.getChartIds();
		Map<String, List<DataSetQuery>> chartsQueries = form.getChartQueries();
		
		if (StringUtil.isEmpty(dashboardId))
			throw new IllegalInputException();
		
		SessionHtmlTplDashboardManager dashboardManager = getSessionHtmlTplDashboardManagerNotNull(request);
		HtmlTplDashboard dashboard = dashboardManager.get(dashboardId);

		if (dashboard == null)
			throw new RecordNotFoundException();

		if (chartIds == null || chartIds.isEmpty())
			return dashboard.getDataSetResults();
		else
		{
			Set<String> chartIdSet = new HashSet<>(chartIds.size());
			chartIdSet.addAll(chartIds);

			return dashboard.getDataSetResults(chartIdSet, convertChartDataSetQueries(dashboard, chartsQueries));
		}
	}

	protected Map<String, List<DataSetQuery>> convertChartDataSetQueries(Dashboard dashboard,
			Map<String, ? extends List<? extends DataSetQuery>> chartsQueries)
	{
		if(chartsQueries == null)
			return Collections.emptyMap();
		
		Map<String, List<DataSetQuery>> re = new HashMap<String, List<DataSetQuery>>();
		
		for (Map.Entry<String, ? extends List<? extends DataSetQuery>> entry : chartsQueries.entrySet())
		{
			String chartId = entry.getKey();
			Chart chart = dashboard.getChart(chartId);
			
			if(chart == null)
				continue;
			
			ChartDataSet[] chartDataSets = chart.getChartDataSets();

			if (chartDataSets == null || chartDataSets.length == 0)
			{
				re.put(entry.getKey(), Collections.emptyList());
			}
			else
			{
				List<? extends DataSetQuery> rawQueries = chartsQueries.get(chartId);
				List<DataSetQuery> reQueries = new ArrayList<DataSetQuery>();
				
				if (chartDataSets != null && chartDataSets.length > 0)
				{
					for(int j = 0; j<chartDataSets.length; j++)
					{
						DataSetQuery reQuery = rawQueries.get(j);
						reQuery = getDataSetParamValueConverter().convert(reQuery, chartDataSets[j].getDataSet(), true);
						reQueries.add(reQuery);
					}
				}
				
				re.put(chartId, reQueries);
			}
		}
		
		return re;
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
		String heartbeatURL = "/analysis/dashboard/heartbeat";
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
	
	public static class DashboardUpdateDataForm
	{
		/**更新数据的看板ID*/
		private String dashboardId;
		
		/**更新数据的图表ID*/
		private List<String> chartIds;
		
		/**更新数据的图表查询*/
		private Map<String, List<DataSetQuery>> chartQueries;

		public DashboardUpdateDataForm()
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

		public List<String> getChartIds()
		{
			return chartIds;
		}

		public void setChartIds(List<String> chartIds)
		{
			this.chartIds = chartIds;
		}

		public Map<String, List<DataSetQuery>> getChartQueries()
		{
			return chartQueries;
		}

		public void setChartQueries(Map<String, List<DataSetQuery>> chartQueries)
		{
			this.chartQueries = chartQueries;
		}
	}
}
