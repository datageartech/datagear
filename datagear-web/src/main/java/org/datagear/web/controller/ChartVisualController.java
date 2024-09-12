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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.analysis.DashboardResult;
import org.datagear.analysis.TplDashboardWidgetResManager;
import org.datagear.analysis.support.ErrorMessageDashboardResult;
import org.datagear.analysis.support.html.DefaultHtmlTitleHandler;
import org.datagear.analysis.support.html.HtmlTitleHandler;
import org.datagear.analysis.support.html.HtmlTplDashboard;
import org.datagear.analysis.support.html.HtmlTplDashboardRenderContext;
import org.datagear.analysis.support.html.HtmlTplDashboardWidget;
import org.datagear.analysis.support.html.HtmlTplDashboardWidgetHtmlRenderer;
import org.datagear.analysis.support.html.LoadableChartWidgets;
import org.datagear.management.domain.HtmlChartWidgetEntity;
import org.datagear.management.domain.User;
import org.datagear.management.service.HtmlChartWidgetEntityService;
import org.datagear.management.service.HtmlChartWidgetEntityService.ChartWidgetSourceContext;
import org.datagear.util.IOUtil;
import org.datagear.web.util.SessionDashboardInfoSupport.DashboardInfo;
import org.datagear.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.request.WebRequest;

/**
 * 图表展示控制器。
 * 
 * @author datagear@163.com
 *
 */
@Controller
@RequestMapping(ChartVisualController.PATH_PREFIX)
public class ChartVisualController extends AbstractDataAnalysisController implements ServletContextAware
{
	/** 展示页路径前缀 */
	public static final String PATH_PREFIX = "/cv";

	@Autowired
	private HtmlChartWidgetEntityService htmlChartWidgetEntityService;

	@Autowired
	private HtmlTplDashboardWidgetHtmlRenderer htmlTplDashboardWidgetHtmlRenderer;

	@Autowired
	private TplDashboardWidgetResManager tplDashboardWidgetResManager;

	private ServletContext servletContext;

	public ChartVisualController()
	{
		super();
	}

	public HtmlChartWidgetEntityService getHtmlChartWidgetEntityService()
	{
		return htmlChartWidgetEntityService;
	}

	public void setHtmlChartWidgetEntityService(HtmlChartWidgetEntityService htmlChartWidgetEntityService)
	{
		this.htmlChartWidgetEntityService = htmlChartWidgetEntityService;
	}

	public HtmlTplDashboardWidgetHtmlRenderer getHtmlTplDashboardWidgetHtmlRenderer()
	{
		return htmlTplDashboardWidgetHtmlRenderer;
	}

	public void setHtmlTplDashboardWidgetHtmlRenderer(
			HtmlTplDashboardWidgetHtmlRenderer htmlTplDashboardWidgetHtmlRenderer)
	{
		this.htmlTplDashboardWidgetHtmlRenderer = htmlTplDashboardWidgetHtmlRenderer;
	}

	public TplDashboardWidgetResManager getTplDashboardWidgetResManager()
	{
		return tplDashboardWidgetResManager;
	}

	public void setTplDashboardWidgetResManager(TplDashboardWidgetResManager tplDashboardWidgetResManager)
	{
		this.tplDashboardWidgetResManager = tplDashboardWidgetResManager;
	}

	public ServletContext getServletContext()
	{
		return servletContext;
	}

	@Override
	public void setServletContext(ServletContext servletContext)
	{
		this.servletContext = servletContext;
	}

	/**
	 * 展示图表。
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @param id
	 * @throws Exception
	 */
	@RequestMapping({ "/{id}/", "/{id}" })
	public void show(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@PathVariable("id") String id) throws Exception
	{
		String requestPath = resolvePathAfter(request, "");
		String correctPath = WebUtils.getContextPath(request) + resolveShowPath(request, id);

		// 如果是"/{id}"请求，则应跳转到"/{id}/"，因为图表展示页内的超链接使用的都是相对路径，
		// 如果末尾不加"/"，将会导致这些超链接路径错误
		if (requestPath.indexOf(correctPath) < 0)
		{
			String redirectPath = correctPath;
			redirectPath = addSessionIdParamIfNeed(redirectPath, request);
			redirectPath = appendRequestQueryString(redirectPath, request);
			response.sendRedirect(redirectPath);
		}
		else
		{
			User user = getCurrentUser();
			HtmlChartWidgetEntity chart = this.htmlChartWidgetEntityService.getById(user, id);

			showChart(request, response, model, user, chart);
		}
	}

	/**
	 * 加载展示图表的资源。
	 * 
	 * @param request
	 * @param response
	 * @param webRequest
	 * @param model
	 * @param id
	 * @throws Exception
	 */
	@RequestMapping("/{id}/**")
	public void showResource(HttpServletRequest request, HttpServletResponse response, WebRequest webRequest,
			org.springframework.ui.Model model, @PathVariable("id") String id) throws Exception
	{
		User user = getCurrentUser();
		HtmlChartWidgetEntity chart = this.htmlChartWidgetEntityService.getById(user, id);

		String resName = resolvePathAfter(request, resolveShowPath(request, id));

		if (isEmpty(resName))
		{
			showChart(request, response, model, user, chart);
		}
		else
		{
			// 处理可能的中文资源名
			resName = WebUtils.decodeURL(resName);

			long lastModified = this.tplDashboardWidgetResManager.lastModified(id, resName);
			if (webRequest.checkNotModified(lastModified))
				return;

			setContentTypeByName(request, response, servletContext, resName);
			setCacheControlNoCache(response);

			InputStream in = this.tplDashboardWidgetResManager.getInputStream(id, resName);
			OutputStream out = response.getOutputStream();

			try
			{
				IOUtil.write(in, out);
			}
			finally
			{
				IOUtil.close(in);
			}
		}
	}

	/**
	 * 展示数据。
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @param id
	 * @throws Exception
	 */
	@RequestMapping(value = "/data", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ErrorMessageDashboardResult showData(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model, @RequestBody DashboardQueryForm form) throws Exception
	{
		// 此处获取ChartWidget不再需要权限控制，应显式移除线程变量
		ChartWidgetSourceContext.remove();

		DashboardResult dashboardResult = getDashboardResult(request, response, form,
				this.htmlTplDashboardWidgetHtmlRenderer);

		return new ErrorMessageDashboardResult(dashboardResult, true);
	}

	/**
	 * 心跳。
	 * <p>
	 * 图表展示页面有停留较长时间再操作的场景，此时可能会因为会话超时导致操作失败，所以这里添加心跳请求，避免会话超时。
	 * </p>
	 * 
	 * @param request
	 * @param response
	 * @param dashboardId
	 * @return
	 * @throws Throwable
	 */
	@RequestMapping(value = HEARTBEAT_TAIL_URL, produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public Map<String, Object> heartbeat(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(HEARTBEAT_PARAM_DASHBOARD_ID) String dashboardId) throws Throwable
	{
		return super.handleHeartbeat(request, response, dashboardId);
	}

	/**
	 * 图表展示页卸载。
	 * <p>
	 * 图表展示页面关闭后，应卸载后台数据。
	 * </p>
	 * 
	 * @param request
	 * @param response
	 * @param dashboardId
	 * @return
	 * @throws Throwable
	 */
	@RequestMapping(value = UNLOAD_TAIL_URL, produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public Map<String, Object> unloadDashboard(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(UNLOAD_PARAM_DASHBOARD_ID) String dashboardId) throws Throwable
	{
		return super.handleUnloadDashboard(request, response, dashboardId);
	}

	/**
	 * 展示图表。
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @param id
	 * @throws Exception
	 */
	protected void showChart(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model, User user, HtmlChartWidgetEntity chart) throws Exception
	{
		if (chart == null)
			throw new RecordNotFoundException();

		Reader templateIn = null;
		Writer out = null;

		ChartWidgetSourceContext.set(new ChartWidgetSourceContext(user));

		try
		{
			String id = chart.getId();
			String htmlTitle = chart.getName();
			HtmlTplDashboardWidget dashboardWidget = buildHtmlTplDashboardWidget(id);

			// 图表展示页面应禁用异步加载功能，避免越权访问隐患
			String htmlAttr = this.htmlTplDashboardWidgetHtmlRenderer.getAttrNameLoadableChartWidgets() + "=\""
					+ LoadableChartWidgets.PATTERN_NONE + "\"";
			String simpleTemplate = this.htmlTplDashboardWidgetHtmlRenderer.simpleTemplateContent(new String[] { id },
					htmlAttr, IOUtil.CHARSET_UTF_8, htmlTitle,
					this.htmlTplDashboardWidgetHtmlRenderer.getDashboardStyleName(), "",
					"dg-chart-for-show-chart " + this.htmlTplDashboardWidgetHtmlRenderer.getChartStyleName(),
					"dg-chart-disable-setting=\"false\"");
			templateIn = IOUtil.getReader(simpleTemplate);

			String responseEncoding = dashboardWidget.getTemplateEncoding();
			response.setCharacterEncoding(responseEncoding);
			response.setContentType(CONTENT_TYPE_HTML);
			out = IOUtil.getBufferedWriter(response.getWriter());

			HtmlTitleHandler htmlTitleHandler = getShowChartHtmlTitleHandler(request, response, user, chart);
			HtmlTplDashboardRenderContext renderContext = createRenderContext(request, response,
					dashboardWidget.getFirstTemplate(), out, createWebContext(request),
					buildWebHtmlTplDashboardImportBuilderForShow(request), htmlTitleHandler);
			renderContext.setTemplateReader(templateIn);
			renderContext.setTemplateLastModified(HtmlTplDashboardRenderContext.TEMPLATE_LAST_MODIFIED_NONE);

			HtmlTplDashboard dashboard = dashboardWidget.render(renderContext);
			getSessionDashboardInfoSupport().setDashboardInfo(request, new DashboardInfo(dashboard, false));
		}
		finally
		{
			IOUtil.close(templateIn);
			IOUtil.close(out);
			ChartWidgetSourceContext.remove();
		}
	}

	protected HtmlTitleHandler getShowChartHtmlTitleHandler(HttpServletRequest request, HttpServletResponse response,
			User user, HtmlChartWidgetEntity chart) throws Exception
	{
		return new DefaultHtmlTitleHandler(
				getMessage(request, "chart.show.htmlTitleSuffix", getMessage(request, "app.name")));
	}

	@Override
	protected boolean isDashboardThemeAuto(HttpServletRequest request, String theme)
	{
		// 由于图表展示无法自定义页面样式，因此，参数未指定主题时，也应自动匹配系统主题
		return (theme == null || super.isDashboardThemeAuto(request, theme));
	}

	protected HtmlTplDashboardWidget buildHtmlTplDashboardWidget(String chartId)
	{
		return new HtmlTplDashboardWidget(chartId, "index.html", this.htmlTplDashboardWidgetHtmlRenderer,
				this.tplDashboardWidgetResManager);
	}

	protected WebContext createWebContext(HttpServletRequest request)
	{
		WebContext webContext = createInitWebContext(request);

		addUpdateDataValue(request, webContext, resolveDataPath(request));
		addLoadChartValue(request, webContext, resolveLoadChartPath(request));
		addHeartBeatValue(request, webContext, resolveHeartbeatPath(request));
		addUnloadValue(request, webContext, resolveUnloadPath(request));

		return webContext;
	}

	/**
	 * 解析展示路径。
	 * 
	 * @param request
	 * @param chartId
	 * @return
	 */
	protected String resolveShowPath(HttpServletRequest request, String chartId)
	{
		return PATH_PREFIX + "/" + chartId + "/";
	}

	/**
	 * 解析加载数据路径。
	 * 
	 * @param request
	 * @return
	 */
	protected String resolveDataPath(HttpServletRequest request)
	{
		return PATH_PREFIX + "/data";
	}

	/**
	 * 解析加载图表路径。
	 * 
	 * @param request
	 * @return
	 */
	protected String resolveLoadChartPath(HttpServletRequest request)
	{
		return PATH_PREFIX + "/loadChart";
	}

	/**
	 * 解析心跳路径。
	 * 
	 * @param request
	 * @return
	 */
	protected String resolveHeartbeatPath(HttpServletRequest request)
	{
		return PATH_PREFIX + HEARTBEAT_TAIL_URL;
	}

	/**
	 * 解析卸载看板路径。
	 * 
	 * @param request
	 * @return
	 */
	protected String resolveUnloadPath(HttpServletRequest request)
	{
		return PATH_PREFIX + UNLOAD_TAIL_URL;
	}
}
