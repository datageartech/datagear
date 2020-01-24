/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.web.controller;

import java.io.Serializable;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.datagear.analysis.DataSetResult;
import org.datagear.analysis.RenderStyle;
import org.datagear.analysis.support.html.DefaultHtmlRenderContext;
import org.datagear.analysis.support.html.HtmlDashboard;
import org.datagear.analysis.support.html.HtmlRenderAttributes;
import org.datagear.analysis.support.html.HtmlRenderContext;
import org.datagear.analysis.support.html.HtmlRenderContext.WebContext;
import org.datagear.util.StringUtil;
import org.datagear.web.convert.ClassDataConverter;
import org.datagear.web.util.WebUtils;
import org.springframework.context.MessageSource;

/**
 * 抽象数据分析控制器。
 * 
 * @author datagear@163.com
 *
 */
public class AbstractDataAnalysisController extends AbstractController
{
	public AbstractDataAnalysisController()
	{
		super();
	}

	public AbstractDataAnalysisController(MessageSource messageSource, ClassDataConverter classDataConverter)
	{
		super(messageSource, classDataConverter);
	}

	protected RenderStyle resolveRenderStyle(HttpServletRequest request)
	{
		String theme = WebUtils.getTheme(request);

		if (StringUtil.isEmpty(theme))
			return RenderStyle.LIGHT;

		theme = theme.toLowerCase();

		if (theme.indexOf("dark") > -1)
			return RenderStyle.DARK;

		return RenderStyle.LIGHT;
	}

	protected HtmlRenderContext createHtmlRenderContext(HttpServletRequest request, WebContext webContext, Writer out)
	{
		DefaultHtmlRenderContext renderContext = new DefaultHtmlRenderContext(webContext, out);
		HtmlRenderAttributes.setRenderStyle(renderContext, resolveRenderStyle(request));

		return renderContext;
	}

	/**
	 * 获取看板数据。
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @param webContext
	 * @return
	 * @throws Exception
	 */
	protected Map<String, DataSetResult[]> getDashboardData(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model, WebContext webContext) throws Exception
	{
		String dashboardId = request.getParameter(webContext.getDashboardIdParam());
		String[] chartsId = request.getParameterValues(webContext.getChartsIdParam());

		if (StringUtil.isEmpty(dashboardId))
			throw new IllegalInputException();

		SessionHtmlDashboardManager dashboardManager = getSessionHtmlDashboardManagerNotNull(request);

		HtmlDashboard dashboard = dashboardManager.get(dashboardId);

		if (dashboard == null)
			throw new RecordNotFoundException();

		Map<String, ?> dataSetParamValues = new HashMap<String, Object>();

		if (chartsId == null || chartsId.length == 0)
			return dashboard.getDataSetResults(dataSetParamValues);
		else
			return dashboard.getDataSetResults(Arrays.asList(chartsId), dataSetParamValues);
	}

	protected SessionHtmlDashboardManager getSessionHtmlDashboardManagerNotNull(HttpServletRequest request)
	{
		HttpSession session = request.getSession();

		SessionHtmlDashboardManager dashboardManager = (SessionHtmlDashboardManager) session
				.getAttribute(SessionHtmlDashboardManager.class.getName());

		synchronized (session)
		{
			if (dashboardManager == null)
			{
				dashboardManager = new SessionHtmlDashboardManager();
				session.setAttribute(SessionHtmlDashboardManager.class.getName(), dashboardManager);
			}
		}

		return dashboardManager;
	}

	protected static class SessionHtmlDashboardManager implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private transient Map<String, HtmlDashboard> htmlDashboards;

		public SessionHtmlDashboardManager()
		{
			super();
		}

		public synchronized HtmlDashboard get(String htmlDashboardId)
		{
			if (this.htmlDashboards == null)
				return null;

			return this.htmlDashboards.get(htmlDashboardId);
		}

		public synchronized void put(HtmlDashboard dashboard)
		{
			if (this.htmlDashboards == null)
				this.htmlDashboards = new HashMap<String, HtmlDashboard>();

			this.htmlDashboards.put(dashboard.getId(), dashboard);
		}
	}
}
