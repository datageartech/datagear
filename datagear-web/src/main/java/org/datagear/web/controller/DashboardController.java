/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.controller;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.analysis.support.DashboardWidgetResManager;
import org.datagear.analysis.support.html.DefaultHtmlRenderContext;
import org.datagear.analysis.support.html.HtmlRenderAttributes;
import org.datagear.analysis.support.html.HtmlRenderContext;
import org.datagear.analysis.support.html.HtmlTplDashboardWidget;
import org.datagear.management.domain.HtmlTplDashboardWidgetEntity;
import org.datagear.management.domain.User;
import org.datagear.management.service.HtmlTplDashboardWidgetEntityService;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.PagingQuery;
import org.datagear.util.IDUtil;
import org.datagear.util.IOUtil;
import org.datagear.web.OperationMessage;
import org.datagear.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

/**
 * 看板控制器。
 * 
 * @author datagear@163.com
 *
 */
@Controller
@RequestMapping("/analysis/dashboard")
public class DashboardController extends AbstractDataAnalysisController
{
	@Autowired
	private HtmlTplDashboardWidgetEntityService htmlTplDashboardWidgetEntityService;

	public DashboardController()
	{
		super();
	}

	public DashboardController(HtmlTplDashboardWidgetEntityService htmlTplDashboardWidgetEntityService)
	{
		super();
		this.htmlTplDashboardWidgetEntityService = htmlTplDashboardWidgetEntityService;
	}

	public HtmlTplDashboardWidgetEntityService getHtmlTplDashboardWidgetEntityService()
	{
		return htmlTplDashboardWidgetEntityService;
	}

	public void setHtmlTplDashboardWidgetEntityService(
			HtmlTplDashboardWidgetEntityService htmlTplDashboardWidgetEntityService)
	{
		this.htmlTplDashboardWidgetEntityService = htmlTplDashboardWidgetEntityService;
	}

	@RequestMapping("/add")
	public String add(HttpServletRequest request, org.springframework.ui.Model model)
	{
		HtmlTplDashboardWidgetEntity dashboard = new HtmlTplDashboardWidgetEntity();

		model.addAttribute("dashboard", dashboard);
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "dashboard.addDashboard");
		model.addAttribute(KEY_FORM_ACTION, "saveAdd");

		return "/analysis/dashboard/dashboard_form";
	}

	@RequestMapping(value = "/saveAdd", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAdd(HttpServletRequest request, HttpServletResponse response,
			HtmlTplDashboardWidgetEntity dashboard, @RequestParam("templateContent") String templateContent)
			throws Exception
	{
		User user = WebUtils.getUser(request, response);

		dashboard.setTemplate(HtmlTplDashboardWidgetEntity.DEFAULT_TEMPLATE);

		checkSaveEntity(dashboard);

		dashboard.setId(IDUtil.uuid());
		dashboard.setCreateUser(user);

		boolean add = this.htmlTplDashboardWidgetEntityService.add(user, dashboard);

		if (add)
			saveTemplateContent(dashboard, templateContent);

		return buildOperationMessageSaveSuccessResponseEntity(request);
	}

	@RequestMapping("/edit")
	public String edit(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id) throws Exception
	{
		User user = WebUtils.getUser(request, response);

		HtmlTplDashboardWidgetEntity dashboard = this.htmlTplDashboardWidgetEntityService.getByIdForEdit(user, id);

		if (dashboard == null)
			throw new RecordNotFoundException();

		model.addAttribute("dashboard", dashboard);
		readAndSetTemplateContent(dashboard, model);
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "dashboard.editDashboard");
		model.addAttribute(KEY_FORM_ACTION, "saveEdit");

		return "/analysis/dashboard/dashboard_form";
	}

	@RequestMapping(value = "/saveEdit", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveEdit(HttpServletRequest request, HttpServletResponse response,
			HtmlTplDashboardWidgetEntity dashboard, @RequestParam("templateContent") String templateContent)
			throws Exception
	{
		User user = WebUtils.getUser(request, response);

		dashboard.setTemplate(HtmlTplDashboardWidgetEntity.DEFAULT_TEMPLATE);

		checkSaveEntity(dashboard);

		boolean updated = this.htmlTplDashboardWidgetEntityService.update(user, dashboard);

		if (updated)
			saveTemplateContent(dashboard, templateContent);

		return buildOperationMessageSaveSuccessResponseEntity(request);
	}

	@RequestMapping("/view")
	public String view(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id) throws Exception
	{
		User user = WebUtils.getUser(request, response);

		HtmlTplDashboardWidgetEntity dashboard = this.htmlTplDashboardWidgetEntityService.getById(user, id);

		if (dashboard == null)
			throw new RecordNotFoundException();

		model.addAttribute("dashboard", dashboard);
		readAndSetTemplateContent(dashboard, model);
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "dashboard.viewDashboard");
		model.addAttribute(KEY_READONLY, true);

		return "/analysis/dashboard/dashboard_form";
	}

	@RequestMapping(value = "/delete", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> delete(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("id") String[] ids)
	{
		User user = WebUtils.getUser(request, response);

		for (int i = 0; i < ids.length; i++)
		{
			String id = ids[i];
			this.htmlTplDashboardWidgetEntityService.deleteById(user, id);
		}

		return buildOperationMessageDeleteSuccessResponseEntity(request);
	}

	@RequestMapping("/pagingQuery")
	public String pagingQuery(HttpServletRequest request, org.springframework.ui.Model model)
	{
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "dashboard.manageDashboard");

		return "/analysis/dashboard/dashboard_grid";
	}

	@RequestMapping(value = "/select")
	public String select(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model)
	{
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "dashboard.selectDashboard");
		model.addAttribute(KEY_SELECTONLY, true);

		return "/analysis/dashboard/dashboard_grid";
	}

	@RequestMapping(value = "/pagingQueryData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public PagingData<HtmlTplDashboardWidgetEntity> pagingQueryData(HttpServletRequest request,
			HttpServletResponse response, final org.springframework.ui.Model springModel) throws Exception
	{
		User user = WebUtils.getUser(request, response);

		PagingQuery pagingQuery = getPagingQuery(request);

		PagingData<HtmlTplDashboardWidgetEntity> pagingData = this.htmlTplDashboardWidgetEntityService.pagingQuery(user,
				pagingQuery);

		return pagingData;
	}

	/**
	 * 展示看板。
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @param id
	 * @throws Exception
	 */
	@RequestMapping({ "/show/{id}/", "/show/{id}/index" })
	public void show(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@PathVariable("id") String id) throws Exception
	{
		User user = WebUtils.getUser(request, response);

		HtmlTplDashboardWidget<HtmlRenderContext> dashboardWidget = this.htmlTplDashboardWidgetEntityService
				.getHtmlTplDashboardWidget(user, id);

		if (dashboardWidget == null)
			throw new RecordNotFoundException();

		response.setCharacterEncoding(
				this.htmlTplDashboardWidgetEntityService.getHtmlTplDashboardWidgetRenderer().getWriterEncoding());

		Writer out = response.getWriter();

		DefaultHtmlRenderContext renderContext = new DefaultHtmlRenderContext(out);
		renderContext.setContextPath(request.getContextPath());
		HtmlRenderAttributes.setRenderStyle(renderContext, resolveRenderStyle(request));

		dashboardWidget.render(renderContext);
	}

	/**
	 * 从看板目录中加载看板资源。
	 * 
	 * @param request
	 * @param response
	 * @param webRequest
	 * @param model
	 * @param id
	 * @throws Exception
	 */
	@RequestMapping("/show/{id}/**/*")
	public void showResource(HttpServletRequest request, HttpServletResponse response, WebRequest webRequest,
			org.springframework.ui.Model model, @PathVariable("id") String id) throws Exception
	{
		String pathInfo = request.getPathInfo();
		String resPath = pathInfo.substring(pathInfo.indexOf(id) + id.length() + 1);

		DashboardWidgetResManager resManager = this.htmlTplDashboardWidgetEntityService
				.getHtmlTplDashboardWidgetRenderer().getDashboardWidgetResManager();

		File resFile = resManager.getFile(id, resPath);

		if (!resFile.exists())
			throw new FileNotFoundException(resPath);

		long lastModified = resFile.lastModified();
		if (webRequest.checkNotModified(lastModified))
			return;

		OutputStream out = response.getOutputStream();

		IOUtil.write(resFile, out);
	}

	protected void checkSaveEntity(HtmlTplDashboardWidgetEntity dashboard)
	{
		if (isBlank(dashboard.getName()))
			throw new IllegalInputException();

		if (isEmpty(dashboard.getTemplate()))
			throw new IllegalInputException();
	}

	protected String readAndSetTemplateContent(HtmlTplDashboardWidgetEntity dashboard,
			org.springframework.ui.Model model) throws IOException
	{
		String templateContent = this.htmlTplDashboardWidgetEntityService.getHtmlTplDashboardWidgetRenderer()
				.readTemplateContent(dashboard);

		model.addAttribute("templateContent", templateContent);

		return templateContent;
	}

	protected void saveTemplateContent(HtmlTplDashboardWidgetEntity dashboard, String templateContent)
			throws IOException
	{
		this.htmlTplDashboardWidgetEntityService.getHtmlTplDashboardWidgetRenderer().saveTemplateContent(dashboard,
				templateContent);
	}
}
