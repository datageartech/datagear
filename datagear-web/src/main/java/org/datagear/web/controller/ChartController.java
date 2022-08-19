/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.controller;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.datagear.analysis.ChartDataSet;
import org.datagear.analysis.ChartPluginManager;
import org.datagear.analysis.DashboardResult;
import org.datagear.analysis.DataSet;
import org.datagear.analysis.DataSetQuery;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.ResultDataFormat;
import org.datagear.analysis.TemplateDashboardWidgetResManager;
import org.datagear.analysis.support.ErrorMessageDashboardResult;
import org.datagear.analysis.support.html.HtmlChartPlugin;
import org.datagear.analysis.support.html.HtmlTplDashboard;
import org.datagear.analysis.support.html.HtmlTplDashboardRenderAttr.DefaultHtmlTitleHandler;
import org.datagear.analysis.support.html.HtmlTplDashboardRenderAttr.WebContext;
import org.datagear.analysis.support.html.HtmlTplDashboardWidget;
import org.datagear.analysis.support.html.HtmlTplDashboardWidgetHtmlRenderer;
import org.datagear.management.domain.Authorization;
import org.datagear.management.domain.ChartDataSetVO;
import org.datagear.management.domain.HtmlChartWidgetEntity;
import org.datagear.management.domain.User;
import org.datagear.management.service.AnalysisProjectService;
import org.datagear.management.service.DataSetEntityService;
import org.datagear.management.service.HtmlChartWidgetEntityService;
import org.datagear.management.service.HtmlChartWidgetEntityService.ChartWidgetSourceContext;
import org.datagear.persistence.PagingData;
import org.datagear.util.IDUtil;
import org.datagear.util.IOUtil;
import org.datagear.web.util.OperationMessage;
import org.datagear.web.util.WebUtils;
import org.datagear.web.vo.APIDDataFilterPagingQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.request.WebRequest;

/**
 * 图表控制器。
 * 
 * @author datagear@163.com
 *
 */
@Controller
@RequestMapping("/chart")
public class ChartController extends AbstractChartPluginAwareController implements ServletContextAware
{
	static
	{
		AuthorizationResourceMetas.registerForShare(HtmlChartWidgetEntity.AUTHORIZATION_RESOURCE_TYPE);
	}

	@Autowired
	private HtmlChartWidgetEntityService htmlChartWidgetEntityService;

	@Autowired
	private AnalysisProjectService analysisProjectService;

	@Autowired
	private ChartPluginManager chartPluginManager;

	@Autowired
	private HtmlTplDashboardWidgetHtmlRenderer htmlTplDashboardWidgetHtmlRenderer;

	@Autowired
	private TemplateDashboardWidgetResManager templateDashboardWidgetResManager;

	@Autowired
	private DataSetEntityService dataSetEntityService;

	private ServletContext servletContext;

	public ChartController()
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

	public AnalysisProjectService getAnalysisProjectService()
	{
		return analysisProjectService;
	}

	public void setAnalysisProjectService(AnalysisProjectService analysisProjectService)
	{
		this.analysisProjectService = analysisProjectService;
	}

	public ChartPluginManager getChartPluginManager()
	{
		return chartPluginManager;
	}

	public void setChartPluginManager(ChartPluginManager chartPluginManager)
	{
		this.chartPluginManager = chartPluginManager;
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

	public TemplateDashboardWidgetResManager getTemplateDashboardWidgetResManager()
	{
		return templateDashboardWidgetResManager;
	}

	public void setTemplateDashboardWidgetResManager(
			TemplateDashboardWidgetResManager templateDashboardWidgetResManager)
	{
		this.templateDashboardWidgetResManager = templateDashboardWidgetResManager;
	}

	public DataSetEntityService getDataSetEntityService()
	{
		return dataSetEntityService;
	}

	public void setDataSetEntityService(DataSetEntityService dataSetEntityService)
	{
		this.dataSetEntityService = dataSetEntityService;
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

	@RequestMapping("/add")
	public String add(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model)
	{
		HtmlChartWidgetEntity chart = new HtmlChartWidgetEntity();
		setCookieAnalysisProject(request, response, chart);

		addAttributeForWriteJson(model, "chartPluginVO", null);
		model.addAttribute("initResultDataFormat", createDefaultResultDataFormat());
		model.addAttribute("enableResultDataFormat", false);

		setFormModel(model, chart, REQUEST_ACTION_ADD, SUBMIT_ACTION_SAVE);

		return "/chart/chart_form";
	}

	@RequestMapping("/edit")
	public String edit(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id)
	{
		User user = WebUtils.getUser();
		
		HtmlChartWidgetEntity chart = getByIdForEdit(this.htmlChartWidgetEntityService, user, id);
		convertForFormModel(chart, request);
		setResultDataFormatModel(chart, model);

		setFormModel(model, chart, REQUEST_ACTION_EDIT, SUBMIT_ACTION_SAVE);
		
		return "/chart/chart_form";
	}

	@RequestMapping("/copy")
	public String copy(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id)
	{
		User user = WebUtils.getUser();

		HtmlChartWidgetEntity chart = getByIdForView(this.htmlChartWidgetEntityService, user, id);
		setNullAnalysisProjectIfNoPermission(user, chart, getAnalysisProjectService());

		ChartDataSet[] chartDataSets = chart.getChartDataSets();
		if (chartDataSets != null)
		{
			List<ChartDataSet> chartDataSetsPermited = new ArrayList<ChartDataSet>(chartDataSets.length);

			for (int i = 0; i < chartDataSets.length; i++)
			{
				ChartDataSet chartDataSet = chartDataSets[i];
				DataSet dataSet = (chartDataSet == null ? null : chartDataSet.getDataSet());
				int permission = (dataSet != null ? getDataSetEntityService().getPermission(user, dataSet.getId())
						: Authorization.PERMISSION_NONE_START);

				// 只添加有权限的
				if (Authorization.canRead(permission))
				{
					chartDataSetsPermited.add(chartDataSet);
				}
			}

			chartDataSets = chartDataSetsPermited.toArray(new ChartDataSet[chartDataSetsPermited.size()]);
		}

		chart.setId(null);
		convertForFormModel(chart, request);
		setResultDataFormatModel(chart, model);

		setFormModel(model, chart, REQUEST_ACTION_COPY, SUBMIT_ACTION_SAVE);
		
		return "/chart/chart_form";
	}

	@RequestMapping(value = "/save", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> save(HttpServletRequest request, HttpServletResponse response,
			@RequestBody HtmlChartWidgetEntity entity)
	{
		User user = WebUtils.getUser();

		trimAnalysisProjectAwareEntityForSave(entity);

		HtmlChartPlugin paramPlugin = entity.getHtmlChartPlugin();

		if (isEmpty(entity.getId()))
		{
			entity.setId(IDUtil.randomIdOnTime20());
			entity.setCreateUser(user.cloneNoPassword());
			entity.setCreateTime(new Date());
			inflateHtmlChartWidgetEntity(entity, request);

			checkSaveEntity(entity);

			this.htmlChartWidgetEntityService.add(user, entity);
		}
		else
		{
			inflateHtmlChartWidgetEntity(entity, request);
			checkSaveEntity(entity);
			this.htmlChartWidgetEntityService.update(user, entity);
		}

		// 返回参数不应该完全加载插件对象
		entity.setHtmlChartPlugin(paramPlugin);
		
		return operationSuccessResponseEntity(request, entity);
	}

	@RequestMapping("/view")
	public String view(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id)
	{
		User user = WebUtils.getUser();

		HtmlChartWidgetEntity chart = getByIdForView(this.htmlChartWidgetEntityService, user, id);
		convertForFormModel(chart, request);
		setResultDataFormatModel(chart, model);

		setFormModel(model, chart, REQUEST_ACTION_VIEW, SUBMIT_ACTION_NONE);
		
		return "/chart/chart_form";
	}

	@RequestMapping(value = "/delete", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> delete(HttpServletRequest request, HttpServletResponse response,
			@RequestBody String[] ids)
	{
		User user = WebUtils.getUser();

		for (int i = 0; i < ids.length; i++)
		{
			String id = ids[i];
			this.htmlChartWidgetEntityService.deleteById(user, id);
		}

		return operationSuccessResponseEntity(request);
	}

	@RequestMapping("/pagingQuery")
	public String pagingQuery(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model)
	{
		model.addAttribute("serverURL", WebUtils.getServerURL(request));
		model.addAttribute(KEY_REQUEST_ACTION, REQUEST_ACTION_QUERY);
		addAttributeForWriteJson(model, KEY_CURRENT_ANALYSIS_PROJECT,
				getCookieAnalysisProject(request, response, getAnalysisProjectService()));
		
		return "/chart/chart_table";
	}

	@RequestMapping(value = "/select")
	public String select(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model)
	{
		model.addAttribute("serverURL", WebUtils.getServerURL(request));
		setSelectAction(request, model);
		addAttributeForWriteJson(model, KEY_CURRENT_ANALYSIS_PROJECT,
				getCookieAnalysisProject(request, response, getAnalysisProjectService()));
		
		return "/chart/chart_table";
	}

	@RequestMapping(value = "/pagingQueryData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public PagingData<HtmlChartWidgetEntity> pagingQueryData(HttpServletRequest request, HttpServletResponse response,
			final org.springframework.ui.Model springModel,
			@RequestBody(required = false) APIDDataFilterPagingQuery pagingQueryParam) throws Exception
	{
		User user = WebUtils.getUser();
		final APIDDataFilterPagingQuery pagingQuery = inflateAPIDDataFilterPagingQuery(request, pagingQueryParam);

		PagingData<HtmlChartWidgetEntity> pagingData = this.htmlChartWidgetEntityService.pagingQuery(user, pagingQuery,
				pagingQuery.getDataFilter(), pagingQuery.getAnalysisProjectId());
		setChartPluginView(request, pagingData.getItems());

		return pagingData;
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
	@RequestMapping({"/show/{id}/", "/show/{id}"})
	public void show(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@PathVariable("id") String id) throws Exception
	{
		String requestPath = resolvePathAfter(request, "");
		String correctPath = WebUtils.getContextPath(request) + "/chart/show/" + id + "/";
		
		//如果是"/show/{id}"请求，则应跳转到"/show/{id}/"，因为看板内的超链接使用的都是相对路径，
		//如果末尾不加"/"，将会导致这些超链接路径错误
		if(requestPath.indexOf(correctPath) < 0)
		{
			String redirectPath = appendRequestQueryString(correctPath, request);
			response.sendRedirect(redirectPath);
		}
		else
		{
			User user = WebUtils.getUser();
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
	@RequestMapping("/show/{id}/**")
	public void showResource(HttpServletRequest request, HttpServletResponse response, WebRequest webRequest,
			org.springframework.ui.Model model, @PathVariable("id") String id) throws Exception
	{
		User user = WebUtils.getUser();
		HtmlChartWidgetEntity chart = this.htmlChartWidgetEntityService.getById(user, id);

		String resName = resolvePathAfter(request, "/show/" + id + "/");

		if (isEmpty(resName))
		{
			showChart(request, response, model, user, chart);
		}
		else
		{
			// 处理可能的中文资源名
			resName = WebUtils.decodeURL(resName);

			long lastModified = this.templateDashboardWidgetResManager.lastModified(id, resName);
			if (webRequest.checkNotModified(lastModified))
				return;

			setContentTypeByName(request, response, servletContext, resName);
			setCacheControlNoCache(response);

			InputStream in = this.templateDashboardWidgetResManager.getInputStream(id, resName);
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
	@RequestMapping(value = "/showData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ErrorMessageDashboardResult showData(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model, @RequestBody DashboardQueryForm form) throws Exception
	{
		DashboardResult dashboardResult = getDashboardResult(request, response, form,
				this.htmlTplDashboardWidgetHtmlRenderer);

		return new ErrorMessageDashboardResult(dashboardResult, true);
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

		ChartWidgetSourceContext.set(new ChartWidgetSourceContext(user));

		String id = chart.getId();

		String htmlTitle = chart.getName();
		HtmlTplDashboardWidget dashboardWidget = buildHtmlTplDashboardWidget(id);

		Reader templateIn = null;
		Writer out = null;

		try
		{
			templateIn = IOUtil
					.getReader(this.htmlTplDashboardWidgetHtmlRenderer.simpleTemplateContent(IOUtil.CHARSET_UTF_8,
							htmlTitle, "dg-chart-for-show-chart", new String[] { id },
							"dg-chart-disable-setting=\"false\""));

			String responseEncoding = dashboardWidget.getTemplateEncoding();
			response.setCharacterEncoding(responseEncoding);
			response.setContentType(CONTENT_TYPE_HTML);
			out = IOUtil.getBufferedWriter(response.getWriter());

			DefaultHtmlTitleHandler htmlTitleHandler = new DefaultHtmlTitleHandler(
					getMessage(request, "chart.show.htmlTitleSuffix", getMessage(request, "app.name")));
			RenderContext renderContext = createHtmlRenderContext(request, response, out,
					createWebContext(request), buildHtmlTplDashboardImports(request), htmlTitleHandler);

			HtmlTplDashboard dashboard = dashboardWidget.render(renderContext, dashboardWidget.getFirstTemplate(),
					templateIn);

			SessionDashboardInfoManager dashboardInfoManager = getSessionDashboardInfoManagerNotNull(request);
			dashboardInfoManager.put(new DashboardInfo(dashboard));
		}
		finally
		{
			IOUtil.close(templateIn);
			IOUtil.close(out);
		}
	}

	protected HtmlTplDashboardWidget buildHtmlTplDashboardWidget(String chartId)
	{
		return new HtmlTplDashboardWidget(chartId, "index.html",
				this.htmlTplDashboardWidgetHtmlRenderer, this.templateDashboardWidgetResManager);
	}

	protected WebContext createWebContext(HttpServletRequest request)
	{
		HttpSession session = request.getSession();

		WebContext webContext = createInitWebContext(request);

		webContext.addAttribute(DASHBOARD_UPDATE_URL_NAME,
				addJsessionidParam("/chart/showData", session.getId()));
		webContext.addAttribute(DASHBOARD_LOAD_CHART_URL_NAME,
				addJsessionidParam("/dashboard/loadChart", session.getId()));
		addHeartBeatValue(request, webContext);

		return webContext;
	}

	protected void setChartPluginView(HttpServletRequest request, List<HtmlChartWidgetEntity> entities)
	{
		if (entities == null)
			return;

		Locale locale = WebUtils.getLocale(request);
		String themeName = resolveChartPluginIconThemeName(request);

		for (HtmlChartWidgetEntity entity : entities)
		{
			entity.setPlugin(toHtmlChartPluginView(entity.getPlugin(), themeName, locale));
		}
	}

	protected void inflateHtmlChartWidgetEntity(HtmlChartWidgetEntity entity, HttpServletRequest request)
	{
		HtmlChartPlugin htmlChartPlugin = entity.getHtmlChartPlugin();

		if (htmlChartPlugin != null)
		{
			htmlChartPlugin = (HtmlChartPlugin) this.chartPluginManager.get(htmlChartPlugin.getId());

			entity.setHtmlChartPlugin(htmlChartPlugin);
		}

		ChartDataSetVO[] chartDataSetVOs = entity.getChartDataSetVOs();
		if (chartDataSetVOs != null)
		{
			for (ChartDataSetVO vo : chartDataSetVOs)
			{
				DataSetQuery query = vo.getQuery();
				query = getDataSetParamValueConverter().convert(query, vo.getDataSet());
				vo.setQuery(query);
			}
		}
	}

	protected void setCookieAnalysisProject(HttpServletRequest request, HttpServletResponse response,
			HtmlChartWidgetEntity entity)
	{
		setCookieAnalysisProjectIfValid(request, response, this.analysisProjectService, entity);
	}

	protected ResultDataFormat createDefaultResultDataFormat()
	{
		ResultDataFormat rdf = new ResultDataFormat();
		return rdf;
	}

	protected void checkSaveEntity(HtmlChartWidgetEntity chart)
	{
		if (isBlank(chart.getName()))
			throw new IllegalInputException();

		if (isEmpty(chart.getPlugin()))
			throw new IllegalInputException();
	}
	
	protected void convertForFormModel(HtmlChartWidgetEntity entity, HttpServletRequest request)
	{
		HtmlChartPlugin plugin = entity.getHtmlChartPlugin();
		
		if(plugin != null)
			entity.setPlugin(getHtmlChartPluginView(request, plugin.getId()));
		
		entity.setChartDataSets(toChartDataSetViews(entity.getChartDataSets()));
	}
	
	protected void setResultDataFormatModel(HtmlChartWidgetEntity entity, org.springframework.ui.Model model)
	{
		addAttributeForWriteJson(model, "initResultDataFormat",
				(entity.getResultDataFormat() != null ? entity.getResultDataFormat() : createDefaultResultDataFormat()));
		model.addAttribute("enableResultDataFormat", (entity.getResultDataFormat() != null));
	}
}
