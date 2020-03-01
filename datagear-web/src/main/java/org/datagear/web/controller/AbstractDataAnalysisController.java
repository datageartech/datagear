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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.datagear.analysis.DataSetResult;
import org.datagear.analysis.RenderStyle;
import org.datagear.analysis.support.html.DefaultHtmlRenderContext;
import org.datagear.analysis.support.html.HtmlTplDashboard;
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

	protected HtmlRenderContext createHtmlRenderContext(HttpServletRequest request, WebContext webContext, Writer out)
	{
		DefaultHtmlRenderContext renderContext = new DefaultHtmlRenderContext(webContext, out);
		HtmlRenderAttributes.setRenderStyle(renderContext, resolveRenderStyle(request));

		return renderContext;
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
	protected String resolveDashboardResName(HttpServletRequest request, HttpServletResponse response,
			String id)
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

		SessionHtmlTplDashboardManager dashboardManager = getSessionHtmlTplDashboardManagerNotNull(request);

		HtmlTplDashboard dashboard = dashboardManager.get(dashboardId);

		if (dashboard == null)
			throw new RecordNotFoundException();

		Map<String, ?> dataSetParamValues = new HashMap<String, Object>();

		if (chartsId == null || chartsId.length == 0)
			return dashboard.getDataSetResults(dataSetParamValues);
		else
			return dashboard.getDataSetResults(Arrays.asList(chartsId), dataSetParamValues);
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
				this.htmlTplDashboards = new HashMap<String, HtmlTplDashboard>();

			this.htmlTplDashboards.put(dashboard.getId(), dashboard);
		}
	}
}
