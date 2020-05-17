/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.web.controller;

import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
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
import org.datagear.analysis.DataSetParam;
import org.datagear.analysis.DataSetResult;
import org.datagear.analysis.RenderStyle;
import org.datagear.analysis.support.DataSetParamValueConverter;
import org.datagear.analysis.support.html.DefaultHtmlRenderContext;
import org.datagear.analysis.support.html.HtmlRenderAttributes;
import org.datagear.analysis.support.html.HtmlRenderContext;
import org.datagear.analysis.support.html.HtmlRenderContext.WebContext;
import org.datagear.analysis.support.html.HtmlTplDashboard;
import org.datagear.util.StringUtil;
import org.datagear.web.util.WebUtils;

/**
 * 抽象数据分析控制器。
 * 
 * @author datagear@163.com
 *
 */
public class AbstractDataAnalysisController extends AbstractController
{
	private DataSetParamValueConverter dataSetParamValueConverter = new DataSetParamValueConverter();

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

	protected RenderStyle resolveRenderStyle(HttpServletRequest request)
	{
		String style = request.getParameter("renderStyle");

		if (!StringUtil.isEmpty(style))
		{
			EnumSet<RenderStyle> enumSet = EnumSet.allOf(RenderStyle.class);

			for (RenderStyle e : enumSet)
			{
				if (e.name().equalsIgnoreCase(style))
					return e;
			}
		}
		else
		{
			String theme = WebUtils.getTheme(request);

			if (!StringUtil.isEmpty(theme))
			{
				theme = theme.toLowerCase();

				if (theme.indexOf("dark") > -1)
					return RenderStyle.DARK;
			}
		}

		return RenderStyle.LIGHT;
	}

	protected HtmlRenderContext createHtmlRenderContext(HttpServletRequest request, WebContext webContext,
			RenderStyle renderStyle, Writer out)
	{
		DefaultHtmlRenderContext renderContext = new DefaultHtmlRenderContext(webContext, out);
		HtmlRenderAttributes.setRenderStyle(renderContext, renderStyle);

		return renderContext;
	}

	protected void setDashboardThemeAttribute(HttpSession session, DashboardTheme theme)
	{
		session.setAttribute(AbstractDataAnalysisController.class.getSimpleName(), theme);
	}

	/**
	 * 获取{@linkplain DashboardTheme}，没有则返回{@code null}。
	 * 
	 * @param session
	 * @return
	 */
	protected DashboardTheme getDashboardThemeAttribute(HttpSession session)
	{
		return (DashboardTheme) session.getAttribute(AbstractDataAnalysisController.class.getSimpleName());
	}

	/**
	 * 解析展示看板请求路径的看板资源名。
	 * <p>
	 * 返回空字符串表情请求展示首页。
	 * </p>
	 * 
	 * @param request
	 * @param response
	 * @param id
	 * @return
	 */
	protected String resolveDashboardResName(HttpServletRequest request, HttpServletResponse response, String id)
	{
		String pathInfo = request.getPathInfo();

		String idPath = id + "/";

		if (pathInfo.endsWith(id) || pathInfo.endsWith(idPath))
			return "";

		String resPath = pathInfo.substring(pathInfo.indexOf(idPath) + idPath.length());

		return resPath;
	}

	/**
	 * 获取看板数据。
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @param webContext
	 * @param dashboardParams
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	protected Map<String, DataSetResult[]> getDashboardData(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model, WebContext webContext, Map<String, ?> dashboardParams) throws Exception
	{
		String dashboardId = (String) dashboardParams.get(webContext.getDashboardIdParam());
		Collection<String> chartIds = (Collection<String>) dashboardParams.get(webContext.getChartIdsParam());
		Map<String, ? extends List<? extends Map<String, ?>>> chartsParamValues = (Map<String, ? extends List<? extends Map<String, ?>>>) dashboardParams
				.get(webContext.getChartsParamValuesParam());

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
			if (chartsParamValues == null)
				chartsParamValues = Collections.EMPTY_MAP;

			Set<String> chartIdSet = new HashSet<>(chartIds.size());
			chartIdSet.addAll(chartIds);

			return dashboard.getDataSetResults(chartIdSet, convertChartsParamValues(dashboard, chartsParamValues));
		}
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

	@SuppressWarnings("unchecked")
	protected Map<String, List<? extends Map<String, ?>>> convertChartsParamValues(Dashboard dashboard,
			Map<String, ? extends List<? extends Map<String, ?>>> chartsParamValues)
	{
		if (chartsParamValues == null)
			return Collections.EMPTY_MAP;

		Map<String, List<? extends Map<String, ?>>> re = new HashMap<String, List<? extends Map<String, ?>>>();

		for (Map.Entry<String, ? extends List<? extends Map<String, ?>>> entry : chartsParamValues.entrySet())
		{
			Chart chart = dashboard.getChart(entry.getKey());
			ChartDataSet[] chartDataSets = chart.getChartDataSets();

			if (chartDataSets == null || chartDataSets.length == 0)
				re.put(entry.getKey(), Collections.EMPTY_LIST);
			else
			{
				List<? extends Map<String, ?>> paramValuess = entry.getValue();
				List<Map<String, ?>> converteds = new ArrayList<Map<String, ?>>();

				for (int i = 0; i < chartDataSets.length; i++)
				{
					List<DataSetParam> dataSetParams = chartDataSets[i].getDataSet().getParams();
					converteds.add(getDataSetParamValueConverter().convert(paramValuess.get(i), dataSetParams));
				}

				re.put(entry.getKey(), converteds);
			}
		}

		return re;
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
}
