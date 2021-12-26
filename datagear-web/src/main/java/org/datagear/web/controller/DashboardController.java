/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.datagear.analysis.DashboardResult;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.TemplateDashboardWidgetResManager;
import org.datagear.analysis.support.ErrorMessageDashboardResult;
import org.datagear.analysis.support.html.HtmlChart;
import org.datagear.analysis.support.html.HtmlChartWidget;
import org.datagear.analysis.support.html.HtmlChartWidgetJsonWriter;
import org.datagear.analysis.support.html.HtmlTplDashboard;
import org.datagear.analysis.support.html.HtmlTplDashboardRenderAttr.DefaultHtmlTitleHandler;
import org.datagear.analysis.support.html.HtmlTplDashboardRenderAttr.WebContext;
import org.datagear.analysis.support.html.HtmlTplDashboardWidget;
import org.datagear.analysis.support.html.HtmlTplDashboardWidgetRenderer;
import org.datagear.management.domain.AnalysisProject;
import org.datagear.management.domain.HtmlTplDashboardWidgetEntity;
import org.datagear.management.domain.User;
import org.datagear.management.service.AnalysisProjectService;
import org.datagear.management.service.HtmlChartWidgetEntityService.ChartWidgetSourceContext;
import org.datagear.management.service.HtmlTplDashboardWidgetEntityService;
import org.datagear.persistence.PagingData;
import org.datagear.util.FileUtil;
import org.datagear.util.Global;
import org.datagear.util.IDUtil;
import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;
import org.datagear.web.config.ApplicationProperties;
import org.datagear.web.config.CoreConfig;
import org.datagear.web.util.OperationMessage;
import org.datagear.web.util.WebUtils;
import org.datagear.web.vo.APIDDataFilterPagingQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
@RequestMapping("/dashboard")
public class DashboardController extends AbstractDataAnalysisController implements ServletContextAware
{
	/** 加载看板图表参数：看板ID */
	public static final String LOAD_CHART_PARAM_DASHBOARD_ID = "dashboardId";

	/** 加载看板图表参数：图表部件ID */
	public static final String LOAD_CHART_PARAM_CHART_WIDGET_ID = "chartWidgetId";

	public static final String HEARTBEAT_TAIL_URL = "/heartbeat";

	public static final String SERVERTIME_JS_VAR = "_" + Global.PRODUCT_NAME_EN + "ServerTime";

	static
	{
		AuthorizationResourceMetas.registerForShare(HtmlTplDashboardWidgetEntity.AUTHORIZATION_RESOURCE_TYPE,
				"dashboard");
	}

	@Autowired
	private HtmlTplDashboardWidgetEntityService htmlTplDashboardWidgetEntityService;

	@Autowired
	@Qualifier(CoreConfig.NAME_DASHBOARD_GLOBAL_RES_ROOT_DIRECTORY)
	private File dashboardGlobalResRootDirectory;

	@Autowired
	private AnalysisProjectService analysisProjectService;

	@Autowired
	private HtmlChartWidgetJsonWriter htmlChartWidgetJsonWriter;

	@Autowired
	private File tempDirectory;

	private ServletContext servletContext;

	@Autowired
	private ApplicationProperties applicationProperties;

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

	public File getDashboardGlobalResRootDirectory()
	{
		return dashboardGlobalResRootDirectory;
	}

	public void setDashboardGlobalResRootDirectory(File dashboardGlobalResRootDirectory)
	{
		this.dashboardGlobalResRootDirectory = dashboardGlobalResRootDirectory;
	}

	public AnalysisProjectService getAnalysisProjectService()
	{
		return analysisProjectService;
	}

	public void setAnalysisProjectService(AnalysisProjectService analysisProjectService)
	{
		this.analysisProjectService = analysisProjectService;
	}

	public HtmlChartWidgetJsonWriter getHtmlChartWidgetJsonWriter()
	{
		return htmlChartWidgetJsonWriter;
	}

	public void setHtmlChartWidgetJsonWriter(HtmlChartWidgetJsonWriter htmlChartWidgetJsonWriter)
	{
		this.htmlChartWidgetJsonWriter = htmlChartWidgetJsonWriter;
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

	public ApplicationProperties getApplicationProperties()
	{
		return applicationProperties;
	}

	public void setApplicationProperties(ApplicationProperties applicationProperties)
	{
		this.applicationProperties = applicationProperties;
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
		model.addAttribute("dashboardGlobalResUrlPrefix",
				(StringUtil.isEmpty(this.applicationProperties.getDashboardGlobalResUrlPrefix()) ? ""
						: this.applicationProperties.getDashboardGlobalResUrlPrefix()));
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "dashboard.addDashboard");
		model.addAttribute(KEY_FORM_ACTION, "save");

		return "/dashboard/dashboard_form";
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
		model.addAttribute("dashboardGlobalResUrlPrefix",
				(StringUtil.isEmpty(this.applicationProperties.getDashboardGlobalResUrlPrefix()) ? ""
						: this.applicationProperties.getDashboardGlobalResUrlPrefix()));
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "dashboard.editDashboard");
		model.addAttribute(KEY_FORM_ACTION, "save");

		return "/dashboard/dashboard_form";
	}

	@RequestMapping("/copy")
	public String copy(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id) throws Exception
	{
		User user = WebUtils.getUser(request, response);

		HtmlTplDashboardWidgetEntity dashboard = this.htmlTplDashboardWidgetEntityService.getById(user, id);

		if (dashboard == null)
			throw new RecordNotFoundException();

		setNullAnalysisProjectIfNoPermission(user, dashboard, getAnalysisProjectService());

		HtmlTplDashboardWidgetRenderer renderer = getHtmlTplDashboardWidgetEntityService()
				.getHtmlTplDashboardWidgetRenderer();

		String defaultTemplateContent = renderer.simpleTemplateContent(dashboard.getTemplateEncoding());

		model.addAttribute("dashboard", dashboard);
		model.addAttribute("templates", toWriteJsonTemplateModel(dashboard.getTemplates()));
		model.addAttribute("templateName", dashboard.getFirstTemplate());
		model.addAttribute("templateContent", readResourceContent(dashboard, dashboard.getFirstTemplate()));
		model.addAttribute("defaultTemplateContent", defaultTemplateContent);
		model.addAttribute("dashboardGlobalResUrlPrefix",
				(StringUtil.isEmpty(this.applicationProperties.getDashboardGlobalResUrlPrefix()) ? ""
						: this.applicationProperties.getDashboardGlobalResUrlPrefix()));
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "dashboard.addDashboard");
		model.addAttribute(KEY_FORM_ACTION, "save");

		model.addAttribute("copySourceId", id);

		dashboard.setId(null);

		return "/dashboard/dashboard_form";
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

		if (isSaveAdd)
		{
			dashboard.setId(IDUtil.randomIdOnTime20());
			dashboard.setCreateUser(user);
			this.htmlTplDashboardWidgetEntityService.add(user, dashboard);

			if (form.hasCopySourceId())
			{
				TemplateDashboardWidgetResManager dashboardWidgetResManager = this.htmlTplDashboardWidgetEntityService
						.getTemplateDashboardWidgetResManager();

				dashboardWidgetResManager.copyTo(form.getCopySourceId(), dashboard.getId());
			}
		}
		else
		{
			this.htmlTplDashboardWidgetEntityService.update(user, dashboard);
		}

		for (int i = 0; i < resourceNames.length; i++)
			saveResourceContent(dashboard, resourceNames[i], resourceContents[i]);

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
				.getTemplateDashboardWidgetResManager();

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
				.getTemplateDashboardWidgetResManager();

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
				.getTemplateDashboardWidgetResManager();

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
		model.addAttribute("availableCharsetNames", getAvailableCharsetNames());
		model.addAttribute("zipFileNameEncodingDefault", IOUtil.CHARSET_UTF_8);

		return "/dashboard/dashboard_import";
	}

	@RequestMapping(value = "/uploadImportFile", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public Map<String, Object> uploadImportFile(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("file") MultipartFile multipartFile,
			@RequestParam(name = "zipFileNameEncoding", required = false) String zipFileNameEncoding) throws Exception
	{
		String dasboardName = "";
		String dashboardFileName = "";
		List<String> templates = new ArrayList<>();

		File dashboardDirectory = FileUtil.generateUniqueDirectory(this.tempDirectory);

		String fileName = multipartFile.getOriginalFilename();

		if (FileUtil.isExtension(fileName, "zip"))
		{
			ZipInputStream in = IOUtil.getZipInputStream(multipartFile.getInputStream(), zipFileNameEncoding);
			try
			{
				IOUtil.unzip(in, dashboardDirectory);
			}
			finally
			{
				IOUtil.close(in);
			}

			File[] files = dashboardDirectory.listFiles();

			// 如果压缩包里仅有一个文件夹，那么继续往里查找，解决用户压缩包里有多余嵌套目录的情况
			while (files != null && files.length == 1 && files[0].isDirectory())
			{
				dashboardDirectory = files[0];
				files = dashboardDirectory.listFiles();
			}

			inflateTemplates(dashboardDirectory, dashboardDirectory, templates);
			dashboardFileName = FileUtil.getRelativePath(this.tempDirectory, dashboardDirectory);
		}
		else
		{
			File file = FileUtil.getFile(dashboardDirectory, fileName);

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
			dashboardFileName = dashboardDirectory.getName();
		}

		dasboardName = FileUtil.deleteExtension(fileName);

		Map<String, Object> results = new HashMap<>();

		results.put("dashboardName", dasboardName);
		results.put("template", HtmlTplDashboardWidgetEntity.concatTemplates(templates));
		results.put("dashboardFileName", dashboardFileName);

		return results;
	}

	protected void inflateTemplates(File startDirectory, File currentDirectory, List<String> templates)
	{
		if (currentDirectory == null || !currentDirectory.isDirectory())
			return;

		File[] children = currentDirectory.listFiles();

		// "index.html"、"index.htm"靠前排，作为首页模板，文件夹靠后排
		Arrays.sort(children, new Comparator<File>()
		{
			@Override
			public int compare(File o1, File o2)
			{
				String o1Name = o1.getName();
				String o2Name = o2.getName();

				if (o1.isDirectory() && o2.isDirectory())
				{
					return o1Name.compareTo(o2Name);
				}
				else if (o1.isDirectory())
				{
					return 1;
				}
				else if (o2.isDirectory())
				{
					return -1;
				}
				else
				{
					if (o1Name.equalsIgnoreCase("index.html") || o1Name.equalsIgnoreCase("index.htm"))
						return -1;
					else if (o2Name.equalsIgnoreCase("index.html") || o2Name.equalsIgnoreCase("index.htm"))
						return 1;
					else
						return o1Name.compareTo(o2Name);
				}
			}
		});

		for (File child : children)
		{
			if (child.isDirectory())
				inflateTemplates(startDirectory, child, templates);
			else
			{
				String name = child.getName();

				if (FileUtil.isExtension(name, "html") || FileUtil.isExtension(name, "htm"))
				{
					String path = FileUtil.getRelativePath(startDirectory, child);
					path = FileUtil.trimPath(path, FileUtil.PATH_SEPARATOR_SLASH);

					templates.add(path);
				}
			}
		}
	}

	@RequestMapping(value = "/saveImport", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveImport(HttpServletRequest request, HttpServletResponse response,
			@RequestBody SaveImportForm form) throws Exception
	{
		if (isEmpty(form.getName()) || isEmpty(form.getTemplate()) || isEmpty(form.getDashboardFileName()))
			throw new IllegalInputException();

		File uploadDirectory = FileUtil.getDirectory(this.tempDirectory, form.getDashboardFileName(), false);

		if (!uploadDirectory.exists())
			throw new IllegalInputException();

		String[] templates = HtmlTplDashboardWidgetEntity.splitTemplates(form.getTemplate());

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
		dashboard.setTemplateSplit(form.getTemplate());
		dashboard.setTemplateEncoding(templateEncoding);
		dashboard.setName(form.getName());

		if (!isEmpty(form.getAnalysisProject()))
			dashboard.setAnalysisProject(form.getAnalysisProject());

		if (isBlank(dashboard.getName()) || isEmpty(dashboard.getTemplates()))
			throw new IllegalInputException();

		dashboard.setId(IDUtil.randomIdOnTime20());
		dashboard.setCreateUser(user);

		trimAnalysisProjectAwareEntityForSave(dashboard);

		this.htmlTplDashboardWidgetEntityService.add(user, dashboard);

		TemplateDashboardWidgetResManager dashboardWidgetResManager = this.htmlTplDashboardWidgetEntityService
				.getTemplateDashboardWidgetResManager();

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
		model.addAttribute("dashboardGlobalResUrlPrefix",
				(StringUtil.isEmpty(this.applicationProperties.getDashboardGlobalResUrlPrefix()) ? ""
						: this.applicationProperties.getDashboardGlobalResUrlPrefix()));
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "dashboard.viewDashboard");
		model.addAttribute(KEY_READONLY, true);

		return "/dashboard/dashboard_form";
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
				.getTemplateDashboardWidgetResManager();

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

		return "/dashboard/dashboard_grid";
	}

	@RequestMapping(value = "/select")
	public String select(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model)
	{
		User user = WebUtils.getUser(request, response);
		model.addAttribute("currentUser", user);

		model.addAttribute("serverURL", WebUtils.getServerURL(request));

		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "dashboard.selectDashboard");
		model.addAttribute(KEY_SELECT_OPERATION, true);

		return "/dashboard/dashboard_grid";
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
	
	@RequestMapping("/editTemplateVisual")
	public String editTemplateVisual(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model,
			@RequestParam("id") String id, @RequestParam("template") String template,
			@RequestParam("templateContent") String templateContent) throws Exception
	{
		User user = WebUtils.getUser(request, response);

		HtmlTplDashboardWidgetEntity dashboard = this.htmlTplDashboardWidgetEntityService.getByIdForEdit(user, id);

		if (dashboard == null)
			throw new RecordNotFoundException();

		Map<String, String> templateContents = new HashMap<String, String>();
		templateContents.put("sourceContent", templateContent);
		templateContents.put("showContent", templateContent);

		model.addAttribute("template", template);
		model.addAttribute("templateContents", templateContents);
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "dashboard.editTemplateVisual");

		return "/dashboard/dashboard_template_visual";
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
	@RequestMapping({"/show/{id}/", "/show/{id}"})
	public void show(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@PathVariable("id") String id) throws Exception
	{
		String requestPath = resolvePathAfter(request, "");
		String correctPath = WebUtils.getContextPath(request) + "/dashboard/show/" + id + "/";
		
		//如果是"/show/{id}"请求，则应跳转到"/show/{id}/"，因为看板内的超链接使用的都是相对路径，
		//如果末尾不加"/"，将会导致这些超链接路径错误
		if(requestPath.indexOf(correctPath) < 0)
		{
			String redirectPath = appendRequestQueryString(correctPath, request);
			response.sendRedirect(redirectPath);
		}
		else
		{
			User user = WebUtils.getUser(request, response);
	
			HtmlTplDashboardWidgetEntity dashboardWidget = this.htmlTplDashboardWidgetEntityService
						.getHtmlTplDashboardWidget(user, id);
	
			if (dashboardWidget == null)
				throw new RecordNotFoundException();
	
			String firstTemplate = dashboardWidget.getFirstTemplate();
	
			// 如果首页模板是在嵌套路径下，则应重定向到具体路径，避免页面内以相对路径引用的资源找不到
			int subPathSlashIdx = firstTemplate.indexOf(FileUtil.PATH_SEPARATOR_SLASH);
			if (subPathSlashIdx > 0 && subPathSlashIdx < firstTemplate.length() - 1)
			{
				String redirectPath = correctPath + firstTemplate;
				redirectPath = appendRequestQueryString(redirectPath, request);
	
				response.sendRedirect(redirectPath);
			}
			else
			{
				showDashboard(request, response, model, user, dashboardWidget, firstTemplate);
			}
		}
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

		if (StringUtil.isEmpty(resName))
			throw new FileNotFoundException(resName);

		resName = WebUtils.decodeURL(resName);

		if (entity.isTemplate(resName))
		{
			HtmlTplDashboardWidgetEntity dashboardWidget = this.htmlTplDashboardWidgetEntityService
					.getHtmlTplDashboardWidget(user, id);

			showDashboard(request, response, model, user, dashboardWidget, resName);
		}
		else
		{
			InputStream in = null;

			TemplateDashboardWidgetResManager resManager = this.htmlTplDashboardWidgetEntityService
					.getTemplateDashboardWidgetResManager();

			// 优先本地资源
			if (resManager.exists(id, resName))
			{
				setContentTypeByName(request, response, getServletContext(), resName);

				long lastModified = resManager.lastModified(id, resName);
				if (webRequest.checkNotModified(lastModified))
					return;

				in = resManager.getInputStream(id, resName);
			}
			// 其次全局资源
			else
			{
				if (!StringUtil.isEmpty(this.applicationProperties.getDashboardGlobalResUrlPrefix())
						&& resName.startsWith(this.applicationProperties.getDashboardGlobalResUrlPrefix()))
				{
					resName = resName.substring(this.applicationProperties.getDashboardGlobalResUrlPrefix().length());
				}

				File globalRes = FileUtil.getFile(dashboardGlobalResRootDirectory, resName);

				if (globalRes.exists() && !globalRes.isDirectory())
				{
					setContentTypeByName(request, response, getServletContext(), resName);

					long lastModified = globalRes.lastModified();
					if (webRequest.checkNotModified(lastModified))
						return;

					in = IOUtil.getInputStream(globalRes);
				}
			}

			if (in != null)
			{
				in = IOUtil.getBufferedInputStream(in);
				OutputStream out = IOUtil.getBufferedOutputStream(response.getOutputStream());

				try
				{
					IOUtil.write(in, out);
				}
				finally
				{
					IOUtil.close(in);
					IOUtil.close(out);
				}
			}
			else
				throw new FileNotFoundException(resName);
		}
	}

	/**
	 * 展示看板。
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @param user
	 * @param dashboardWidget
	 * @param template
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

		Writer out = null;

		try
		{
			String responseEncoding = dashboardWidget.getTemplateEncoding();

			if (StringUtil.isEmpty(responseEncoding))
				responseEncoding = this.htmlTplDashboardWidgetEntityService
						.getTemplateDashboardWidgetResManager().getDefaultEncoding();

			response.setCharacterEncoding(responseEncoding);
			response.setContentType(CONTENT_TYPE_HTML);
			out = IOUtil.getBufferedWriter(response.getWriter());

			DefaultHtmlTitleHandler htmlTitleHandler = new DefaultHtmlTitleHandler(
					getMessage(request, "dashboard.show.htmlTitleSuffix", getMessage(request, "app.name")),
					getMessage(request, "dashboard.show.htmlTitleSuffixForEmpty", dashboardWidget.getName(),
							getMessage(request, "app.name")));
			RenderContext renderContext = createHtmlRenderContext(request, response, out,
					createWebContext(request), htmlTitleHandler);

			HtmlTplDashboard dashboard = dashboardWidget.render(renderContext, template);

			SessionDashboardInfoManager dashboardInfoManager = getSessionDashboardInfoManagerNotNull(request);
			dashboardInfoManager.put(new DashboardInfo(dashboard));
		}
		finally
		{
			IOUtil.close(out);
			ChartWidgetSourceContext.remove();
		}
	}

	/**
	 * 看板数据。
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @param form
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/showData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ErrorMessageDashboardResult showData(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model, @RequestBody DashboardQueryForm form) throws Exception
	{
		DashboardResult dashboardResult = getDashboardResult(request, response, form,
				this.htmlTplDashboardWidgetEntityService.getHtmlTplDashboardWidgetRenderer());

		return new ErrorMessageDashboardResult(dashboardResult, true);
	}

	/**
	 * 加载多个看板图表的JSON对象数组。
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @param dashboardId
	 * @param chartWidgetIds
	 * @throws Throwable
	 */
	@RequestMapping(value = "/loadChart", produces = CONTENT_TYPE_JSON)
	public void loadChart(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam(LOAD_CHART_PARAM_DASHBOARD_ID) String dashboardId,
			@RequestParam(LOAD_CHART_PARAM_CHART_WIDGET_ID) String[] chartWidgetIds) throws Throwable
	{
		User user = WebUtils.getUser(request, response);

		SessionDashboardInfoManager dashboardInfoManager = getSessionDashboardInfoManagerNotNull(request);
		DashboardInfo dashboardInfo = dashboardInfoManager.get(dashboardId);

		if (dashboardInfo == null)
			throw new RecordNotFoundException();

		HtmlTplDashboardWidgetEntity dashboardWidget = this.htmlTplDashboardWidgetEntityService
				.getHtmlTplDashboardWidget(user, dashboardInfo.getDashboardWidgetId());

		if (dashboardWidget == null)
			throw new RecordNotFoundException();

		// 确保看板创建用户对看板模板内定义的图表有权限
		ChartWidgetSourceContext.set(new ChartWidgetSourceContext(dashboardWidget.getCreateUser()));

		HtmlChartWidget[] chartWidgets = new HtmlChartWidget[chartWidgetIds.length];
		HtmlTplDashboardWidgetRenderer dashboardWidgetRenderer = getHtmlTplDashboardWidgetEntityService()
				.getHtmlTplDashboardWidgetRenderer();
		try
		{
			for (int i = 0; i < chartWidgetIds.length; i++)
				chartWidgets[i] = dashboardWidgetRenderer.getHtmlChartWidget(chartWidgetIds[i]);

			// 不缓存
			response.setContentType(CONTENT_TYPE_JSON);
			PrintWriter out = response.getWriter();

			HtmlChart[] charts = this.htmlChartWidgetJsonWriter.write(out, chartWidgets);

			Map<String, String> chartIdToChartWidgetIds = new HashMap<String, String>();
			for (int i = 0; i < chartWidgetIds.length; i++)
				chartIdToChartWidgetIds.put(charts[i].getId(), chartWidgetIds[i]);

			dashboardInfo.putChartWidgetIds(chartIdToChartWidgetIds);
		}
		finally
		{
			ChartWidgetSourceContext.remove();
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
	@RequestMapping(value = HEARTBEAT_TAIL_URL, produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public Map<String, Object> heartbeat(HttpServletRequest request, HttpServletResponse response) throws Throwable
	{
		Map<String, Object> data = new HashMap<>();
		data.put("heartbeat", true);
		data.put("time", System.currentTimeMillis());

		return data;
	}

	@RequestMapping("/serverTime.js")
	public void serverTimeJs(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		response.setContentType(CONTENT_TYPE_JAVASCRIPT);

		PrintWriter out = response.getWriter();

		out.println("(function(global)");
		out.println("{");

		out.println("global." + SERVERTIME_JS_VAR + "=" + new java.util.Date().getTime() + ";");

		out.println("})(this);");
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

		WebContext webContext = createInitWebContext(request);

		webContext.addAttribute(DASHBOARD_UPDATE_URL_NAME,
				addJsessionidParam("/dashboard/showData", session.getId()));
		webContext.addAttribute(DASHBOARD_LOAD_CHART_URL_NAME,
				addJsessionidParam("/dashboard/loadChart", session.getId()));
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

	/**
	 * 读取指定资源内容。
	 * <p>
	 * 如果资源不存在，将返回空字符串。
	 * </p>
	 * @param widget
	 * @param name
	 * @return
	 * @throws IOException
	 */
	protected String readResourceContent(HtmlTplDashboardWidgetEntity widget, String name) throws IOException
	{
		Reader reader = getResourceReaderNonNull(widget, name);
		return IOUtil.readString(reader, true);
	}

	/**
	 * 保存指定资源内容。
	 * 
	 * @param widget
	 * @param name
	 * @param content
	 * @throws IOException
	 */
	protected void saveResourceContent(HtmlTplDashboardWidgetEntity widget, String name, String content)
			throws IOException
	{
		Writer writer = null;

		try
		{
			writer = getResourceWriter(widget, name);
			writer.write(content);
		}
		finally
		{
			IOUtil.close(writer);
		}
	}

	protected Reader getResourceReaderNonNull(HtmlTplDashboardWidget widget, String name) throws IOException
	{
		Reader reader = null;

		try
		{
			reader = this.htmlTplDashboardWidgetEntityService.getTemplateDashboardWidgetResManager().getReader(widget,
					name);
		}
		catch(FileNotFoundException e)
		{
		}

		if (reader == null)
			reader = IOUtil.getReader("");

		return IOUtil.getBufferedReader(reader);
	}

	protected Writer getResourceWriter(HtmlTplDashboardWidget widget, String template) throws IOException
	{
		return IOUtil.getBufferedWriter(this.htmlTplDashboardWidgetEntityService.getTemplateDashboardWidgetResManager()
				.getWriter(widget, template));
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

	public static class HtmlTplDashboardSaveForm implements ControllerForm
	{
		private static final long serialVersionUID = 1L;

		private HtmlTplDashboardWidgetEntity dashboard;

		private String[] resourceNames;

		private String[] resourceContents;

		private boolean[] resourceIsTemplates;

		private String copySourceId = "";

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

		public boolean hasCopySourceId()
		{
			return !StringUtil.isEmpty(this.copySourceId);
		}

		public String getCopySourceId()
		{
			return copySourceId;
		}

		public void setCopySourceId(String copySourceId)
		{
			this.copySourceId = copySourceId;
		}
	}

	public static class SaveImportForm implements ControllerForm
	{
		private static final long serialVersionUID = 1L;

		private String name;

		private String template;

		private String dashboardFileName;

		private String zipFileNameEncoding;

		private AnalysisProject analysisProject;

		public SaveImportForm()
		{
			super();
		}

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		public String getTemplate()
		{
			return template;
		}

		public void setTemplate(String template)
		{
			this.template = template;
		}

		public String getDashboardFileName()
		{
			return dashboardFileName;
		}

		public void setDashboardFileName(String dashboardFileName)
		{
			this.dashboardFileName = dashboardFileName;
		}

		public String getZipFileNameEncoding()
		{
			return zipFileNameEncoding;
		}

		public void setZipFileNameEncoding(String zipFileNameEncoding)
		{
			this.zipFileNameEncoding = zipFileNameEncoding;
		}

		public AnalysisProject getAnalysisProject()
		{
			return analysisProject;
		}

		public void setAnalysisProject(AnalysisProject analysisProject)
		{
			this.analysisProject = analysisProject;
		}
	}
}
