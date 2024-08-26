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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.analysis.TplDashboardWidgetResManager;
import org.datagear.analysis.support.html.HtmlTplDashboardWidget;
import org.datagear.analysis.support.html.HtmlTplDashboardWidgetRenderer;
import org.datagear.management.domain.AnalysisProject;
import org.datagear.management.domain.DashboardShareSet;
import org.datagear.management.domain.HtmlTplDashboardWidgetEntity;
import org.datagear.management.domain.User;
import org.datagear.management.service.AnalysisProjectService;
import org.datagear.management.service.DashboardShareSetService;
import org.datagear.management.service.HtmlTplDashboardWidgetEntityService;
import org.datagear.persistence.PagingData;
import org.datagear.util.FileUtil;
import org.datagear.util.IDUtil;
import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;
import org.datagear.web.config.ApplicationProperties;
import org.datagear.web.util.OperationMessage;
import org.datagear.web.util.WebUtils;
import org.datagear.web.vo.APIDDataFilterPagingQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

/**
 * 看板控制器。
 * 
 * @author datagear@163.com
 *
 */
@Controller
@RequestMapping("/dashboard")
public class DashboardController extends AbstractDataAnalysisController
{
	@Autowired
	private HtmlTplDashboardWidgetEntityService htmlTplDashboardWidgetEntityService;

	@Autowired
	private AnalysisProjectService analysisProjectService;

	@Autowired
	private DashboardShareSetService dashboardShareSetService;

	@Autowired
	private File tempDirectory;

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

	public File getTempDirectory()
	{
		return tempDirectory;
	}

	public void setTempDirectory(File tempDirectory)
	{
		this.tempDirectory = tempDirectory;
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
		setRequestAnalysisProject(request, response, dashboard);
		dashboard.setId(IDUtil.randomIdOnTime20());
		dashboard.setTemplates(new String[0]);
		dashboard.setTemplateEncoding(HtmlTplDashboardWidget.DEFAULT_TEMPLATE_ENCODING);
		
		setFormModel(model, dashboard, REQUEST_ACTION_ADD, SUBMIT_ACTION_SAVE);
		setFormPageAttributes(model);
		setEnableInsertNewChartAttr(request, response, model);

		return "/dashboard/dashboard_form";
	}

	@RequestMapping("/edit")
	public String edit(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id) throws Exception
	{
		User user = getCurrentUser();

		HtmlTplDashboardWidgetEntity dashboard = getByIdForEdit(this.htmlTplDashboardWidgetEntityService, user, id);
		
		setFormModel(model, dashboard, REQUEST_ACTION_EDIT, SUBMIT_ACTION_SAVE);
		setFormPageAttributes(model);
		setEnableInsertNewChartAttr(request, response, model);

		return "/dashboard/dashboard_form";
	}

	@RequestMapping("/copy")
	public String copy(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id) throws Exception
	{
		User user = getCurrentUser();

		HtmlTplDashboardWidgetEntity dashboard = getByIdForView(this.htmlTplDashboardWidgetEntityService, user, id);
		setNullAnalysisProjectIfNoPermission(user, dashboard, getAnalysisProjectService());

		dashboard.setId(IDUtil.randomIdOnTime20());
		inflateCreateTime(dashboard, null);
		
		setFormModel(model, dashboard, REQUEST_ACTION_COPY, SUBMIT_ACTION_SAVE);
		setFormPageAttributes(model);
		model.addAttribute("copySourceId", id);
		setEnableInsertNewChartAttr(request, response, model);

		return "/dashboard/dashboard_form";
	}

	protected boolean setEnableInsertNewChartAttr(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model)
	{
		boolean enable = isEnableInsertNewChart(request, response, model);
		model.addAttribute("enableInsertNewChart", enable);

		return enable;
	}

	/**
	 * 是否在看板表单页面启用【插入图表】功能按钮。
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 */
	protected boolean isEnableInsertNewChart(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model)
	{
		return true;
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

		User user = getCurrentUser();

		HtmlTplDashboardWidgetEntity dashboard = form.getDashboard();

		String[] templates = trimResourceNames(dashboard.getTemplates());
		String[] resourceNames = trimResourceNames(form.getResourceNames());
		String[] resourceContents = form.getResourceContents();
		boolean[] resourceIsTemplates = form.getResourceIsTemplates();

		templates = mergeTemplates(templates, resourceNames, resourceIsTemplates);
		dashboard.setTemplates(templates);
		trimAnalysisProjectAwareEntityForSave(dashboard);

		if (isEmpty(dashboard.getId()) || isBlank(dashboard.getName()) || isEmpty(templates))
			throw new IllegalInputException();

		// 如果编辑了首页模板，则应重新解析编码
		int firstTemplateIndex = StringUtil.search(resourceNames, templates[0]);
		if (firstTemplateIndex > -1)
			dashboard.setTemplateEncoding(resolveTemplateEncoding(resourceContents[firstTemplateIndex]));

		if (form.isSaveAdd())
		{
			inflateCreateUserAndTime(dashboard, user);
			this.htmlTplDashboardWidgetEntityService.add(user, dashboard);

			if (form.hasCopySourceId())
			{
				TplDashboardWidgetResManager dashboardWidgetResManager = this.htmlTplDashboardWidgetEntityService
						.getTplDashboardWidgetResManager();

				dashboardWidgetResManager.copyTo(form.getCopySourceId(), dashboard.getId());
			}
		}
		else
		{
			this.htmlTplDashboardWidgetEntityService.update(user, dashboard);
		}

		for (int i = 0; i < resourceNames.length; i++)
			saveResourceContent(dashboard, resourceNames[i], resourceContents[i]);

		// 返回部分基本信息
		HtmlTplDashboardWidgetEntity data = new HtmlTplDashboardWidgetEntity();
		data.setId(dashboard.getId());
		data.setName(dashboard.getName());
		data.setTemplates(templates);
		data.setTemplateEncoding(dashboard.getTemplateEncoding());
		inflateCreateUserAndTime(data, dashboard.getCreateUser(), dashboard.getCreateTime());
		data.setAnalysisProject(dashboard.getAnalysisProject());

		return optSuccessDataResponseEntity(request, data);
	}

	@RequestMapping(value = "/saveTemplateNames", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveTemplateNames(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model, @RequestParam("id") String id, @RequestBody String[] templates)
			throws Exception
	{
		if (isEmpty(templates))
			throw new IllegalInputException();

		User user = getCurrentUser();

		HtmlTplDashboardWidgetEntity widget = getByIdForEdit(this.htmlTplDashboardWidgetEntityService, user, id);

		templates = trimResourceNames(templates);
		widget.setTemplates(templates);

		this.htmlTplDashboardWidgetEntityService.update(user, widget);

		Map<String, Object> data = buildDashboardIdTemplatesHashMap(id, templates);
		return optSuccessDataResponseEntity(request, data);
	}

	@RequestMapping(value = "/getResourceContent", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public Map<String, Object> getResourceContent(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model, @RequestParam("id") String id,
			@RequestParam("resourceName") String resourceName) throws Exception
	{
		User user = getCurrentUser();
		
		resourceName = trimResourceName(resourceName);

		Map<String, Object> data = new HashMap<>();
		data.put("id", id);
		data.put("resourceName", resourceName);
		
		HtmlTplDashboardWidgetEntity widget = this.htmlTplDashboardWidgetEntityService.getById(user, id);
		boolean exists = false;
		
		if (widget == null)
		{
			exists = false;
			data.put("resourceContent", null);
		}
		else
		{
			exists = this.htmlTplDashboardWidgetEntityService.getTplDashboardWidgetResManager().exists(id, resourceName);
			
			if(exists)
				data.put("resourceContent", readResourceContent(widget, resourceName));
		}
		
		data.put("resourceExists", exists);
		
		if(!exists && (FileUtil.isExtension(resourceName, "html") || FileUtil.isExtension(resourceName, "htm")))
		{
			HtmlTplDashboardWidgetRenderer renderer = getHtmlTplDashboardWidgetEntityService()
					.getHtmlTplDashboardWidgetRenderer();
			String templateEnding = (widget == null ? HtmlTplDashboardWidget.DEFAULT_TEMPLATE_ENCODING : widget.getTemplateEncoding());
			String templateContent = renderer.simpleTemplateContent(templateEnding);

			data.put("defaultTemplateContent", templateContent);
		}

		return data;
	}

	@RequestMapping(value = "/listResources", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public List<String> listResources(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model, @RequestParam("id") String id) throws Exception
	{
		User user = getCurrentUser();

		HtmlTplDashboardWidgetEntity dashboard = this.htmlTplDashboardWidgetEntityService.getById(user, id);

		if (dashboard == null)
			return new ArrayList<>(0);

		TplDashboardWidgetResManager dashboardWidgetResManager = this.htmlTplDashboardWidgetEntityService
				.getTplDashboardWidgetResManager();

		List<String> resources = dashboardWidgetResManager.list(dashboard.getId());

		return resources;
	}

	@RequestMapping(value = "/deleteResource", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> deleteResource(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model, @RequestParam("id") String id, @RequestParam("name") String name)
			throws Exception
	{
		User user = getCurrentUser();
		
		name = trimResourceName(name);
		boolean isNameDirectory = FileUtil.isDirectoryName(name);
		
		HtmlTplDashboardWidgetEntity dashboard = getByIdForEdit(this.htmlTplDashboardWidgetEntityService, user, id);
		
		String[] templates = dashboard.getTemplates();
		List<String> newTemplatesList = new ArrayList<String>(templates.length);
		
		for(String tpl : templates)
		{
			if((isNameDirectory && !tpl.startsWith(name)) || (!isNameDirectory && !tpl.equals(name)))
				newTemplatesList.add(tpl);
		}
		
		String[] newTemplates = newTemplatesList.toArray(new String[newTemplatesList.size()]);
		
		if(isEmpty(newTemplates))
			throw new IllegalInputException();
		
		TplDashboardWidgetResManager dashboardWidgetResManager = this.htmlTplDashboardWidgetEntityService
				.getTplDashboardWidgetResManager();

		dashboardWidgetResManager.delete(id, name);
		
		if (!Arrays.equals(templates, newTemplates))
		{
			dashboard.setTemplates(newTemplates);
			this.htmlTplDashboardWidgetEntityService.update(user, dashboard);
		}

		Map<String, Object> data = buildDashboardIdTemplatesHashMap(id, newTemplates);
		return optSuccessDataResponseEntity(request, data);
	}

	@RequestMapping(value = "/renameResource", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> renameResource(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model, @RequestParam("id") String id, @RequestParam("srcName") String srcName,
			@RequestParam("destName") String destName)
			throws Exception
	{
		User user = getCurrentUser();
		
		srcName = trimResourceName(srcName);
		destName = trimResourceName(destName);
		
		HtmlTplDashboardWidgetEntity dashboard = getByIdForEdit(this.htmlTplDashboardWidgetEntityService, user, id);
		
		String[] templates = dashboard.getTemplates();
		
		TplDashboardWidgetResManager dashboardWidgetResManager = this.htmlTplDashboardWidgetEntityService
				.getTplDashboardWidgetResManager();

		if (dashboardWidgetResManager.exists(id, destName))
		{
			return optFailResponseEntity(request, "file.error.targetFileExists");
		}

		if (destName.startsWith(srcName) && destName.length() > srcName.length())
		{
			return optFailResponseEntity(request, "file.error.moveToSubDirNotAllowed");
		}
		
		Map<String, String> renames = dashboardWidgetResManager.rename(id, srcName, destName);
		
		String[] newTemplates = new String[templates.length];
		
		for(int i=0; i<templates.length; i++)
		{
			String tpl = templates[i];
			
			String destTpl = renames.get(tpl);
			if(!StringUtil.isEmpty(destTpl))
				tpl = destTpl;
			
			newTemplates[i] = tpl;
		}
		
		if (!Arrays.equals(templates, newTemplates))
		{
			dashboard.setTemplates(newTemplates);
			this.htmlTplDashboardWidgetEntityService.update(user, dashboard);
		}

		Map<String, Object> data = buildDashboardIdTemplatesHashMap(id, newTemplates);
		data.put("renames", renames);
		
		return optSuccessDataResponseEntity(request, data);
	}

	@RequestMapping(value = "/uploadResourceFile", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public Map<String, Object> uploadResourceFile(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("file") MultipartFile multipartFile) throws Exception
	{
		File tmpDirectory = FileUtil.generateUniqueDirectory(this.tempDirectory);
		String fileName = multipartFile.getOriginalFilename();
		File file = FileUtil.getFile(tmpDirectory, fileName);

		writeMultipartFile(multipartFile, file);

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
			@RequestParam("resourceName") String resourceName,
			@RequestParam("autoUnzip") boolean autoUnzip,
			@RequestParam("zipFileNameEncoding") String zipFileNameEncoding) throws Exception
	{
		User user = getCurrentUser();

		getByIdForEdit(this.htmlTplDashboardWidgetEntityService, user, id);

		File uploadFile = FileUtil.getDirectory(this.tempDirectory, resourceFilePath, false);

		if (!uploadFile.exists())
			throw new IllegalInputException();

		TplDashboardWidgetResManager dashboardWidgetResManager = this.htmlTplDashboardWidgetEntityService
				.getTplDashboardWidgetResManager();
		
		if(autoUnzip && FileUtil.isExtension(uploadFile, "zip"))
		{
			File unzipRoot = FileUtil.generateUniqueDirectory(this.tempDirectory);
			File unzipDirectory = null;
			
			if (!StringUtil.isEmpty(resourceName))
				unzipDirectory = FileUtil.getDirectory(unzipRoot, resourceName, true);
			else
				unzipDirectory = unzipRoot;

			ZipInputStream zin = null;

			try
			{
				zin = IOUtil.getZipInputStream(uploadFile, zipFileNameEncoding);
				IOUtil.unzipCheckMalformed(zin, unzipDirectory);
			}
			finally
			{
				IOUtil.close(zin);
			}
			
			dashboardWidgetResManager.copyFrom(id, unzipRoot);
		}
		else
		{
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
		}

		return optSuccessResponseEntity(request);
	}

	@RequestMapping(value = "/saveResourceContent", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveResourceContent(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam("id") String id, @RequestParam("resourceName") String resourceName,
			@RequestParam("resourceContent") String resourceContent,
			@RequestParam(value = "isTemplate", required = false) Boolean isTemplate) throws Exception
	{
		User user = getCurrentUser();

		HtmlTplDashboardWidgetEntity dashboard = getByIdForEdit(this.htmlTplDashboardWidgetEntityService, user, id);

		resourceName = trimResourceName(resourceName);
		
		boolean templatesChanged = false;
		boolean resourceExists = this.htmlTplDashboardWidgetEntityService.getTplDashboardWidgetResManager()
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
		
		return optSuccessDataResponseEntity(request, data);
	}

	@RequestMapping("/import")
	public String impt(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model)
	{
		DashboardImportForm form = new DashboardImportForm();
		form.setZipFileNameEncoding(IOUtil.CHARSET_UTF_8);
		form.setAnalysisProject(getRequestAnalysisProject(request, response, getAnalysisProjectService()));

		setFormModel(model, form, REQUEST_ACTION_IMPORT, SUBMIT_ACTION_SAVE_IMPORT);
		addAttributeForWriteJson(model, "availableCharsetNames", getAvailableCharsetNames());

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
				IOUtil.unzipCheckMalformed(in, dashboardDirectory);
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
			@RequestBody DashboardImportForm form) throws Exception
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
				return optFailResponseEntity(request, HttpStatus.BAD_REQUEST,
						"dashboard.import.templateFileNotExists", fileName);
		}

		String templateEncoding = resolveTemplateEncoding(FileUtil.getFile(uploadDirectory, templates[0]));

		User user = getCurrentUser();

		HtmlTplDashboardWidgetEntity dashboard = new HtmlTplDashboardWidgetEntity();
		dashboard.setTemplateSplit(form.getTemplate());
		dashboard.setTemplateEncoding(templateEncoding);
		dashboard.setName(form.getName());

		if (!isEmpty(form.getAnalysisProject()))
			dashboard.setAnalysisProject(form.getAnalysisProject());

		if (isBlank(dashboard.getName()) || isEmpty(dashboard.getTemplates()))
			throw new IllegalInputException();

		dashboard.setId(IDUtil.randomIdOnTime20());
		inflateCreateUserAndTime(dashboard, user);

		trimAnalysisProjectAwareEntityForSave(dashboard);

		this.htmlTplDashboardWidgetEntityService.add(user, dashboard);

		TplDashboardWidgetResManager dashboardWidgetResManager = this.htmlTplDashboardWidgetEntityService
				.getTplDashboardWidgetResManager();

		dashboardWidgetResManager.copyFrom(dashboard.getId(), uploadDirectory);

		return optSuccessResponseEntity(request);
	}

	@RequestMapping("/view")
	public String view(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id) throws Exception
	{
		User user = getCurrentUser();

		HtmlTplDashboardWidgetEntity dashboard = getByIdForView(this.htmlTplDashboardWidgetEntityService, user, id);

		setFormModel(model, dashboard, REQUEST_ACTION_VIEW, SUBMIT_ACTION_NONE);
		setFormPageAttributes(model);
		
		return "/dashboard/dashboard_form";
	}

	@RequestMapping("/export")
	public void export(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id) throws Exception
	{
		User user = getCurrentUser();

		HtmlTplDashboardWidgetEntity dashboard = getByIdForEdit(this.htmlTplDashboardWidgetEntityService, user, id);

		TplDashboardWidgetResManager dashboardWidgetResManager = this.htmlTplDashboardWidgetEntityService
				.getTplDashboardWidgetResManager();

		File tmpDirectory = FileUtil.generateUniqueDirectory(this.tempDirectory);
		dashboardWidgetResManager.copyTo(dashboard.getId(), tmpDirectory);

		setDownloadResponseHeader(request, response, dashboard.getName() + ".zip");
		response.setContentType(CONTENT_TYPE_OCTET_STREAM);

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
		User user = getCurrentUser();

		for (int i = 0; i < ids.length; i++)
		{
			String id = ids[i];
			this.htmlTplDashboardWidgetEntityService.deleteById(user, id);
		}

		return optSuccessResponseEntity(request);
	}

	@RequestMapping("/manage")
	public String manage(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model)
	{
		model.addAttribute("serverURL", WebUtils.getServerURL(request));
		model.addAttribute(KEY_REQUEST_ACTION, REQUEST_ACTION_MANAGE);
		setReadonlyAction(model);
		addAttributeForWriteJson(model, KEY_CURRENT_ANALYSIS_PROJECT,
				getRequestAnalysisProject(request, response, getAnalysisProjectService()));
		
		return "/dashboard/dashboard_table";
	}

	@RequestMapping(value = "/select")
	public String select(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model)
	{
		model.addAttribute("serverURL", WebUtils.getServerURL(request));
		setSelectAction(request, model);
		addAttributeForWriteJson(model, KEY_CURRENT_ANALYSIS_PROJECT,
				getRequestAnalysisProject(request, response, getAnalysisProjectService()));
		
		return "/dashboard/dashboard_table";
	}

	@RequestMapping(value = "/pagingQueryData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public PagingData<HtmlTplDashboardWidgetEntity> pagingQueryData(HttpServletRequest request,
			HttpServletResponse response, final org.springframework.ui.Model springModel,
			@RequestBody(required = false) APIDDataFilterPagingQuery pagingQueryParam) throws Exception
	{
		User user = getCurrentUser();
		final APIDDataFilterPagingQuery pagingQuery = inflateAPIDDataFilterPagingQuery(request, pagingQueryParam);

		PagingData<HtmlTplDashboardWidgetEntity> pagingData = this.htmlTplDashboardWidgetEntityService.pagingQuery(user,
				pagingQuery, pagingQuery.getDataFilter(), pagingQuery.getAnalysisProjectId());

		return pagingData;
	}
	
	@RequestMapping("/shareSet")
	public String shareSet(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id) throws Exception
	{
		User user = getCurrentUser();

		HtmlTplDashboardWidgetEntity dashboard = getByIdForEdit(this.htmlTplDashboardWidgetEntityService, user, id);

		DashboardShareSet dashboardShareSet = this.dashboardShareSetService.getById(id);

		if (dashboardShareSet == null)
		{
			dashboardShareSet = new DashboardShareSet(dashboard.getId());
			dashboardShareSet.setEnablePassword(false);
			dashboardShareSet.setAnonymousPassword(false);
		}

		dashboardShareSet.setPassword("");

		setFormModel(model, dashboardShareSet, "shareSet", "saveShareSet");

		return "/dashboard/dashboard_share_set";
	}

	@RequestMapping(value = "/saveShareSet", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveShareSet(HttpServletRequest request, HttpServletResponse response,
			@RequestBody DashboardShareSet form) throws Exception
	{
		if (isEmpty(form.getId()))
			throw new IllegalInputException();

		User user = getCurrentUser();

		getByIdForEdit(this.htmlTplDashboardWidgetEntityService, user, form.getId());

		this.dashboardShareSetService.save(form);

		return optSuccessResponseEntity(request);
	}
	
	protected void setFormPageAttributes(org.springframework.ui.Model model)
	{
		model.addAttribute("dashboardGlobalResUrlPrefix",
				(StringUtil.isEmpty(this.applicationProperties.getDashboardGlobalResUrlPrefix()) ? ""
						: this.applicationProperties.getDashboardGlobalResUrlPrefix()));
		model.addAttribute("defaultTempalteName", HtmlTplDashboardWidgetEntity.DEFAULT_TEMPLATES[0]);

		addAttributeForWriteJson(model, "availableCharsetNames", getAvailableCharsetNames());
		model.addAttribute("zipFileNameEncodingDefault", IOUtil.CHARSET_UTF_8);
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
					.getTplDashboardWidgetResManager().getWriter(widget, name));
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
			reader = this.htmlTplDashboardWidgetEntityService.getTplDashboardWidgetResManager().getReader(widget,
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

	protected void setRequestAnalysisProject(HttpServletRequest request, HttpServletResponse response,
			HtmlTplDashboardWidgetEntity entity)
	{
		setRequestAnalysisProjectIfValid(request, response, this.analysisProjectService, entity);
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

	public static class DashboardImportForm implements ControllerForm
	{
		private static final long serialVersionUID = 1L;

		private String name;

		private String template;

		private String dashboardFileName;

		private String zipFileNameEncoding;

		private AnalysisProject analysisProject;

		public DashboardImportForm()
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
