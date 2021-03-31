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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.analysis.DataSet;
import org.datagear.analysis.DataSetOption;
import org.datagear.analysis.DataSetParam;
import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.ResolvedDataSetResult;
import org.datagear.analysis.support.AbstractDataSet;
import org.datagear.analysis.support.CsvValueDataSet;
import org.datagear.analysis.support.DataSetFmkTemplateResolver;
import org.datagear.analysis.support.DataSetParamValueConverter;
import org.datagear.analysis.support.JsonValueDataSet;
import org.datagear.analysis.support.ProfileDataSet;
import org.datagear.analysis.support.SqlDataSet;
import org.datagear.analysis.support.TemplateContext;
import org.datagear.analysis.support.TemplateResolvedDataSetResult;
import org.datagear.management.domain.Authorization;
import org.datagear.management.domain.CsvFileDataSetEntity;
import org.datagear.management.domain.CsvValueDataSetEntity;
import org.datagear.management.domain.DataSetEntity;
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
import org.datagear.management.service.PermissionDeniedException;
import org.datagear.persistence.PagingData;
import org.datagear.util.FileUtil;
import org.datagear.util.IDUtil;
import org.datagear.util.IOUtil;
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
@RequestMapping("/analysis/dataSet")
public class DataSetController extends AbstractSchemaConnController
{
	static
	{
		AuthorizationResourceMetas.registerForShare(SqlDataSetEntity.AUTHORIZATION_RESOURCE_TYPE, "dataSet");
	}

	@Autowired
	private DataSetEntityService dataSetEntityService;

	@Autowired
	private AnalysisProjectService analysisProjectService;

	@Autowired
	private File tempDirectory;

	private DataSetParamValueConverter dataSetParamValueConverter = new DataSetParamValueConverter();

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

	@RequestMapping("/addForSql")
	public String addForSql(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model)
	{
		SqlDataSetEntity dataSet = new SqlDataSetEntity();
		setCookieAnalysisProject(request, response, dataSet);

		model.addAttribute("dataSet", dataSet);
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "dataSet.addDataSet");
		model.addAttribute(KEY_FORM_ACTION, "saveAddForSql");

		return buildFormView(dataSet.getDataSetType());
	}

	@RequestMapping(value = "/saveAddForSql", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAddForSql(HttpServletRequest request, HttpServletResponse response,
			@RequestBody SqlDataSetEntity dataSet)
	{
		User user = WebUtils.getUser(request, response);

		dataSet.setId(IDUtil.randomIdOnTime20());
		dataSet.setCreateUser(User.copyWithoutPassword(user));

		ResponseEntity<OperationMessage> responseEntity = checkSaveSqlDataSetEntity(request, dataSet);

		if (responseEntity != null)
			return responseEntity;

		trimAnalysisProjectAwareEntityForSave(dataSet);

		this.dataSetEntityService.add(user, dataSet);

		return buildOperationMessageSaveSuccessResponseEntity(request, dataSet);
	}

	@RequestMapping("/addForJsonValue")
	public String addForJsonValue(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model)
	{
		JsonValueDataSetEntity dataSet = new JsonValueDataSetEntity();
		setCookieAnalysisProject(request, response, dataSet);

		model.addAttribute("dataSet", dataSet);
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "dataSet.addDataSet");
		model.addAttribute(KEY_FORM_ACTION, "saveAddForJsonValue");

		return buildFormView(dataSet.getDataSetType());
	}

	@RequestMapping(value = "/saveAddForJsonValue", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAddForJsonValue(HttpServletRequest request,
			HttpServletResponse response, @RequestBody JsonValueDataSetEntity dataSet)
	{
		User user = WebUtils.getUser(request, response);

		dataSet.setId(IDUtil.randomIdOnTime20());
		dataSet.setCreateUser(User.copyWithoutPassword(user));

		ResponseEntity<OperationMessage> responseEntity = checkSaveJsonValueDataSetEntity(request, dataSet);

		if (responseEntity != null)
			return responseEntity;

		trimAnalysisProjectAwareEntityForSave(dataSet);

		this.dataSetEntityService.add(user, dataSet);

		return buildOperationMessageSaveSuccessResponseEntity(request, dataSet);
	}

	@RequestMapping("/addForJsonFile")
	public String addForJsonFile(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model)
	{
		JsonFileDataSetEntity dataSet = new JsonFileDataSetEntity();
		setCookieAnalysisProject(request, response, dataSet);

		model.addAttribute("dataSet", dataSet);
		model.addAttribute("availableCharsetNames", getAvailableCharsetNames());
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "dataSet.addDataSet");
		model.addAttribute(KEY_FORM_ACTION, "saveAddForJsonFile");

		return buildFormView(dataSet.getDataSetType());
	}

	@RequestMapping(value = "/saveAddForJsonFile", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAddForJsonFile(HttpServletRequest request, HttpServletResponse response,
			@RequestBody JsonFileDataSetEntity dataSet) throws Throwable
	{
		User user = WebUtils.getUser(request, response);

		dataSet.setId(IDUtil.randomIdOnTime20());
		dataSet.setCreateUser(User.copyWithoutPassword(user));

		ResponseEntity<OperationMessage> responseEntity = checkSaveJsonFileDataSetEntity(request, dataSet);

		if (responseEntity != null)
			return responseEntity;

		trimAnalysisProjectAwareEntityForSave(dataSet);
		trimDirectoryFileDataSetEntityForSave(dataSet);

		this.dataSetEntityService.add(user, dataSet);
		copyToDirectoryFileDataSetEntityDirectoryIf(dataSet, "");

		return buildOperationMessageSaveSuccessResponseEntity(request, dataSet);
	}

	@RequestMapping("/addForExcel")
	public String addForExcel(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model)
	{
		ExcelDataSetEntity dataSet = new ExcelDataSetEntity();
		dataSet.setNameRow(1);
		setCookieAnalysisProject(request, response, dataSet);

		model.addAttribute("dataSet", dataSet);
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "dataSet.addDataSet");
		model.addAttribute(KEY_FORM_ACTION, "saveAddForExcel");

		return buildFormView(dataSet.getDataSetType());
	}

	@RequestMapping(value = "/saveAddForExcel", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAddForExcel(HttpServletRequest request, HttpServletResponse response,
			@RequestBody ExcelDataSetEntity dataSet) throws Throwable
	{
		User user = WebUtils.getUser(request, response);

		dataSet.setId(IDUtil.randomIdOnTime20());
		dataSet.setCreateUser(User.copyWithoutPassword(user));

		ResponseEntity<OperationMessage> responseEntity = checkSaveExcelDataSetEntity(request, dataSet);

		if (responseEntity != null)
			return responseEntity;

		trimAnalysisProjectAwareEntityForSave(dataSet);
		trimDirectoryFileDataSetEntityForSave(dataSet);

		this.dataSetEntityService.add(user, dataSet);
		copyToDirectoryFileDataSetEntityDirectoryIf(dataSet, "");

		return buildOperationMessageSaveSuccessResponseEntity(request, dataSet);
	}

	@RequestMapping("/addForCsvValue")
	public String addForCsvValue(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model)
	{
		CsvValueDataSetEntity dataSet = new CsvValueDataSetEntity();
		dataSet.setNameRow(1);
		setCookieAnalysisProject(request, response, dataSet);

		model.addAttribute("dataSet", dataSet);
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "dataSet.addDataSet");
		model.addAttribute(KEY_FORM_ACTION, "saveAddForCsvValue");

		return buildFormView(dataSet.getDataSetType());
	}

	@RequestMapping(value = "/saveAddForCsvValue", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAddForCsvValue(HttpServletRequest request, HttpServletResponse response,
			@RequestBody CsvValueDataSetEntity dataSet)
	{
		User user = WebUtils.getUser(request, response);

		dataSet.setId(IDUtil.randomIdOnTime20());
		dataSet.setCreateUser(User.copyWithoutPassword(user));

		ResponseEntity<OperationMessage> responseEntity = checkSaveCsvValueDataSetEntity(request, dataSet);

		if (responseEntity != null)
			return responseEntity;

		trimAnalysisProjectAwareEntityForSave(dataSet);

		this.dataSetEntityService.add(user, dataSet);

		return buildOperationMessageSaveSuccessResponseEntity(request, dataSet);
	}

	@RequestMapping("/addForCsvFile")
	public String addForCsvFile(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model)
	{
		CsvFileDataSetEntity dataSet = new CsvFileDataSetEntity();
		dataSet.setNameRow(1);
		setCookieAnalysisProject(request, response, dataSet);

		model.addAttribute("dataSet", dataSet);
		model.addAttribute("availableCharsetNames", getAvailableCharsetNames());
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "dataSet.addDataSet");
		model.addAttribute(KEY_FORM_ACTION, "saveAddForCsvFile");

		return buildFormView(dataSet.getDataSetType());
	}

	@RequestMapping(value = "/saveAddForCsvFile", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAddForCsvFile(HttpServletRequest request, HttpServletResponse response,
			@RequestBody CsvFileDataSetEntity dataSet) throws Throwable
	{
		User user = WebUtils.getUser(request, response);

		dataSet.setId(IDUtil.randomIdOnTime20());
		dataSet.setCreateUser(User.copyWithoutPassword(user));

		ResponseEntity<OperationMessage> responseEntity = checkSaveCsvFileDataSetEntity(request, dataSet);

		if (responseEntity != null)
			return responseEntity;

		trimAnalysisProjectAwareEntityForSave(dataSet);
		trimDirectoryFileDataSetEntityForSave(dataSet);

		this.dataSetEntityService.add(user, dataSet);
		copyToDirectoryFileDataSetEntityDirectoryIf(dataSet, "");

		return buildOperationMessageSaveSuccessResponseEntity(request, dataSet);
	}

	@RequestMapping("/addForHttp")
	public String addForHttp(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model)
	{
		HttpDataSetEntity dataSet = new HttpDataSetEntity();
		setCookieAnalysisProject(request, response, dataSet);

		model.addAttribute("dataSet", dataSet);
		model.addAttribute("availableCharsetNames", getAvailableCharsetNames());
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "dataSet.addDataSet");
		model.addAttribute(KEY_FORM_ACTION, "saveAddForHttp");

		return buildFormView(dataSet.getDataSetType());
	}

	@RequestMapping(value = "/saveAddForHttp", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAddForHttp(HttpServletRequest request, HttpServletResponse response,
			@RequestBody HttpDataSetEntity dataSet)
	{
		User user = WebUtils.getUser(request, response);

		dataSet.setId(IDUtil.randomIdOnTime20());
		dataSet.setCreateUser(User.copyWithoutPassword(user));

		ResponseEntity<OperationMessage> responseEntity = checkSaveHttpDataSetEntity(request, dataSet);

		if (responseEntity != null)
			return responseEntity;

		trimAnalysisProjectAwareEntityForSave(dataSet);

		this.dataSetEntityService.add(user, dataSet);

		return buildOperationMessageSaveSuccessResponseEntity(request, dataSet);
	}

	@RequestMapping("/edit")
	public String edit(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id)
	{
		User user = WebUtils.getUser(request, response);

		DataSetEntity dataSet = this.dataSetEntityService.getByIdForEdit(user, id);

		if (dataSet == null)
			throw new RecordNotFoundException();

		model.addAttribute("dataSet", dataSet);
		model.addAttribute("dataSetProperties", toWriteJsonTemplateModel(dataSet.getProperties()));
		model.addAttribute("dataSetParams", toWriteJsonTemplateModel(dataSet.getParams()));
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "dataSet.editDataSet");
		model.addAttribute(KEY_FORM_ACTION, "saveEditFor" + dataSet.getDataSetType());

		if (DataSetEntity.DATA_SET_TYPE_JsonFile.equals(dataSet.getDataSetType())
				|| DataSetEntity.DATA_SET_TYPE_CsvFile.equals(dataSet.getDataSetType())
				|| DataSetEntity.DATA_SET_TYPE_Http.equals(dataSet.getDataSetType()))
			model.addAttribute("availableCharsetNames", getAvailableCharsetNames());

		return buildFormView(dataSet.getDataSetType());
	}

	@RequestMapping(value = "/saveEditFor" + DataSetEntity.DATA_SET_TYPE_SQL, produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveEditForSql(HttpServletRequest request, HttpServletResponse response,
			@RequestBody SqlDataSetEntity dataSet)
	{
		User user = WebUtils.getUser(request, response);

		ResponseEntity<OperationMessage> responseEntity = checkSaveSqlDataSetEntity(request, dataSet);

		if (responseEntity != null)
			return responseEntity;

		trimAnalysisProjectAwareEntityForSave(dataSet);

		this.dataSetEntityService.update(user, dataSet);

		return buildOperationMessageSaveSuccessResponseEntity(request, dataSet);
	}

	@RequestMapping(value = "/saveEditFor" + DataSetEntity.DATA_SET_TYPE_JsonValue, produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveEditForJsonValue(HttpServletRequest request,
			HttpServletResponse response, @RequestBody JsonValueDataSetEntity dataSet)
	{
		User user = WebUtils.getUser(request, response);

		ResponseEntity<OperationMessage> responseEntity = checkSaveJsonValueDataSetEntity(request, dataSet);

		if (responseEntity != null)
			return responseEntity;

		trimAnalysisProjectAwareEntityForSave(dataSet);

		this.dataSetEntityService.update(user, dataSet);

		return buildOperationMessageSaveSuccessResponseEntity(request, dataSet);
	}

	@RequestMapping(value = "/saveEditFor" + DataSetEntity.DATA_SET_TYPE_JsonFile, produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveEditForJsonFile(HttpServletRequest request,
			HttpServletResponse response, @RequestBody JsonFileDataSetEntity dataSet,
			@RequestParam("originalFileName") String originalFileName) throws Throwable
	{
		User user = WebUtils.getUser(request, response);

		ResponseEntity<OperationMessage> responseEntity = checkSaveJsonFileDataSetEntity(request, dataSet);

		if (responseEntity != null)
			return responseEntity;

		trimAnalysisProjectAwareEntityForSave(dataSet);
		trimDirectoryFileDataSetEntityForSave(dataSet);

		this.dataSetEntityService.update(user, dataSet);
		copyToDirectoryFileDataSetEntityDirectoryIf(dataSet, originalFileName);

		return buildOperationMessageSaveSuccessResponseEntity(request, dataSet);
	}

	@RequestMapping(value = "/saveEditFor" + DataSetEntity.DATA_SET_TYPE_Excel, produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveEditForExcel(HttpServletRequest request, HttpServletResponse response,
			@RequestBody ExcelDataSetEntity dataSet, @RequestParam("originalFileName") String originalFileName)
			throws Throwable
	{
		User user = WebUtils.getUser(request, response);

		ResponseEntity<OperationMessage> responseEntity = checkSaveExcelDataSetEntity(request, dataSet);

		if (responseEntity != null)
			return responseEntity;

		trimAnalysisProjectAwareEntityForSave(dataSet);
		trimDirectoryFileDataSetEntityForSave(dataSet);

		this.dataSetEntityService.update(user, dataSet);
		copyToDirectoryFileDataSetEntityDirectoryIf(dataSet, originalFileName);

		return buildOperationMessageSaveSuccessResponseEntity(request, dataSet);
	}

	@RequestMapping(value = "/saveEditFor" + DataSetEntity.DATA_SET_TYPE_CsvValue, produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveEditForCsvValue(HttpServletRequest request,
			HttpServletResponse response, @RequestBody CsvValueDataSetEntity dataSet)
	{
		User user = WebUtils.getUser(request, response);

		ResponseEntity<OperationMessage> responseEntity = checkSaveCsvValueDataSetEntity(request, dataSet);

		if (responseEntity != null)
			return responseEntity;

		trimAnalysisProjectAwareEntityForSave(dataSet);

		this.dataSetEntityService.update(user, dataSet);

		return buildOperationMessageSaveSuccessResponseEntity(request, dataSet);
	}

	@RequestMapping(value = "/saveEditFor" + DataSetEntity.DATA_SET_TYPE_CsvFile, produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveEditForCsvFile(HttpServletRequest request, HttpServletResponse response,
			@RequestBody CsvFileDataSetEntity dataSet, @RequestParam("originalFileName") String originalFileName)
			throws Throwable
	{
		User user = WebUtils.getUser(request, response);

		ResponseEntity<OperationMessage> responseEntity = checkSaveCsvFileDataSetEntity(request, dataSet);

		if (responseEntity != null)
			return responseEntity;

		trimAnalysisProjectAwareEntityForSave(dataSet);
		trimDirectoryFileDataSetEntityForSave(dataSet);

		this.dataSetEntityService.update(user, dataSet);
		copyToDirectoryFileDataSetEntityDirectoryIf(dataSet, originalFileName);

		return buildOperationMessageSaveSuccessResponseEntity(request, dataSet);
	}

	@RequestMapping(value = "/saveEditFor" + DataSetEntity.DATA_SET_TYPE_Http, produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveEditForHttp(HttpServletRequest request, HttpServletResponse response,
			@RequestBody HttpDataSetEntity dataSet)
	{
		User user = WebUtils.getUser(request, response);

		ResponseEntity<OperationMessage> responseEntity = checkSaveHttpDataSetEntity(request, dataSet);

		if (responseEntity != null)
			return responseEntity;

		trimAnalysisProjectAwareEntityForSave(dataSet);

		this.dataSetEntityService.update(user, dataSet);

		return buildOperationMessageSaveSuccessResponseEntity(request, dataSet);
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
		User user = WebUtils.getUser(request, response);

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
		User user = WebUtils.getUser(request, response);

		DataSetEntity dataSet = this.dataSetEntityService.getById(user, id);

		if (dataSet == null)
			throw new RecordNotFoundException();

		model.addAttribute("dataSet", dataSet);
		model.addAttribute("dataSetProperties", toWriteJsonTemplateModel(dataSet.getProperties()));
		model.addAttribute("dataSetParams", toWriteJsonTemplateModel(dataSet.getParams()));
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "dataSet.viewDataSet");
		model.addAttribute(KEY_READONLY, true);

		if (DataSetEntity.DATA_SET_TYPE_JsonFile.equals(dataSet.getDataSetType())
				|| DataSetEntity.DATA_SET_TYPE_CsvFile.equals(dataSet.getDataSetType())
				|| DataSetEntity.DATA_SET_TYPE_Http.equals(dataSet.getDataSetType()))
			model.addAttribute("availableCharsetNames", getAvailableCharsetNames());

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
			User user = WebUtils.getUser(request, response);

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
		User user = WebUtils.getUser(request, response);

		for (int i = 0; i < ids.length; i++)
		{
			String id = ids[i];
			this.dataSetEntityService.deleteById(user, id);

			File dataSetDirectory = getDataSetEntityService().getDataSetDirectory(id);
			FileUtil.deleteFile(dataSetDirectory);
		}

		return buildOperationMessageDeleteSuccessResponseEntity(request);
	}

	@RequestMapping("/pagingQuery")
	public String pagingQuery(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model)
	{
		User user = WebUtils.getUser(request, response);
		model.addAttribute("currentUser", user);

		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "dataSet.manageDataSet");

		return "/analysis/dataSet/dataSet_grid";
	}

	@RequestMapping(value = "/select")
	public String select(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model)
	{
		User user = WebUtils.getUser(request, response);
		model.addAttribute("currentUser", user);

		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "dataSet.selectDataSet");
		model.addAttribute(KEY_SELECT_OPERATION, true);
		setIsMultipleSelectAttribute(request, model);

		return "/analysis/dataSet/dataSet_grid";
	}

	@RequestMapping(value = "/pagingQueryData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public PagingData<DataSetEntity> pagingQueryData(HttpServletRequest request, HttpServletResponse response,
			final org.springframework.ui.Model springModel,
			@RequestBody(required = false) APIDDataFilterPagingQuery pagingQueryParam) throws Exception
	{
		User user = WebUtils.getUser(request, response);
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
		final User user = WebUtils.getUser(request, response);

		final SqlDataSet dataSet = preview.getDataSet();

		String schemaId = preview.getSchemaId();

		// 新建时操作时未创建数据集
		boolean notFound = checkDataSetEntityIdReadPermission(user, dataSet.getId());
		// 如果数据集已创建，则使用数据集权限；如果数据集未创建，则需使用数据源权限
		Schema schema = (notFound ? getSchemaForUserNotNull(user, schemaId) : getSchemaNotNull(schemaId));

		SchemaConnectionFactory connectionFactory = new SchemaConnectionFactory(getConnectionSource(), schema);
		dataSet.setConnectionFactory(connectionFactory);

		Map<String, Object> convertedParamValues = getDataSetParamValueConverter().convert(preview.getParamValues(),
				dataSet.getParams());

		TemplateResolvedDataSetResult result = dataSet.resolve(convertedParamValues, preview.getDataSetOption());

		return result;
	}

	@RequestMapping(value = "/resolveSql", produces = CONTENT_TYPE_HTML)
	@ResponseBody
	public String resolveSql(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @RequestBody ResolveSqlParam resolveSqlParam) throws Throwable
	{
		return resolveFmkSource(resolveSqlParam.getSql(), resolveSqlParam.getParamValues(),
				resolveSqlParam.getDataSetParams());
	}

	@RequestMapping(value = "/previewJsonValue", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public TemplateResolvedDataSetResult previewJsonValue(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @RequestBody JsonValueDataSetPreview preview) throws Throwable
	{
		final User user = WebUtils.getUser(request, response);

		JsonValueDataSet dataSet = preview.getDataSet();

		checkDataSetEntityIdReadPermission(user, dataSet.getId());

		Map<String, Object> convertedParamValues = getDataSetParamValueConverter().convert(preview.getParamValues(),
				dataSet.getParams());

		TemplateResolvedDataSetResult result = dataSet.resolve(convertedParamValues, preview.getDataSetOption());

		return result;
	}

	@RequestMapping(value = "/previewJsonFile", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResolvedDataSetResult previewJsonFile(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @RequestBody JsonFileDataSetEntityPreview preview)
			throws Throwable
	{
		final User user = WebUtils.getUser(request, response);

		JsonFileDataSetEntity dataSet = preview.getDataSet();

		checkDataSetEntityIdReadPermission(user, dataSet.getId());

		setDirectoryFileDataSetDirectory(dataSet, preview.getOriginalFileName());

		Map<String, Object> convertedParamValues = getDataSetParamValueConverter().convert(preview.getParamValues(),
				dataSet.getParams());

		ResolvedDataSetResult result = dataSet.resolve(convertedParamValues, preview.getDataSetOption());

		return result;
	}

	@RequestMapping(value = "/previewExcel", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResolvedDataSetResult previewExcel(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @RequestBody ExcelDataSetEntityPreview preview) throws Throwable
	{
		final User user = WebUtils.getUser(request, response);

		ExcelDataSetEntity dataSet = preview.getDataSet();

		checkDataSetEntityIdReadPermission(user, dataSet.getId());

		setDirectoryFileDataSetDirectory(dataSet, preview.getOriginalFileName());

		Map<String, Object> convertedParamValues = getDataSetParamValueConverter().convert(preview.getParamValues(),
				dataSet.getParams());

		ResolvedDataSetResult result = dataSet.resolve(convertedParamValues, preview.getDataSetOption());

		return result;
	}

	@RequestMapping(value = "/previewCsvValue", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public TemplateResolvedDataSetResult previewCsvValue(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @RequestBody CsvValueDataSetPreview preview) throws Throwable
	{
		final User user = WebUtils.getUser(request, response);

		CsvValueDataSet dataSet = preview.getDataSet();

		checkDataSetEntityIdReadPermission(user, dataSet.getId());

		Map<String, Object> convertedParamValues = getDataSetParamValueConverter().convert(preview.getParamValues(),
				dataSet.getParams());

		TemplateResolvedDataSetResult result = dataSet.resolve(convertedParamValues, preview.getDataSetOption());

		return result;
	}

	@RequestMapping(value = "/previewCsvFile", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResolvedDataSetResult previewCsvFile(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @RequestBody CsvFileDataSetEntityPreview preview) throws Throwable
	{
		final User user = WebUtils.getUser(request, response);

		CsvFileDataSetEntity dataSet = preview.getDataSet();

		checkDataSetEntityIdReadPermission(user, dataSet.getId());

		setDirectoryFileDataSetDirectory(dataSet, preview.getOriginalFileName());

		Map<String, Object> convertedParamValues = getDataSetParamValueConverter().convert(preview.getParamValues(),
				dataSet.getParams());

		ResolvedDataSetResult result = dataSet.resolve(convertedParamValues, preview.getDataSetOption());

		return result;
	}

	@RequestMapping(value = "/previewHttp", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public TemplateResolvedDataSetResult previewHttp(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @RequestBody HttpDataSetEntityPreview preview) throws Throwable
	{
		final User user = WebUtils.getUser(request, response);

		HttpDataSetEntity dataSet = preview.getDataSet();

		checkDataSetEntityIdReadPermission(user, dataSet.getId());

		dataSet.setHttpClient(getDataSetEntityService().getHttpClient());

		Map<String, Object> convertedParamValues = getDataSetParamValueConverter().convert(preview.getParamValues(),
				dataSet.getParams());

		TemplateResolvedDataSetResult result = dataSet.resolve(convertedParamValues, preview.getDataSetOption());

		return result;
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

		if (!isEmpty(originalFileName) && originalFileName.equals(fileName))
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
		return "/analysis/dataSet/dataSet_form_" + dataSetType;
	}

	protected File getTempDataSetDirectory()
	{
		return FileUtil.getDirectory(this.tempDirectory, "dataSet", true);
	}

	protected String resolveFmkSource(String source, Map<String, ?> paramValues, Collection<DataSetParam> dataSetParams)
	{
		Map<String, ?> converted = getDataSetParamValueConverter().convert(paramValues, dataSetParams);
		return getDataSetFmkTemplateResolver().resolve(source, new TemplateContext(converted));
	}

	protected DataSetFmkTemplateResolver getDataSetFmkTemplateResolver()
	{
		return AbstractDataSet.FMK_TEMPLATE_RESOLVER;
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

		if (isEmpty(dataSet.getProperties()))
			throw new IllegalInputException();

		List<DataSetParam> params = dataSet.getParams();
		if (params != null)
		{
			Set<String> names = new HashSet<>();
			for (DataSetParam param : params)
			{
				String name = param.getName();

				if (names.contains(name))
				{
					return buildOperationMessageFailResponseEntity(null, HttpStatus.BAD_REQUEST,
							"dataSet.error.duplicateParamName");
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

				if (names.contains(name))
				{
					return buildOperationMessageFailResponseEntity(null, HttpStatus.BAD_REQUEST,
							"dataSet.error.duplicatePropertyName");
				}
				else
					names.add(name);
			}
		}

		return null;
	}

	public static class AbstractDataSetPreview<T extends DataSet>
	{
		private T dataSet;

		@SuppressWarnings("unchecked")
		private Map<String, Object> paramValues = Collections.EMPTY_MAP;

		private DataSetOption dataSetOption = null;

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

		public Map<String, Object> getParamValues()
		{
			return paramValues;
		}

		public void setParamValues(Map<String, Object> paramValues)
		{
			this.paramValues = paramValues;
		}

		public DataSetOption getDataSetOption()
		{
			return dataSetOption;
		}

		public void setDataSetOption(DataSetOption dataSetOption)
		{
			this.dataSetOption = dataSetOption;
		}
	}

	public static class SqlDataSetPreview extends AbstractDataSetPreview<SqlDataSet>
	{
		private String schemaId;

		public SqlDataSetPreview()
		{
			super();
		}

		public String getSchemaId()
		{
			return schemaId;
		}

		public void setSchemaId(String schemaId)
		{
			this.schemaId = schemaId;
		}
	}

	public static class JsonValueDataSetPreview extends AbstractDataSetPreview<JsonValueDataSet>
	{
		public JsonValueDataSetPreview()
		{
			super();
		}
	}

	public static class JsonFileDataSetEntityPreview extends AbstractDataSetPreview<JsonFileDataSetEntity>
	{
		private String originalFileName;

		public JsonFileDataSetEntityPreview()
		{
			super();
		}

		public String getOriginalFileName()
		{
			return originalFileName;
		}

		public void setOriginalFileName(String originalFileName)
		{
			this.originalFileName = originalFileName;
		}
	}

	public static class ExcelDataSetEntityPreview extends AbstractDataSetPreview<ExcelDataSetEntity>
	{
		private String originalFileName;

		public ExcelDataSetEntityPreview()
		{
			super();
		}

		public String getOriginalFileName()
		{
			return originalFileName;
		}

		public void setOriginalFileName(String originalFileName)
		{
			this.originalFileName = originalFileName;
		}
	}

	public static class CsvValueDataSetPreview extends AbstractDataSetPreview<CsvValueDataSet>
	{
		public CsvValueDataSetPreview()
		{
			super();
		}
	}

	public static class CsvFileDataSetEntityPreview extends AbstractDataSetPreview<CsvFileDataSetEntity>
	{
		private String originalFileName;

		public CsvFileDataSetEntityPreview()
		{
			super();
		}

		public String getOriginalFileName()
		{
			return originalFileName;
		}

		public void setOriginalFileName(String originalFileName)
		{
			this.originalFileName = originalFileName;
		}
	}

	public static class HttpDataSetEntityPreview extends AbstractDataSetPreview<HttpDataSetEntity>
	{
		public HttpDataSetEntityPreview()
		{
			super();
		}
	}

	public static class ResolveSqlParam
	{
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
