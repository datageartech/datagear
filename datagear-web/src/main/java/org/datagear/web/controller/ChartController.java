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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.analysis.ChartPluginManager;
import org.datagear.analysis.DashboardResult;
import org.datagear.analysis.DataSet;
import org.datagear.analysis.DataSetBind;
import org.datagear.analysis.DataSetQuery;
import org.datagear.analysis.ResultDataFormat;
import org.datagear.analysis.TplDashboardWidgetResManager;
import org.datagear.analysis.support.ChartPluginAttributeValueConverter;
import org.datagear.analysis.support.ErrorMessageDashboardResult;
import org.datagear.analysis.support.html.DefaultHtmlTitleHandler;
import org.datagear.analysis.support.html.HtmlChartPlugin;
import org.datagear.analysis.support.html.HtmlTitleHandler;
import org.datagear.analysis.support.html.HtmlTplDashboard;
import org.datagear.analysis.support.html.HtmlTplDashboardRenderContext;
import org.datagear.analysis.support.html.HtmlTplDashboardWidget;
import org.datagear.analysis.support.html.HtmlTplDashboardWidgetHtmlRenderer;
import org.datagear.analysis.support.html.LoadableChartWidgets;
import org.datagear.management.domain.Authorization;
import org.datagear.management.domain.DataSetBindVO;
import org.datagear.management.domain.HtmlChartPluginVo;
import org.datagear.management.domain.HtmlChartWidgetEntity;
import org.datagear.management.domain.User;
import org.datagear.management.service.AnalysisProjectService;
import org.datagear.management.service.DataSetEntityService;
import org.datagear.management.service.HtmlChartWidgetEntityService;
import org.datagear.management.service.HtmlChartWidgetEntityService.ChartWidgetSourceContext;
import org.datagear.management.service.UserService;
import org.datagear.persistence.PagingData;
import org.datagear.util.IDUtil;
import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;
import org.datagear.web.util.OperationMessage;
import org.datagear.web.util.SessionDashboardInfoSupport.DashboardInfo;
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
	@Autowired
	private HtmlChartWidgetEntityService htmlChartWidgetEntityService;

	@Autowired
	private AnalysisProjectService analysisProjectService;

	@Autowired
	private ChartPluginManager chartPluginManager;

	@Autowired
	private HtmlTplDashboardWidgetHtmlRenderer htmlTplDashboardWidgetHtmlRenderer;

	@Autowired
	private TplDashboardWidgetResManager tplDashboardWidgetResManager;

	@Autowired
	private DataSetEntityService dataSetEntityService;

	@Autowired
	private UserService userService;

	private ChartPluginAttributeValueConverter chartPluginAttributeValueConverter = new ChartPluginAttributeValueConverter();

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

	public TplDashboardWidgetResManager getTplDashboardWidgetResManager()
	{
		return tplDashboardWidgetResManager;
	}

	public void setTplDashboardWidgetResManager(
			TplDashboardWidgetResManager tplDashboardWidgetResManager)
	{
		this.tplDashboardWidgetResManager = tplDashboardWidgetResManager;
	}

	public DataSetEntityService getDataSetEntityService()
	{
		return dataSetEntityService;
	}

	public void setDataSetEntityService(DataSetEntityService dataSetEntityService)
	{
		this.dataSetEntityService = dataSetEntityService;
	}

	public UserService getUserService()
	{
		return userService;
	}

	public void setUserService(UserService userService)
	{
		this.userService = userService;
	}

	public ChartPluginAttributeValueConverter getChartPluginAttributeValueConverter()
	{
		return chartPluginAttributeValueConverter;
	}

	public void setChartPluginAttributeValueConverter(
			ChartPluginAttributeValueConverter chartPluginAttributeValueConverter)
	{
		this.chartPluginAttributeValueConverter = chartPluginAttributeValueConverter;
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
		setRequestAnalysisProject(request, response, chart);

		addAttributeForWriteJson(model, "chartPluginVO", null);
		model.addAttribute("initResultDataFormat", createDefaultResultDataFormat());
		model.addAttribute("enableResultDataFormat", false);
		setDisableSaveShowAttr(request, response, model);

		setFormModel(model, chart, REQUEST_ACTION_ADD, SUBMIT_ACTION_SAVE);

		return "/chart/chart_form";
	}

	@RequestMapping("/edit")
	public String edit(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id)
	{
		User user = getCurrentUser();
		
		HtmlChartWidgetEntity chart = getByIdForEdit(this.htmlChartWidgetEntityService, user, id);
		convertForFormModel(chart, request);
		setResultDataFormatModel(chart, model);
		setDisableSaveShowAttr(request, response, model);

		setFormModel(model, chart, REQUEST_ACTION_EDIT, SUBMIT_ACTION_SAVE);
		
		return "/chart/chart_form";
	}

	@RequestMapping("/copy")
	public String copy(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id)
	{
		User user = getCurrentUser();

		HtmlChartWidgetEntity chart = getByIdForView(this.htmlChartWidgetEntityService, user, id);
		setNullAnalysisProjectIfNoPermission(user, chart, getAnalysisProjectService());

		DataSetBind[] dataSetBinds = chart.getDataSetBinds();
		if (dataSetBinds != null)
		{
			List<DataSetBind> dataSetBindsPermited = new ArrayList<DataSetBind>(dataSetBinds.length);

			for (int i = 0; i < dataSetBinds.length; i++)
			{
				DataSetBind dataSetBind = dataSetBinds[i];
				DataSet dataSet = (dataSetBind == null ? null : dataSetBind.getDataSet());
				int permission = (dataSet != null ? getDataSetEntityService().getPermission(user, dataSet.getId())
						: Authorization.PERMISSION_NONE_START);

				// 只添加有权限的
				if (Authorization.canRead(permission))
				{
					dataSetBindsPermited.add(dataSetBind);
				}
			}

			dataSetBinds = dataSetBindsPermited.toArray(new DataSetBind[dataSetBindsPermited.size()]);
		}

		chart.setId(null);
		convertForFormModel(chart, request);
		setResultDataFormatModel(chart, model);
		setDisableSaveShowAttr(request, response, model);

		setFormModel(model, chart, REQUEST_ACTION_COPY, SUBMIT_ACTION_SAVE);
		
		return "/chart/chart_form";
	}

	@RequestMapping(value = "/save", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> save(HttpServletRequest request, HttpServletResponse response,
			@RequestBody HtmlChartWidgetEntity entity)
	{
		User user = getCurrentUser();

		trimAnalysisProjectAwareEntityForSave(entity);

		HtmlChartPluginVo paramPlugin = entity.getPluginVo();

		if (isEmpty(entity.getId()))
		{
			entity.setId(IDUtil.randomIdOnTime20());
			inflateCreateUserAndTime(entity, user);
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
		entity.setPluginVo(paramPlugin);
		
		return optSuccessDataResponseEntity(request, entity);
	}

	@RequestMapping("/view")
	public String view(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id)
	{
		User user = getCurrentUser();

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
		User user = getCurrentUser();

		for (int i = 0; i < ids.length; i++)
		{
			String id = ids[i];
			this.htmlChartWidgetEntityService.deleteById(user, id);
		}

		return optSuccessResponseEntity(request);
	}

	@RequestMapping("/pagingQuery")
	public String pagingQuery(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model)
	{
		model.addAttribute("serverURL", WebUtils.getServerURL(request));
		model.addAttribute(KEY_REQUEST_ACTION, REQUEST_ACTION_QUERY);
		setReadonlyAction(model);
		addAttributeForWriteJson(model, KEY_CURRENT_ANALYSIS_PROJECT,
				getRequestAnalysisProject(request, response, getAnalysisProjectService()));
		
		return "/chart/chart_table";
	}

	@RequestMapping(value = "/select")
	public String select(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model)
	{
		model.addAttribute("serverURL", WebUtils.getServerURL(request));
		setSelectAction(request, model);
		addAttributeForWriteJson(model, KEY_CURRENT_ANALYSIS_PROJECT,
				getRequestAnalysisProject(request, response, getAnalysisProjectService()));
		
		return "/chart/chart_table";
	}

	@RequestMapping(value = "/pagingQueryData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public PagingData<HtmlChartWidgetEntity> pagingQueryData(HttpServletRequest request, HttpServletResponse response,
			final org.springframework.ui.Model springModel,
			@RequestBody(required = false) APIDDataFilterPagingQuery pagingQueryParam) throws Exception
	{
		User user = getCurrentUser();
		final APIDDataFilterPagingQuery pagingQuery = inflateAPIDDataFilterPagingQuery(request, pagingQueryParam);

		PagingData<HtmlChartWidgetEntity> pagingData = this.htmlChartWidgetEntityService.pagingQuery(user, pagingQuery,
				pagingQuery.getDataFilter(), pagingQuery.getAnalysisProjectId());
		setChartPluginView(request, pagingData.getItems());

		return pagingData;
	}

	@RequestMapping(value = "/hasReadPermission", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public boolean[] hasReadPermission(HttpServletRequest request, HttpServletResponse response,
			@RequestBody HasReadPermissionForm form) throws Exception
	{
		if(StringUtil.isEmpty(form.getUserId()))
			throw new IllegalInputException();
		
		String[] chartWidgetIds = form.getChartWidgetIds();
		
		if(isEmpty(chartWidgetIds))
			return new boolean[0];
		
		User user  = this.userService.getByIdSimple(form.getUserId());
		
		if(user == null)
			throw new IllegalInputException();
		
		boolean[] re = new boolean[chartWidgetIds.length];
		
		int[] permissions = getHtmlChartWidgetEntityService().getPermissions(user, chartWidgetIds);
		
		for(int i=0; i<chartWidgetIds.length; i++)
		{
			re[i] = Authorization.canRead(permissions[i]);
		}
		
		return re;
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
	@RequestMapping("/show/{id}/**")
	public void showResource(HttpServletRequest request, HttpServletResponse response, WebRequest webRequest,
			org.springframework.ui.Model model, @PathVariable("id") String id) throws Exception
	{
		User user = getCurrentUser();
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
	@RequestMapping(value = "/showData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ErrorMessageDashboardResult showData(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model, @RequestBody DashboardQueryForm form) throws Exception
	{
		//此处获取ChartWidget不再需要权限控制，应显式移除线程变量
		ChartWidgetSourceContext.remove();
		
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
			HtmlTplDashboardRenderContext renderContext = createRenderContext(request, response, dashboardWidget.getFirstTemplate(), out,
					createWebContext(request), buildHtmlTplDashboardImportsForShow(request), htmlTitleHandler);
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
		//由于图表展示无法自定义页面样式，因此，参数未指定主题时，也应自动匹配系统主题
		return (theme == null || super.isDashboardThemeAuto(request, theme));
	}

	protected HtmlTplDashboardWidget buildHtmlTplDashboardWidget(String chartId)
	{
		return new HtmlTplDashboardWidget(chartId, "index.html",
				this.htmlTplDashboardWidgetHtmlRenderer, this.tplDashboardWidgetResManager);
	}

	protected WebContext createWebContext(HttpServletRequest request)
	{
		WebContext webContext = createInitWebContext(request);

		addUpdateDataValue(request, webContext, "/chart/showData");
		addLoadChartValue(request, webContext, "/dashboard/loadChart");
		addHeartBeatValue(request, webContext, "/dashboard" + HEARTBEAT_TAIL_URL);
		addUnloadValue(request, webContext, "/dashboard" + UNLOAD_TAIL_URL);

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
			entity.setPluginVo(toHtmlChartPluginView(entity.getPluginVo(), themeName, locale));
		}
	}

	protected void inflateHtmlChartWidgetEntity(HtmlChartWidgetEntity entity, HttpServletRequest request)
	{
		// 如果插件不存在，应置为null
		HtmlChartPluginVo pluginVo = entity.getPluginVo();
		String pluginId = (pluginVo == null ? null : pluginVo.getId());
		HtmlChartPlugin plugin = null;
		if (!StringUtil.isEmpty(pluginId))
		{
			plugin = (HtmlChartPlugin) this.chartPluginManager.get(pluginId);
			pluginVo = (plugin == null ? null
					: new HtmlChartPluginVo(plugin.getId(), plugin.getNameLabel()));
			entity.setPluginVo(pluginVo);
		}

		DataSetBindVO[] dataSetBindVOs = entity.getDataSetBindVOs();
		if (dataSetBindVOs != null)
		{
			for (DataSetBindVO vo : dataSetBindVOs)
			{
				DataSetQuery query = vo.getQuery();
				query = getWebDashboardQueryConverter().convert(query, vo.getDataSet());
				vo.setQuery(query);
			}
		}

		Map<String, Object> attrValues = entity.getAttrValues();
		if (attrValues != null && plugin != null)
		{
			attrValues = getChartPluginAttributeValueConverter().convert(attrValues, plugin.getAttributes());
			entity.setAttrValues(attrValues);
		}
	}

	protected void setRequestAnalysisProject(HttpServletRequest request, HttpServletResponse response,
			HtmlChartWidgetEntity entity)
	{
		setRequestAnalysisProjectIfValid(request, response, this.analysisProjectService, entity);
	}

	protected boolean setDisableSaveShowAttr(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model)
	{
		boolean disable = isDisableSaveShow(request, response, model);
		model.addAttribute("disableSaveShow", disable);

		return disable;
	}

	/**
	 * 是否在图表表单页面禁用【保存并展示】功能按钮。
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 */
	protected boolean isDisableSaveShow(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model)
	{
		String pv = request.getParameter("disableSaveShow");
		return ("1".equals(pv) || "true".equals(pv));
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

		if (isEmpty(chart.getPluginVo()))
			throw new IllegalInputException();
	}
	
	protected void convertForFormModel(HtmlChartWidgetEntity entity, HttpServletRequest request)
	{
		HtmlChartPlugin plugin = entity.getPluginVo();
		
		if(plugin != null)
			entity.setPluginVo(getHtmlChartPluginView(request, plugin.getId()));
		
		entity.setDataSetBinds(toDataSetBindViews(entity.getDataSetBinds()));
	}
	
	protected void setResultDataFormatModel(HtmlChartWidgetEntity entity, org.springframework.ui.Model model)
	{
		addAttributeForWriteJson(model, "initResultDataFormat",
				(entity.getResultDataFormat() != null ? entity.getResultDataFormat() : createDefaultResultDataFormat()));
		model.addAttribute("enableResultDataFormat", (entity.getResultDataFormat() != null));
	}
	
	public static class HasReadPermissionForm implements ControllerForm
	{
		private static final long serialVersionUID = 1L;
		
		private String userId;
		private String[] chartWidgetIds;
		
		public HasReadPermissionForm()
		{
			super();
		}

		public String getUserId()
		{
			return userId;
		}

		public void setUserId(String userId)
		{
			this.userId = userId;
		}

		public String[] getChartWidgetIds()
		{
			return chartWidgetIds;
		}

		public void setChartWidgetIds(String[] chartWidgetIds)
		{
			this.chartWidgetIds = chartWidgetIds;
		}
	}
}
