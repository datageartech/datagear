/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.analysis.DataSet;
import org.datagear.analysis.DataSetParam;
import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.ResolvedDataSetResult;
import org.datagear.analysis.support.AbstractFmkTemplateDataSet;
import org.datagear.analysis.support.DataSetFmkTemplateResolver;
import org.datagear.analysis.support.DataSetParamValueConverter;
import org.datagear.analysis.support.JsonDirectoryFileDataSet;
import org.datagear.analysis.support.JsonValueDataSet;
import org.datagear.analysis.support.SqlDataSet;
import org.datagear.analysis.support.SqlDataSetSupport;
import org.datagear.analysis.support.TemplateContext;
import org.datagear.analysis.support.TemplateResolvedDataSetResult;
import org.datagear.management.domain.DataSetEntity;
import org.datagear.management.domain.JsonFileDataSetEntity;
import org.datagear.management.domain.JsonValueDataSetEntity;
import org.datagear.management.domain.Schema;
import org.datagear.management.domain.SchemaConnectionFactory;
import org.datagear.management.domain.SqlDataSetEntity;
import org.datagear.management.domain.User;
import org.datagear.management.service.DataSetEntityService;
import org.datagear.meta.Column;
import org.datagear.meta.Table;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.support.SqlSelectManager;
import org.datagear.util.FileUtil;
import org.datagear.util.IDUtil;
import org.datagear.util.IOUtil;
import org.datagear.util.SqlType;
import org.datagear.web.OperationMessage;
import org.datagear.web.util.WebUtils;
import org.datagear.web.vo.DataFilterPagingQuery;
import org.springframework.beans.factory.annotation.Autowired;
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
	public static final int DEFAULT_SQL_RESULTSET_FETCH_SIZE = 20;

	static
	{
		AuthorizationResourceMetas.registerForShare(SqlDataSetEntity.AUTHORIZATION_RESOURCE_TYPE, "dataSet");
	}

	@Autowired
	private DataSetEntityService dataSetEntityService;

	@Autowired
	private SqlSelectManager sqlSelectManager;

	@Autowired
	private File tempDirectory;

	private SqlDataSetSupport sqlDataSetSupport = new SqlDataSetSupport();

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

	public SqlSelectManager getSqlSelectManager()
	{
		return sqlSelectManager;
	}

	public void setSqlSelectManager(SqlSelectManager sqlSelectManager)
	{
		this.sqlSelectManager = sqlSelectManager;
	}

	public SqlDataSetSupport getSqlDataSetSupport()
	{
		return sqlDataSetSupport;
	}

	public void setSqlDataSetSupport(SqlDataSetSupport sqlDataSetSupport)
	{
		this.sqlDataSetSupport = sqlDataSetSupport;
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
	public String addForSql(HttpServletRequest request, org.springframework.ui.Model model)
	{
		SqlDataSetEntity dataSet = new SqlDataSetEntity();

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

		checkSaveSqlDataSetEntity(dataSet);

		this.dataSetEntityService.add(user, dataSet);

		return buildOperationMessageSaveSuccessResponseEntity(request, dataSet);
	}

	@RequestMapping("/addForJsonValue")
	public String addForJsonValue(HttpServletRequest request, org.springframework.ui.Model model)
	{
		JsonValueDataSetEntity dataSet = new JsonValueDataSetEntity();

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

		checkSaveJsonValueDataSetEntity(dataSet);

		this.dataSetEntityService.add(user, dataSet);

		return buildOperationMessageSaveSuccessResponseEntity(request, dataSet);
	}

	@RequestMapping("/addForJsonFile")
	public String addForJsonFile(HttpServletRequest request, org.springframework.ui.Model model)
	{
		JsonFileDataSetEntity dataSet = new JsonFileDataSetEntity();

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

		checkSaveJsonFileDataSetEntity(dataSet);

		this.dataSetEntityService.add(user, dataSet);
		copyToJsonFileDataSetEntityDirectoryIf(dataSet, "");

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

		if (DataSetEntity.DATA_SET_TYPE_JsonFile.equals(dataSet.getDataSetType()))
			model.addAttribute("availableCharsetNames", getAvailableCharsetNames());

		return buildFormView(dataSet.getDataSetType());
	}

	@RequestMapping(value = "/saveEditFor" + DataSetEntity.DATA_SET_TYPE_SQL, produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveEditForSql(HttpServletRequest request, HttpServletResponse response,
			@RequestBody SqlDataSetEntity dataSet)
	{
		User user = WebUtils.getUser(request, response);

		checkSaveSqlDataSetEntity(dataSet);

		this.dataSetEntityService.update(user, dataSet);

		return buildOperationMessageSaveSuccessResponseEntity(request, dataSet);
	}

	@RequestMapping(value = "/saveEditFor" + DataSetEntity.DATA_SET_TYPE_JsonValue, produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveEditForJsonValue(HttpServletRequest request,
			HttpServletResponse response, @RequestBody JsonValueDataSetEntity dataSet)
	{
		User user = WebUtils.getUser(request, response);

		checkSaveJsonValueDataSetEntity(dataSet);

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

		checkSaveJsonFileDataSetEntity(dataSet);

		this.dataSetEntityService.update(user, dataSet);
		copyToJsonFileDataSetEntityDirectoryIf(dataSet, originalFileName);

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

		if (DataSetEntity.DATA_SET_TYPE_JsonFile.equals(dataSet.getDataSetType()))
			model.addAttribute("availableCharsetNames", getAvailableCharsetNames());

		return buildFormView(dataSet.getDataSetType());
	}

	@RequestMapping(value = "/getById", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public DataSetEntity getById(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model, @RequestParam("id") String id)
	{
		User user = WebUtils.getUser(request, response);

		DataSetEntity dataSet = this.dataSetEntityService.getById(user, id);

		if (dataSet == null)
			throw new RecordNotFoundException();

		return dataSet;
	}

	@RequestMapping(value = "/getByIds", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public List<DataSetEntity> getByIds(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model, @RequestParam("id") String[] ids)
	{
		List<DataSetEntity> dataSets = new ArrayList<>();

		if (!isEmpty(ids))
		{
			User user = WebUtils.getUser(request, response);

			for (String id : ids)
			{
				DataSetEntity dataSet = this.dataSetEntityService.getById(user, id);

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
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "dataSet.selectDataSet");
		model.addAttribute(KEY_SELECT_OPERATION, true);
		setIsMultipleSelectAttribute(request, model);

		return "/analysis/dataSet/dataSet_grid";
	}

	@RequestMapping(value = "/pagingQueryData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public PagingData<DataSetEntity> pagingQueryData(HttpServletRequest request, HttpServletResponse response,
			final org.springframework.ui.Model springModel,
			@RequestBody(required = false) DataFilterPagingQuery pagingQueryParam) throws Exception
	{
		User user = WebUtils.getUser(request, response);
		final DataFilterPagingQuery pagingQuery = inflateDataFilterPagingQuery(request, pagingQueryParam);

		PagingData<DataSetEntity> pagingData = this.dataSetEntityService.pagingQuery(user, pagingQuery,
				pagingQuery.getDataFilter());

		return pagingData;
	}

	@RequestMapping(value = "/previewSql", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public TemplateResolvedDataSetResult previewSql(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @RequestBody SqlDataSetPreview sqlDataSetPreview) throws Throwable
	{
		final User user = WebUtils.getUser(request, response);

		String schemaId = sqlDataSetPreview.getSchemaId();

		TemplateResolvedDataSetResult result = new ReturnSchemaConnExecutor<TemplateResolvedDataSetResult>(request,
				response, springModel, schemaId, true)
		{
			@Override
			protected TemplateResolvedDataSetResult execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema) throws Throwable
			{
				checkReadTableDataPermission(schema, user);

				SqlDataSet dataSet = sqlDataSetPreview.getDataSet();
				SchemaConnectionFactory connectionFactory = new SchemaConnectionFactory(getConnectionSource(), schema);
				dataSet.setConnectionFactory(connectionFactory);

				TemplateResolvedDataSetResult result = dataSet.resolve(sqlDataSetPreview.getParamValues());

				return result;
			}
		}.execute();

		return result;
	}

	@RequestMapping(value = "/resolveSql", produces = CONTENT_TYPE_JSON)
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
		JsonValueDataSet dataSet = preview.getDataSet();
		TemplateResolvedDataSetResult result = dataSet.resolve(preview.getParamValues());

		return result;
	}

	@RequestMapping(value = "/previewJsonFile", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResolvedDataSetResult previewJsonFile(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @RequestBody JsonDirectoryFileDataSetPreview preview)
			throws Throwable
	{
		JsonDirectoryFileDataSet dataSet = preview.getDataSet();
		setJsonDirectoryFileDataSetDirectory(dataSet, preview.getOriginalFileName());

		ResolvedDataSetResult result = dataSet.resolve(preview.getParamValues());

		return result;
	}

	protected boolean copyToJsonFileDataSetEntityDirectoryIf(JsonFileDataSetEntity entity, String originalFileName)
			throws IOException
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

	protected void setJsonDirectoryFileDataSetDirectory(JsonDirectoryFileDataSet dataSet, String originalFileName)
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
		return AbstractFmkTemplateDataSet.TEMPLATE_RESOLVER;
	}

	/**
	 * 解析{@linkplain DataSetProperty}列表。
	 * 
	 * @param table
	 * @param labels
	 *            {@linkplain DataSetProperty#getLabel()}数组，允许为{@code null}或任意长度的数组
	 * @return
	 * @throws Throwable
	 */
	protected List<DataSetProperty> resolveDataSetProperties(Table table, String[] labels) throws Throwable
	{
		Column[] properties = table.getColumns();

		List<DataSetProperty> dataSetProperties = new ArrayList<>(properties == null ? 0 : properties.length);

		if (properties != null)
		{
			for (int i = 0; i < properties.length; i++)
			{
				Column column = properties[i];

				DataSetProperty dataSetProperty = new DataSetProperty(column.getName(), sqlDataSetSupport
						.toPropertyDataType(new SqlType(column.getType(), column.getTypeName()), column.getName()));

				if (labels != null && labels.length > i)
					dataSetProperty.setLabel(labels[i]);

				dataSetProperties.add(dataSetProperty);
			}
		}

		return dataSetProperties;
	}

	protected void checkSaveEntity(DataSetEntity dataSet)
	{
		if (isBlank(dataSet.getName()))
			throw new IllegalInputException();

		if (isEmpty(dataSet.getProperties()))
			throw new IllegalInputException();
	}

	protected void checkSaveSqlDataSetEntity(SqlDataSetEntity dataSet)
	{
		checkSaveEntity(dataSet);

		if (isEmpty(dataSet.getConnectionFactory()))
			throw new IllegalInputException();

		if (isEmpty(dataSet.getConnectionFactory().getSchema()))
			throw new IllegalInputException();

		if (isEmpty(dataSet.getConnectionFactory().getSchema().getId()))
			throw new IllegalInputException();

		if (isBlank(dataSet.getSql()))
			throw new IllegalInputException();
	}

	protected void checkSaveJsonValueDataSetEntity(JsonValueDataSetEntity dataSet)
	{
		checkSaveEntity(dataSet);

		if (isEmpty(dataSet.getValue()))
			throw new IllegalInputException();
	}

	protected void checkSaveJsonFileDataSetEntity(JsonFileDataSetEntity dataSet)
	{
		checkSaveEntity(dataSet);

		if (isEmpty(dataSet.getFileName()))
			throw new IllegalInputException();

		if (isEmpty(dataSet.getDisplayName()))
			throw new IllegalInputException();
	}

	public static class AbstractDataSetPreview<T extends DataSet>
	{
		private T dataSet;

		@SuppressWarnings("unchecked")
		private Map<String, Object> paramValues = Collections.EMPTY_MAP;

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
		private String value;

		public JsonValueDataSetPreview()
		{
			super();
		}

		public JsonValueDataSetPreview(String value)
		{
			super();
			this.value = value;
		}

		public String getValue()
		{
			return value;
		}

		public void setValue(String value)
		{
			this.value = value;
		}
	}

	public static class JsonDirectoryFileDataSetPreview extends AbstractDataSetPreview<JsonDirectoryFileDataSet>
	{
		private String originalFileName;

		public JsonDirectoryFileDataSetPreview()
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
