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
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
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
import org.datagear.analysis.support.html.HtmlTplDashboardImport;
import org.datagear.analysis.support.html.HtmlTplDashboardRenderAttr.DefaultHtmlTitleHandler;
import org.datagear.analysis.support.html.HtmlTplDashboardRenderAttr.WebContext;
import org.datagear.analysis.support.html.HtmlTplDashboardWidget;
import org.datagear.analysis.support.html.HtmlTplDashboardWidgetRenderer;
import org.datagear.management.domain.AnalysisProject;
import org.datagear.management.domain.Authorization;
import org.datagear.management.domain.DashboardShareSet;
import org.datagear.management.domain.HtmlTplDashboardWidgetEntity;
import org.datagear.management.domain.User;
import org.datagear.management.service.AnalysisProjectService;
import org.datagear.management.service.DashboardShareSetService;
import org.datagear.management.service.HtmlChartWidgetEntityService.ChartWidgetSourceContext;
import org.datagear.management.service.HtmlTplDashboardWidgetEntityService;
import org.datagear.management.service.PermissionDeniedException;
import org.datagear.persistence.PagingData;
import org.datagear.util.FileUtil;
import org.datagear.util.Global;
import org.datagear.util.IDUtil;
import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;
import org.datagear.util.html.DefaultFilterHandler;
import org.datagear.util.html.HeadBodyAwareFilterHandler;
import org.datagear.util.html.HtmlFilter;
import org.datagear.util.html.RedirectWriter;
import org.datagear.web.config.ApplicationProperties;
import org.datagear.web.config.CoreConfig;
import org.datagear.web.controller.DashboardController.DashboardShowForEdit.EditHtmlInfo;
import org.datagear.web.controller.DashboardController.DashboardShowForEdit.EditHtmlInfoFilterHandler;
import org.datagear.web.controller.DashboardController.DashboardShowForEdit.ShowHtmlFilterHandler;
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

	/**
	 * 看板内置渲染上下文属性名：{@linkplain EditHtmlInfo}。
	 */
	public static final String DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_EDIT_HTML_INFO = DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_PREFIX
			+ "EDIT_HTML_INFO";

	/**
	 * 看板分享密码占位。
	 */
	public static final String DASHBOARD_SHARE_SET_PASSWORD_PLACEHOLDER = "DSP-PLACEHOLDER-"
			+ IDUtil.randomIdOnTime20();

	public static final String DASHBOARD_SHOW_AUTH_PARAM_NAME = "name";

	static
	{
		AuthorizationResourceMetas.registerForShare(HtmlTplDashboardWidgetEntity.AUTHORIZATION_RESOURCE_TYPE);
	}

	@Autowired
	private HtmlTplDashboardWidgetEntityService htmlTplDashboardWidgetEntityService;

	@Autowired
	@Qualifier(CoreConfig.NAME_DASHBOARD_GLOBAL_RES_ROOT_DIRECTORY)
	private File dashboardGlobalResRootDirectory;

	@Autowired
	private AnalysisProjectService analysisProjectService;

	@Autowired
	private DashboardShareSetService dashboardShareSetService;

	@Autowired
	private HtmlChartWidgetJsonWriter htmlChartWidgetJsonWriter;

	@Autowired
	private File tempDirectory;

	private ServletContext servletContext;

	@Autowired
	private ApplicationProperties applicationProperties;

	@Autowired
	private HtmlFilter htmlFilter;

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

	public DashboardShareSetService getDashboardShareSetService()
	{
		return dashboardShareSetService;
	}

	public void setDashboardShareSetService(DashboardShareSetService dashboardShareSetService)
	{
		this.dashboardShareSetService = dashboardShareSetService;
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

	public HtmlFilter getHtmlFilter()
	{
		return htmlFilter;
	}

	public void setHtmlFilter(HtmlFilter htmlFilter)
	{
		this.htmlFilter = htmlFilter;
	}

	@RequestMapping("/add")
	public String add(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model)
	{
		HtmlTplDashboardWidgetEntity dashboard = new HtmlTplDashboardWidgetEntity();
		setCookieAnalysisProject(request, response, dashboard);

		dashboard.setId(IDUtil.randomIdOnTime20());
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
		model.addAttribute("isAdd", true);

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
		model.addAttribute("isAdd", true);

		model.addAttribute("copySourceId", id);

		dashboard.setId(IDUtil.randomIdOnTime20());

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

		String[] templates = trimResourceNames(dashboard.getTemplates());
		String[] resourceNames = trimResourceNames(form.getResourceNames());
		String[] resourceContents = form.getResourceContents();
		boolean[] resourceIsTemplates = form.getResourceIsTemplates();

		templates = mergeTemplates(templates, resourceNames, resourceIsTemplates);
		dashboard.setTemplates(templates);
		trimAnalysisProjectAwareEntityForSave(dashboard);

		if (isEmpty(dashboard.getId()) || isBlank(dashboard.getName())
				|| isEmpty(templates) || (form.isSaveAdd() && isEmpty(resourceNames)))
			throw new IllegalInputException();

		// 如果编辑了首页模板，则应重新解析编码
		int firstTemplateIndex = StringUtil.search(resourceNames, templates[0]);
		if (firstTemplateIndex > -1)
			dashboard.setTemplateEncoding(resolveTemplateEncoding(resourceContents[firstTemplateIndex]));

		if (form.isSaveAdd())
		{
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

		ResponseEntity<OperationMessage> responseEntity = optMsgSaveSuccessResponseEntity(request);
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

		templates = trimResourceNames(templates);
		widget.setTemplates(templates);

		this.htmlTplDashboardWidgetEntityService.update(user, widget);

		Map<String, Object> data = buildDashboardIdTemplatesHashMap(id, templates);
		return optMsgSaveSuccessResponseEntity(request, data);
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

		String[] templates = dashboard.getTemplates();
		String[] newTemplates = mergeTemplates(templates, name, false);

		if (!Arrays.equals(templates, newTemplates))
		{
			dashboard.setTemplates(newTemplates);
			this.htmlTplDashboardWidgetEntityService.update(user, dashboard);
		}

		Map<String, Object> data = buildDashboardIdTemplatesHashMap(id, newTemplates);
		return optMsgSaveSuccessResponseEntity(request, data);
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

	@RequestMapping(value = "/saveUploadResourceFile", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveUploadResourceFile(HttpServletRequest request, HttpServletResponse response,
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

		return optMsgSaveSuccessResponseEntity(request);
	}

	@RequestMapping(value = "/saveResourceContent", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveResourceContent(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam("id") String id, @RequestParam("resourceName") String resourceName,
			@RequestParam("resourceContent") String resourceContent,
			@RequestParam(value = "isTemplate", required = false) Boolean isTemplate) throws Exception
	{
		User user = WebUtils.getUser(request, response);

		HtmlTplDashboardWidgetEntity dashboard = this.htmlTplDashboardWidgetEntityService.getByIdForEdit(user, id);

		if (dashboard == null)
			throw new RecordNotFoundException();

		resourceName = trimResourceName(resourceName);
		
		boolean templatesChanged = false;
		boolean resourceExists = this.htmlTplDashboardWidgetEntityService.getTemplateDashboardWidgetResManager()
				.exists(id, resourceName);
		
		saveResourceContent(dashboard, resourceName, resourceContent);

		String[] templates = dashboard.getTemplates();
		String[] newTemplates = mergeTemplates(templates, resourceName, Boolean.TRUE.equals(isTemplate));
		templatesChanged = !Arrays.equals(templates, newTemplates);

		if (templatesChanged)
		{
			dashboard.setTemplates(newTemplates);
			this.htmlTplDashboardWidgetEntityService.update(user, dashboard);
		}

		Map<String, Object> data = buildDashboardIdTemplatesHashMap(id, newTemplates);
		data.put("templatesChanged", templatesChanged);
		data.put("resourceExists", resourceExists);
		return optMsgSaveSuccessResponseEntity(request, data);
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
				return optMsgFailResponseEntity(request, HttpStatus.BAD_REQUEST,
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

		return optMsgSaveSuccessResponseEntity(request);
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

		return optMsgDeleteSuccessResponseEntity(request);
	}

	@RequestMapping("/pagingQuery")
	public String pagingQuery(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model)
	{
		model.addAttribute("serverURL", WebUtils.getServerURL(request));
		model.addAttribute(KEY_REQUEST_ACTION, REQUEST_ACTION_QUERY);
		return "/dashboard/dashboard_table";
	}

	@RequestMapping(value = "/select")
	public String select(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model)
	{
		model.addAttribute("serverURL", WebUtils.getServerURL(request));
		setSelectAction(request, model);
		return "/chart/dashboard_table";
	}

	@RequestMapping(value = "/pagingQueryData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public PagingData<HtmlTplDashboardWidgetEntity> pagingQueryData(HttpServletRequest request,
			HttpServletResponse response, final org.springframework.ui.Model springModel,
			@RequestBody(required = false) APIDDataFilterPagingQuery pagingQueryParam) throws Exception
	{
		User user = WebUtils.getUser();
		final APIDDataFilterPagingQuery pagingQuery = inflateAPIDDataFilterPagingQuery(request, pagingQueryParam);

		PagingData<HtmlTplDashboardWidgetEntity> pagingData = this.htmlTplDashboardWidgetEntityService.pagingQuery(user,
				pagingQuery, pagingQuery.getDataFilter(), pagingQuery.getAnalysisProjectId());

		return pagingData;
	}
	
	@RequestMapping("/shareSet")
	public String shareSet(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id) throws Exception
	{
		User user = WebUtils.getUser(request, response);

		HtmlTplDashboardWidgetEntity dashboard = this.htmlTplDashboardWidgetEntityService.getByIdForEdit(user, id);

		if (dashboard == null)
			throw new RecordNotFoundException();

		DashboardShareSet dashboardShareSet = this.dashboardShareSetService.getById(id);

		if (dashboardShareSet == null)
		{
			dashboardShareSet = new DashboardShareSet();
			dashboardShareSet.setEnablePassword(false);
			dashboardShareSet.setAnonymousPassword(false);
		}

		model.addAttribute("dashboard", dashboard);
		model.addAttribute("dashboardShareSet", dashboardShareSet);
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "dashboardShareSet.dashboardShareSet");
		model.addAttribute(KEY_FORM_ACTION, "saveShareSet");

		return "/dashboard/dashboard_share_set";
	}

	@RequestMapping(value = "/saveShareSet", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveShareSet(HttpServletRequest request, HttpServletResponse response,
			@RequestBody DashboardShareSet form) throws Exception
	{
		if (isEmpty(form.getId()))
			throw new IllegalInputException();

		User user = WebUtils.getUser(request, response);

		HtmlTplDashboardWidgetEntity dashboard = this.htmlTplDashboardWidgetEntityService.getByIdForEdit(user,
				form.getId());

		if (dashboard == null)
			throw new RecordNotFoundException();

		this.dashboardShareSetService.save(form);

		return optMsgSaveSuccessResponseEntity(request);
	}

	/**
	 * 看板展示认证页面。
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @param id
	 *            看板ID
	 * @param name
	 *            重定向的看板资源名，允许为{@code null}，格式为："abc.html"、"def/def.js"、"detail/detail.html?param=0"
	 * @throws Exception
	 */
	@RequestMapping("/auth/{id}")
	public String showAuth(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@PathVariable("id") String id,
			@RequestParam(value = DASHBOARD_SHOW_AUTH_PARAM_NAME, required = false) String name) throws Exception
	{
		User user = WebUtils.getUser(request, response);
		HtmlTplDashboardWidgetEntity dashboardWidget = this.htmlTplDashboardWidgetEntityService
				.getHtmlTplDashboardWidget(user, id);

		if (dashboardWidget == null)
			throw new RecordNotFoundException();

		if (!StringUtil.isEmpty(name))
			name = WebUtils.decodeURL(name);

		String redirectPath = WebUtils.getContextPath(request) + "/dashboard/show/" + id + "/";
		if (!StringUtil.isEmpty(name))
			redirectPath = FileUtil.concatPath(redirectPath, name, FileUtil.PATH_SEPARATOR_SLASH, false);

		boolean authed = isShowAuthed(request, user, dashboardWidget);

		model.addAttribute("id", id);
		model.addAttribute("name", (name == null ? "" : name));
		model.addAttribute("redirectPath", redirectPath);
		model.addAttribute("authed", authed);
		model.addAttribute("dashboardWidget", dashboardWidget);
		model.addAttribute("dashboardNameMask", StringUtil.mask(dashboardWidget.getName(), 2, 2, 6));
		model.addAttribute("currentUser", user.cloneNoPassword());

		return "/dashboard/dashboard_show_auth";
	}

	/**
	 * 看板展示认证校验。
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @param form
	 * @throws Exception
	 */
	@RequestMapping(value = "/authcheck", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> showAuthCheck(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model, @RequestBody ShowAuthCheckForm form) throws Exception
	{
		if (isEmpty(form.getId()) || form.getPassword() == null)
			throw new IllegalInputException();

		User user = WebUtils.getUser(request, response);
		HtmlTplDashboardWidgetEntity dashboardWidget = this.htmlTplDashboardWidgetEntityService
				.getHtmlTplDashboardWidget(user, form.getId());

		if (dashboardWidget == null)
			throw new RecordNotFoundException();

		ResponseEntity<OperationMessage> responseEntity = null;

		String dashboardWidgetId = dashboardWidget.getId();
		DashboardShareSet dashboardShareSet = this.dashboardShareSetService.getById(dashboardWidgetId);

		if (dashboardShareSet == null || !dashboardShareSet.isEnablePassword())
		{
			responseEntity = optMsgSuccessResponseEntity(
					ShowAuthCheckResponse.valueOf(ShowAuthCheckResponse.TYPE_SUCCESS));
		}
		else
		{
			DashboardShowAuthCheckManager manager = getDashboardShowAuthCheckManager(request, true);

			if (manager.isAuthDenied(dashboardWidgetId))
			{
				responseEntity = optMsgSuccessResponseEntity(
						ShowAuthCheckResponse.valueOf(ShowAuthCheckResponse.TYPE_DENY, manager.getAuthFailThreshold(),
								0));
			}
			else if (form.getPassword().equals(dashboardShareSet.getPassword()))
			{
				manager.setAuthed(dashboardWidgetId, true);
				responseEntity = optMsgSuccessResponseEntity(
						ShowAuthCheckResponse.valueOf(ShowAuthCheckResponse.TYPE_SUCCESS));
			}
			else
			{
				manager.setAuthed(dashboardWidgetId, false);
				responseEntity = optMsgSuccessResponseEntity(
						ShowAuthCheckResponse.valueOf(ShowAuthCheckResponse.TYPE_FAIL, manager.getAuthFailThreshold(),
								manager.authRemain(dashboardWidgetId)));
			}
		}

		return responseEntity;
	}

	/**
	 * 看板展示请求是否已通过密码验证。
	 * 
	 * @param request
	 * @param user
	 * @param dashboardWidget
	 * @return
	 */
	protected boolean isShowAuthed(HttpServletRequest request, User user, HtmlTplDashboardWidgetEntity dashboardWidget)
	{
		// 有编辑权限，无需认证
		if (Authorization.canEdit(dashboardWidget.getDataPermission()))
			return true;

		DashboardShareSet dashboardShareSet = this.dashboardShareSetService.getById(dashboardWidget.getId());

		if (dashboardShareSet == null || !dashboardShareSet.isEnablePassword())
			return true;

		if (dashboardShareSet.isAnonymousPassword() && !user.isAnonymous())
			return true;

		DashboardShowAuthCheckManager manager = getDashboardShowAuthCheckManager(request, false);

		return (manager == null ? false : manager.isAuthed(dashboardWidget.getId()));
	}

	protected String buildShowAuthUrlForShowRequest(HttpServletRequest request, String id, String resName)
			throws Exception
	{
		resName = appendRequestQueryString((resName == null ? "" : resName), request);

		String authPath = WebUtils.getContextPath(request) + "/dashboard/auth/" + id + "/";
		if (!StringUtil.isEmpty(resName))
			authPath += "?" + DASHBOARD_SHOW_AUTH_PARAM_NAME + "=" + WebUtils.encodeURL(resName);

		return authPath;
	}

	protected DashboardShowAuthCheckManager getDashboardShowAuthCheckManager(HttpServletRequest request,
			boolean nonNull)
	{
		HttpSession session = request.getSession();

		DashboardShowAuthCheckManager manager = (DashboardShowAuthCheckManager) session
				.getAttribute(DashboardShowAuthCheckManager.class.getName());

		if (!nonNull)
			return manager;

		synchronized (session)
		{
			if (manager == null)
			{
				manager = new DashboardShowAuthCheckManager(
						this.applicationProperties.getDashboardSharePasswordAuthFailThreshold(),
						this.applicationProperties.getDashboardSharePasswordAuthFailPastMinutes() * 60 * 1000);
				session.setAttribute(DashboardShowAuthCheckManager.class.getName(), manager);
			}
		}

		return manager;
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
			HtmlTplDashboardWidgetEntity dashboardWidget = getHtmlTplDashboardWidgetEntityForShow(request, user, id);

			if (!isShowAuthed(request, user, dashboardWidget))
			{
				String authPath = buildShowAuthUrlForShowRequest(request, id, "");
				response.sendRedirect(authPath);
				return;
			}

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
				showDashboard(request, response, model, user, dashboardWidget, firstTemplate,
						isDashboardShowForEditRequest(request, dashboardWidget));
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
	@RequestMapping("/show/{id}/**")
	public void showResource(HttpServletRequest request, HttpServletResponse response, WebRequest webRequest,
			org.springframework.ui.Model model, @PathVariable("id") String id) throws Exception
	{
		User user = WebUtils.getUser(request, response);

		HtmlTplDashboardWidgetEntity dashboardWidget = getHtmlTplDashboardWidgetEntityForShow(request, user, id);
		String resName = resolvePathAfter(request, "/show/" + id + "/");

		if (StringUtil.isEmpty(resName))
			throw new FileNotFoundException(resName);

		resName = WebUtils.decodeURL(resName);

		if (!isShowAuthed(request, user, dashboardWidget))
		{
			String authPath = buildShowAuthUrlForShowRequest(request, id, resName);
			response.sendRedirect(authPath);
			return;
		}

		boolean isShowForEdit = isDashboardShowForEditRequest(request, dashboardWidget);

		if (dashboardWidget.isTemplate(resName) || isShowForEdit)
		{
			showDashboard(request, response, model, user, dashboardWidget, resName, isShowForEdit);
		}
		else
		{
			InputStream in = null;

			TemplateDashboardWidgetResManager resManager = this.htmlTplDashboardWidgetEntityService
					.getTemplateDashboardWidgetResManager();

			// 优先本地资源
			if (resManager.exists(id, resName))
			{
				long lastModified = resManager.lastModified(id, resName);
				if (webRequest.checkNotModified(lastModified))
					return;

				setContentTypeByName(request, response, getServletContext(), resName);
				setCacheControlNoCache(response);

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
					long lastModified = globalRes.lastModified();
					if (webRequest.checkNotModified(lastModified))
						return;

					setContentTypeByName(request, response, getServletContext(), resName);
					setCacheControlNoCache(response);

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
	 * 加载看板全局资源。
	 * <p>
	 * 上述{@linkplain #showResource(HttpServletRequest, HttpServletResponse, WebRequest, org.springframework.ui.Model, String)}
	 * 支持在看版内以{@code "global/**"}相对地址的方式引入全局资源，这简化了写法，但会导致浏览器无法跨看板缓存全局资源，
	 * 因为全局资源的绝对路径对于不同的看板也是不同的。
	 * </p>
	 * <p>
	 * 此方法则是以固定的绝对路径加载全局资源，使得浏览器可以跨看板缓存它们，缺点是看板内需以{@code "../global/**"}相对地址的方式引入全局资源。
	 * </p>
	 * 
	 * @param request
	 * @param response
	 * @param webRequest
	 * @throws Exception
	 * @see {@linkplain ApplicationProperties#getDashboardGlobalResUrlPrefixName()}
	 */
	@RequestMapping("/show/#{applicationProperties.getDashboardGlobalResUrlPrefixName()}/**")
	public void showGlobalResource(HttpServletRequest request, HttpServletResponse response, WebRequest webRequest)
			throws Exception
	{
		String resName = resolvePathAfter(request,
				"/show/" + this.applicationProperties.getDashboardGlobalResUrlPrefixName() + "/");

		if (StringUtil.isEmpty(resName))
			throw new FileNotFoundException(resName);

		resName = WebUtils.decodeURL(resName);
		File globalRes = FileUtil.getFile(dashboardGlobalResRootDirectory, resName);

		if (!globalRes.exists() || globalRes.isDirectory())
			throw new FileNotFoundException(resName);

		long lastModified = globalRes.lastModified();
		if (webRequest.checkNotModified(lastModified))
			return;

		setContentTypeByName(request, response, getServletContext(), resName);
		setCacheControlNoCache(response);

		InputStream in = null;
		OutputStream out = null;

		try
		{
			in = IOUtil.getBufferedInputStream(IOUtil.getInputStream(globalRes));
			out = IOUtil.getBufferedOutputStream(response.getOutputStream());

			IOUtil.write(in, out);
		}
		finally
		{
			IOUtil.close(in);
			IOUtil.close(out);
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
	 * @param isShowForEdit
	 * @throws Exception
	 */
	protected void showDashboard(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model, User user, HtmlTplDashboardWidgetEntity dashboardWidget,
			String template, boolean isShowForEdit) throws Exception
	{
		User createUser = dashboardWidget.getCreateUser();

		String showHtml = (isShowForEdit
				? buildShowForEditShowHtml(request.getParameter(DASHBOARD_SHOW_PARAM_TEMPLATE_CONTENT))
				: "");
		EditHtmlInfo editHtmlInfo = (isShowForEdit ? buildShowForEditEditHtmlInfo(showHtml) : null);

		// 确保看板创建用户对看板模板内定义的图表有权限
		ChartWidgetSourceContext.set(new ChartWidgetSourceContext(createUser));

		Reader showHtmlIn = (isShowForEdit ? IOUtil.getReader(showHtml) : null);
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

			List<HtmlTplDashboardImport> importList = buildHtmlTplDashboardImports(request);
			if (isShowForEdit)
			{
				importList.add(HtmlTplDashboardImport.valueOfLinkCss("dashboardEditorStyle",
						WebUtils.getContextPath(request) + "/static/css/dashboardEditor.css?v=" + Global.VERSION));
				importList.add(HtmlTplDashboardImport.valueOfJavaScript("dashboardEditor",
						WebUtils.getContextPath(request) + "/static/script/dashboardEditor.js?v=" + Global.VERSION));
			}

			DefaultHtmlTitleHandler htmlTitleHandler = new DefaultHtmlTitleHandler(
					getMessage(request, "dashboard.show.htmlTitleSuffix", getMessage(request, "app.name")),
					getMessage(request, "dashboard.show.htmlTitleSuffixForEmpty", dashboardWidget.getName(),
							getMessage(request, "app.name")));
			RenderContext renderContext = createHtmlRenderContext(request, response, out,
					createWebContext(request), importList, htmlTitleHandler);

			// 移除参数中的模板内容，一是它不应该传入页面，二是它可能包含"</script>"子串，传回浏览器端时会导致页面解析出错
			renderContext.removeAttribute(DASHBOARD_SHOW_PARAM_TEMPLATE_CONTENT);

			if (isShowForEdit)
				renderContext.setAttribute(DASHBOARD_BUILTIN_RENDER_CONTEXT_ATTR_EDIT_HTML_INFO, editHtmlInfo);

			HtmlTplDashboard dashboard = (showHtmlIn != null
					? dashboardWidget.render(renderContext, template, showHtmlIn)
					: dashboardWidget.render(renderContext, template));

			SessionDashboardInfoManager dashboardInfoManager = getSessionDashboardInfoManagerNotNull(request);
			dashboardInfoManager.put(new DashboardInfo(dashboard));
		}
		finally
		{
			IOUtil.close(showHtmlIn);
			IOUtil.close(out);
			ChartWidgetSourceContext.remove();
		}
	}
	
	/**
	 * 获取看板展示操作时使用的看板部件。
	 * <p>
	 * 如果是可视编辑请求，且记录不存在，此方法返回一个仿造看板部件，以解决展示操作时看板部件必须已存在的问题（当可视编辑模式时的新建看板操作）。
	 * </p>
	 * 
	 * @param request
	 * @param user
	 * @param id
	 * @return
	 * @throws PermissionDeniedException
	 *             没有权限时
	 * @throws RecordNotFoundException
	 *             记录不存在时
	 */
	protected HtmlTplDashboardWidgetEntity getHtmlTplDashboardWidgetEntityForShow(HttpServletRequest request,
			User user, String id) throws PermissionDeniedException, RecordNotFoundException
	{
		if (StringUtil.isEmpty(id))
			throw new IllegalInputException();

		HtmlTplDashboardWidgetEntity dashboardWidget = this.htmlTplDashboardWidgetEntityService.getHtmlTplDashboardWidget(user, id);

		if (dashboardWidget == null)
		{
			// 可视编辑模式时的新增操作
			if (isDashboardShowForEditParam(request))
				dashboardWidget = createMockHtmlTplDashboardWidgetEntity(id, user);
			else
				throw new RecordNotFoundException();
		}

		return dashboardWidget;
	}

	protected HtmlTplDashboardWidgetEntity createMockHtmlTplDashboardWidgetEntity(String id, User createUser)
	{
		HtmlTplDashboardWidgetEntity dashboardWidget = new HtmlTplDashboardWidgetEntity(id,
				HtmlTplDashboardWidgetEntity.DEFAULT_TEMPLATES[0],
				this.htmlTplDashboardWidgetEntityService.getHtmlTplDashboardWidgetRenderer(),
				this.htmlTplDashboardWidgetEntityService.getTemplateDashboardWidgetResManager(), id, createUser);

		dashboardWidget.setDataPermission(Authorization.PERMISSION_EDIT_START);

		return dashboardWidget;
	}

	protected String buildShowForEditShowHtml(String templateContent) throws IOException
	{
		String showHtml = "";

		if (templateContent != null)
		{
			// 加一个"s"前缀与前端生成的区分
			String staticIdPrefix = "datagears" + Long.toHexString(System.currentTimeMillis());

			StringReader in = null;
			StringWriter out = null;

			try
			{
				in = IOUtil.getReader(templateContent);
				out = new StringWriter(templateContent.length());

				ShowHtmlFilterHandler fh = new ShowHtmlFilterHandler(out, staticIdPrefix);
				this.htmlFilter.filter(in, fh);

				showHtml = out.toString();
			}
			finally
			{
				IOUtil.close(in);
				IOUtil.close(out);
			}
		}

		return showHtml;
	}

	protected EditHtmlInfo buildShowForEditEditHtmlInfo(String showHtml) throws IOException
	{
		String beforeBodyHtml = "";
		String bodyHtml = "";
		String afterBodyHtml = "";
		Map<String, String> placeholderSources = Collections.emptyMap();

		if (showHtml != null)
		{
			StringReader in = null;
			EditHtmlInfoFilterHandler fh = null;

			try
			{
				in = IOUtil.getReader(showHtml);
				fh = new EditHtmlInfoFilterHandler();

				this.htmlFilter.filter(in, fh);
			}
			finally
			{
				IOUtil.close(in);
			}

			beforeBodyHtml = fh.getBeforeBodyHtml();
			bodyHtml = fh.getBodyHtml();
			afterBodyHtml = fh.getAfterBodyHtml();
			placeholderSources = fh.getPlaceholderSources();
		}

		return new EditHtmlInfo(escapeDashboardRenderContextAttrValue(beforeBodyHtml),
				escapeDashboardRenderContextAttrValue(bodyHtml), escapeDashboardRenderContextAttrValue(afterBodyHtml),
				escapeDashboardRenderContextAttrValue(placeholderSources));
	}

	protected boolean isDashboardShowForEditParam(HttpServletRequest request)
	{
		String editTemplate = request.getParameter(DASHBOARD_SHOW_PARAM_EDIT_TEMPLATE);
		// 有编辑模板请求参数
		return (("true".equalsIgnoreCase(editTemplate) || "1".equals(editTemplate)));
	}

	protected boolean isDashboardShowForEditRequest(HttpServletRequest request,
			HtmlTplDashboardWidgetEntity dashboardWidget)
	{
		// 有编辑模板请求参数、且有编辑权限
		return (isDashboardShowForEditParam(request) && Authorization.canEdit(dashboardWidget.getDataPermission()));
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

		// 新建看板的可视编辑模式时还没有看板部件，这里需mock一个保证逻辑正确
		if (dashboardWidget == null)
			dashboardWidget = createMockHtmlTplDashboardWidgetEntity(dashboardInfo.getDashboardWidgetId(), user);

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

	protected Map<String, Object> buildDashboardIdTemplatesHashMap(String id, String[] templates)
	{
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", id);
		map.put("templates", templates);

		return map;
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
			writer = IOUtil.getBufferedWriter(this.htmlTplDashboardWidgetEntityService
					.getTemplateDashboardWidgetResManager().getWriter(widget, name));
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

	/**
	 * 合并模板。
	 * 
	 * @param templates          原模板数组，允许为{@code null}
	 * @param resourceName
	 * @param resourceIsTemplate
	 * @return
	 */
	protected String[] mergeTemplates(String[] templates, String resourceName, boolean resourceIsTemplate)
	{
		return mergeTemplates(templates, new String[] { resourceName }, new boolean[] { resourceIsTemplate });
	}

	/**
	 * 合并模板。
	 * 
	 * @param templates           原模板数组，允许为{@code null}
	 * @param resourceNames
	 * @param resourceIsTemplates
	 * @return
	 */
	protected String[] mergeTemplates(String[] templates, String[] resourceNames, boolean[] resourceIsTemplates)
	{
		List<String> ts = new ArrayList<>();

		if (templates != null)
			ts.addAll(Arrays.asList(templates));

		boolean autoFirstTemplate = ts.isEmpty();

		for (int i = 0; i < resourceNames.length; i++)
		{
			boolean contains = ts.contains(resourceNames[i]);

			if (resourceIsTemplates[i] && !contains)
				ts.add(resourceNames[i]);
			else if (!resourceIsTemplates[i] && contains)
				ts.remove(resourceNames[i]);
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

	protected String[] trimResourceNames(String[] resourceNames)
	{
		if (resourceNames == null)
			return new String[0];

		String[] rns = new String[resourceNames.length];

		for (int i = 0; i < resourceNames.length; i++)
			rns[i] = trimResourceName(resourceNames[i]);

		return rns;
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

		private boolean saveAdd = false;

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

		public boolean isSaveAdd()
		{
			return saveAdd;
		}

		public void setSaveAdd(boolean saveAdd)
		{
			this.saveAdd = saveAdd;
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

	/**
	 * 看板可视编辑支持类。
	 * <p>
	 * 看板可视编辑的基本思路是：
	 * </p>
	 * <ol>
	 * <li>
	 * 读取原始看板HTML模板，为其<code>&lt;body&gt;&lt;/body&gt;</code>中的所有元素添加<code>dg-visual-edit-id</code>属性，
	 * 生成【展示HTML】（参考{@linkplain ShowHtmlFilterHandler}）。</li>
	 * <li>读取【展示HTML】，生成【编辑HTML信息】（参考{@linkplain EditHtmlInfoFilterHandler}），
	 * 【编辑HTML信息】由四部分组成：<br>
	 * 【body前】、【body体】、【body后】、【占位标签源映射表】，<br>
	 * 其中：<br>
	 * 【body前】是【展示HTML】的<code>&lt;body&gt;</code>前片段；<br>
	 * 【body体】是【展示HTML】的<code>&lt;body&gt;&lt;/body&gt;</code>体片段，且是已进行【占位标签替换】的；<br>
	 * 【body后】是【展示HTML】的<code>&lt;/body&gt;</code>后片段；<br>
	 * 【占位标签源映射表】存储【body体】中占位标签的原始标签信息。<br>
	 * 【占位标签替换】是将【展示HTML】的<code>&lt;body&gt;&lt;/body&gt;</code>体中的在渲染时会影响DOM结构的标签（<code>link, style, script</code>）替换为
	 * <code>&lt;--dg-placeholder-i--&gt;</code>注释标签。</li>
	 * <li>
	 * 在【展示HTML】中插入【可视编辑JS库】、在其{@linkplain RenderContext}插入【编辑HTML信息】，渲染为【可视编辑效果页面】。
	 * </li>
	 * <li>在【可视编辑效果页面】中，【可视编辑JS库】将【编辑HTML信息】的【body体】渲染至一个隔离iframe中。</li>
	 * <li>【可视编辑JS库】将所有【可视编辑效果页面】中的可视编辑操作同步至隔离iframe中的相同<code>dg-visual-edit-id</code>的HTML元素。</li>
	 * <li>【可视编辑JS库】读取隔离iframe中的<code>&lt;body&gt;&lt;/body&gt;</code>体HTML，使用【编辑HTML信息】中的【占位标签源映射表】进行还原，
	 * 使用【body前】、【body后】进行拼接，生成可视编辑结果HTML。</li>
	 * </ol>
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class DashboardShowForEdit
	{
		/**
		 * 看板可视编辑时的内元素可视编辑ID属性名。
		 */
		public static final String ELEMENT_ATTR_VISUAL_EDIT_ID = HtmlTplDashboardWidgetRenderer.DASHBOARD_ELEMENT_ATTR_PREFIX
				+ "visual-edit-id";

		/**
		 * 【展示HTML】过滤处理器。
		 * 
		 * @author datagear@163.com
		 *
		 */
		public static class ShowHtmlFilterHandler extends HeadBodyAwareFilterHandler
		{
			private final String staticIdPrefix;

			private int staticIdSequence = 0;

			public ShowHtmlFilterHandler(Writer out, String staticIdPrefix)
			{
				super(out);
				this.staticIdPrefix = staticIdPrefix;
			}

			public String getStaticIdPrefix()
			{
				return staticIdPrefix;
			}

			@Override
			public void beforeWriteTagEnd(Reader in, String tagName, String tagEnd, Map<String, String> attrs)
					throws IOException
			{
				if (this.isInBodyTag() && !isCloseTagName(tagName))
				{
					write(" " + ELEMENT_ATTR_VISUAL_EDIT_ID + "=\"" + this.staticIdPrefix + (this.staticIdSequence++)
							+ "\" ");
				}
			}
		}

		/**
		 * 【编辑HTML信息】过滤处理器。
		 * 
		 * @author datagear@163.com
		 *
		 */
		public static class EditHtmlInfoFilterHandler extends DefaultFilterHandler
		{
			private static final String PLACEHOLDER_PREFIX = HtmlTplDashboardWidgetRenderer.DASHBOARD_ELEMENT_ATTR_PREFIX
					+ "placeholder-";

			private final Map<String, String> placeholderSources = new HashMap<String, String>();

			private int placeholderSequence = 0;

			private String currentPlaceholderKey;

			public EditHtmlInfoFilterHandler()
			{
				super(new EditHtmlInfoWriter(new StringWriter(), new StringWriter(), new StringWriter(), new StringWriter(),
						false));
			}

			public String getBeforeBodyHtml()
			{
				return getEditHtmlWriter().getBeforeBodyOut().toString();
			}

			public String getBodyHtml()
			{
				return getEditHtmlWriter().getBodyOut().toString();
			}

			public String getAfterBodyHtml()
			{
				return getEditHtmlWriter().getAfterBodyOut().toString();
			}

			public Map<String, String> getPlaceholderSources()
			{
				return placeholderSources;
			}

			@Override
			public void beforeWriteTagStart(Reader in, String tagName) throws IOException
			{
				EditHtmlInfoWriter out = getEditHtmlWriter();

				// 设置<body> 写入out.getBodyOut()
				if (out.isOutStatusBeforeBody() && "body".equalsIgnoreCase(tagName))
					out.setOutStatus(EditHtmlInfoWriter.OUT_STATUS_BODY);

				if (out.isOutStatusBody() && !out.isRedirect())
				{
					// 将这些标签写入变向输出流
					if ("link".equalsIgnoreCase(tagName) || "style".equalsIgnoreCase(tagName)
							|| "script".equalsIgnoreCase(tagName))
					{
						this.currentPlaceholderKey = nextPlaceholderKey();
						out.write(this.currentPlaceholderKey);

						out.setRedirect(true);
					}
				}
			}

			@Override
			public void afterWriteTagEnd(Reader in, String tagName, String tagEnd) throws IOException
			{
				EditHtmlInfoWriter out = getEditHtmlWriter();

				if (out.isOutStatusBody() && out.isRedirect())
				{
					// 从变向输出流中读取占位内容
					if ("/link".equalsIgnoreCase(tagName) || "/style".equalsIgnoreCase(tagName)
							|| "/script".equalsIgnoreCase(tagName)
							|| (isSelfCloseTagEnd(tagEnd) && ("link".equalsIgnoreCase(tagName)
									|| "style".equalsIgnoreCase(tagName) || "script".equalsIgnoreCase(tagName))))
					{
						String placeholderValue = ((StringWriter) out.getRedirectOut()).toString();
						this.placeholderSources.put(this.currentPlaceholderKey, placeholderValue);

						out.setRedirectOut(new StringWriter());
						out.setRedirect(false);
					}
				}

				// 设置</body>后写入out.getAfterBodyOut()
				if (out.isOutStatusBody() && ("/body".equalsIgnoreCase(tagName)
						|| "body".equalsIgnoreCase(tagName) && isSelfCloseTagEnd(tagEnd)))
					out.setOutStatus(EditHtmlInfoWriter.OUT_STATUS_AFTER_BODY);
			}

			protected String nextPlaceholderKey()
			{
				return "<!--" + PLACEHOLDER_PREFIX + (this.placeholderSequence++) + "-->";
			}

			protected EditHtmlInfoWriter getEditHtmlWriter()
			{
				return (EditHtmlInfoWriter) super.getOut();
			}
		}

		/**
		 * 【编辑HTML信息】输出流。
		 * 
		 * @author datagear@163.com
		 *
		 */
		public static class EditHtmlInfoWriter extends RedirectWriter
		{
			/** 输出至{@linkplain #getBeforeBodyOut()} */
			public static final int OUT_STATUS_BEFORE_BODY = 0;

			/** 输出至{@linkplain #getBodyOut()} */
			public static final int OUT_STATUS_BODY = OUT_STATUS_BEFORE_BODY + 1;

			/** 输出至{@linkplain #getAfterBodyOut()} */
			public static final int OUT_STATUS_AFTER_BODY = OUT_STATUS_BODY + 1;

			private Writer bodyOut;

			private Writer afterBodyOut;

			private int outStatus = OUT_STATUS_BEFORE_BODY - 1;

			public EditHtmlInfoWriter()
			{
				super();
			}

			public EditHtmlInfoWriter(Writer beforeBodyOut, Writer bodyOut, Writer afterBodyOut, Writer redirectOut,
					boolean redirect)
			{
				super(beforeBodyOut, redirectOut, redirect);
				this.bodyOut = bodyOut;
				this.afterBodyOut = afterBodyOut;
			}

			public EditHtmlInfoWriter(Writer beforeBodyOut, Writer bodyOut, Writer afterBodyOut, Writer redirectOut,
					boolean redirect, Object lock)
			{
				super(beforeBodyOut, redirectOut, redirect, lock);
				this.bodyOut = bodyOut;
				this.afterBodyOut = afterBodyOut;
			}

			public Writer getBeforeBodyOut()
			{
				return super.getOut();
			}

			public void setBeforeBodyOut(Writer beforeBodyOut)
			{
				super.setOut(beforeBodyOut);
			}

			public Writer getBodyOut()
			{
				return bodyOut;
			}

			public void setBodyOut(Writer bodyOut)
			{
				this.bodyOut = bodyOut;
			}

			public Writer getAfterBodyOut()
			{
				return afterBodyOut;
			}

			public void setAfterBodyOut(Writer afterBodyOut)
			{
				this.afterBodyOut = afterBodyOut;
			}

			public int getOutStatus()
			{
				return outStatus;
			}

			public void setOutStatus(int outStatus)
			{
				this.outStatus = outStatus;
			}

			public boolean isOutStatusBeforeBody()
			{
				return (OUT_STATUS_BEFORE_BODY >= this.outStatus);
			}

			public boolean isOutStatusBody()
			{
				return (OUT_STATUS_BODY >= this.outStatus && this.outStatus > OUT_STATUS_BEFORE_BODY);
			}

			public boolean isOutStatusAfterBody()
			{
				return (OUT_STATUS_AFTER_BODY >= this.outStatus && this.outStatus > OUT_STATUS_BODY);
			}

			@Override
			public void write(char[] cbuf, int off, int len) throws IOException
			{
				if(isRedirect())
					getRedirectOut().write(cbuf, off, len);
				else if (isOutStatusBeforeBody())
					getBeforeBodyOut().write(cbuf, off, len);
				else if (isOutStatusBody())
					getBodyOut().write(cbuf, off, len);
				else if (isOutStatusAfterBody())
					getAfterBodyOut().write(cbuf, off, len);
				else
					throw new IllegalStateException();
			}

			@Override
			public void flush() throws IOException
			{
				super.flush();
				this.bodyOut.flush();
				this.afterBodyOut.flush();
			}

			@Override
			public void close() throws IOException
			{
				super.close();
				this.bodyOut.close();
				this.afterBodyOut.close();
			}
		}

		/**
		 * 【编辑HTML信息】。
		 * 
		 * @author datagear@163.com
		 *
		 */
		public static class EditHtmlInfo implements Serializable
		{
			private static final long serialVersionUID = 1L;

			/**
			 * 【body前】HTML
			 */
			private String beforeBodyHtml = "";

			/**
			 * 【body体】HTML
			 */
			private String bodyHtml = "";

			/**
			 * 【body后】HTML
			 */
			private String afterBodyHtml = "";

			/** 【占位标签源映射表】 */
			private Map<String, String> placeholderSources;

			public EditHtmlInfo(String beforeBodyHtml, String bodyHtml, String afterBodyHtml,
					Map<String, String> placeholderSources)
			{
				super();
				this.beforeBodyHtml = beforeBodyHtml;
				this.bodyHtml = bodyHtml;
				this.afterBodyHtml = afterBodyHtml;
				this.placeholderSources = placeholderSources;
			}

			public String getBeforeBodyHtml()
			{
				return beforeBodyHtml;
			}

			public void setBeforeBodyHtml(String beforeBodyHtml)
			{
				this.beforeBodyHtml = beforeBodyHtml;
			}

			public String getBodyHtml()
			{
				return bodyHtml;
			}

			public void setBodyHtml(String bodyHtml)
			{
				this.bodyHtml = bodyHtml;
			}

			public String getAfterBodyHtml()
			{
				return afterBodyHtml;
			}

			public void setAfterBodyHtml(String afterBodyHtml)
			{
				this.afterBodyHtml = afterBodyHtml;
			}

			public Map<String, String> getPlaceholderSources()
			{
				return placeholderSources;
			}

			public void setPlaceholderSources(Map<String, String> placeholderSources)
			{
				this.placeholderSources = placeholderSources;
			}
		}
	}

	public static class ShowAuthCheckForm implements ControllerForm
	{
		private static final long serialVersionUID = 1L;

		private String id;

		private String password;

		public ShowAuthCheckForm()
		{
			super();
		}

		public String getId()
		{
			return id;
		}

		public void setId(String id)
		{
			this.id = id;
		}

		public String getPassword()
		{
			return password;
		}

		public void setPassword(String password)
		{
			this.password = password;
		}
	}

	public static class ShowAuthCheckResponse implements Serializable
	{
		private static final long serialVersionUID = 1L;

		/** 验证通过 */
		public static final String TYPE_SUCCESS = "success";

		/** 验证未通过 */
		public static final String TYPE_FAIL = "fail";

		/** 验证拒绝 */
		public static final String TYPE_DENY = "deny";

		/** 校验结果类型 */
		private String type;

		private int authFailThreshold = -1;

		/** 验证剩余次数 */
		private int authRemain = -1;

		public ShowAuthCheckResponse(String type)
		{
			super();
			this.type = type;
		}

		public ShowAuthCheckResponse(String type, int authFailThreshold, int authRemain)
		{
			super();
			this.type = type;
			this.authFailThreshold = authFailThreshold;
			this.authRemain = authRemain;
		}

		public String getType()
		{
			return type;
		}

		public void setType(String type)
		{
			this.type = type;
		}

		public int getAuthFailThreshold()
		{
			return authFailThreshold;
		}

		public void setAuthFailThreshold(int authFailThreshold)
		{
			this.authFailThreshold = authFailThreshold;
		}

		public int getAuthRemain()
		{
			return authRemain;
		}

		public void setAuthRemain(int authRemain)
		{
			this.authRemain = authRemain;
		}

		public static ShowAuthCheckResponse valueOf(String type)
		{
			return new ShowAuthCheckResponse(type);
		}

		public static ShowAuthCheckResponse valueOf(String type, int authFailThreshold, int authRemain)
		{
			return new ShowAuthCheckResponse(type, authFailThreshold, authRemain);
		}
	}

	public static class DashboardShowAuthCheckManager implements Serializable
	{
		private static final long serialVersionUID = 1L;

		/** 失败验证次数，-1表示不限 */
		private int authFailThreshold = -1;

		/** 失败验证次数过去毫秒内 */
		private long authFailPastMs = 2 * 60 * 60 * 1000;

		private final Map<String, AuthCheckInfo> authCheckInfos = new HashMap<>();

		public DashboardShowAuthCheckManager()
		{
			super();
		}

		public DashboardShowAuthCheckManager(int authFailThreshold, long authFailPastMs)
		{
			super();
			this.authFailThreshold = authFailThreshold;
			this.authFailPastMs = authFailPastMs;
		}

		public int getAuthFailThreshold()
		{
			return authFailThreshold;
		}

		public void setAuthFailThreshold(int authFailThreshold)
		{
			this.authFailThreshold = authFailThreshold;
		}

		public long getAuthFailPastMs()
		{
			return authFailPastMs;
		}

		public void setAuthFailPastMs(long authFailPastMs)
		{
			this.authFailPastMs = authFailPastMs;
		}

		public synchronized boolean isAuthed(String dashboardId)
		{
			AuthCheckInfo aci = this.authCheckInfos.get(dashboardId);
			return (aci == null ? false : aci.isAuthed());
		}

		public synchronized void setAuthed(String dashboardId, boolean authed)
		{
			AuthCheckInfo aci = this.authCheckInfos.get(dashboardId);
			if (aci == null)
			{
				aci = new AuthCheckInfo();
				this.authCheckInfos.put(dashboardId, aci);
			}

			if (authed)
			{
				aci.setAuthed(true);
				aci.clearFailedDate();
			}
			else
			{
				aci.offerFailedDate();
				aci.pollFailedDate(this.authFailThreshold);
			}
		}

		public synchronized boolean isAuthDenied(String dashboardId)
		{
			if (this.authFailThreshold < 0)
				return false;

			AuthCheckInfo aci = this.authCheckInfos.get(dashboardId);

			if (aci == null)
				return false;

			return ((this.authFailThreshold - aci.failedDateCount(this.authFailPastMs)) <= 0);
		}

		public synchronized int authRemain(String dashboardId)
		{
			if (this.authFailThreshold < 0)
				return -1;

			AuthCheckInfo aci = this.authCheckInfos.get(dashboardId);

			int remain = this.authFailThreshold;

			if (aci != null)
				remain = this.authFailThreshold - aci.failedDateCount(this.authFailPastMs);

			return (remain < 0 ? 0 : remain);
		}

		public static class AuthCheckInfo implements Serializable
		{
			private static final long serialVersionUID = 1L;

			private boolean authed = false;

			private Queue<Long> failedDates = new LinkedList<Long>();

			public AuthCheckInfo()
			{
				super();
			}

			public boolean isAuthed()
			{
				return authed;
			}

			public void setAuthed(boolean authed)
			{
				this.authed = authed;
			}

			public Queue<Long> getFailedDates()
			{
				return failedDates;
			}

			public void setFailedDates(Queue<Long> failedDates)
			{
				this.failedDates = failedDates;
			}

			public void offerFailedDate()
			{
				this.failedDates.offer(System.currentTimeMillis());
			}

			public void offerFailedDate(long date)
			{
				this.failedDates.offer(date);
			}

			public void pollFailedDate(int keepCount)
			{
				while (this.failedDates.size() > keepCount)
					this.failedDates.poll();
			}

			public void clearFailedDate()
			{
				this.failedDates.clear();
			}

			public int failedDateCount(long pastMs)
			{
				int count = 0;

				long startDate = System.currentTimeMillis() - pastMs;
				for (Long ele : this.failedDates)
				{
					if (ele >= startDate)
						count++;
				}

				return count;
			}
		}
	}
}
