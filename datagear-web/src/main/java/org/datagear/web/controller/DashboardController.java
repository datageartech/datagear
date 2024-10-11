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
import java.util.function.Supplier;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.analysis.TplDashboardWidgetResManager;
import org.datagear.analysis.support.html.HtmlTplDashboardWidget;
import org.datagear.analysis.support.html.HtmlTplDashboardWidgetRenderer;
import org.datagear.management.domain.AnalysisProject;
import org.datagear.management.domain.AnalysisProjectAwareEntity;
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
import org.datagear.util.function.OnceSupplier;
import org.datagear.web.config.ApplicationProperties;
import org.datagear.web.util.AnalysisProjectAwareSupport;
import org.datagear.web.util.OperationMessage;
import org.datagear.web.util.WebUtils;
import org.datagear.web.vo.APIDDataFilterPagingQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

	@Autowired
	private AnalysisProjectAwareSupport analysisProjectAwareSupport;

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

	public AnalysisProjectAwareSupport getAnalysisProjectAwareSupport()
	{
		return analysisProjectAwareSupport;
	}

	public void setAnalysisProjectAwareSupport(AnalysisProjectAwareSupport analysisProjectAwareSupport)
	{
		this.analysisProjectAwareSupport = analysisProjectAwareSupport;
	}

	@RequestMapping("/add")
	public String add(HttpServletRequest request, HttpServletResponse response, Model model)
	{
		setFormAction(model, REQUEST_ACTION_ADD, SUBMIT_ACTION_SAVE_ADD);

		HtmlTplDashboardWidgetEntity entity = createAdd(request, model);
		setRequestAnalysisProject(request, entity);
		toFormResponseData(request, entity);
		setFormPageAttr(request, model, entity);

		return "/dashboard/dashboard_form";
	}

	protected HtmlTplDashboardWidgetEntity createAdd(HttpServletRequest request, Model model)
	{
		HtmlTplDashboardWidgetEntity entity = createInstance();
		entity.setTemplates(HtmlTplDashboardWidgetEntity.DEFAULT_TEMPLATES);
		entity.setTemplateEncoding(HtmlTplDashboardWidget.DEFAULT_TEMPLATE_ENCODING);

		return entity;
	}

	@RequestMapping(value = "/saveAdd", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAdd(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "copySourceId", required = false) String copySourceId,
			@RequestBody HtmlTplDashboardWidgetEntity entity) throws Exception
	{
		User user = getCurrentUser();

		entity.setId(IDUtil.randomIdOnTime20());
		inflateCreateUserAndTime(entity, user);
		inflateSaveEntity(request, user, entity);

		ResponseEntity<OperationMessage> re = checkSaveEntity(request, user, entity, null);

		if (re != null)
			return re;

		this.htmlTplDashboardWidgetEntityService.add(entity);

		if (!isEmpty(copySourceId))
		{
			getByIdForEdit(this.htmlTplDashboardWidgetEntityService, user, copySourceId);

			TplDashboardWidgetResManager dashboardWidgetResManager = this.htmlTplDashboardWidgetEntityService
					.getTplDashboardWidgetResManager();
			dashboardWidgetResManager.copyTo(copySourceId, entity.getId());
		}

		toFormResponseData(request, entity);

		return optSuccessDataResponseEntity(request, entity);
	}

	@RequestMapping("/edit")
	public String edit(HttpServletRequest request, HttpServletResponse response, Model model,
			@RequestParam("id") String id) throws Exception
	{
		User user = getCurrentUser();
		setFormAction(model, REQUEST_ACTION_EDIT, SUBMIT_ACTION_SAVE_EDIT);

		HtmlTplDashboardWidgetEntity entity = getByIdForEdit(this.htmlTplDashboardWidgetEntityService, user, id);
		toFormResponseData(request, entity);
		setFormPageAttr(request, model, entity);

		return "/dashboard/dashboard_form";
	}

	@RequestMapping(value = "/saveEdit", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveEdit(HttpServletRequest request, HttpServletResponse response,
			@RequestBody HtmlTplDashboardWidgetEntity entity) throws Exception
	{
		User user = getCurrentUser();

		inflateSaveEntity(request, user, entity);

		OnceSupplier<HtmlTplDashboardWidgetEntity> persist = new OnceSupplier<>(() ->
		{
			return getByIdForEdit(this.htmlTplDashboardWidgetEntityService, user, entity.getId());
		});

		ResponseEntity<OperationMessage> re = checkSaveEntity(request, user, entity, persist);

		if (re != null)
			return re;

		mergeSaveEditEntity(entity, persist.get());

		this.htmlTplDashboardWidgetEntityService.update(user, entity);

		toFormResponseData(request, entity);

		return optSuccessDataResponseEntity(request, entity);
	}

	protected void mergeSaveEditEntity(HtmlTplDashboardWidgetEntity entity, HtmlTplDashboardWidgetEntity persist)
	{
		// 设置不允许修改的项
		entity.setTemplates(persist.getTemplates());
		entity.setTemplateEncoding(persist.getTemplateEncoding());
	}

	@RequestMapping("/copy")
	public String copy(HttpServletRequest request, HttpServletResponse response, Model model,
			@RequestParam("id") String id) throws Exception
	{
		User user = getCurrentUser();
		setFormAction(model, REQUEST_ACTION_COPY, SUBMIT_ACTION_SAVE_ADD);

		// 统一复制规则，至少有编辑权限才允许复制
		HtmlTplDashboardWidgetEntity entity = getByIdForEdit(this.htmlTplDashboardWidgetEntityService, user, id);
		toCopyResponseData(request, model, user, entity);
		setFormPageAttr(request, model, entity);
		model.addAttribute("copySourceId", id);

		return "/dashboard/dashboard_form";
	}

	protected void toCopyResponseData(HttpServletRequest request, Model model, User user,
			HtmlTplDashboardWidgetEntity entity) throws Exception
	{
		this.analysisProjectAwareSupport.setRefNullIfDenied(user, entity, getAnalysisProjectService());

		toFormResponseData(request, entity);
		entity.setId(null);
	}

	@RequestMapping("/design")
	public String design(HttpServletRequest request, HttpServletResponse response, Model model,
			@RequestParam("id") String id) throws Exception
	{
		User user = getCurrentUser();
		setFormAction(model, "design", "saveDesign");

		HtmlTplDashboardWidgetEntity entity = getByIdForEdit(this.htmlTplDashboardWidgetEntityService, user, id);
		toFormResponseData(request, entity);
		setFormPageAttr(request, model, entity);
		setDesignPageAttr(request, model);

		return "/dashboard/dashboard_design_form";
	}

	@RequestMapping(value = "/saveDesign", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveDesign(HttpServletRequest request, HttpServletResponse response,
			@RequestBody HtmlTplDashboardDesignForm form) throws Exception
	{
		if (isEmpty(form.getDashboard()) || isNull(form.getResourceNames()) || isNull(form.getResourceContents())
				|| isNull(form.getResourceIsTemplates())
				|| form.getResourceNames().length != form.getResourceContents().length
				|| form.getResourceContents().length != form.getResourceIsTemplates().length)
		{
			throw new IllegalInputException();
		}

		User user = getCurrentUser();

		form.setResourceNames(trimResourceNames(form.getResourceNames()));

		HtmlTplDashboardWidgetEntity entity = form.getDashboard();
		inflateSaveDesignEntity(request, user, form, entity);

		if (isEmpty(entity.getId()) || isEmpty(entity.getTemplates()))
			throw new IllegalInputException();

		this.htmlTplDashboardWidgetEntityService.updateTemplate(user, entity);

		String[] resourceNames = form.getResourceNames();
		String[] resourceContents = form.getResourceContents();

		for (int i = 0; i < resourceNames.length; i++)
			saveResourceContent(entity, resourceNames[i], resourceContents[i]);

		toFormResponseData(request, entity);

		return optSuccessDataResponseEntity(request, entity);
	}

	protected void inflateSaveDesignEntity(HttpServletRequest request, User user, HtmlTplDashboardDesignForm form,
			HtmlTplDashboardWidgetEntity entity) throws Exception
	{
		String[] templates = trimResourceNames(entity.getTemplates());
		String[] resourceNames = form.getResourceNames();
		String[] resourceContents = form.getResourceContents();
		boolean[] resourceIsTemplates = form.getResourceIsTemplates();

		templates = mergeTemplates(templates, resourceNames, resourceIsTemplates);
		entity.setTemplates(templates);

		// 如果编辑了首页模板，则应重新解析编码
		int firstTemplateIndex = StringUtil.search(resourceNames, templates[0]);
		if (firstTemplateIndex > -1)
			entity.setTemplateEncoding(resolveTemplateEncoding(resourceContents[firstTemplateIndex]));

	}

	@RequestMapping(value = "/saveTemplateNames", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveTemplateNames(HttpServletRequest request, HttpServletResponse response,
			Model model, @RequestParam("id") String id, @RequestBody String[] templates)
			throws Exception
	{
		if (isEmpty(templates))
			throw new IllegalInputException();

		User user = getCurrentUser();

		HtmlTplDashboardWidgetEntity entity = getByIdForEdit(this.htmlTplDashboardWidgetEntityService, user, id);

		templates = trimResourceNames(templates);
		entity.setTemplates(templates);

		this.htmlTplDashboardWidgetEntityService.updateTemplate(user, entity);

		Map<String, Object> data = buildDashboardIdTemplatesHashMap(id, templates);
		return optSuccessDataResponseEntity(request, data);
	}

	@RequestMapping(value = "/getResourceContent", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public Map<String, Object> getResourceContent(HttpServletRequest request, HttpServletResponse response,
			Model model, @RequestParam("id") String id,
			@RequestParam("resourceName") String resourceName) throws Exception
	{
		User user = getCurrentUser();
		
		resourceName = trimResourceName(resourceName);

		Map<String, Object> data = new HashMap<>();
		data.put("id", id);
		data.put("resourceName", resourceName);
		
		HtmlTplDashboardWidgetEntity entity = this.htmlTplDashboardWidgetEntityService.getById(user, id);
		boolean exists = false;
		
		if (entity == null)
		{
			exists = false;
			data.put("resourceContent", null);
		}
		else
		{
			exists = this.htmlTplDashboardWidgetEntityService.getTplDashboardWidgetResManager().exists(id, resourceName);
			
			if(exists)
				data.put("resourceContent", readResourceContent(entity, resourceName));
		}
		
		data.put("resourceExists", exists);
		
		if(!exists && (FileUtil.isExtension(resourceName, "html") || FileUtil.isExtension(resourceName, "htm")))
		{
			HtmlTplDashboardWidgetRenderer renderer = getHtmlTplDashboardWidgetEntityService()
					.getHtmlTplDashboardWidgetRenderer();
			String templateEnding = (entity == null ? HtmlTplDashboardWidget.DEFAULT_TEMPLATE_ENCODING : entity.getTemplateEncoding());
			String templateContent = renderer.simpleTemplateContent(templateEnding);

			data.put("defaultTemplateContent", templateContent);
		}

		return data;
	}

	@RequestMapping(value = "/listResources", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public List<String> listResources(HttpServletRequest request, HttpServletResponse response,
			Model model, @RequestParam("id") String id) throws Exception
	{
		User user = getCurrentUser();

		HtmlTplDashboardWidgetEntity entity = this.htmlTplDashboardWidgetEntityService.getById(user, id);

		if (entity == null)
			return new ArrayList<>(0);

		TplDashboardWidgetResManager dashboardWidgetResManager = this.htmlTplDashboardWidgetEntityService
				.getTplDashboardWidgetResManager();

		List<String> resources = dashboardWidgetResManager.list(entity.getId());

		return resources;
	}

	@RequestMapping(value = "/deleteResource", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> deleteResource(HttpServletRequest request, HttpServletResponse response,
			Model model, @RequestParam("id") String id, @RequestParam("name") String name)
			throws Exception
	{
		User user = getCurrentUser();
		
		name = trimResourceName(name);
		boolean isNameDirectory = FileUtil.isDirectoryName(name);
		
		HtmlTplDashboardWidgetEntity entity = getByIdForEdit(this.htmlTplDashboardWidgetEntityService, user, id);
		
		String[] templates = entity.getTemplates();
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
			entity.setTemplates(newTemplates);
			this.htmlTplDashboardWidgetEntityService.updateTemplate(user, entity);
		}

		Map<String, Object> data = buildDashboardIdTemplatesHashMap(id, newTemplates);
		return optSuccessDataResponseEntity(request, data);
	}

	@RequestMapping(value = "/renameResource", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> renameResource(HttpServletRequest request, HttpServletResponse response,
			Model model, @RequestParam("id") String id, @RequestParam("srcName") String srcName,
			@RequestParam("destName") String destName)
			throws Exception
	{
		User user = getCurrentUser();
		
		srcName = trimResourceName(srcName);
		destName = trimResourceName(destName);
		
		HtmlTplDashboardWidgetEntity entity = getByIdForEdit(this.htmlTplDashboardWidgetEntityService, user, id);
		
		String[] templates = entity.getTemplates();
		
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
			entity.setTemplates(newTemplates);
			this.htmlTplDashboardWidgetEntityService.updateTemplate(user, entity);
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

		HtmlTplDashboardWidgetEntity entity = getByIdForEdit(this.htmlTplDashboardWidgetEntityService, user, id);

		resourceName = trimResourceName(resourceName);
		
		boolean templatesChanged = false;
		boolean resourceExists = this.htmlTplDashboardWidgetEntityService.getTplDashboardWidgetResManager()
				.exists(id, resourceName);
		
		saveResourceContent(entity, resourceName, resourceContent);

		String[] templates = entity.getTemplates();
		String[] newTemplates = mergeTemplates(templates, resourceName, Boolean.TRUE.equals(isTemplate));
		templatesChanged = !Arrays.equals(templates, newTemplates);

		if (templatesChanged)
		{
			entity.setTemplates(newTemplates);
			this.htmlTplDashboardWidgetEntityService.updateTemplate(user, entity);
		}

		Map<String, Object> data = buildDashboardIdTemplatesHashMap(id, newTemplates);
		data.put("templatesChanged", templatesChanged);
		data.put("resourceExists", resourceExists);
		
		return optSuccessDataResponseEntity(request, data);
	}

	@RequestMapping("/import")
	public String impt(HttpServletRequest request, HttpServletResponse response, Model model)
	{
		setFormAction(model, REQUEST_ACTION_IMPORT, SUBMIT_ACTION_SAVE_IMPORT);

		DashboardImportForm form = new DashboardImportForm();
		form.setZipFileNameEncoding(IOUtil.CHARSET_UTF_8);
		form.setAnalysisProject(getRequestAnalysisProject(request, getAnalysisProjectService()));
		setFormModel(model, form);
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

		User user = getCurrentUser();
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

		HtmlTplDashboardWidgetEntity entity = createInstance();
		inflateSaveImportEntity(request, user, form, uploadDirectory, templates, entity);

		ResponseEntity<OperationMessage> re = checkSaveEntity(request, user, entity, null);

		if (re != null)
			return re;

		this.htmlTplDashboardWidgetEntityService.add(user, entity);

		TplDashboardWidgetResManager dashboardWidgetResManager = this.htmlTplDashboardWidgetEntityService
				.getTplDashboardWidgetResManager();

		dashboardWidgetResManager.copyFrom(entity.getId(), uploadDirectory);

		toFormResponseData(request, entity);

		return optSuccessDataResponseEntity(request, entity);
	}

	protected void inflateSaveImportEntity(HttpServletRequest request, User user, DashboardImportForm form,
			File uploadDirectory, String[] templates, HtmlTplDashboardWidgetEntity entity) throws Exception
	{
		String templateEncoding = resolveTemplateEncoding(FileUtil.getFile(uploadDirectory, templates[0]));

		entity.setId(IDUtil.randomIdOnTime20());
		entity.setTemplateSplit(form.getTemplate());
		entity.setTemplateEncoding(templateEncoding);
		entity.setName(form.getName());
		entity.setAnalysisProject(form.getAnalysisProject());
		inflateCreateUserAndTime(entity, user);
		inflateSaveEntity(request, user, entity);
	}

	@RequestMapping("/view")
	public String view(HttpServletRequest request, HttpServletResponse response, Model model,
			@RequestParam("id") String id) throws Exception
	{
		User user = getCurrentUser();
		setFormAction(model, REQUEST_ACTION_VIEW, SUBMIT_ACTION_NONE);

		HtmlTplDashboardWidgetEntity entity = getByIdForView(this.htmlTplDashboardWidgetEntityService, user, id);
		toFormResponseData(request, entity);
		setFormPageAttr(request, model, entity);
		setDesignPageAttr(request, model);
		
		return "/dashboard/dashboard_design_form";
	}

	@RequestMapping("/export")
	public void export(HttpServletRequest request, HttpServletResponse response, Model model,
			@RequestParam("id") String id) throws Exception
	{
		User user = getCurrentUser();

		HtmlTplDashboardWidgetEntity entity = getByIdForEdit(this.htmlTplDashboardWidgetEntityService, user, id);

		TplDashboardWidgetResManager dashboardWidgetResManager = this.htmlTplDashboardWidgetEntityService
				.getTplDashboardWidgetResManager();

		File tmpDirectory = FileUtil.generateUniqueDirectory(this.tempDirectory);
		dashboardWidgetResManager.copyTo(entity.getId(), tmpDirectory);

		setDownloadResponseHeader(request, response, entity.getName() + ".zip");
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
			Model model)
	{
		model.addAttribute("serverURL", WebUtils.getServerURL(request));
		model.addAttribute(KEY_REQUEST_ACTION, REQUEST_ACTION_MANAGE);
		setReadonlyAction(model);
		addAttributeForWriteJson(model, KEY_CURRENT_ANALYSIS_PROJECT,
				getRequestAnalysisProject(request, getAnalysisProjectService()));
		
		return "/dashboard/dashboard_table";
	}

	@RequestMapping(value = "/select")
	public String select(HttpServletRequest request, HttpServletResponse response, Model model)
	{
		model.addAttribute("serverURL", WebUtils.getServerURL(request));
		setSelectAction(request, model);
		addAttributeForWriteJson(model, KEY_CURRENT_ANALYSIS_PROJECT,
				getRequestAnalysisProject(request, getAnalysisProjectService()));
		
		return "/dashboard/dashboard_table";
	}

	@RequestMapping(value = "/pagingQueryData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public PagingData<HtmlTplDashboardWidgetEntity> pagingQueryData(HttpServletRequest request,
			HttpServletResponse response, final Model springModel,
			@RequestBody(required = false) APIDDataFilterPagingQuery pagingQueryParam) throws Exception
	{
		User user = getCurrentUser();
		final APIDDataFilterPagingQuery pagingQuery = inflateAPIDDataFilterPagingQuery(request, pagingQueryParam);

		PagingData<HtmlTplDashboardWidgetEntity> pagingData = this.htmlTplDashboardWidgetEntityService.pagingQuery(user,
				pagingQuery, pagingQuery.getDataFilter(), pagingQuery.getAnalysisProjectId());
		toQueryResponseData(request, pagingData.getItems());

		return pagingData;
	}
	
	@RequestMapping("/shareSet")
	public String shareSet(HttpServletRequest request, HttpServletResponse response, Model model,
			@RequestParam("id") String id) throws Exception
	{
		User user = getCurrentUser();
		setFormAction(model, "shareSet", "saveShareSet");

		HtmlTplDashboardWidgetEntity dashboard = getByIdForEdit(this.htmlTplDashboardWidgetEntityService, user, id);

		DashboardShareSet entity = this.dashboardShareSetService.getById(id);

		if (entity == null)
			entity = createDashboardShareSet(dashboard);

		toFormResponseData(request, entity);
		setFormModel(model, entity);

		return "/dashboard/dashboard_share_set";
	}

	protected DashboardShareSet createDashboardShareSet(HtmlTplDashboardWidgetEntity dashboard)
	{
		DashboardShareSet entity = new DashboardShareSet(dashboard.getId());
		entity.setEnablePassword(false);
		entity.setAnonymousPassword(false);

		return entity;
	}

	@RequestMapping(value = "/saveShareSet", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveShareSet(HttpServletRequest request, HttpServletResponse response,
			@RequestBody DashboardShareSet entity) throws Exception
	{
		if (isEmpty(entity.getId()))
			throw new IllegalInputException();

		User user = getCurrentUser();

		getByIdForEdit(this.htmlTplDashboardWidgetEntityService, user, entity.getId());

		this.dashboardShareSetService.save(entity);

		toFormResponseData(request, entity);

		return optSuccessDataResponseEntity(request, entity);
	}
	
	protected void toFormResponseData(HttpServletRequest request, DashboardShareSet entity)
	{
		entity.setPassword("");
	}

	protected void setDesignPageAttr(HttpServletRequest request, Model model)
	{
		model.addAttribute("dashboardGlobalResUrlPrefix",
				(StringUtil.isEmpty(this.applicationProperties.getDashboardGlobalResUrlPrefix()) ? ""
						: this.applicationProperties.getDashboardGlobalResUrlPrefix()));
		model.addAttribute("defaultTempalteName", HtmlTplDashboardWidgetEntity.DEFAULT_TEMPLATES[0]);

		addAttributeForWriteJson(model, "availableCharsetNames", getAvailableCharsetNames());
		model.addAttribute("zipFileNameEncodingDefault", IOUtil.CHARSET_UTF_8);

		setEnableInsertNewChartAttr(request, model);
	}

	protected boolean setEnableInsertNewChartAttr(HttpServletRequest request, Model model)
	{
		boolean enable = isEnableInsertNewChart(request, model);
		model.addAttribute("enableInsertNewChart", enable);

		return enable;
	}

	/**
	 * 是否在看板表单页面启用【插入图表】功能按钮。
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	protected boolean isEnableInsertNewChart(HttpServletRequest request, Model model)
	{
		return true;
	}

	protected void setFormPageAttr(HttpServletRequest request, Model model, HtmlTplDashboardWidgetEntity entity)
	{
		setFormModel(model, entity);
	}

	protected void toFormResponseData(HttpServletRequest request, HtmlTplDashboardWidgetEntity entity)
	{
	}

	protected void toQueryResponseData(HttpServletRequest request, List<HtmlTplDashboardWidgetEntity> items)
	{
	}

	protected void inflateSaveEntity(HttpServletRequest request, User user, HtmlTplDashboardWidgetEntity entity)
	{
		trimAnalysisProjectAware(entity);
	}

	protected HtmlTplDashboardWidgetEntity createInstance()
	{
		return new HtmlTplDashboardWidgetEntity();
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

	protected ResponseEntity<OperationMessage> checkSaveEntity(HttpServletRequest request, User user,
			HtmlTplDashboardWidgetEntity entity, OnceSupplier<HtmlTplDashboardWidgetEntity> persist)
	{
		if (isEmpty(entity.getId()) || isBlank(entity.getName()))
			throw new IllegalInputException();

		if (isEmpty(entity.getTemplates()))
			throw new IllegalInputException();

		checkSaveRefAnalysisProject(request, user, entity, persist);

		return null;
	}

	protected void trimAnalysisProjectAware(AnalysisProjectAwareEntity entity)
	{
		this.analysisProjectAwareSupport.trim(entity);
	}

	@SuppressWarnings("unchecked")
	protected void checkSaveRefAnalysisProject(HttpServletRequest request, User user,
			AnalysisProjectAwareEntity dataSet, Supplier<? extends AnalysisProjectAwareEntity> persist)
	{
		this.analysisProjectAwareSupport.checkSaveSupplier(user, dataSet,
				(Supplier<AnalysisProjectAwareEntity>) persist, getAnalysisProjectService());
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

	protected void setRequestAnalysisProject(HttpServletRequest request,
			HtmlTplDashboardWidgetEntity entity)
	{
		setRequestAnalysisProjectIfValid(request, this.analysisProjectService, entity);
	}

	public static class HtmlTplDashboardDesignForm implements ControllerForm
	{
		private static final long serialVersionUID = 1L;

		private HtmlTplDashboardWidgetEntity dashboard;

		private String[] resourceNames;

		private String[] resourceContents;

		private boolean[] resourceIsTemplates;

		public HtmlTplDashboardDesignForm()
		{
			super();
		}

		public HtmlTplDashboardDesignForm(HtmlTplDashboardWidgetEntity dashboard, String[] resourceNames,
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
