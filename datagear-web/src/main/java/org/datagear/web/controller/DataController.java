/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.controller;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.connection.ConnectionSource;
import org.datagear.connection.IOUtil;
import org.datagear.dbmodel.CachedDbModelFactory;
import org.datagear.management.domain.Schema;
import org.datagear.management.service.SchemaService;
import org.datagear.model.Model;
import org.datagear.model.Property;
import org.datagear.model.support.PropertyPath;
import org.datagear.model.support.PropertyPathInfo;
import org.datagear.persistence.ColumnPropertyPath;
import org.datagear.persistence.Dialect;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.PagingQuery;
import org.datagear.persistence.PersistenceManager;
import org.datagear.persistence.QueryResultMetaInfo;
import org.datagear.persistence.columnconverter.LOBConversionContext;
import org.datagear.persistence.columnconverter.LOBConversionContext.LOBConversionSetting;
import org.datagear.persistence.support.ExpressionEvaluationContext;
import org.datagear.persistence.support.SelectOptions;
import org.datagear.persistence.support.SqlExpressionSyntaxErrorException;
import org.datagear.persistence.support.VariableExpressionSyntaxErrorException;
import org.datagear.web.OperationMessage;
import org.datagear.web.convert.ClassDataConverter;
import org.datagear.web.convert.ConverterException;
import org.datagear.web.convert.ModelDataConverter;
import org.datagear.web.format.DateFormatter;
import org.datagear.web.format.SqlDateFormatter;
import org.datagear.web.format.SqlTimeFormatter;
import org.datagear.web.format.SqlTimestampFormatter;
import org.datagear.web.util.FileUtils;
import org.datagear.web.util.ModelUtils;
import org.datagear.web.util.WebUtils;
import org.datagear.web.vo.FileInfo;
import org.datagear.web.vo.PropertyPathDisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

/**
 * 数据管理控制器。
 * 
 * @author datagear@163.com
 *
 */
@Controller
@RequestMapping("/data")
public class DataController extends AbstractSchemaModelController
{
	public static final byte[] BLOB_TO_BYTES_PLACEHOLDER = new byte[] { 0x00 };

	public static final String PARAM_IGNORE_DUPLICATION = "ignoreDuplication";

	@Autowired
	private PersistenceManager persistenceManager;

	@Autowired
	private SelectOptions selectOptions;

	@Autowired
	private ModelDataConverter modelDataConverter;

	@Autowired
	@Qualifier("blobFileManagerDirectory")
	private File blobFileManagerDirectory;

	@Autowired
	@Qualifier("blobToFilePlaceholderName")
	private String blobToFilePlaceholderName;

	@Autowired
	private DateFormatter dateFormatter;

	@Autowired
	private SqlDateFormatter sqlDateFormatter;

	@Autowired
	private SqlTimestampFormatter sqlTimestampFormatter;

	@Autowired
	private SqlTimeFormatter sqlTimeFormatter;

	public DataController()
	{
		super();
	}

	public DataController(MessageSource messageSource, ClassDataConverter classDataConverter,
			SchemaService schemaService, ConnectionSource connectionSource, CachedDbModelFactory cachedDbModelFactory,
			PersistenceManager persistenceManager, SelectOptions selectOptions, ModelDataConverter modelDataConverter,
			File blobFileManagerDirectory, String blobToFilePlaceholderName, DateFormatter dateFormatter,
			SqlDateFormatter sqlDateFormatter, SqlTimestampFormatter sqlTimestampFormatter,
			SqlTimeFormatter sqlTimeFormatter)
	{
		super(messageSource, classDataConverter, schemaService, connectionSource, cachedDbModelFactory);
		this.persistenceManager = persistenceManager;
		this.selectOptions = selectOptions;
		this.modelDataConverter = modelDataConverter;
		this.blobFileManagerDirectory = blobFileManagerDirectory;
		this.blobToFilePlaceholderName = blobToFilePlaceholderName;
		this.dateFormatter = dateFormatter;
		this.sqlDateFormatter = sqlDateFormatter;
		this.sqlTimestampFormatter = sqlTimestampFormatter;
		this.sqlTimeFormatter = sqlTimeFormatter;
	}

	public PersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	public void setPersistenceManager(PersistenceManager persistenceManager)
	{
		this.persistenceManager = persistenceManager;
	}

	public SelectOptions getSelectOptions()
	{
		return selectOptions;
	}

	public void setSelectOptions(SelectOptions selectOptions)
	{
		this.selectOptions = selectOptions;
	}

	public ModelDataConverter getModelDataConverter()
	{
		return modelDataConverter;
	}

	public void setModelDataConverter(ModelDataConverter modelDataConverter)
	{
		this.modelDataConverter = modelDataConverter;
	}

	public File getBlobFileManagerDirectory()
	{
		return blobFileManagerDirectory;
	}

	public void setBlobFileManagerDirectory(File blobFileManagerDirectory)
	{
		this.blobFileManagerDirectory = blobFileManagerDirectory;
	}

	public String getBlobToFilePlaceholderName()
	{
		return blobToFilePlaceholderName;
	}

	public void setBlobToFilePlaceholderName(String blobToFilePlaceholderName)
	{
		this.blobToFilePlaceholderName = blobToFilePlaceholderName;
	}

	public DateFormatter getDateFormatter()
	{
		return dateFormatter;
	}

	public void setDateFormatter(DateFormatter dateFormatter)
	{
		this.dateFormatter = dateFormatter;
	}

	public SqlDateFormatter getSqlDateFormatter()
	{
		return sqlDateFormatter;
	}

	public void setSqlDateFormatter(SqlDateFormatter sqlDateFormatter)
	{
		this.sqlDateFormatter = sqlDateFormatter;
	}

	public SqlTimestampFormatter getSqlTimestampFormatter()
	{
		return sqlTimestampFormatter;
	}

	public void setSqlTimestampFormatter(SqlTimestampFormatter sqlTimestampFormatter)
	{
		this.sqlTimestampFormatter = sqlTimestampFormatter;
	}

	public SqlTimeFormatter getSqlTimeFormatter()
	{
		return sqlTimeFormatter;
	}

	public void setSqlTimeFormatter(SqlTimeFormatter sqlTimeFormatter)
	{
		this.sqlTimeFormatter = sqlTimeFormatter;
	}

	@RequestMapping("/{schemaId}/{tableName}/query")
	public String query(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("tableName") String tableName, @RequestParam(value = "page", required = false) Integer page,
			@RequestParam(value = "pageSize", required = false) Integer pageSize) throws Throwable
	{
		new VoidExecutor(request, response, springModel, schemaId, tableName, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable
			{
				Connection cn = getConnection();

				QueryResultMetaInfo queryResultMetaInfo = persistenceManager.getQueryResultMetaInfo(cn, model);

				springModel.addAttribute("conditionSource", getPropertyPathDisplayNames(request, queryResultMetaInfo));
			}
		}.execute();

		return "/data/data_grid";
	}

	@RequestMapping(value = "/{schemaId}/{tableName}/queryData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public PagingData<Object> queryData(HttpServletRequest request, HttpServletResponse response,
			final org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("tableName") String tableName) throws Throwable
	{
		final PagingQuery pagingQuery = getPagingQuery(request);

		ReturnExecutor<PagingData<Object>> executor = new ReturnExecutor<PagingData<Object>>(request, response,
				springModel, schemaId, tableName, true)
		{
			@Override
			protected PagingData<Object> execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable
			{
				Connection cn = getConnection();

				LOBConversionContext.set(buildQueryLobConversionSetting());

				PagingData<Object> pagingData = persistenceManager.query(cn, model, pagingQuery);

				LOBConversionContext.remove();

				return pagingData;
			}
		};

		return executor.execute();
	}

	@RequestMapping("/{schemaId}/{tableName}/add")
	public String add(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("tableName") String tableName) throws Throwable
	{
		new VoidExecutor(request, response, springModel, schemaId, tableName, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable
			{
				springModel.addAttribute("titleOperationMessageKey", "add");
				springModel.addAttribute("clientOperation", "true");
				springModel.addAttribute("submitAction", "saveAdd");
			}
		}.execute();

		setParseDateFormats(request, springModel);

		return "/data/data_form";
	}

	@RequestMapping(value = "/{schemaId}/{tableName}/saveAdd", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAdd(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("tableName") String tableName,
			@RequestParam(value = "batchCount", required = false) Integer batchCount,
			@RequestParam(value = "batchHandleErrorMode", required = false) BatchHandleErrorMode batchHandleErrorMode)
			throws Throwable
	{
		if (batchCount != null && batchCount >= 0)
			return saveAddBatch(request, response, springModel, schemaId, tableName, batchCount, batchHandleErrorMode);
		else
			return saveAddSingle(request, response, springModel, schemaId, tableName);
	}

	/**
	 * 单个添加。
	 * 
	 * @param request
	 * @param response
	 * @param springModel
	 * @param schemaId
	 * @param tableName
	 * @return
	 * @throws Throwable
	 */
	protected ResponseEntity<OperationMessage> saveAddSingle(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, String schemaId, String tableName) throws Throwable
	{
		final Object dataParam = getParamMap(request, "data");

		Object data = new ReturnExecutor<Object>(request, response, springModel, schemaId, tableName, false)
		{
			@Override
			protected Object execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable
			{
				Connection cn = getConnection();

				Object data = modelDataConverter.convert(dataParam, model);
				persistenceManager.insert(cn, model, data);

				return data;
			}
		}.execute();

		ResponseEntity<OperationMessage> responseEntity = buildOperationMessageSaveSuccessResponseEntity(request);
		responseEntity.getBody().setData(data);

		return responseEntity;
	}

	/**
	 * 批量添加。
	 * 
	 * @param request
	 * @param response
	 * @param springModel
	 * @param schemaId
	 * @param tableName
	 * @param batchCount
	 * @param batchHandleErrorMode
	 * @return
	 * @throws Throwable
	 */
	protected ResponseEntity<OperationMessage> saveAddBatch(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, String schemaId, String tableName, int batchCount,
			BatchHandleErrorMode batchHandleErrorMode) throws Throwable
	{
		final Object dataParam = getParamMap(request, "data");

		ResponseEntity<OperationMessage> batchResponseEntity = new BatchReturnExecutor(request, response, springModel,
				schemaId, tableName, batchCount, batchHandleErrorMode)
		{
			@Override
			protected void doBatchUnit(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Model model, Connection cn,
					Dialect dialect, String table, ExpressionEvaluationContext context) throws Throwable
			{
				Object data = modelDataConverter.convert(dataParam, model);
				persistenceManager.insert(cn, dialect, table, model, data, context);
			}
		}.execute();

		return batchResponseEntity;
	}

	@RequestMapping("/{schemaId}/{tableName}/edit")
	public String edit(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("tableName") String tableName) throws Throwable
	{
		final Object dataParam = getParamObj(request, "data");

		new VoidExecutor(request, response, springModel, schemaId, tableName, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable
			{
				Connection cn = getConnection();

				Object data = modelDataConverter.convert(dataParam, model);

				LOBConversionContext.set(buildGetLobConversionSetting());

				List<Object> resultList = persistenceManager.getByParam(cn, model, data);

				LOBConversionContext.remove();

				if (resultList == null || resultList.isEmpty())
					throw new RecordNotFoundException();

				springModel.addAttribute("data", resultList.get(0));
				springModel.addAttribute("titleOperationMessageKey", "edit");
				springModel.addAttribute("clientOperation", "false");
				springModel.addAttribute("submitAction", "saveEdit");
			}
		}.execute();

		setParseDateFormats(request, springModel);

		return "/data/data_form";
	}

	@RequestMapping(value = "/{schemaId}/{tableName}/saveEdit", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveEdit(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("tableName") String tableName,
			@RequestParam(value = PARAM_IGNORE_DUPLICATION, required = false) final Boolean ignoreDuplication)
			throws Throwable
	{
		final Object originalDataParam = getParamObj(request, "originalData");
		final Object dataParam = getParamMap(request, "data");

		ResponseEntity<OperationMessage> responseEntity = new ReturnExecutor<ResponseEntity<OperationMessage>>(request,
				response, springModel, schemaId, tableName, false)
		{
			@Override
			protected ResponseEntity<OperationMessage> execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable
			{
				Connection cn = getConnection();

				Object originalData = modelDataConverter.convert(originalDataParam, model);
				Object data = modelDataConverter.convert(dataParam, model);

				int count = persistenceManager.update(cn, model, originalData, data, false);

				checkDuplicateRecord(1, count, ignoreDuplication);

				ResponseEntity<OperationMessage> responseEntity = buildOperationMessageSaveCountResponseEntity(request,
						count);
				responseEntity.getBody().setData(data);

				return responseEntity;
			}
		}.execute();

		return responseEntity;
	}

	@RequestMapping(value = "/{schemaId}/{tableName}/delete", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> delete(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("tableName") String tableName,
			@RequestParam(value = PARAM_IGNORE_DUPLICATION, required = false) final Boolean ignoreDuplication)
			throws Throwable
	{
		final Object dataParam = getParamObj(request, "data");

		ResponseEntity<OperationMessage> responseEntity = new ReturnExecutor<ResponseEntity<OperationMessage>>(request,
				response, springModel, schemaId, tableName, false)
		{
			@Override
			protected ResponseEntity<OperationMessage> execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable
			{
				Connection cn = getConnection();

				Object[] datas = modelDataConverter.convertToArray(dataParam, model);

				int count = persistenceManager.delete(cn, model, datas);

				checkDuplicateRecord(datas.length, count, ignoreDuplication);

				ResponseEntity<OperationMessage> responseEntity = buildOperationMessageDeleteCountResponseEntity(
						request, count);
				responseEntity.getBody().setData(count);

				return responseEntity;
			}
		}.execute();

		return responseEntity;
	}

	@RequestMapping("/{schemaId}/{tableName}/view")
	public String view(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("tableName") String tableName) throws Throwable
	{
		final Object dataParam = getParamObj(request, "data");

		new VoidExecutor(request, response, springModel, schemaId, tableName, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable
			{
				Connection cn = getConnection();

				Object data = modelDataConverter.convert(dataParam, model);

				LOBConversionContext.set(buildGetLobConversionSetting());

				List<Object> resultList = persistenceManager.getByParam(cn, model, data);

				LOBConversionContext.remove();

				if (resultList == null || resultList.isEmpty())
					throw new RecordNotFoundException();

				springModel.addAttribute("data", resultList.get(0));
				springModel.addAttribute("titleOperationMessageKey", "view");
				springModel.addAttribute("readonly", "true");
			}
		}.execute();

		setParseDateFormats(request, springModel);

		return "/data/data_form";
	}

	@RequestMapping("/{schemaId}/{tableName}/selectPropValue")
	public String selectPropValue(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("tableName") String tableName, @RequestParam("propertyPath") final String propertyPath,
			@RequestParam(value = "page", required = false) Integer page,
			@RequestParam(value = "pageSize", required = false) Integer pageSize) throws Throwable
	{
		final Object dataParam = getParamMap(request, "data");

		new VoidExecutor(request, response, springModel, schemaId, tableName, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable
			{
				Connection cn = getConnection();

				Object data = modelDataConverter.convert(dataParam, model);

				PropertyPathInfo propertyPathInfo = ModelUtils.toPropertyPathInfoConcrete(model, propertyPath, data);

				QueryResultMetaInfo queryResultMetaInfo = persistenceManager
						.getQueryPropValueSourceQueryResultMetaInfo(cn, model, data, propertyPathInfo);

				springModel.addAttribute("data", data);
				springModel.addAttribute("propertyPath", propertyPath);
				springModel.addAttribute("conditionSource", getPropertyPathDisplayNames(request, queryResultMetaInfo));
			}
		}.execute();

		return "/data/data_select_prop_value";
	}

	@RequestMapping(value = "/{schemaId}/{tableName}/selectPropValueData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public PagingData<Object> selectPropValueData(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("tableName") String tableName, @RequestParam("propertyPath") final String propertyPath)
			throws Throwable
	{
		final Object dataParam = getParamMap(request, "data");
		final PagingQuery pagingQuery = getPagingQuery(request);

		PagingData<Object> propValueSourcePagingData = new ReturnExecutor<PagingData<Object>>(request, response,
				springModel, schemaId, tableName, true)
		{
			@Override
			protected PagingData<Object> execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable
			{
				Connection cn = getConnection();

				Object data = modelDataConverter.convert(dataParam, model);
				PropertyPathInfo propertyPathInfo = ModelUtils.toPropertyPathInfoConcrete(model, propertyPath, data);

				LOBConversionContext.set(buildQueryLobConversionSetting());

				PagingData<Object> pagingData = persistenceManager.queryPropValueSource(cn, model, data,
						propertyPathInfo, pagingQuery);

				LOBConversionContext.remove();

				return pagingData;
			}
		}.execute();

		return propValueSourcePagingData;
	}

	@RequestMapping("/{schemaId}/{tableName}/addSinglePropValue")
	public String addSinglePropValue(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("tableName") String tableName, @RequestParam("propertyPath") final String propertyPath)
			throws Throwable
	{
		final Object dataParam = getParamMap(request, "data");

		new VoidExecutor(request, response, springModel, schemaId, tableName, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable
			{
				Object data = modelDataConverter.convert(dataParam, model);

				springModel.addAttribute("data", data);
				springModel.addAttribute("propertyPath", propertyPath);
				springModel.addAttribute("titleOperationMessageKey", "add");
				springModel.addAttribute("clientOperation", "true");
				springModel.addAttribute("submitAction", "saveAddSinglePropValue");
			}
		}.execute();

		setParseDateFormats(request, springModel);

		return "/data/data_prop_value_form";
	}

	@RequestMapping(value = "/{schemaId}/{tableName}/saveAddSinglePropValue", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAddSinglePropValue(HttpServletRequest request,
			HttpServletResponse response, org.springframework.ui.Model springModel,
			@PathVariable("schemaId") String schemaId, @PathVariable("tableName") String tableName,
			@RequestParam("propertyPath") final String propertyPath) throws Throwable
	{
		final Object dataParam = getParamMap(request, "data");
		final Object propValueParam = getParamMap(request, "propValue");

		Object propValue = new ReturnExecutor<Object>(request, response, springModel, schemaId, tableName, false)
		{
			@Override
			protected Object execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable
			{
				Connection cn = getConnection();

				Object data = modelDataConverter.convert(dataParam, model);

				PropertyPathInfo propertyPathInfo = ModelUtils.toPropertyPathInfoConcrete(model, propertyPath, data);
				Model propModel = propertyPathInfo.getModelTail();

				Object propValue = modelDataConverter.convert(propValueParam, propModel);

				persistenceManager.insertSinglePropValue(cn, model, data, propertyPathInfo, propValue);

				return propValue;
			}
		}.execute();

		ResponseEntity<OperationMessage> responseEntity = buildOperationMessageSaveSuccessResponseEntity(request);
		responseEntity.getBody().setData(propValue);

		return responseEntity;
	}

	@RequestMapping("/{schemaId}/{tableName}/editSinglePropValue")
	public String editSinglePropValue(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("tableName") String tableName, @RequestParam("propertyPath") final String propertyPath)
			throws Throwable
	{
		final Object dataParam = getParamMap(request, "data");
		final boolean clientOperation = isClientOperation(request);

		new VoidExecutor(request, response, springModel, schemaId, tableName, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable
			{
				Connection cn = getConnection();

				Object data = modelDataConverter.convert(dataParam, model);

				if (!clientOperation)
				{
					PropertyPathInfo propertyPathInfo = ModelUtils.toPropertyPathInfoConcrete(model, propertyPath,
							data);

					LOBConversionContext.set(buildGetLobConversionSetting());

					List<Object> resultList = persistenceManager.getPropValueByParam(cn, model, data, propertyPathInfo);

					if (resultList == null || resultList.isEmpty())
						throw new RecordNotFoundException();

					propertyPathInfo.setValueTail(resultList.get(0));
				}

				springModel.addAttribute("data", data);
				springModel.addAttribute("propertyPath", propertyPath);
				springModel.addAttribute("titleOperationMessageKey", "edit");
				springModel.addAttribute("submitAction", "saveEditSinglePropValue");
			}
		}.execute();

		setParseDateFormats(request, springModel);

		return "/data/data_prop_value_form";
	}

	@RequestMapping(value = "/{schemaId}/{tableName}/saveEditSinglePropValue", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveEditSinglePropValue(HttpServletRequest request,
			HttpServletResponse response, org.springframework.ui.Model springModel,
			@PathVariable("schemaId") String schemaId, @PathVariable("tableName") String tableName,
			@RequestParam("propertyPath") final String propertyPath,
			@RequestParam(value = PARAM_IGNORE_DUPLICATION, required = false) final Boolean ignoreDuplication)
			throws Throwable
	{
		final Object dataParam = getParamMap(request, "data");
		final Object propValueParam = getParamMap(request, "propValue");

		ResponseEntity<OperationMessage> responseEntity = new ReturnExecutor<ResponseEntity<OperationMessage>>(request,
				response, springModel, schemaId, tableName, false)
		{
			@Override
			protected ResponseEntity<OperationMessage> execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable
			{
				Connection cn = getConnection();

				Object data = modelDataConverter.convert(dataParam, model);

				PropertyPathInfo propertyPathInfo = ModelUtils.toPropertyPathInfoConcrete(model, propertyPath, data);
				Model propModel = propertyPathInfo.getModelTail();

				Object propValue = modelDataConverter.convert(propValueParam, propModel);

				int count = persistenceManager.updateSinglePropValue(cn, model, data, propertyPathInfo, propValue);

				checkDuplicateRecord(1, count, ignoreDuplication);

				ResponseEntity<OperationMessage> responseEntity = buildOperationMessageSaveCountResponseEntity(request,
						count);
				responseEntity.getBody().setData(propValue);

				return responseEntity;
			}
		}.execute();

		return responseEntity;
	}

	@RequestMapping("/{schemaId}/{tableName}/viewSinglePropValue")
	public String viewSinglePropValue(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("tableName") String tableName, @RequestParam("propertyPath") final String propertyPath)
			throws Throwable
	{
		final Object dataParam = getParamMap(request, "data");
		final boolean clientOperation = isClientOperation(request);

		new VoidExecutor(request, response, springModel, schemaId, tableName, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable
			{
				Connection cn = getConnection();

				Object data = modelDataConverter.convert(dataParam, model);

				if (!clientOperation)
				{
					PropertyPathInfo propertyPathInfo = ModelUtils.toPropertyPathInfoConcrete(model, propertyPath,
							data);

					LOBConversionContext.set(buildGetLobConversionSetting());

					List<Object> resultList = persistenceManager.getPropValueByParam(cn, model, data, propertyPathInfo);

					if (resultList == null || resultList.isEmpty())
						throw new RecordNotFoundException();

					propertyPathInfo.setValueTail(resultList.get(0));
				}

				springModel.addAttribute("data", data);
				springModel.addAttribute("propertyPath", propertyPath);
				springModel.addAttribute("readonly", "true");
				springModel.addAttribute("titleOperationMessageKey", "view");
			}
		}.execute();

		setParseDateFormats(request, springModel);

		return "/data/data_prop_value_form";
	}

	@RequestMapping("/{schemaId}/{tableName}/editMultiplePropValue")
	public String editMultiplePropValue(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("tableName") String tableName, @RequestParam("propertyPath") final String propertyPath,
			@RequestParam(value = "clientOperation", required = false) final Boolean clientOperation) throws Throwable
	{
		final Object dataParam = getParamMap(request, "data");

		new VoidExecutor(request, response, springModel, schemaId, tableName, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable
			{
				Connection cn = getConnection();

				Object data = modelDataConverter.convert(dataParam, model);

				PropertyPathInfo propertyPathInfo = ModelUtils.toPropertyPathInfoConcrete(model, propertyPath, data);

				QueryResultMetaInfo queryResultMetaInfo = persistenceManager
						.getQueryMultiplePropValueQueryResultMetaInfo(cn, model, data, propertyPathInfo, true);

				springModel.addAttribute("data", data);
				springModel.addAttribute("propertyPath", propertyPath);
				springModel.addAttribute("titleOperationMessageKey", "edit");
				springModel.addAttribute("conditionSource", getPropertyPathDisplayNames(request, queryResultMetaInfo));
			}
		}.execute();

		return "/data/data_prop_value_grid";
	}

	@RequestMapping(value = "/{schemaId}/{tableName}/queryMultiplePropValueData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public PagingData<Object> queryMultiplePropValueData(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("tableName") String tableName, @RequestParam("propertyPath") final String propertyPath)
			throws Throwable
	{
		final Object dataParam = getParamMap(request, "data");

		final PagingQuery pagingQuery = getPagingQuery(request);

		PagingData<Object> pagingPropValue = new ReturnExecutor<PagingData<Object>>(request, response, springModel,
				schemaId, tableName, true)
		{
			@Override
			protected PagingData<Object> execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable
			{
				Connection cn = getConnection();

				Object data = modelDataConverter.convert(dataParam, model);

				PropertyPathInfo propertyPathInfo = ModelUtils.toPropertyPathInfoConcrete(model, propertyPath, data);

				LOBConversionContext.set(buildQueryLobConversionSetting());

				PagingData<Object> pagingData = persistenceManager.queryMultiplePropValue(cn, model, data,
						propertyPathInfo, pagingQuery, true);

				LOBConversionContext.remove();

				return pagingData;
			}
		}.execute();

		return pagingPropValue;
	}

	@RequestMapping("/{schemaId}/{tableName}/viewMultiplePropValue")
	public String viewMultiplePropValue(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("tableName") String tableName, @RequestParam("propertyPath") final String propertyPath)
			throws Throwable
	{
		final Object dataParam = getParamMap(request, "data");

		new VoidExecutor(request, response, springModel, schemaId, tableName, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable
			{
				Connection cn = getConnection();

				Object data = modelDataConverter.convert(dataParam, model);

				PropertyPathInfo propertyPathInfo = ModelUtils.toPropertyPathInfoConcrete(model, propertyPath, data);

				QueryResultMetaInfo queryResultMetaInfo = persistenceManager
						.getQueryMultiplePropValueQueryResultMetaInfo(cn, model, data, propertyPathInfo, true);

				springModel.addAttribute("data", data);
				springModel.addAttribute("propertyPath", propertyPath);
				springModel.addAttribute("readonly", "true");
				springModel.addAttribute("titleOperationMessageKey", "view");
				springModel.addAttribute("conditionSource", getPropertyPathDisplayNames(request, queryResultMetaInfo));
			}
		}.execute();

		return "/data/data_prop_value_grid";
	}

	@RequestMapping("/{schemaId}/{tableName}/addMultiplePropValueElement")
	public String addMultiplePropValueElement(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("tableName") String tableName, @RequestParam("propertyPath") final String propertyPath)
			throws Throwable
	{
		final Object dataParam = getParamMap(request, "data");

		new VoidExecutor(request, response, springModel, schemaId, tableName, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable
			{
				Object data = modelDataConverter.convert(dataParam, model);

				springModel.addAttribute("data", data);
				springModel.addAttribute("propertyPath", propertyPath);
				springModel.addAttribute("titleOperationMessageKey", "add");
				springModel.addAttribute("clientOperation", "true");
				springModel.addAttribute("submitAction", "saveAddMultiplePropValueElement");
			}
		}.execute();

		setParseDateFormats(request, springModel);

		return "/data/data_prop_value_form";
	}

	@RequestMapping(value = "/{schemaId}/{tableName}/saveAddMultiplePropValueElement", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAddMultiplePropValueElement(HttpServletRequest request,
			HttpServletResponse response, org.springframework.ui.Model springModel,
			@PathVariable("schemaId") String schemaId, @PathVariable("tableName") String tableName,
			@RequestParam("propertyPath") final String propertyPath,
			@RequestParam(value = "batchCount", required = false) Integer batchCount,
			@RequestParam(value = "batchHandleErrorMode", required = false) BatchHandleErrorMode batchHandleErrorMode)
			throws Throwable
	{
		if (batchCount != null && batchCount >= 0)
			return saveAddMultiplePropValueElementBatch(request, response, springModel, schemaId, tableName,
					propertyPath, batchCount, batchHandleErrorMode);
		else
			return saveAddMultiplePropValueElementSingle(request, response, springModel, schemaId, tableName,
					propertyPath);
	}

	protected ResponseEntity<OperationMessage> saveAddMultiplePropValueElementSingle(HttpServletRequest request,
			HttpServletResponse response, org.springframework.ui.Model springModel, String schemaId, String tableName,
			final String propertyPath) throws Throwable
	{
		final Object dataParam = getParamMap(request, "data");
		final Object propValueElementParam = getParamMap(request, "propValue");

		Object propValueElement = new ReturnExecutor<Object>(request, response, springModel, schemaId, tableName, false)
		{
			@Override
			protected Object execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable
			{
				Connection cn = getConnection();

				Object data = modelDataConverter.convert(dataParam, model);
				PropertyPathInfo propertyPathInfo = ModelUtils.toPropertyPathInfoConcrete(model, propertyPath, data);
				Object propValueElement = modelDataConverter.convert(propValueElementParam,
						propertyPathInfo.getModelTail());

				persistenceManager.insertMultiplePropValueElement(cn, model, data, propertyPathInfo,
						new Object[] { propValueElement });

				return propValueElement;
			}
		}.execute();

		ResponseEntity<OperationMessage> responseEntity = buildOperationMessageSaveSuccessResponseEntity(request);
		responseEntity.getBody().setData(propValueElement);

		return responseEntity;
	}

	protected ResponseEntity<OperationMessage> saveAddMultiplePropValueElementBatch(HttpServletRequest request,
			HttpServletResponse response, org.springframework.ui.Model springModel, String schemaId, String tableName,
			final String propertyPath, int batchCount, BatchHandleErrorMode batchHandleErrorMode) throws Throwable
	{
		final Object dataParam = getParamMap(request, "data");
		final Object propValueElementParam = getParamMap(request, "propValue");

		ResponseEntity<OperationMessage> batchResponseEntity = new BatchReturnExecutor(request, response, springModel,
				schemaId, tableName, batchCount, batchHandleErrorMode)
		{
			@Override
			protected void doBatchUnit(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Model model, Connection cn,
					Dialect dialect, String table, ExpressionEvaluationContext context) throws Throwable
			{
				Object data = modelDataConverter.convert(dataParam, model);
				PropertyPathInfo propertyPathInfo = ModelUtils.toPropertyPathInfoConcrete(model, propertyPath, data);
				Object propValueElement = modelDataConverter.convert(propValueElementParam,
						propertyPathInfo.getModelTail());

				persistenceManager.insertMultiplePropValueElement(cn, dialect, model, data, propertyPathInfo,
						propValueElement, context);
			}
		}.execute();

		return batchResponseEntity;
	}

	@RequestMapping(value = "/{schemaId}/{tableName}/saveAddMultiplePropValueElements", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAddMultiplePropValueElements(HttpServletRequest request,
			HttpServletResponse response, org.springframework.ui.Model springModel,
			@PathVariable("schemaId") String schemaId, @PathVariable("tableName") String tableName,
			@RequestParam("propertyPath") final String propertyPath) throws Throwable
	{
		final Object dataParam = getParamMap(request, "data");
		final Object propValueElementsParam = getParamMap(request, "propValueElements");

		int count = new ReturnExecutor<Integer>(request, response, springModel, schemaId, tableName, false)
		{
			@Override
			protected Integer execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable
			{
				Connection cn = getConnection();

				Object data = modelDataConverter.convert(dataParam, model);
				PropertyPathInfo propertyPathInfo = ModelUtils.toPropertyPathInfoConcrete(model, propertyPath, data);
				Object[] propValueElements = modelDataConverter.convertToArray(propValueElementsParam,
						propertyPathInfo.getModelTail());

				persistenceManager.insertMultiplePropValueElement(cn, model, data, propertyPathInfo, propValueElements);

				return 1;
			}
		}.execute();

		ResponseEntity<OperationMessage> responseEntity = buildOperationMessageSaveSuccessResponseEntity(request);
		responseEntity.getBody().setData(count);

		return responseEntity;
	}

	@RequestMapping("/{schemaId}/{tableName}/editMultiplePropValueElement")
	public String editMultiplePropValueElement(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("tableName") String tableName, @RequestParam("propertyPath") final String propertyPath)
			throws Throwable
	{
		final Object dataParam = getParamMap(request, "data");
		final boolean clientOperation = isClientOperation(request);

		new VoidExecutor(request, response, springModel, schemaId, tableName, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable
			{
				Connection cn = getConnection();

				Object data = modelDataConverter.convert(dataParam, model);

				if (!clientOperation)
				{
					PropertyPathInfo propertyPathInfo = ModelUtils.toPropertyPathInfoConcrete(model, propertyPath,
							data);

					LOBConversionContext.set(buildGetLobConversionSetting());

					List<Object> resultList = persistenceManager.getMultiplePropValueElementByParam(cn, model, data,
							propertyPathInfo, propertyPathInfo.getValueTail());

					if (resultList == null || resultList.isEmpty())
						throw new RecordNotFoundException();

					propertyPathInfo.setValueTail(resultList.get(0));
				}

				springModel.addAttribute("data", data);
				springModel.addAttribute("propertyPath", propertyPath);
				springModel.addAttribute("titleOperationMessageKey", "edit");
				springModel.addAttribute("submitAction", "saveEditMultiplePropValueElement");
			}
		}.execute();

		setParseDateFormats(request, springModel);

		return "/data/data_prop_value_form";
	}

	@RequestMapping(value = "/{schemaId}/{tableName}/saveEditMultiplePropValueElement", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveEditMultiplePropValueElement(HttpServletRequest request,
			HttpServletResponse response, org.springframework.ui.Model springModel,
			@PathVariable("schemaId") String schemaId, @PathVariable("tableName") String tableName,
			@RequestParam("propertyPath") final String propertyPath,
			@RequestParam(value = PARAM_IGNORE_DUPLICATION, required = false) final Boolean ignoreDuplication)
			throws Throwable
	{
		final Object dataParam = getParamMap(request, "data");
		final Object propValueElementParam = getParamMap(request, "propValue");

		ResponseEntity<OperationMessage> responseEntity = new ReturnExecutor<ResponseEntity<OperationMessage>>(request,
				response, springModel, schemaId, tableName, false)
		{
			@Override
			protected ResponseEntity<OperationMessage> execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable
			{
				Connection cn = getConnection();

				Object data = modelDataConverter.convert(dataParam, model);
				PropertyPathInfo propertyPathInfo = ModelUtils.toPropertyPathInfoConcrete(model, propertyPath, data);
				Object propValueElement = modelDataConverter.convert(propValueElementParam,
						propertyPathInfo.getModelTail());

				int count = persistenceManager.updateMultiplePropValueElement(cn, model, data, propertyPathInfo,
						propValueElement);

				checkDuplicateRecord(1, count, ignoreDuplication);

				ResponseEntity<OperationMessage> responseEntity = buildOperationMessageSaveCountResponseEntity(request,
						count);
				responseEntity.getBody().setData(propValueElement);

				return responseEntity;
			}
		}.execute();

		return responseEntity;
	}

	@RequestMapping(value = "/{schemaId}/{tableName}/deleteMultiplePropValueElements", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> deleteMultiplePropValueElements(HttpServletRequest request,
			HttpServletResponse response, org.springframework.ui.Model springModel,
			@PathVariable("schemaId") String schemaId, @PathVariable("tableName") String tableName,
			@RequestParam("propertyPath") final String propertyPath,
			@RequestParam(value = PARAM_IGNORE_DUPLICATION, required = false) final Boolean ignoreDuplication)
			throws Throwable
	{
		final Object dataParam = getParamMap(request, "data");
		final Object propValueElementsParam = getParamMap(request, "propValueElements");

		ResponseEntity<OperationMessage> responseEntity = new ReturnExecutor<ResponseEntity<OperationMessage>>(request,
				response, springModel, schemaId, tableName, false)
		{
			@Override
			protected ResponseEntity<OperationMessage> execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable
			{
				Connection cn = getConnection();

				Object data = modelDataConverter.convert(dataParam, model);
				PropertyPathInfo propertyPathInfo = ModelUtils.toPropertyPathInfoConcrete(model, propertyPath, data);
				Object[] propValueElements = modelDataConverter.convertToArray(propValueElementsParam,
						propertyPathInfo.getModelTail());

				int count = persistenceManager.deleteMultiplePropValueElement(cn, model, data, propertyPathInfo,
						propValueElements);

				checkDuplicateRecord(propValueElements.length, count, ignoreDuplication);

				ResponseEntity<OperationMessage> responseEntity = buildOperationMessageDeleteCountResponseEntity(
						request, count);
				responseEntity.getBody().setData(count);

				return responseEntity;
			}
		}.execute();

		return responseEntity;
	}

	@RequestMapping("/{schemaId}/{tableName}/viewMultiplePropValueElement")
	public String viewMultiplePropValueElement(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("tableName") String tableName, @RequestParam("propertyPath") final String propertyPath)
			throws Throwable
	{
		final Object dataParam = getParamMap(request, "data");
		final boolean clientOperation = isClientOperation(request);

		new VoidExecutor(request, response, springModel, schemaId, tableName, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable
			{
				Connection cn = getConnection();

				Object data = modelDataConverter.convert(dataParam, model);

				if (!clientOperation)
				{
					PropertyPathInfo propertyPathInfo = ModelUtils.toPropertyPathInfoConcrete(model, propertyPath,
							data);

					LOBConversionContext.set(buildGetLobConversionSetting());

					List<Object> resultList = persistenceManager.getMultiplePropValueElementByParam(cn, model, data,
							propertyPathInfo, propertyPathInfo.getValueTail());

					if (resultList == null || resultList.isEmpty())
						throw new RecordNotFoundException();

					propertyPathInfo.setValueTail(resultList.get(0));
				}

				springModel.addAttribute("data", data);
				springModel.addAttribute("propertyPath", propertyPath);
				springModel.addAttribute("readonly", "true");
				springModel.addAttribute("titleOperationMessageKey", "view");
			}
		}.execute();

		setParseDateFormats(request, springModel);

		return "/data/data_prop_value_form";
	}

	@RequestMapping(value = "/{schemaId}/{tableName}/downloadSinglePropertyValueFile")
	public void downloadSinglePropertyValueFile(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("tableName") String tableName, @RequestParam("propertyPath") final String propertyPath)
			throws Throwable
	{
		final Object dataParam = getParamMap(request, "data");

		Object[] propValueInfo = new ReturnExecutor<Object[]>(request, response, springModel, schemaId, tableName, true)
		{
			@Override
			protected Object[] execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable
			{
				Connection cn = getConnection();

				Object data = modelDataConverter.convert(dataParam, model);
				PropertyPathInfo propertyPathInfo = ModelUtils.toPropertyPathInfoConcrete(model, propertyPath, data);

				List<Object> resultList = persistenceManager.getPropValueByParam(cn, model, data, propertyPathInfo);

				return new Object[] { resultList == null || resultList.isEmpty() ? null : resultList.get(0),
						propertyPathInfo.getPropertyTail().getName() };
			}
		}.execute();

		Object propValue = propValueInfo[0];
		String propValueFileName = (String) propValueInfo[1];

		response.setCharacterEncoding("utf-8");
		response.setHeader("Content-Disposition", "attachment; filename=" + propValueFileName + "");

		InputStream in = null;
		OutputStream out = null;

		try
		{
			if (propValue == null)
			{

			}
			else
			{
				if (propValue instanceof File)
				{
					in = new FileInputStream((File) propValue);
				}
				else if (propValue instanceof byte[])
				{
					in = new ByteArrayInputStream((byte[]) propValue);
				}
				else
					throw new IllegalArgumentException(
							"The property value [" + propertyPath + "] of [" + tableName + "] is not download-able");

				out = response.getOutputStream();

				byte[] buffer = new byte[1024];

				int readLen = 0;
				while ((readLen = in.read(buffer)) > 0)
					out.write(buffer, 0, readLen);
			}
		}
		finally
		{
			close(out);
			close(in);
		}
	}

	@RequestMapping(value = "/file/upload", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public FileInfo upload(HttpServletRequest request, @RequestParam("file") MultipartFile multipartFile)
			throws Throwable
	{
		File file = FileUtils.generateUniqueFile(this.blobFileManagerDirectory);

		multipartFile.transferTo(file);

		FileInfo fileInfo = new FileInfo(file.getName(), file.length());

		return fileInfo;
	}

	@RequestMapping(value = "/file/download")
	public void download(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("file") String fileName) throws Throwable
	{
		response.setCharacterEncoding("utf-8");
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName + "");

		OutputStream out = null;

		try
		{
			out = response.getOutputStream();

			File file = IOUtil.getFile(this.blobFileManagerDirectory, fileName);
			IOUtil.write(file, out);
		}
		finally
		{
			IOUtil.close(out);
		}
	}

	@RequestMapping(value = "/file/delete", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public FileInfo delete(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("file") String fileName) throws Throwable
	{
		File file = IOUtil.getFile(this.blobFileManagerDirectory, fileName);

		FileInfo fileInfo = FileUtils.getFileInfo(file);

		IOUtil.deleteFile(file);

		return fileInfo;
	}

	@Override
	protected String buildMessageCode(String code)
	{
		return super.buildMessageCode("data", code);
	}

	/**
	 * 获取{@linkplain QueryResultMetaInfo}的{@linkplain PropertyPathDisplayName}列表。
	 * 
	 * @param request
	 * @param queryResultMetaInfo
	 * @return
	 */
	protected List<PropertyPathDisplayName> getPropertyPathDisplayNames(HttpServletRequest request,
			QueryResultMetaInfo queryResultMetaInfo)
	{
		List<PropertyPathDisplayName> propertyPathDisplayNames = new ArrayList<PropertyPathDisplayName>();

		List<ColumnPropertyPath> columnPropertyPaths = queryResultMetaInfo.getColumnPropertyPaths();
		for (ColumnPropertyPath columnPropertyPath : columnPropertyPaths)
		{
			PropertyPathDisplayName propertyPathDisplayName = new PropertyPathDisplayName();

			String propertyPath = columnPropertyPath.getPropertyPath();

			propertyPathDisplayName.setPropertyPath(propertyPath);
			propertyPathDisplayName.setDisplayName(ModelUtils.displayName(queryResultMetaInfo.getModel(),
					PropertyPath.valueOf(propertyPath), WebUtils.getLocale(request), ".", false));

			propertyPathDisplayNames.add(propertyPathDisplayName);
		}

		return propertyPathDisplayNames;
	}

	/**
	 * 构建用于查询多条数据的{@linkplain LOBConversionSetting}。
	 * 
	 * @return
	 */
	protected LOBConversionSetting buildQueryLobConversionSetting()
	{
		File blobToFilePlaceholder = new File(this.blobFileManagerDirectory, this.blobToFilePlaceholderName);

		return new LOBConversionSetting(blobToFilePlaceholder, BLOB_TO_BYTES_PLACEHOLDER, 100);
	}

	/**
	 * 构建用于获取单条数据的的{@linkplain LOBConversionSetting}。
	 * 
	 * @return
	 */
	protected LOBConversionSetting buildGetLobConversionSetting()
	{
		File blobToFilePlaceholder = new File(this.blobFileManagerDirectory, this.blobToFilePlaceholderName);

		return new LOBConversionSetting(blobToFilePlaceholder, BLOB_TO_BYTES_PLACEHOLDER);
	}

	/**
	 * 获取{@linkplain Property}。
	 * 
	 * @param model
	 * @param propertyPath
	 * @return
	 */
	protected Property getProperty(Model model, String propertyPath)
	{
		return model.getProperty(propertyPath);
	}

	protected void checkDuplicateRecord(int expectedCount, int actualCount, Boolean ignoreDuplication)
			throws DuplicateRecordException
	{
		if (actualCount > expectedCount && !Boolean.TRUE.equals(ignoreDuplication))
			throw new DuplicateRecordException(expectedCount, actualCount);
	}

	/**
	 * 判断请求是否是客户端操作。
	 * 
	 * @param request
	 * @return
	 */
	protected boolean isClientOperation(HttpServletRequest request)
	{
		String clientOperation = request.getParameter("clientOperation");

		if (clientOperation == null)
			return false;

		return ("true".equals(clientOperation) || "1".equals(clientOperation));
	}

	/**
	 * 获取{@linkplain PagingQuery}。
	 * 
	 * @param request
	 * @return
	 * @throws Throwable
	 */
	protected PagingQuery getPagingQuery(HttpServletRequest request) throws Throwable
	{
		return super.getPagingQuery(request, WebUtils.COOKIE_PAGINATION_SIZE);
	}

	/**
	 * 设置日期格式。
	 * 
	 * @param request
	 * @param springModel
	 */
	protected void setParseDateFormats(HttpServletRequest request, org.springframework.ui.Model springModel)
	{
		Locale locale = WebUtils.getLocale(request);

		springModel.addAttribute("dateFormat", this.dateFormatter.getParsePatternDesc(locale));
		springModel.addAttribute("sqlDateFormat", this.sqlDateFormatter.getParsePatternDesc(locale));
		springModel.addAttribute("sqlTimestampFormat", this.sqlTimestampFormatter.getParsePatternDesc(locale));
		springModel.addAttribute("sqlTimeFormat", this.sqlTimeFormatter.getParsePatternDesc(locale));
	}

	/**
	 * 关闭{@linkplain Closeable}。
	 * 
	 * @param closeable
	 */
	protected void close(Closeable closeable)
	{
		if (closeable == null)
			return;

		try
		{
			closeable.close();
		}
		catch (IOException e)
		{
		}
	}

	/**
	 * 批量执行器。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected abstract class BatchReturnExecutor extends ReturnExecutor<ResponseEntity<OperationMessage>>
	{
		private int batchCount;

		private BatchHandleErrorMode batchHandleErrorMode;

		public BatchReturnExecutor(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, String schemaId, String tableName, int batchCount,
				BatchHandleErrorMode batchHandleErrorMode)
		{
			super(request, response, springModel, schemaId, tableName, false, true);
			this.batchCount = batchCount;
			this.batchHandleErrorMode = batchHandleErrorMode;

			if (this.batchHandleErrorMode == null)
				this.batchHandleErrorMode = BatchHandleErrorMode.IGNORE;
		}

		public int getBatchCount()
		{
			return batchCount;
		}

		protected void setBatchCount(int batchCount)
		{
			this.batchCount = batchCount;
		}

		public BatchHandleErrorMode getBatchHandleErrorMode()
		{
			return batchHandleErrorMode;
		}

		protected void setBatchHandleErrorMode(BatchHandleErrorMode batchHandleErrorMode)
		{
			this.batchHandleErrorMode = batchHandleErrorMode;
		}

		@Override
		protected ResponseEntity<OperationMessage> execute(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable
		{
			Connection cn = getConnection();

			List<BatchUnitResult> batchResults = new ArrayList<BatchUnitResult>();
			int successCount = 0;
			int failCount = 0;

			Dialect dialect = persistenceManager.getDialectSource().getDialect(cn);
			String table = persistenceManager.getTableName(model);
			ExpressionEvaluationContext context = new ExpressionEvaluationContext();

			int index = 0;

			for (; index < this.batchCount; index++)
			{
				try
				{
					doBatchUnit(request, response, springModel, schema, model, cn, dialect, table, context);

					BatchUnitResult batchUnitResult = new BatchUnitResult(index);
					batchResults.add(batchUnitResult);
					successCount++;
				}
				catch (Exception e)
				{
					if (isBatchBreakException(e) && index == 0)
						throw e;
					else
					{
						BatchUnitResult batchUnitResult = new BatchUnitResult(index, e.getMessage());
						batchResults.add(batchUnitResult);
						failCount++;

						if (BatchHandleErrorMode.ROLLBACK.equals(this.batchHandleErrorMode))
						{
							rollbackConnection();
							break;
						}
						else if (BatchHandleErrorMode.ABORT.equals(this.batchHandleErrorMode))
						{
							commitConnection();
							break;
						}
					}
				}
				finally
				{
					context.clearCachedValue();
					context.incrementVariableIndex();
				}
			}

			if (BatchHandleErrorMode.IGNORE.equals(this.batchHandleErrorMode))
				commitConnection();

			ResponseEntity<OperationMessage> responseEntity = null;

			if (successCount == this.batchCount)// 全部成功
			{
				OperationMessage operationMessage = buildOperationMessageSuccess(request,
						buildMessageCode("batchOperationSuccess"), this.batchCount, this.batchCount, 0);
				operationMessage.setDetail(toBatchUnitResultHtml(request, batchResults));

				responseEntity = buildOperationMessageResponseEntity(HttpStatus.OK, operationMessage);
			}
			else if (failCount == this.batchCount)// 全部失败
			{
				OperationMessage operationMessage = buildOperationMessageFail(request,
						buildMessageCode("batchOperationFail"), this.batchCount, 0, this.batchCount);
				operationMessage.setDetail(toBatchUnitResultHtml(request, batchResults));

				responseEntity = buildOperationMessageResponseEntity(HttpStatus.BAD_REQUEST, operationMessage);
			}
			else
			{
				OperationMessage operationMessage = null;

				if (BatchHandleErrorMode.IGNORE.equals(this.batchHandleErrorMode))
				{
					operationMessage = buildOperationMessageFail(request,
							buildMessageCode("batchOperationFinish.ignore"), this.batchCount, successCount, failCount);
				}
				else if (BatchHandleErrorMode.ROLLBACK.equals(this.batchHandleErrorMode))
				{
					operationMessage = buildOperationMessageFail(request,
							buildMessageCode("batchOperationFinish.rollback"), this.batchCount, successCount);
				}
				else if (BatchHandleErrorMode.ABORT.equals(this.batchHandleErrorMode))
				{
					operationMessage = buildOperationMessageFail(request,
							buildMessageCode("batchOperationFinish.abort"), this.batchCount, successCount,
							this.batchCount - successCount);
				}
				else
					throw new UnsupportedOperationException();

				operationMessage.setDetail(toBatchUnitResultHtml(request, batchResults));

				responseEntity = buildOperationMessageResponseEntity(HttpStatus.BAD_REQUEST, operationMessage);
			}

			return responseEntity;
		}

		/**
		 * 判断异常是否是批处理打断异常。
		 * <p>
		 * 如果是，那么批处理没有继续执行的必要。
		 * </p>
		 * 
		 * @param e
		 * @return
		 */
		protected boolean isBatchBreakException(Exception e)
		{
			if (e instanceof ConverterException)
				return true;

			// 变量表达式语法错误
			if (e instanceof VariableExpressionSyntaxErrorException)
				return true;

			// SQL语法错误
			if (e instanceof SqlExpressionSyntaxErrorException)
				return true;

			return false;
		}

		protected String toBatchUnitResultHtml(HttpServletRequest request, List<BatchUnitResult> batchUnitResults)
		{
			StringBuilder sb = new StringBuilder();

			String mcs = buildMessageCode("batchUnitResult.successHtml");
			String mcf = buildMessageCode("batchUnitResult.failHtml");

			for (BatchUnitResult batchUnitResult : batchUnitResults)
			{
				if (batchUnitResult.isSuccess())
					sb.append(getMessage(request, mcs, batchUnitResult.getIndex()));
				else
					sb.append(getMessage(request, mcf, batchUnitResult.getIndex(), batchUnitResult.getErrorMessage()));
			}

			return sb.toString();
		}

		/**
		 * 执行批处理单元。
		 * 
		 * @param request
		 * @param response
		 * @param springModel
		 * @param schema
		 * @param model
		 * @param cn
		 * @param dialect
		 * @param table
		 * @param context
		 * @throws Throwable
		 */
		protected abstract void doBatchUnit(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, Schema schema, Model model, Connection cn, Dialect dialect,
				String table, ExpressionEvaluationContext context) throws Throwable;
	}

	/**
	 * 批量操作单元执行结果。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static class BatchUnitResult implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private int index;

		/** 当操作出错时的错误消息 */
		private String errorMessage;

		public BatchUnitResult(int index)
		{
			super();
			this.index = index;
		}

		public BatchUnitResult(int index, String errorMessage)
		{
			super();
			this.index = index;
			this.errorMessage = errorMessage;
		}

		public int getIndex()
		{
			return index;
		}

		protected void setIndex(int index)
		{
			this.index = index;
		}

		/**
		 * 操作是否成功。
		 * 
		 * @return
		 */
		public boolean isSuccess()
		{
			return (this.errorMessage == null);
		}

		/**
		 * 操作是否失败。
		 * 
		 * @return
		 */
		public boolean isFail()
		{
			return (this.errorMessage != null);
		}

		public String getErrorMessage()
		{
			return errorMessage;
		}

		protected void setErrorMessage(String errorMessage)
		{
			this.errorMessage = errorMessage;
		}

		@Override
		public String toString()
		{
			return getClass().getSimpleName() + " [index=" + index + ", errorMessage=" + errorMessage + "]";
		}

	}

	/**
	 * 批量出错处理方式。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static enum BatchHandleErrorMode
	{
		/** 忽略 */
		IGNORE,

		/** 终止 */
		ABORT,

		/** 回滚 */
		ROLLBACK
	}
}
