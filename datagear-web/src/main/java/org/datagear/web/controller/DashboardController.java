/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.datagear.analysis.Chart;
import org.datagear.analysis.DataSetResult;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.TemplateDashboardWidgetResManager;
import org.datagear.analysis.support.ChartWidget;
import org.datagear.analysis.support.ChartWidgetSource;
import org.datagear.analysis.support.DefaultRenderContext;
import org.datagear.analysis.support.html.HtmlChartRenderAttr;
import org.datagear.analysis.support.html.HtmlChartRenderAttr.HtmlChartRenderOption;
import org.datagear.analysis.support.html.HtmlTplDashboard;
import org.datagear.analysis.support.html.HtmlTplDashboardRenderAttr;
import org.datagear.analysis.support.html.HtmlTplDashboardRenderAttr.WebContext;
import org.datagear.analysis.support.html.HtmlTplDashboardWidget;
import org.datagear.analysis.support.html.HtmlTplDashboardWidgetRenderer;
import org.datagear.analysis.support.html.HtmlTplDashboardWidgetRenderer.AddPrefixHtmlTitleHandler;
import org.datagear.management.domain.AnalysisProject;
import org.datagear.management.domain.HtmlTplDashboardWidgetEntity;
import org.datagear.management.domain.User;
import org.datagear.management.service.AnalysisProjectService;
import org.datagear.management.service.HtmlChartWidgetEntityService.ChartWidgetSourceContext;
import org.datagear.management.service.HtmlTplDashboardWidgetEntityService;
import org.datagear.persistence.PagingData;
import org.datagear.util.FileUtil;
import org.datagear.util.IDUtil;
import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;
import org.datagear.web.util.OperationMessage;
import org.datagear.web.util.WebUtils;
import org.datagear.web.vo.APIDDataFilterPagingQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;

/**
 * 看板控制器。
 * 
 * @author datagear@163.com
 *
 */
@Controller
@RequestMapping("/analysis/dashboard")
public class DashboardController extends AbstractDataAnalysisController implements ServletContextAware
{
	/** 加载看板图表参数：看板ID */
	public static final String LOAD_CHART_PARAM_DASHBOARD_ID = "dashboardId";

	/** 加载看板图表参数：图表部件ID */
	public static final String LOAD_CHART_PARAM_CHART_WIDGET_ID = "chartWidgetId";

	/** 加载看板图表参数：图表HTML元素ID */
	public static final String LOAD_CHART_PARAM_CHART_ELEMENT_ID = "chartElementId";

	static
	{
		AuthorizationResourceMetas.registerForShare(HtmlTplDashboardWidgetEntity.AUTHORIZATION_RESOURCE_TYPE,
				"dashboard");
	}

	@Autowired
	private HtmlTplDashboardWidgetEntityService htmlTplDashboardWidgetEntityService;

	@Autowired
	private AnalysisProjectService analysisProjectService;

	@Autowired
	private File tempDirectory;

	private ServletContext servletContext;

	public DashboardController()
	{
		super();
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

	public AnalysisProjectService getAnalysisProjectService()
	{
		return analysisProjectService;
	}

	public void setAnalysisProjectService(AnalysisProjectService analysisProjectService)
	{
		this.analysisProjectService = analysisProjectService;
	}

	public File getTempDirectory()
	{
		return tempDirectory;
	}

	public void setTempDirectory(File tempDirectory)
	{
		this.tempDirectory = tempDirectory;
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
		HtmlTplDashboardWidgetEntity dashboard = new HtmlTplDashboardWidgetEntity();
		setCookieAnalysisProject(request, response, dashboard);

		dashboard.setTemplates(new String[0]);
		dashboard.setTemplateEncoding(HtmlTplDashboardWidget.DEFAULT_TEMPLATE_ENCODING);

		HtmlTplDashboardWidgetRenderer renderer = getHtmlTplDashboardWidgetEntityService()
				.getHtmlTplDashboardWidgetRenderer();

		String templateContent = renderer.simpleTemplateContent(dashboard.getTemplateEncoding());

		model.addAttribute("dashboard", dashboard);
		model.addAttribute("templates", toWriteJsonTemplateModel(dashboard.getTemplates()));
		model.addAttribute("templateName", HtmlTplDashboardWidgetEntity.DEFAULT_TEMPLATES[0]);
		model.addAttribute("templateContent", templateContent);
		model.addAttribute("defaultTemplateContent", templateContent);
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "dashboard.addDashboard");
		model.addAttribute(KEY_FORM_ACTION, "save");

		return "/analysis/dashboard/dashboard_form";
	}

	@RequestMapping("/edit")
	public String edit(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id) throws Exception
	{
		User user = WebUtils.getUser(request, response);

		HtmlTplDashboardWidgetEntity dashboard = this.htmlTplDashboardWidgetEntityService.getByIdForEdit(user, id);

		if (dashboard == null)
			throw new RecordNotFoundException();

		HtmlTplDashboardWidgetRenderer renderer = getHtmlTplDashboardWidgetEntityService()
				.getHtmlTplDashboardWidgetRenderer();

		String defaultTemplateContent = renderer.simpleTemplateContent(dashboard.getTemplateEncoding());

		model.addAttribute("dashboard", dashboard);
		model.addAttribute("templates", toWriteJsonTemplateModel(dashboard.getTemplates()));
		model.addAttribute("templateName", dashboard.getFirstTemplate());
		model.addAttribute("templateContent", readResourceContent(dashboard, dashboard.getFirstTemplate()));
		model.addAttribute("defaultTemplateContent", defaultTemplateContent);
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "dashboard.editDashboard");
		model.addAttribute(KEY_FORM_ACTION, "save");

		return "/analysis/dashboard/dashboard_form";
	}

	@RequestMapping(value = "/save", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> save(HttpServletRequest request, HttpServletResponse response,
			@RequestBody HtmlTplDashboardSaveForm form) throws Exception
	{
		if (isEmpty(form.getDashboard()) || isNull(form.getResourceNames()) || isNull(form.getResourceContents())
				|| isNull(form.getResourceIsTemplates())
				|| form.getResourceNames().length != form.getResourceContents().length
				|| form.getResourceContents().length != form.getResourceIsTemplates().length)
			throw new IllegalInputException();

		User user = WebUtils.getUser(request, response);

		HtmlTplDashboardWidgetEntity dashboard = form.getDashboard();
		boolean isSaveAdd = isEmpty(dashboard.getId());

		String[] templates = dashboard.getTemplates();
		String[] resourceNames = form.getResourceNames();
		String[] resourceContents = form.getResourceContents();
		boolean[] resourceIsTemplates = form.getResourceIsTemplates();

		trimResourceNames(templates);
		trimResourceNames(resourceNames);

		templates = mergeTemplates(templates, resourceNames, resourceIsTemplates);
		dashboard.setTemplates(templates);
		trimAnalysisProjectAwareEntityForSave(dashboard);

		if (isBlank(dashboard.getName()) || isEmpty(templates) || (isSaveAdd && isEmpty(resourceNames)))
			throw new IllegalInputException();

		// 如果编辑了首页模板，则应重新解析编码
		int firstTemplateIndex = StringUtil.search(resourceNames, templates[0]);
		if (firstTemplateIndex > -1)
			dashboard.setTemplateEncoding(resolveTemplateEncoding(resourceContents[firstTemplateIndex]));

		boolean save = false;

		if (isSaveAdd)
		{
			dashboard.setId(IDUtil.randomIdOnTime20());
			dashboard.setCreateUser(user);
			save = this.htmlTplDashboardWidgetEntityService.add(user, dashboard);
		}
		else
		{
			save = this.htmlTplDashboardWidgetEntityService.update(user, dashboard);
		}

		if (save)
		{
			for (int i = 0; i < resourceNames.length; i++)
				saveResourceContent(dashboard, resourceNames[i], resourceContents[i]);
		}

		Map<String, Object> data = new HashMap<>();
		data.put("id", dashboard.getId());
		data.put("templates", templates);

		ResponseEntity<OperationMessage> responseEntity = buildOperationMessageSaveSuccessResponseEntity(request);
		responseEntity.getBody().setData(data);

		return responseEntity;
	}

	@RequestMapping(value = "/saveTemplateNames", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveTemplateNames(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model, @RequestParam("id") String id, @RequestBody String[] templates)
			throws Exception
	{
		if (isEmpty(templates))
			throw new IllegalInputException();

		User user = WebUtils.getUser(request, response);

		HtmlTplDashboardWidgetEntity widget = this.htmlTplDashboardWidgetEntityService.getById(user, id);

		if (widget == null)
			throw new RecordNotFoundException();

		trimResourceNames(templates);
		widget.setTemplates(templates);

		this.htmlTplDashboardWidgetEntityService.update(user, widget);

		Map<String, Object> data = new HashMap<>();
		data.put("id", id);
		data.put("templates", templates);

		ResponseEntity<OperationMessage> responseEntity = buildOperationMessageSaveSuccessResponseEntity(request);
		responseEntity.getBody().setData(data);

		return responseEntity;
	}

	@RequestMapping(value = "/getResourceContent", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public Map<String, Object> getResourceContent(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model, @RequestParam("id") String id,
			@RequestParam("resourceName") String resourceName) throws Exception
	{
		User user = WebUtils.getUser(request, response);

		HtmlTplDashboardWidgetEntity widget = this.htmlTplDashboardWidgetEntityService.getById(user, id);

		if (widget == null)
			throw new RecordNotFoundException();

		resourceName = trimResourceName(resourceName);

		Map<String, Object> data = new HashMap<>();
		data.put("id", id);
		data.put("resourceName", resourceName);
		data.put("resourceContent", readResourceContent(widget, resourceName));

		return data;
	}

	@RequestMapping(value = "/listResources", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public List<String> listResources(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model, @RequestParam("id") String id) throws Exception
	{
		User user = WebUtils.getUser(request, response);

		HtmlTplDashboardWidgetEntity dashboard = this.htmlTplDashboardWidgetEntityService.getById(user, id);

		if (dashboard == null)
			return new ArrayList<>(0);

		TemplateDashboardWidgetResManager dashboardWidgetResManager = this.htmlTplDashboardWidgetEntityService
				.getHtmlTplDashboardWidgetRenderer().getTemplateDashboardWidgetResManager();

		List<String> resources = dashboardWidgetResManager.list(dashboard.getId());

		return resources;
	}

	@RequestMapping(value = "/deleteResource", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> deleteResource(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model, @RequestParam("id") String id, @RequestParam("name") String name)
			throws Exception
	{
		name = trimResourceName(name);

		User user = WebUtils.getUser(request, response);

		HtmlTplDashboardWidgetEntity dashboard = this.htmlTplDashboardWidgetEntityService.getByIdForEdit(user, id);

		if (dashboard == null)
			throw new RecordNotFoundException();

		TemplateDashboardWidgetResManager dashboardWidgetResManager = this.htmlTplDashboardWidgetEntityService
				.getHtmlTplDashboardWidgetRenderer().getTemplateDashboardWidgetResManager();

		dashboardWidgetResManager.delete(id, name);

		return buildOperationMessageSuccessEmptyResponseEntity();
	}

	@RequestMapping(value = "/uploadResourceFile", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public Map<String, Object> uploadResourceFile(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("file") MultipartFile multipartFile) throws Exception
	{
		File tmpDirectory = FileUtil.generateUniqueDirectory(this.tempDirectory);
		String fileName = multipartFile.getOriginalFilename();
		File file = FileUtil.getFile(tmpDirectory, fileName);

		InputStream in = null;
		OutputStream out = null;
		try
		{
			in = multipartFile.getInputStream();
			out = IOUtil.getOutputStream(file);
			IOUtil.write(in, out);
		}
		finally
		{
			IOUtil.close(in);
			IOUtil.close(out);
		}

		String uploadFilePath = FileUtil.getRelativePath(this.tempDirectory, file);

		Map<String, Object> results = new HashMap<>();
		results.put("uploadFilePath", uploadFilePath);
		results.put("fileName", fileName);

		return results;
	}

	@RequestMapping(value = "/saveResourceFile", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveResourceFile(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("id") String id, @RequestParam("resourceFilePath") String resourceFilePath,
			@RequestParam("resourceName") String resourceName) throws Exception
	{
		User user = WebUtils.getUser(request, response);

		HtmlTplDashboardWidgetEntity dashboard = this.htmlTplDashboardWidgetEntityService.getByIdForEdit(user, id);

		if (dashboard == null)
			throw new RecordNotFoundException();

		File uploadFile = FileUtil.getDirectory(this.tempDirectory, resourceFilePath, false);

		if (!uploadFile.exists())
			throw new IllegalInputException();

		TemplateDashboardWidgetResManager dashboardWidgetResManager = this.htmlTplDashboardWidgetEntityService
				.getHtmlTplDashboardWidgetRenderer().getTemplateDashboardWidgetResManager();

		InputStream in = null;
		OutputStream out = null;

		try
		{
			in = IOUtil.getInputStream(uploadFile);
			out = dashboardWidgetResManager.getOutputStream(id, resourceName);

			IOUtil.write(in, out);
		}
		finally
		{
			IOUtil.close(in);
			IOUtil.close(out);
		}

		return buildOperationMessageSaveSuccessResponseEntity(request);
	}

	@RequestMapping("/import")
	public String impt(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model)
	{
		HtmlTplDashboardWidgetEntity dashboard = new HtmlTplDashboardWidgetEntity();
		setCookieAnalysisProject(request, response, dashboard);

		model.addAttribute("dashboard", dashboard);

		return "/analysis/dashboard/dashboard_import";
	}

	@RequestMapping(value = "/uploadImportFile", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public Map<String, Object> uploadImportFile(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("file") MultipartFile multipartFile) throws Exception
	{
		String dasboardName = "";
		String dashboardFileName = "";
		List<String> templates = new ArrayList<>();

		File tmpDirectory = FileUtil.generateUniqueDirectory(this.tempDirectory);

		dashboardFileName = tmpDirectory.getName();

		String fileName = multipartFile.getOriginalFilename();

		if (FileUtil.isExtension(fileName, "zip"))
		{
			ZipInputStream in = IOUtil.getZipInputStream(multipartFile.getInputStream());
			try
			{
				IOUtil.unzip(in, tmpDirectory);
			}
			finally
			{
				IOUtil.close(in);
			}

			File[] files = tmpDirectory.listFiles();
			if (files != null)
			{
				for (File file : files)
				{
					if (file.isDirectory())
						continue;

					String name = file.getName();

					if (name.equalsIgnoreCase("index.html") || name.equalsIgnoreCase("index.htm"))
						templates.add(0, name);
					else if (FileUtil.isExtension(name, "html") || FileUtil.isExtension(name, "htm"))
						templates.add(name);
				}
			}
		}
		else
		{
			File file = FileUtil.getFile(tmpDirectory, fileName);

			InputStream in = null;
			OutputStream out = null;
			try
			{
				in = multipartFile.getInputStream();
				out = IOUtil.getOutputStream(file);
				IOUtil.write(in, out);
			}
			finally
			{
				IOUtil.close(in);
				IOUtil.close(out);
			}

			templates.add(fileName);
		}

		dasboardName = FileUtil.deleteExtension(fileName);

		Map<String, Object> results = new HashMap<>();

		results.put("dashboardName", dasboardName);
		results.put("template", HtmlTplDashboardWidgetEntity.concatTemplates(templates));
		results.put("dashboardFileName", dashboardFileName);

		return results;
	}

	@RequestMapping(value = "/saveImport", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveImport(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("name") String name, @RequestParam("template") String template,
			@RequestParam("dashboardFileName") String dashboardFileName,
			@RequestParam(name = "analysisProject.id", required = false) String analysisProjectId,
			@RequestParam(name = "analysisProject.name", required = false) String analysisProjectName) throws Exception
	{
		File uploadDirectory = FileUtil.getDirectory(this.tempDirectory, dashboardFileName, false);

		if (!uploadDirectory.exists())
			throw new IllegalInputException();

		String[] templates = HtmlTplDashboardWidgetEntity.splitTemplates(template);

		if (isEmpty(templates))
			throw new IllegalInputException();

		for (String fileName : templates)
		{
			File templateFile = FileUtil.getFile(uploadDirectory, fileName);

			if (!templateFile.exists() || templateFile.isDirectory())
				return buildOperationMessageFailResponseEntity(request, HttpStatus.BAD_REQUEST,
						"dashboard.import.templateFileNotExists", fileName);
		}

		String templateEncoding = resolveTemplateEncoding(FileUtil.getFile(uploadDirectory, templates[0]));

		User user = WebUtils.getUser(request, response);

		HtmlTplDashboardWidgetEntity dashboard = new HtmlTplDashboardWidgetEntity();
		dashboard.setTemplateSplit(template);
		dashboard.setTemplateEncoding(templateEncoding);
		dashboard.setName(name);

		if (!isEmpty(analysisProjectId))
		{
			AnalysisProject analysisProject = new AnalysisProject();
			analysisProject.setId(analysisProjectId);
			analysisProject.setName(analysisProjectName);
			dashboard.setAnalysisProject(analysisProject);
		}

		if (isBlank(dashboard.getName()) || isEmpty(dashboard.getTemplates()))
			throw new IllegalInputException();

		dashboard.setId(IDUtil.randomIdOnTime20());
		dashboard.setCreateUser(user);

		trimAnalysisProjectAwareEntityForSave(dashboard);

		this.htmlTplDashboardWidgetEntityService.add(user, dashboard);

		TemplateDashboardWidgetResManager dashboardWidgetResManager = this.htmlTplDashboardWidgetEntityService
				.getHtmlTplDashboardWidgetRenderer().getTemplateDashboardWidgetResManager();

		dashboardWidgetResManager.copyFrom(dashboard.getId(), uploadDirectory);

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
		model.addAttribute("templates", toWriteJsonTemplateModel(dashboard.getTemplates()));
		model.addAttribute("templateName", dashboard.getFirstTemplate());
		model.addAttribute("templateContent", readResourceContent(dashboard, dashboard.getFirstTemplate()));
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "dashboard.viewDashboard");
		model.addAttribute(KEY_READONLY, true);

		return "/analysis/dashboard/dashboard_form";
	}

	@RequestMapping("/export")
	public void export(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id) throws Exception
	{
		User user = WebUtils.getUser(request, response);

		HtmlTplDashboardWidgetEntity dashboard = this.htmlTplDashboardWidgetEntityService.getById(user, id);

		if (dashboard == null)
			throw new RecordNotFoundException();

		TemplateDashboardWidgetResManager dashboardWidgetResManager = this.htmlTplDashboardWidgetEntityService
				.getHtmlTplDashboardWidgetRenderer().getTemplateDashboardWidgetResManager();

		File tmpDirectory = FileUtil.generateUniqueDirectory(this.tempDirectory);
		dashboardWidgetResManager.copyTo(dashboard.getId(), tmpDirectory);

		response.addHeader("Content-Disposition",
				"attachment;filename=" + toResponseAttachmentFileName(request, response, dashboard.getName() + ".zip"));
		response.setContentType("application/octet-stream");

		ZipOutputStream zout = IOUtil.getZipOutputStream(response.getOutputStream());

		try
		{
			IOUtil.writeFileToZipOutputStream(zout, tmpDirectory, "");
		}
		finally
		{
			zout.flush();
			zout.close();
		}
	}

	@RequestMapping(value = "/delete", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> delete(HttpServletRequest request, HttpServletResponse response,
			@RequestBody String[] ids)
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
	public String pagingQuery(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model)
	{
		User user = WebUtils.getUser(request, response);
		model.addAttribute("currentUser", user);

		model.addAttribute("serverURL", WebUtils.getServerURL(request));

		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "dashboard.manageDashboard");

		return "/analysis/dashboard/dashboard_grid";
	}

	@RequestMapping(value = "/select")
	public String select(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model)
	{
		User user = WebUtils.getUser(request, response);
		model.addAttribute("currentUser", user);

		model.addAttribute("serverURL", WebUtils.getServerURL(request));

		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "dashboard.selectDashboard");
		model.addAttribute(KEY_SELECT_OPERATION, true);

		return "/analysis/dashboard/dashboard_grid";
	}

	@RequestMapping(value = "/pagingQueryData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public PagingData<HtmlTplDashboardWidgetEntity> pagingQueryData(HttpServletRequest request,
			HttpServletResponse response, final org.springframework.ui.Model springModel,
			@RequestBody(required = false) APIDDataFilterPagingQuery pagingQueryParam) throws Exception
	{
		User user = WebUtils.getUser(request, response);
		final APIDDataFilterPagingQuery pagingQuery = inflateAPIDDataFilterPagingQuery(request, pagingQueryParam);

		PagingData<HtmlTplDashboardWidgetEntity> pagingData = this.htmlTplDashboardWidgetEntityService.pagingQuery(user,
				pagingQuery, pagingQuery.getDataFilter(), pagingQuery.getAnalysisProjectId());

		return pagingData;
	}

	/**
	 * 展示看板首页。
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @param id
	 * @throws Exception
	 */
	@RequestMapping("/show/{id}/")
	public void show(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@PathVariable("id") String id) throws Exception
	{
		User user = WebUtils.getUser(request, response);
		HtmlTplDashboardWidgetEntity dashboardWidget = this.htmlTplDashboardWidgetEntityService
				.getHtmlTplDashboardWidget(user, id);

		showDashboard(request, response, model, user, dashboardWidget, dashboardWidget.getFirstTemplate());
	}

	/**
	 * 加载看板资源。
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
		User user = WebUtils.getUser(request, response);
		HtmlTplDashboardWidgetEntity entity = this.htmlTplDashboardWidgetEntityService.getById(user, id);

		if (entity == null)
			throw new RecordNotFoundException();

		String resName = resolvePathAfter(request, "/show/" + id + "/");

		if (entity.isTemplate(resName))
		{
			HtmlTplDashboardWidgetEntity dashboardWidget = this.htmlTplDashboardWidgetEntityService
					.getHtmlTplDashboardWidget(user, id);

			showDashboard(request, response, model, user, dashboardWidget, resName);
		}
		else
		{
			TemplateDashboardWidgetResManager resManager = this.htmlTplDashboardWidgetEntityService
					.getHtmlTplDashboardWidgetRenderer().getTemplateDashboardWidgetResManager();

			if (!resManager.exists(id, resName))
				throw new FileNotFoundException(resName);

			setContentTypeByName(request, response, getServletContext(), resName);

			long lastModified = resManager.lastModified(id, resName);
			if (webRequest.checkNotModified(lastModified))
				return;

			InputStream in = resManager.getInputStream(id, resName);
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
	 * 展示看板。
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @param id
	 * @throws Exception
	 */
	protected void showDashboard(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model, User user, HtmlTplDashboardWidgetEntity dashboardWidget,
			String template) throws Exception
	{
		if (dashboardWidget == null)
			throw new RecordNotFoundException();

		// 确保看板创建用户对看板模板内定义的图表有权限
		ChartWidgetSourceContext.set(new ChartWidgetSourceContext(dashboardWidget.getCreateUser()));

		try
		{
			TemplateDashboardWidgetResManager dashboardWidgetResManager = this.htmlTplDashboardWidgetEntityService
					.getHtmlTplDashboardWidgetRenderer().getTemplateDashboardWidgetResManager();

			String responseEncoding = dashboardWidget.getTemplateEncoding();

			if (StringUtil.isEmpty(responseEncoding))
				responseEncoding = dashboardWidgetResManager.getDefaultEncoding();

			response.setCharacterEncoding(responseEncoding);
			response.setContentType(CONTENT_TYPE_HTML);

			HtmlTplDashboardRenderAttr renderAttr = createHtmlTplDashboardRenderAttr();
			RenderContext renderContext = createHtmlRenderContext(request, response, renderAttr,
					createWebContext(request),
					getHtmlTplDashboardWidgetEntityService().getHtmlTplDashboardWidgetRenderer());
			AddPrefixHtmlTitleHandler htmlTitleHandler = new AddPrefixHtmlTitleHandler(
					getMessage(request, "dashboard.show.htmlTitlePrefix", getMessage(request, "app.name")));
			renderAttr.setHtmlTitleHandler(renderContext, htmlTitleHandler);

			HtmlTplDashboard dashboard = dashboardWidget.render(renderContext, template);

			SessionHtmlTplDashboardManager dashboardManager = getSessionHtmlTplDashboardManagerNotNull(request);
			dashboardManager.put(dashboard);
		}
		finally
		{
			ChartWidgetSourceContext.remove();
		}
	}

	/**
	 * 看板数据。
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @param id
	 * @throws Exception
	 */
	@RequestMapping(value = "/showData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public Map<String, DataSetResult[]> showData(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model, @RequestBody Map<String, ?> paramData) throws Exception
	{
		WebContext webContext = createWebContext(request);
		return getDashboardData(request, response, model, webContext, paramData);
	}

	/**
	 * 加载看板图表。
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @param dashbaordId
	 * @throws Throwable
	 */
	@RequestMapping(value = "/loadChart", produces = CONTENT_TYPE_JSON)
	public void loadChart(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam(LOAD_CHART_PARAM_DASHBOARD_ID) String dashboardId,
			@RequestParam(LOAD_CHART_PARAM_CHART_WIDGET_ID) String chartWidgetId,
			@RequestParam(LOAD_CHART_PARAM_CHART_ELEMENT_ID) String chartElementId) throws Throwable
	{
		SessionHtmlTplDashboardManager dashboardManager = getSessionHtmlTplDashboardManagerNotNull(request);
		HtmlTplDashboard dashboard = dashboardManager.get(dashboardId);

		if (dashboard == null)
			throw new RecordNotFoundException();

		HtmlTplDashboardWidgetEntity dashboardWidget = (HtmlTplDashboardWidgetEntity) dashboard.getWidget();

		// 确保看板创建用户对看板模板内定义的图表有权限
		ChartWidgetSourceContext.set(new ChartWidgetSourceContext(dashboardWidget.getCreateUser()));

		ChartWidgetSource chartWidgetSource = getHtmlTplDashboardWidgetEntityService()
				.getHtmlTplDashboardWidgetRenderer().getChartWidgetSource();

		ChartWidget chartWidget = chartWidgetSource.getChartWidget(chartWidgetId);

		if (chartWidget == null)
			throw new RecordNotFoundException();

		// 不缓存
		response.setContentType(CONTENT_TYPE_JSON);

		RenderContext renderContext = new DefaultRenderContext();
		HtmlChartRenderAttr renderAttr = new HtmlChartRenderAttr();
		PrintWriter out = response.getWriter();
		HtmlChartRenderOption renderOption = new HtmlChartRenderOption(chartElementId,
				"{\"id\": \"" + WebUtils.escapeJavaScriptStringValue(chartWidget.getPlugin().getId()) + "\"}",
				"undefined", "{}");
		renderOption.setNotWriteChartElement(true);
		renderOption.setNotWritePluginObject(true);
		renderOption.setNotWriteRenderContextObject(true);
		renderOption.setNotWriteScriptTag(true);
		renderOption.setNotWriteInvoke(true);
		renderOption.setWriteChartJson(true);
		renderAttr.inflate(renderContext, out, renderOption);

		Chart chart = chartWidget.render(renderContext);

		synchronized (dashboard)
		{
			List<Chart> charts = dashboard.getCharts();
			List<Chart> newCharts = (charts == null || charts.isEmpty() ? new ArrayList<>() : new ArrayList<>(charts));
			newCharts.add(chart);
			dashboard.setCharts(newCharts);
		}
	}

	/**
	 * 看板心跳。
	 * <p>
	 * 看板页面有停留较长时间再操作的场景，此时可能会因为会话超时导致操作失败，所以这里添加心跳请求，避免会话超时。
	 * </p>
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @param dashbaordId
	 * @throws Throwable
	 */
	@RequestMapping(value = "/heartbeat", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public Map<String, Object> heartbeat(HttpServletRequest request, HttpServletResponse response) throws Throwable
	{
		Map<String, Object> data = new HashMap<>();
		data.put("heartbeat", true);
		data.put("time", System.currentTimeMillis());

		return data;
	}

	/**
	 * 解析HTML模板的字符编码。
	 * 
	 * @param templateIn
	 * @return
	 * @throws IOException
	 */
	protected String resolveTemplateEncoding(String templateContent) throws IOException
	{
		String templateEncoding = null;

		if (templateContent != null)
		{
			Reader in = null;
			try
			{
				in = IOUtil.getReader(templateContent);

				templateEncoding = this.htmlTplDashboardWidgetEntityService.getHtmlTplDashboardWidgetRenderer()
						.resolveCharset(in);
			}
			finally
			{
				IOUtil.close(in);
			}
		}

		if (StringUtil.isEmpty(templateEncoding))
			templateEncoding = HtmlTplDashboardWidget.DEFAULT_TEMPLATE_ENCODING;

		return templateEncoding;
	}

	/**
	 * 解析HTML模板文件的字符编码。
	 * 
	 * @param templateIn
	 * @return
	 * @throws IOException
	 */
	protected String resolveTemplateEncoding(File templateFile) throws IOException
	{
		String templateEncoding = null;

		InputStream in = null;
		try
		{
			in = IOUtil.getInputStream(templateFile);

			templateEncoding = this.htmlTplDashboardWidgetEntityService.getHtmlTplDashboardWidgetRenderer()
					.resolveCharset(in);
		}
		finally
		{
			IOUtil.close(in);
		}

		if (StringUtil.isEmpty(templateEncoding))
			templateEncoding = HtmlTplDashboardWidget.DEFAULT_TEMPLATE_ENCODING;

		return templateEncoding;
	}

	protected WebContext createWebContext(HttpServletRequest request)
	{
		HttpSession session = request.getSession();

		String contextPath = WebUtils.getContextPath(request);
		WebContext webContext = new WebContext(contextPath,
				addJsessionidParam(contextPath + "/analysis/dashboard/showData", session.getId()),
				addJsessionidParam(contextPath + "/analysis/dashboard/loadChart", session.getId()));

		webContext.setExtraValues(new HashMap<String, Object>());
		addHeartBeatValue(request, webContext);

		return webContext;
	}

	protected void checkSaveEntity(HtmlTplDashboardWidgetEntity widget)
	{
		if (isBlank(widget.getName()))
			throw new IllegalInputException();

		if (isEmpty(widget.getTemplates()))
			throw new IllegalInputException();
	}

	protected String readResourceContent(HtmlTplDashboardWidgetEntity widget, String templateName) throws IOException
	{
		String templateContent = this.htmlTplDashboardWidgetEntityService.getHtmlTplDashboardWidgetRenderer()
				.readResourceContent(widget, templateName);

		return templateContent;
	}

	protected void saveResourceContent(HtmlTplDashboardWidgetEntity widget, String name, String content)
			throws IOException
	{
		this.htmlTplDashboardWidgetEntityService.getHtmlTplDashboardWidgetRenderer().saveResourceContent(widget, name,
				content);
	}

	protected String[] mergeTemplates(String[] templates, String[] resourceNames, boolean[] resourceIsTemplates)
	{
		List<String> ts = new ArrayList<>();
		if (templates != null)
			ts.addAll(Arrays.asList(templates));

		boolean autoFirstTemplate = ts.isEmpty();

		for (int i = 0; i < resourceNames.length; i++)
		{
			if (resourceIsTemplates[i] && !ts.contains(resourceNames[i]))
				ts.add(resourceNames[i]);
		}

		if (autoFirstTemplate)
		{
			int firstTempalteIdx = -1;

			for (int i = 0; i < ts.size(); i++)
			{
				String tn = ts.get(i);

				if (tn.equalsIgnoreCase("index.html") || tn.equalsIgnoreCase("index.htm"))
				{
					firstTempalteIdx = i;
					break;
				}
			}

			if (firstTempalteIdx > 0)
			{
				String tn = ts.remove(firstTempalteIdx);
				ts.add(0, tn);
			}
		}

		return ts.toArray(new String[ts.size()]);
	}

	protected void trimResourceNames(String[] resourceNames)
	{
		if (resourceNames == null)
			return;

		for (int i = 0; i < resourceNames.length; i++)
			resourceNames[i] = trimResourceName(resourceNames[i]);
	}

	protected String trimResourceName(String resourceName)
	{
		if (StringUtil.isEmpty(resourceName))
			return "";

		resourceName = FileUtil.trimPath(resourceName, FileUtil.PATH_SEPARATOR_SLASH);
		if (resourceName.startsWith(FileUtil.PATH_SEPARATOR_SLASH))
			resourceName = resourceName.substring(1);

		return resourceName;
	}

	protected void setCookieAnalysisProject(HttpServletRequest request, HttpServletResponse response,
			HtmlTplDashboardWidgetEntity entity)
	{
		setCookieAnalysisProjectIfValid(request, response, this.analysisProjectService, entity);
	}

	public static class HtmlTplDashboardSaveForm
	{
		private HtmlTplDashboardWidgetEntity dashboard;

		private String[] resourceNames;

		private String[] resourceContents;

		private boolean[] resourceIsTemplates;

		public HtmlTplDashboardSaveForm()
		{
			super();
		}

		public HtmlTplDashboardSaveForm(HtmlTplDashboardWidgetEntity dashboard, String[] resourceNames,
				String[] resourceContents, boolean[] resourceIsTemplates)
		{
			super();
			this.dashboard = dashboard;
			this.resourceNames = resourceNames;
			this.resourceContents = resourceContents;
			this.resourceIsTemplates = resourceIsTemplates;
		}

		public HtmlTplDashboardWidgetEntity getDashboard()
		{
			return dashboard;
		}

		public void setDashboard(HtmlTplDashboardWidgetEntity dashboard)
		{
			this.dashboard = dashboard;
		}

		public String[] getResourceNames()
		{
			return resourceNames;
		}

		public void setResourceNames(String[] resourceNames)
		{
			this.resourceNames = resourceNames;
		}

		public String[] getResourceContents()
		{
			return resourceContents;
		}

		public void setResourceContents(String[] resourceContents)
		{
			this.resourceContents = resourceContents;
		}

		public boolean[] getResourceIsTemplates()
		{
			return resourceIsTemplates;
		}

		public void setResourceIsTemplates(boolean[] resourceIsTemplates)
		{
			this.resourceIsTemplates = resourceIsTemplates;
		}
	}
}
