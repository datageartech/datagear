/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.analysis.DataSet;
import org.datagear.analysis.DataSetParam;
import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.DataSetQuery;
import org.datagear.analysis.ResolvedDataSetResult;
import org.datagear.analysis.support.DataSetParamValueConverter;
import org.datagear.analysis.support.ProfileDataSet;
import org.datagear.analysis.support.SqlDataSet;
import org.datagear.analysis.support.TemplateContext;
import org.datagear.analysis.support.TemplateResolvedDataSetResult;
import org.datagear.management.domain.Authorization;
import org.datagear.management.domain.CsvFileDataSetEntity;
import org.datagear.management.domain.CsvValueDataSetEntity;
import org.datagear.management.domain.DataSetEntity;
import org.datagear.management.domain.DataSetResDirectory;
import org.datagear.management.domain.DirectoryFileDataSetEntity;
import org.datagear.management.domain.ExcelDataSetEntity;
import org.datagear.management.domain.HttpDataSetEntity;
import org.datagear.management.domain.JsonFileDataSetEntity;
import org.datagear.management.domain.JsonValueDataSetEntity;
import org.datagear.management.domain.Schema;
import org.datagear.management.domain.SchemaConnectionFactory;
import org.datagear.management.domain.SqlDataSetEntity;
import org.datagear.management.domain.User;
import org.datagear.management.service.AnalysisProjectService;
import org.datagear.management.service.DataPermissionEntityService;
import org.datagear.management.service.DataSetEntityService;
import org.datagear.management.service.DataSetResDirectoryService;
import org.datagear.management.service.PermissionDeniedException;
import org.datagear.persistence.PagingData;
import org.datagear.util.FileUtil;
import org.datagear.util.IDUtil;
import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;
import org.datagear.web.controller.AbstractDataAnalysisController.AnalysisUser;
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
 * 数据集控制器。
 * 
 * @author datagear@163.com
 *
 */
@Controller
@RequestMapping("/dataSet")
public class DataSetController extends AbstractSchemaConnController
{
	static
	{
		AuthorizationResourceMetas.registerForShare(SqlDataSetEntity.AUTHORIZATION_RESOURCE_TYPE);
	}

	@Autowired
	private DataSetEntityService dataSetEntityService;

	@Autowired
	private AnalysisProjectService analysisProjectService;

	@Autowired
	private File tempDirectory;

	private DataSetParamValueConverter dataSetParamValueConverter = new DataSetParamValueConverter();

	@Autowired
	private DataSetResDirectoryService dataSetResDirectoryService;

	public DataSetController()
	{
		super();
	}

	public DataSetEntityService getDataSetEntityService()
	{
		return dataSetEntityService;
	}

	public void setDataSetEntityService(DataSetEntityService dataSetEntityService)
	{
		this.dataSetEntityService = dataSetEntityService;
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

	public DataSetParamValueConverter getDataSetParamValueConverter()
	{
		return dataSetParamValueConverter;
	}

	public void setDataSetParamValueConverter(DataSetParamValueConverter dataSetParamValueConverter)
	{
		this.dataSetParamValueConverter = dataSetParamValueConverter;
	}

	public DataSetResDirectoryService getDataSetResDirectoryService()
	{
		return dataSetResDirectoryService;
	}

	public void setDataSetResDirectoryService(DataSetResDirectoryService dataSetResDirectoryService)
	{
		this.dataSetResDirectoryService = dataSetResDirectoryService;
	}

	@RequestMapping("/addFor" + DataSetEntity.DATA_SET_TYPE_SQL)
	public String addForSql(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model)
	{
		SqlDataSetEntity dataSet = new SqlDataSetEntity();
		setCookieAnalysisProject(request, response, dataSet);

		setFormModel(model, dataSet, REQUEST_ACTION_ADD, "saveAddFor" + DataSetEntity.DATA_SET_TYPE_SQL);

		return buildFormView(dataSet.getDataSetType());
	}

	@RequestMapping(value = "/saveAddFor" + DataSetEntity.DATA_SET_TYPE_SQL, produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAddForSql(HttpServletRequest request, HttpServletResponse response,
			@RequestBody SqlDataSetEntity dataSet)
	{
		User user = WebUtils.getUser();

		dataSet.setId(IDUtil.randomIdOnTime20());
		dataSet.setCreateUser(user.cloneNoPassword());
		dataSet.setCreateTime(new Date());

		ResponseEntity<OperationMessage> responseEntity = checkSaveSqlDataSetEntity(request, dataSet);

		if (responseEntity != null)
			return responseEntity;

		trimAnalysisProjectAwareEntityForSave(dataSet);

		this.dataSetEntityService.add(user, dataSet);

		return operationSuccessResponseEntity(request, dataSet);
	}

	@RequestMapping("/addFor" + DataSetEntity.DATA_SET_TYPE_JsonValue)
	public String addForJsonValue(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model)
	{
		JsonValueDataSetEntity dataSet = new JsonValueDataSetEntity();
		setCookieAnalysisProject(request, response, dataSet);

		setFormModel(model, dataSet, REQUEST_ACTION_ADD, "saveAddFor" + DataSetEntity.DATA_SET_TYPE_JsonValue);

		return buildFormView(dataSet.getDataSetType());
	}

	@RequestMapping(value = "/saveAddFor" + DataSetEntity.DATA_SET_TYPE_JsonValue, produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAddForJsonValue(HttpServletRequest request,
			HttpServletResponse response, @RequestBody JsonValueDataSetEntity dataSet)
	{
		User user = WebUtils.getUser();

		dataSet.setId(IDUtil.randomIdOnTime20());
		dataSet.setCreateUser(user.cloneNoPassword());
		dataSet.setCreateTime(new Date());

		ResponseEntity<OperationMessage> responseEntity = checkSaveJsonValueDataSetEntity(request, dataSet);

		if (responseEntity != null)
			return responseEntity;

		trimAnalysisProjectAwareEntityForSave(dataSet);

		this.dataSetEntityService.add(user, dataSet);

		return operationSuccessResponseEntity(request, dataSet);
	}

	@RequestMapping("/addFor" + DataSetEntity.DATA_SET_TYPE_JsonFile)
	public String addForJsonFile(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model)
	{
		JsonFileDataSetEntity dataSet = new JsonFileDataSetEntity();
		dataSet.setFileSourceType(DirectoryFileDataSetEntity.FILE_SOURCE_TYPE_UPLOAD);
		setCookieAnalysisProject(request, response, dataSet);

		addAttributeForWriteJson(model, "availableCharsetNames", getAvailableCharsetNames());
		setFormModel(model, dataSet, REQUEST_ACTION_ADD, "saveAddFor" + DataSetEntity.DATA_SET_TYPE_JsonFile);

		return buildFormView(dataSet.getDataSetType());
	}

	@RequestMapping(value = "/saveAddFor" + DataSetEntity.DATA_SET_TYPE_JsonFile, produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAddForJsonFile(HttpServletRequest request, HttpServletResponse response,
			@RequestBody JsonFileDataSetEntity dataSet) throws Throwable
	{
		User user = WebUtils.getUser();

		dataSet.setId(IDUtil.randomIdOnTime20());
		dataSet.setCreateUser(user.cloneNoPassword());
		dataSet.setCreateTime(new Date());

		ResponseEntity<OperationMessage> responseEntity = checkSaveJsonFileDataSetEntity(request, dataSet);

		if (responseEntity != null)
			return responseEntity;

		trimAnalysisProjectAwareEntityForSave(dataSet);
		trimDirectoryFileDataSetEntityForSave(dataSet);

		this.dataSetEntityService.add(user, dataSet);
		copyToDirectoryFileDataSetEntityDirectoryIf(dataSet, "");
		
		return operationSuccessResponseEntity(request, dataSet);
	}

	@RequestMapping("/addFor" + DataSetEntity.DATA_SET_TYPE_Excel)
	public String addForExcel(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model)
	{
		ExcelDataSetEntity dataSet = new ExcelDataSetEntity();
		dataSet.setFileSourceType(DirectoryFileDataSetEntity.FILE_SOURCE_TYPE_UPLOAD);
		dataSet.setNameRow(1);
		setCookieAnalysisProject(request, response, dataSet);

		setFormModel(model, dataSet, REQUEST_ACTION_ADD, "saveAddFor" + DataSetEntity.DATA_SET_TYPE_Excel);

		return buildFormView(dataSet.getDataSetType());
	}

	@RequestMapping(value = "/saveAddFor" + DataSetEntity.DATA_SET_TYPE_Excel, produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAddForExcel(HttpServletRequest request, HttpServletResponse response,
			@RequestBody ExcelDataSetEntity dataSet) throws Throwable
	{
		User user = WebUtils.getUser();

		dataSet.setId(IDUtil.randomIdOnTime20());
		dataSet.setCreateUser(user.cloneNoPassword());
		dataSet.setCreateTime(new Date());

		ResponseEntity<OperationMessage> responseEntity = checkSaveExcelDataSetEntity(request, dataSet);

		if (responseEntity != null)
			return responseEntity;

		trimAnalysisProjectAwareEntityForSave(dataSet);
		trimDirectoryFileDataSetEntityForSave(dataSet);

		this.dataSetEntityService.add(user, dataSet);
		copyToDirectoryFileDataSetEntityDirectoryIf(dataSet, "");

		return operationSuccessResponseEntity(request, dataSet);
	}

	@RequestMapping("/addFor" + DataSetEntity.DATA_SET_TYPE_CsvValue)
	public String addForCsvValue(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model)
	{
		CsvValueDataSetEntity dataSet = new CsvValueDataSetEntity();
		dataSet.setNameRow(1);
		setCookieAnalysisProject(request, response, dataSet);

		setFormModel(model, dataSet, REQUEST_ACTION_ADD, "saveAddFor" + DataSetEntity.DATA_SET_TYPE_CsvValue);

		return buildFormView(dataSet.getDataSetType());
	}

	@RequestMapping(value = "/saveAddFor" + DataSetEntity.DATA_SET_TYPE_CsvValue, produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAddForCsvValue(HttpServletRequest request, HttpServletResponse response,
			@RequestBody CsvValueDataSetEntity dataSet)
	{
		User user = WebUtils.getUser();

		dataSet.setId(IDUtil.randomIdOnTime20());
		dataSet.setCreateUser(user.cloneNoPassword());
		dataSet.setCreateTime(new Date());

		ResponseEntity<OperationMessage> responseEntity = checkSaveCsvValueDataSetEntity(request, dataSet);

		if (responseEntity != null)
			return responseEntity;

		trimAnalysisProjectAwareEntityForSave(dataSet);

		this.dataSetEntityService.add(user, dataSet);

		return operationSuccessResponseEntity(request, dataSet);
	}

	@RequestMapping("/addFor" + DataSetEntity.DATA_SET_TYPE_CsvFile)
	public String addForCsvFile(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model)
	{
		CsvFileDataSetEntity dataSet = new CsvFileDataSetEntity();
		dataSet.setFileSourceType(DirectoryFileDataSetEntity.FILE_SOURCE_TYPE_UPLOAD);
		dataSet.setNameRow(1);
		setCookieAnalysisProject(request, response, dataSet);

		addAttributeForWriteJson(model, "availableCharsetNames", getAvailableCharsetNames());
		setFormModel(model, dataSet, REQUEST_ACTION_ADD, "saveAddFor" + DataSetEntity.DATA_SET_TYPE_CsvFile);

		return buildFormView(dataSet.getDataSetType());
	}

	@RequestMapping(value = "/saveAddFor" + DataSetEntity.DATA_SET_TYPE_CsvFile, produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAddForCsvFile(HttpServletRequest request, HttpServletResponse response,
			@RequestBody CsvFileDataSetEntity dataSet) throws Throwable
	{
		User user = WebUtils.getUser();

		dataSet.setId(IDUtil.randomIdOnTime20());
		dataSet.setCreateUser(user.cloneNoPassword());
		dataSet.setCreateTime(new Date());

		ResponseEntity<OperationMessage> responseEntity = checkSaveCsvFileDataSetEntity(request, dataSet);

		if (responseEntity != null)
			return responseEntity;

		trimAnalysisProjectAwareEntityForSave(dataSet);
		trimDirectoryFileDataSetEntityForSave(dataSet);

		this.dataSetEntityService.add(user, dataSet);
		copyToDirectoryFileDataSetEntityDirectoryIf(dataSet, "");
		
		return operationSuccessResponseEntity(request, dataSet);
	}

	@RequestMapping("/addFor" + DataSetEntity.DATA_SET_TYPE_Http)
	public String addForHttp(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model)
	{
		HttpDataSetEntity dataSet = new HttpDataSetEntity();
		setCookieAnalysisProject(request, response, dataSet);

		addAttributeForWriteJson(model, "availableCharsetNames", getAvailableCharsetNames());	
		setFormModel(model, dataSet, REQUEST_ACTION_ADD, "saveAddFor" + DataSetEntity.DATA_SET_TYPE_Http);

		return buildFormView(dataSet.getDataSetType());
	}

	@RequestMapping(value = "/saveAddFor" + DataSetEntity.DATA_SET_TYPE_Http, produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAddForHttp(HttpServletRequest request, HttpServletResponse response,
			@RequestBody HttpDataSetEntity dataSet)
	{
		User user = WebUtils.getUser();

		dataSet.setId(IDUtil.randomIdOnTime20());
		dataSet.setCreateUser(user.cloneNoPassword());
		dataSet.setCreateTime(new Date());

		ResponseEntity<OperationMessage> responseEntity = checkSaveHttpDataSetEntity(request, dataSet);

		if (responseEntity != null)
			return responseEntity;

		trimAnalysisProjectAwareEntityForSave(dataSet);

		this.dataSetEntityService.add(user, dataSet);
		
		return operationSuccessResponseEntity(request, dataSet);
	}

	@RequestMapping("/copy")
	public String copy(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id) throws Throwable
	{
		User user = WebUtils.getUser();

		DataSetEntity dataSet = getByIdForView(this.dataSetEntityService, user, id);

		if (dataSet instanceof SqlDataSetEntity)
		{
			SqlDataSetEntity dataSetEntity = (SqlDataSetEntity) dataSet;
			SchemaConnectionFactory connectionFactory = dataSetEntity.getConnectionFactory();
			Schema schema = (connectionFactory == null ? null : connectionFactory.getSchema());
			int permission = (schema != null ? getSchemaService().getPermission(user, schema.getId())
					: Authorization.PERMISSION_NONE_START);

			// 没有读权限，应置为null
			if (!Authorization.canRead(permission))
				dataSetEntity.setConnectionFactory(null);
		}
		else if (dataSet instanceof DirectoryFileDataSetEntity)
		{
			DirectoryFileDataSetEntity dataSetEntity = (DirectoryFileDataSetEntity) dataSet;
			File dataSetDirectory = dataSetEntity.getDirectory();
			String dataSetFileName = dataSetEntity.getFileName();
			DataSetResDirectory dataSetResDirectory = dataSetEntity.getDataSetResDirectory();
			int permission = (dataSetResDirectory != null
					? getDataSetResDirectoryService().getPermission(user, dataSetResDirectory.getId())
					: Authorization.PERMISSION_NONE_START);

			// 没有读权限，应置为null
			if (!Authorization.canRead(permission))
			{
				dataSetEntity.setDataSetResDirectory(null);
				dataSetEntity.setDataSetResFileName(null);
			}

			// 将上传文件拷贝至临时目录，用于执行添加操作
			dataSetEntity.setDirectory(null);
			dataSetEntity.setFileName(null);
			File sourceFile = (dataSetDirectory != null && !StringUtil.isEmpty(dataSetFileName)
					? FileUtil.getFile(dataSetDirectory, dataSetFileName)
					: null);
			if (sourceFile != null && sourceFile.exists())
			{
				File tmpFile = FileUtil.generateUniqueFile(getTempDataSetDirectory(),
						FileUtil.getExtension(dataSetFileName));
				String fileName = tmpFile.getName();
				IOUtil.copy(sourceFile, tmpFile, false);
				dataSetEntity.setFileName(fileName);
			}
		}

		dataSet.setId(null);
		convertForFormModel(dataSet);
		setNullAnalysisProjectIfNoPermission(user, dataSet, getAnalysisProjectService());

		setFormModel(model, dataSet, REQUEST_ACTION_ADD,
						"saveAddFor" + dataSet.getDataSetType());

		if (DataSetEntity.DATA_SET_TYPE_JsonFile.equals(dataSet.getDataSetType())
				|| DataSetEntity.DATA_SET_TYPE_CsvFile.equals(dataSet.getDataSetType())
				|| DataSetEntity.DATA_SET_TYPE_Http.equals(dataSet.getDataSetType()))
		{
			addAttributeForWriteJson(model, "availableCharsetNames", getAvailableCharsetNames());
		}

		return buildFormView(dataSet.getDataSetType());
	}

	@RequestMapping("/edit")
	public String edit(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id)
	{
		User user = WebUtils.getUser();

		DataSetEntity dataSet = getByIdForEdit(this.dataSetEntityService, user, id);
		convertForFormModel(dataSet);
		
		setFormModel(model, dataSet, REQUEST_ACTION_EDIT,
						"saveEditFor" + dataSet.getDataSetType());

		if (DataSetEntity.DATA_SET_TYPE_JsonFile.equals(dataSet.getDataSetType())
				|| DataSetEntity.DATA_SET_TYPE_CsvFile.equals(dataSet.getDataSetType())
				|| DataSetEntity.DATA_SET_TYPE_Http.equals(dataSet.getDataSetType()))
		{
			addAttributeForWriteJson(model, "availableCharsetNames", getAvailableCharsetNames());
		}

		return buildFormView(dataSet.getDataSetType());
	}

	@RequestMapping(value = "/saveEditFor" + DataSetEntity.DATA_SET_TYPE_SQL, produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveEditForSql(HttpServletRequest request, HttpServletResponse response,
			@RequestBody SqlDataSetEntity dataSet)
	{
		User user = WebUtils.getUser();

		ResponseEntity<OperationMessage> responseEntity = checkSaveSqlDataSetEntity(request, dataSet);

		if (responseEntity != null)
			return responseEntity;

		trimAnalysisProjectAwareEntityForSave(dataSet);

		this.dataSetEntityService.update(user, dataSet);
		
		return operationSuccessResponseEntity(request, dataSet);
	}

	@RequestMapping(value = "/saveEditFor" + DataSetEntity.DATA_SET_TYPE_JsonValue, produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveEditForJsonValue(HttpServletRequest request,
			HttpServletResponse response, @RequestBody JsonValueDataSetEntity dataSet)
	{
		User user = WebUtils.getUser();

		ResponseEntity<OperationMessage> responseEntity = checkSaveJsonValueDataSetEntity(request, dataSet);

		if (responseEntity != null)
			return responseEntity;

		trimAnalysisProjectAwareEntityForSave(dataSet);

		this.dataSetEntityService.update(user, dataSet);
		
		return operationSuccessResponseEntity(request, dataSet);
	}

	@RequestMapping(value = "/saveEditFor" + DataSetEntity.DATA_SET_TYPE_JsonFile, produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveEditForJsonFile(HttpServletRequest request,
			HttpServletResponse response, @RequestBody JsonFileDataSetEntity dataSet,
			@RequestParam("originalFileName") String originalFileName) throws Throwable
	{
		User user = WebUtils.getUser();

		ResponseEntity<OperationMessage> responseEntity = checkSaveJsonFileDataSetEntity(request, dataSet);

		if (responseEntity != null)
			return responseEntity;

		trimAnalysisProjectAwareEntityForSave(dataSet);
		trimDirectoryFileDataSetEntityForSave(dataSet);

		this.dataSetEntityService.update(user, dataSet);
		copyToDirectoryFileDataSetEntityDirectoryIf(dataSet, originalFileName);
		
		return operationSuccessResponseEntity(request, dataSet);
	}

	@RequestMapping(value = "/saveEditFor" + DataSetEntity.DATA_SET_TYPE_Excel, produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveEditForExcel(HttpServletRequest request, HttpServletResponse response,
			@RequestBody ExcelDataSetEntity dataSet, @RequestParam("originalFileName") String originalFileName)
			throws Throwable
	{
		User user = WebUtils.getUser();

		ResponseEntity<OperationMessage> responseEntity = checkSaveExcelDataSetEntity(request, dataSet);

		if (responseEntity != null)
			return responseEntity;

		trimAnalysisProjectAwareEntityForSave(dataSet);
		trimDirectoryFileDataSetEntityForSave(dataSet);

		this.dataSetEntityService.update(user, dataSet);
		copyToDirectoryFileDataSetEntityDirectoryIf(dataSet, originalFileName);
		
		return operationSuccessResponseEntity(request, dataSet);
	}

	@RequestMapping(value = "/saveEditFor" + DataSetEntity.DATA_SET_TYPE_CsvValue, produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveEditForCsvValue(HttpServletRequest request,
			HttpServletResponse response, @RequestBody CsvValueDataSetEntity dataSet)
	{
		User user = WebUtils.getUser();

		ResponseEntity<OperationMessage> responseEntity = checkSaveCsvValueDataSetEntity(request, dataSet);

		if (responseEntity != null)
			return responseEntity;

		trimAnalysisProjectAwareEntityForSave(dataSet);

		this.dataSetEntityService.update(user, dataSet);
		
		return operationSuccessResponseEntity(request, dataSet);
	}

	@RequestMapping(value = "/saveEditFor" + DataSetEntity.DATA_SET_TYPE_CsvFile, produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveEditForCsvFile(HttpServletRequest request, HttpServletResponse response,
			@RequestBody CsvFileDataSetEntity dataSet, @RequestParam("originalFileName") String originalFileName)
			throws Throwable
	{
		User user = WebUtils.getUser();

		ResponseEntity<OperationMessage> responseEntity = checkSaveCsvFileDataSetEntity(request, dataSet);

		if (responseEntity != null)
			return responseEntity;

		trimAnalysisProjectAwareEntityForSave(dataSet);
		trimDirectoryFileDataSetEntityForSave(dataSet);

		this.dataSetEntityService.update(user, dataSet);
		copyToDirectoryFileDataSetEntityDirectoryIf(dataSet, originalFileName);
		
		return operationSuccessResponseEntity(request, dataSet);
	}

	@RequestMapping(value = "/saveEditFor" + DataSetEntity.DATA_SET_TYPE_Http, produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveEditForHttp(HttpServletRequest request, HttpServletResponse response,
			@RequestBody HttpDataSetEntity dataSet)
	{
		User user = WebUtils.getUser();

		ResponseEntity<OperationMessage> responseEntity = checkSaveHttpDataSetEntity(request, dataSet);

		if (responseEntity != null)
			return responseEntity;

		trimAnalysisProjectAwareEntityForSave(dataSet);

		this.dataSetEntityService.update(user, dataSet);
		
		return operationSuccessResponseEntity(request, dataSet);
	}

	@RequestMapping(value = "/uploadFile", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public Map<String, Object> uploadFile(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("file") MultipartFile multipartFile) throws Exception
	{
		File directory = getTempDataSetDirectory();
		String displayName = multipartFile.getOriginalFilename();
		File tmpFile = FileUtil.generateUniqueFile(directory, FileUtil.getExtension(displayName));
		String fileName = tmpFile.getName();

		InputStream in = null;
		try
		{
			in = multipartFile.getInputStream();
			IOUtil.write(in, tmpFile);
		}
		finally
		{
			IOUtil.close(in);
		}

		Map<String, Object> results = new HashMap<>();
		results.put("fileName", fileName);
		results.put("displayName", displayName);

		return results;
	}

	@RequestMapping(value = "/downloadFile")
	public void downloadFile(HttpServletRequest request, HttpServletResponse response, @RequestParam("id") String id)
			throws Exception
	{
		User user = WebUtils.getUser();

		DataSetEntity dataSet = this.dataSetEntityService.getById(user, id);

		if (dataSet == null)
			throw new RecordNotFoundException();

		if (!(dataSet instanceof DirectoryFileDataSetEntity))
			throw new IllegalInputException();

		DirectoryFileDataSetEntity dataSetEntity = (DirectoryFileDataSetEntity) dataSet;

		// 服务端文件允许参数化文件名因而无法再这里下载文件，即便如此，也可能保存着用户之前编辑的上传文件而允许下载，所以不应启用下面的逻辑
		// if
		// (!DirectoryFileDataSetEntity.FILE_SOURCE_TYPE_UPLOAD.equals(dataSetEntity.getFileSourceType()))
		// throw new IllegalInputException();

		if (isEmpty(dataSetEntity.getFileName()))
			throw new IllegalInputException();

		File dataSetDirectory = getDataSetEntityService().getDataSetDirectory(dataSetEntity.getId());
		File entityFile = FileUtil.getFile(dataSetDirectory, dataSetEntity.getFileName());

		String displayName = dataSetEntity.getDisplayName();

		response.setCharacterEncoding(IOUtil.CHARSET_UTF_8);
		response.setHeader("Content-Disposition",
				"attachment; filename=" + toResponseAttachmentFileName(request, response, displayName));
		OutputStream out = response.getOutputStream();

		IOUtil.write(entityFile, out);
	}

	@RequestMapping("/view")
	public String view(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id)
	{
		User user = WebUtils.getUser();

		DataSetEntity dataSet = getByIdForView(this.dataSetEntityService, user, id);
		convertForFormModel(dataSet);

		setFormModel(model, dataSet, REQUEST_ACTION_VIEW, SUBMIT_ACTION_NONE);

		if (DataSetEntity.DATA_SET_TYPE_JsonFile.equals(dataSet.getDataSetType())
				|| DataSetEntity.DATA_SET_TYPE_CsvFile.equals(dataSet.getDataSetType())
				|| DataSetEntity.DATA_SET_TYPE_Http.equals(dataSet.getDataSetType()))
		{
			addAttributeForWriteJson(model, "availableCharsetNames", getAvailableCharsetNames());
		}

		return buildFormView(dataSet.getDataSetType());
	}

	@RequestMapping(value = "/getProfileDataSetByIds", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public List<ProfileDataSet> getProfileDataSetByIds(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model, @RequestParam("id") String[] ids)
	{
		List<ProfileDataSet> dataSets = new ArrayList<>();

		if (!isEmpty(ids))
		{
			User user = WebUtils.getUser();

			for (String id : ids)
			{
				ProfileDataSet dataSet = this.dataSetEntityService.getProfileDataSet(user, id);

				if (dataSet == null)
					throw new RecordNotFoundException();

				dataSets.add(dataSet);
			}
		}

		return dataSets;
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
			this.dataSetEntityService.deleteById(user, id);

			File dataSetDirectory = getDataSetEntityService().getDataSetDirectory(id);
			FileUtil.deleteFile(dataSetDirectory);
		}

		return operationSuccessResponseEntity(request);
	}

	@RequestMapping("/pagingQuery")
	public String pagingQuery(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model)
	{
		model.addAttribute(KEY_REQUEST_ACTION, REQUEST_ACTION_QUERY);
		return "/dataSet/dataSet_table";
	}

	@RequestMapping(value = "/select")
	public String select(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model)
	{
		setSelectAction(request, model);
		return "/dataSet/dataSet_table";
	}

	@RequestMapping(value = "/pagingQueryData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public PagingData<DataSetEntity> pagingQueryData(HttpServletRequest request, HttpServletResponse response,
			final org.springframework.ui.Model springModel,
			@RequestBody(required = false) APIDDataFilterPagingQuery pagingQueryParam) throws Exception
	{
		User user = WebUtils.getUser();
		final APIDDataFilterPagingQuery pagingQuery = inflateAPIDDataFilterPagingQuery(request, pagingQueryParam);

		PagingData<DataSetEntity> pagingData = this.dataSetEntityService.pagingQuery(user, pagingQuery,
				pagingQuery.getDataFilter(), pagingQuery.getAnalysisProjectId());

		return pagingData;
	}

	@RequestMapping(value = "/previewSql", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public TemplateResolvedDataSetResult previewSql(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @RequestBody SqlDataSetPreview preview) throws Throwable
	{
		User user = WebUtils.getUser();

		SqlDataSetEntity dataSet = preview.getDataSet();
		SchemaConnectionFactory connFactory = dataSet.getShmConFactory();
		Schema schema = (connFactory == null ? null : connFactory.getSchema());
		String schemaId = (schema == null ? null : schema.getId());
		
		if(StringUtil.isEmpty(schemaId))
			throw new IllegalInputException();

		// 新建时操作时未创建数据集
		boolean notFound = checkDataSetEntityIdReadPermission(user, dataSet.getId());
		// 如果数据集已创建，则使用数据集权限；如果数据集未创建，则需使用数据源权限
		schema = (notFound ? getSchemaForUserNotNull(user, schemaId) : getSchemaNotNull(schemaId));

		SchemaConnectionFactory connectionFactory = new SchemaConnectionFactory(getConnectionSource(), schema);
		dataSet.setConnectionFactory(connectionFactory);
		dataSet.setSqlValidator(this.dataSetEntityService.getSqlDataSetSqlValidator());

		DataSetQuery query = convertDataSetQuery(request, response, preview.getQuery(), dataSet);

		return dataSet.resolve(query);
	}

	@RequestMapping(value = "/resolveSql", produces = CONTENT_TYPE_HTML)
	@ResponseBody
	public String resolveSql(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @RequestBody ResolveSqlParam resolveSqlParam) throws Throwable
	{
		return resolveSqlTemplate(request, response, resolveSqlParam.getSql(), resolveSqlParam.getParamValues(),
				resolveSqlParam.getDataSetParams());
	}

	@RequestMapping(value = "/previewJsonValue", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public TemplateResolvedDataSetResult previewJsonValue(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @RequestBody JsonValueDataSetPreview preview) throws Throwable
	{
		final User user = WebUtils.getUser();

		JsonValueDataSetEntity dataSet = preview.getDataSet();
		checkDataSetEntityIdReadPermission(user, dataSet.getId());

		DataSetQuery query = convertDataSetQuery(request, response, preview.getQuery(), dataSet);

		return dataSet.resolve(query);
	}

	@RequestMapping(value = "/previewJsonFile", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResolvedDataSetResult previewJsonFile(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @RequestBody JsonFileDataSetEntityPreview preview,
			@RequestParam("originalFileName") String originalFileName)
			throws Throwable
	{
		final User user = WebUtils.getUser();

		JsonFileDataSetEntity dataSet = preview.getDataSet();
		checkDataSetEntityIdReadPermission(user, dataSet.getId());
		setDirectoryFileDataSetDirectory(dataSet, originalFileName);

		DataSetQuery query = convertDataSetQuery(request, response, preview.getQuery(), dataSet);

		return dataSet.resolve(query);
	}

	@RequestMapping(value = "/previewExcel", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResolvedDataSetResult previewExcel(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @RequestBody ExcelDataSetEntityPreview preview,
			@RequestParam("originalFileName") String originalFileName) throws Throwable
	{
		final User user = WebUtils.getUser();

		ExcelDataSetEntity dataSet = preview.getDataSet();
		checkDataSetEntityIdReadPermission(user, dataSet.getId());
		setDirectoryFileDataSetDirectory(dataSet, originalFileName);

		DataSetQuery query = convertDataSetQuery(request, response, preview.getQuery(), dataSet);

		return dataSet.resolve(query);
	}

	@RequestMapping(value = "/previewCsvValue", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public TemplateResolvedDataSetResult previewCsvValue(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @RequestBody CsvValueDataSetPreview preview) throws Throwable
	{
		final User user = WebUtils.getUser();

		CsvValueDataSetEntity dataSet = preview.getDataSet();
		checkDataSetEntityIdReadPermission(user, dataSet.getId());

		DataSetQuery query = convertDataSetQuery(request, response, preview.getQuery(), dataSet);

		return dataSet.resolve(query);
	}

	@RequestMapping(value = "/previewCsvFile", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResolvedDataSetResult previewCsvFile(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @RequestBody CsvFileDataSetEntityPreview preview,
			@RequestParam("originalFileName") String originalFileName) throws Throwable
	{
		final User user = WebUtils.getUser();

		CsvFileDataSetEntity dataSet = preview.getDataSet();
		checkDataSetEntityIdReadPermission(user, dataSet.getId());
		setDirectoryFileDataSetDirectory(dataSet, originalFileName);

		DataSetQuery query = convertDataSetQuery(request, response, preview.getQuery(), dataSet);

		return dataSet.resolve(query);
	}

	@RequestMapping(value = "/previewHttp", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public TemplateResolvedDataSetResult previewHttp(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @RequestBody HttpDataSetEntityPreview preview) throws Throwable
	{
		final User user = WebUtils.getUser();

		HttpDataSetEntity dataSet = preview.getDataSet();
		checkDataSetEntityIdReadPermission(user, dataSet.getId());
		dataSet.setHttpClient(getDataSetEntityService().getHttpClient());

		DataSetQuery query = convertDataSetQuery(request, response, preview.getQuery(), dataSet);

		return dataSet.resolve(query);
	}
	
	protected void convertForFormModel(DataSetEntity entity)
	{
		if(entity instanceof SqlDataSetEntity)
			((SqlDataSetEntity) entity).clearSchemaPassword();
		
		if(entity instanceof DirectoryFileDataSetEntity)
			((DirectoryFileDataSetEntity) entity).setDirectory(null);
		
		if(entity instanceof HttpDataSetEntity)
			((HttpDataSetEntity) entity).setHttpClient(null);
	}

	/**
	 * 校验指定ID的读权限，
	 * 
	 * @param user
	 * @param id
	 *            允许为{@code null}
	 * @return 返回{@code true}表明记录未找到
	 */
	protected boolean checkDataSetEntityIdReadPermission(User user, String id) throws PermissionDeniedException
	{
		boolean notFound = true;

		if (!isEmpty(id))
		{
			int permission = this.dataSetEntityService.getPermission(user, id);

			notFound = (DataPermissionEntityService.PERMISSION_NOT_FOUND == permission);

			if (!notFound && !Authorization.canRead(permission))
				throw new PermissionDeniedException();
		}

		return notFound;
	}

	protected void setCookieAnalysisProject(HttpServletRequest request, HttpServletResponse response,
			DataSetEntity entity)
	{
		setCookieAnalysisProjectIfValid(request, response, this.analysisProjectService, entity);
	}

	protected boolean copyToDirectoryFileDataSetEntityDirectoryIf(DirectoryFileDataSetEntity entity,
			String originalFileName) throws IOException
	{
		String fileName = entity.getFileName();

		if (isEmpty(entity.getId()))
			return false;

		if (StringUtil.isEquals(originalFileName, fileName))
			return false;

		File dataSetDirectory = getDataSetEntityService().getDataSetDirectory(entity.getId());
		FileUtil.clearDirectory(dataSetDirectory);

		File tmpFile = FileUtil.getFile(getTempDataSetDirectory(), fileName);
		File entityFile = FileUtil.getFile(dataSetDirectory, fileName);

		IOUtil.copy(tmpFile, entityFile, false);

		return true;
	}

	protected void setDirectoryFileDataSetDirectory(DirectoryFileDataSetEntity dataSet, String originalFileName)
	{
		String fileName = dataSet.getFileName();

		if (!isEmpty(dataSet.getId()) && !isEmpty(originalFileName) && originalFileName.equals(fileName))
			dataSet.setDirectory(getDataSetEntityService().getDataSetDirectory(dataSet.getId()));
		else
			dataSet.setDirectory(getTempDataSetDirectory());
	}

	protected String buildFormView(String dataSetType)
	{
		return "/dataSet/dataSet_form_" + dataSetType;
	}

	protected File getTempDataSetDirectory()
	{
		return FileUtil.getDirectory(this.tempDirectory, "dataSet", true);
	}

	protected String resolveSqlTemplate(HttpServletRequest request, HttpServletResponse response, String source,
			Map<String, ?> paramValues, Collection<DataSetParam> dataSetParams)
	{
		Map<String, ?> converted = getDataSetParamValueConverter().convert(paramValues, dataSetParams);

		DataSetQuery dataSetQuery = DataSetQuery.valueOf(converted);
		setAnalysisUserParamValue(request, response, dataSetQuery);

		return SqlDataSet.SQL_TEMPLATE_RESOLVER.resolve(source, new TemplateContext(dataSetQuery.getParamValues()));
	}

	protected DataSetQuery convertDataSetQuery(HttpServletRequest request, HttpServletResponse response,
			DataSetQuery dataSetQuery, DataSet dataSet)
	{
		DataSetQuery re = getDataSetParamValueConverter().convert(dataSetQuery, dataSet);
		setAnalysisUserParamValue(request, response, re);

		return re;
	}

	protected void setAnalysisUserParamValue(HttpServletRequest request, HttpServletResponse response,
			DataSetQuery dataSetQuery)
	{
		AnalysisUser analysisUser = AnalysisUser.valueOf(WebUtils.getUser());
		analysisUser.setParamValue(dataSetQuery);
	}

	protected ResponseEntity<OperationMessage> checkSaveSqlDataSetEntity(HttpServletRequest request,
			SqlDataSetEntity dataSet)
	{
		ResponseEntity<OperationMessage> responseEntity = checkSaveEntity(request, dataSet);

		if (responseEntity != null)
			return responseEntity;

		if (isEmpty(dataSet.getConnectionFactory()))
			throw new IllegalInputException();

		if (isEmpty(dataSet.getConnectionFactory().getSchema()))
			throw new IllegalInputException();

		if (isEmpty(dataSet.getConnectionFactory().getSchema().getId()))
			throw new IllegalInputException();

		if (isBlank(dataSet.getSql()))
			throw new IllegalInputException();

		return null;
	}

	protected ResponseEntity<OperationMessage> checkSaveJsonValueDataSetEntity(HttpServletRequest request,
			JsonValueDataSetEntity dataSet)
	{
		ResponseEntity<OperationMessage> responseEntity = checkSaveEntity(request, dataSet);

		if (responseEntity != null)
			return responseEntity;

		if (isEmpty(dataSet.getValue()))
			throw new IllegalInputException();

		return null;
	}

	protected ResponseEntity<OperationMessage> checkSaveJsonFileDataSetEntity(HttpServletRequest request,
			JsonFileDataSetEntity dataSet)
	{
		ResponseEntity<OperationMessage> responseEntity = checkSaveEntity(request, dataSet);

		if (responseEntity != null)
			return responseEntity;

		if (isEmpty(dataSet.getFileName()) && isEmpty(dataSet.getDataSetResFileName()))
			throw new IllegalInputException();

		return null;
	}

	protected ResponseEntity<OperationMessage> checkSaveExcelDataSetEntity(HttpServletRequest request,
			ExcelDataSetEntity dataSet)
	{
		ResponseEntity<OperationMessage> responseEntity = checkSaveEntity(request, dataSet);

		if (responseEntity != null)
			return responseEntity;

		if (isEmpty(dataSet.getFileName()) && isEmpty(dataSet.getDataSetResFileName()))
			throw new IllegalInputException();

		return null;
	}

	protected ResponseEntity<OperationMessage> checkSaveCsvValueDataSetEntity(HttpServletRequest request,
			CsvValueDataSetEntity dataSet)
	{
		ResponseEntity<OperationMessage> responseEntity = checkSaveEntity(request, dataSet);

		if (responseEntity != null)
			return responseEntity;

		if (isEmpty(dataSet.getValue()))
			throw new IllegalInputException();

		return null;
	}

	protected ResponseEntity<OperationMessage> checkSaveCsvFileDataSetEntity(HttpServletRequest request,
			CsvFileDataSetEntity dataSet)
	{
		ResponseEntity<OperationMessage> responseEntity = checkSaveEntity(request, dataSet);

		if (responseEntity != null)
			return responseEntity;

		if (isEmpty(dataSet.getFileName()) && isEmpty(dataSet.getDataSetResFileName()))
			throw new IllegalInputException();

		return null;
	}

	protected ResponseEntity<OperationMessage> checkSaveHttpDataSetEntity(HttpServletRequest request,
			HttpDataSetEntity dataSet)
	{
		ResponseEntity<OperationMessage> responseEntity = checkSaveEntity(request, dataSet);

		if (responseEntity != null)
			return responseEntity;

		if (isEmpty(dataSet.getUri()))
			throw new IllegalInputException();

		return null;
	}

	protected ResponseEntity<OperationMessage> checkSaveEntity(HttpServletRequest request, DataSetEntity dataSet)
	{
		if (isBlank(dataSet.getName()))
			throw new IllegalInputException();

		List<DataSetParam> params = dataSet.getParams();
		if (params != null)
		{
			Set<String> names = new HashSet<>();
			for (DataSetParam param : params)
			{
				String name = param.getName();

				if(isEmpty(name))
				{
					return optFailResponseEntity(request, HttpStatus.BAD_REQUEST, "paramNameRequired");
				}
				else if (names.contains(name))
				{
					return optFailResponseEntity(request, HttpStatus.BAD_REQUEST, "paramNameMustBeUnique");
				}
				else
					names.add(name);
			}
		}

		List<DataSetProperty> properties = dataSet.getProperties();
		if (properties != null)
		{
			Set<String> names = new HashSet<>();
			for (DataSetProperty property : properties)
			{
				String name = property.getName();

				if(isEmpty(name))
				{
					return optFailResponseEntity(request, HttpStatus.BAD_REQUEST, "propertyNameRequired");
				}
				else if (names.contains(name))
				{
					return optFailResponseEntity(request, HttpStatus.BAD_REQUEST, "propertyNameMustBeUnique");
				}
				else
					names.add(name);
			}
		}

		return null;
	}

	public static class AbstractDataSetPreview<T extends DataSet> implements ControllerForm
	{
		private static final long serialVersionUID = 1L;

		private T dataSet;

		private DataSetQuery query;

		public AbstractDataSetPreview()
		{
			super();
		}

		public T getDataSet()
		{
			return dataSet;
		}

		public void setDataSet(T dataSet)
		{
			this.dataSet = dataSet;
		}

		public DataSetQuery getQuery()
		{
			return query;
		}

		public void setQuery(DataSetQuery query)
		{
			this.query = query;
		}
	}

	public static class SqlDataSetPreview extends AbstractDataSetPreview<SqlDataSetEntity>
	{
		private static final long serialVersionUID = 1L;

		public SqlDataSetPreview()
		{
			super();
		}
	}

	public static class JsonValueDataSetPreview extends AbstractDataSetPreview<JsonValueDataSetEntity>
	{
		private static final long serialVersionUID = 1L;

		public JsonValueDataSetPreview()
		{
			super();
		}
	}

	public static class JsonFileDataSetEntityPreview extends AbstractDataSetPreview<JsonFileDataSetEntity>
	{
		private static final long serialVersionUID = 1L;

		public JsonFileDataSetEntityPreview()
		{
			super();
		}
	}

	public static class ExcelDataSetEntityPreview extends AbstractDataSetPreview<ExcelDataSetEntity>
	{
		private static final long serialVersionUID = 1L;

		public ExcelDataSetEntityPreview()
		{
			super();
		}
	}

	public static class CsvValueDataSetPreview extends AbstractDataSetPreview<CsvValueDataSetEntity>
	{
		private static final long serialVersionUID = 1L;

		public CsvValueDataSetPreview()
		{
			super();
		}
	}

	public static class CsvFileDataSetEntityPreview extends AbstractDataSetPreview<CsvFileDataSetEntity>
	{
		private static final long serialVersionUID = 1L;
	}

	public static class HttpDataSetEntityPreview extends AbstractDataSetPreview<HttpDataSetEntity>
	{
		private static final long serialVersionUID = 1L;

		public HttpDataSetEntityPreview()
		{
			super();
		}
	}

	public static class ResolveSqlParam implements ControllerForm
	{
		private static final long serialVersionUID = 1L;

		private String sql;

		@SuppressWarnings("unchecked")
		private List<DataSetParam> dataSetParams = Collections.EMPTY_LIST;

		@SuppressWarnings("unchecked")
		private Map<String, Object> paramValues = Collections.EMPTY_MAP;

		public ResolveSqlParam()
		{
			super();
		}

		public ResolveSqlParam(String sql)
		{
			super();
			this.sql = sql;
		}

		public String getSql()
		{
			return sql;
		}

		public void setSql(String sql)
		{
			this.sql = sql;
		}

		public List<DataSetParam> getDataSetParams()
		{
			return dataSetParams;
		}

		public void setDataSetParams(List<DataSetParam> dataSetParams)
		{
			this.dataSetParams = dataSetParams;
		}

		public Map<String, Object> getParamValues()
		{
			return paramValues;
		}

		public void setParamValues(Map<String, Object> paramValues)
		{
			this.paramValues = paramValues;
		}
	}
}
