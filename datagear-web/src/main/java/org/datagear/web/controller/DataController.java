/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.web.controller;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import org.datagear.persistence.DialectSource;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.PagingQuery;
import org.datagear.persistence.PersistenceManager;
import org.datagear.persistence.QueryResultMetaInfo;
import org.datagear.persistence.columnconverter.LOBConversionContext;
import org.datagear.persistence.columnconverter.LOBConversionContext.LOBConversionSetting;
import org.datagear.persistence.support.SelectOptions;
import org.datagear.web.OperationMessage;
import org.datagear.web.convert.ClassDataConverter;
import org.datagear.web.convert.ModelDataConverter;
import org.datagear.web.format.DateFormatter;
import org.datagear.web.format.SqlDateFormatter;
import org.datagear.web.format.SqlTimeFormatter;
import org.datagear.web.format.SqlTimestampFormatter;
import org.datagear.web.util.FileUtils;
import org.datagear.web.util.ModelUtils;
import org.datagear.web.util.WebUtils;
import org.datagear.web.vo.FileInfo;
import org.datagear.web.vo.PropertyPathNameLabel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
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

	@Autowired
	private PersistenceManager persistenceManager;

	@Autowired
	private SelectOptions selectOptions;

	@Autowired
	private DialectSource dialectSource;

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

	public DialectSource getDialectSource()
	{
		return dialectSource;
	}

	public void setDialectSource(DialectSource dialectSource)
	{
		this.dialectSource = dialectSource;
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

	@RequestMapping("/{schemaId}/{modelName}/query")
	public String query(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("modelName") String modelName, @RequestParam(value = "page", required = false) Integer page,
			@RequestParam(value = "pageSize", required = false) Integer pageSize) throws Throwable
	{
		new VoidExecutor(request, response, springModel, schemaId, modelName, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable
			{
				Connection cn = getConnection();

				QueryResultMetaInfo queryResultMetaInfo = persistenceManager.getQueryResultMetaInfo(cn, model);

				springModel.addAttribute("conditionSource", getPropertyPathNameLabels(request, queryResultMetaInfo));
			}
		}.execute();

		return "/data/data_grid";
	}

	@RequestMapping("/{schemaId}/{modelName}/queryData")
	@ResponseBody
	public PagingData<Object> queryData(HttpServletRequest request, HttpServletResponse response,
			final org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("modelName") String modelName) throws Throwable
	{
		final PagingQuery pagingQuery = getPagingQuery(request);

		ReturnExecutor<PagingData<Object>> executor = new ReturnExecutor<PagingData<Object>>(request, response,
				springModel, schemaId, modelName, true)
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

	@RequestMapping("/{schemaId}/{modelName}/add")
	public String add(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("modelName") String modelName) throws Throwable
	{
		new VoidExecutor(request, response, springModel, schemaId, modelName, true)
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

	@RequestMapping(value = "/{schemaId}/{modelName}/saveAdd", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAdd(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("modelName") String modelName) throws Throwable
	{
		final Object dataParam = getParamMap(request, "data");

		Object data = new ReturnExecutor<Object>(request, response, springModel, schemaId, modelName, false)
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

	@RequestMapping("/{schemaId}/{modelName}/edit")
	public String edit(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("modelName") String modelName) throws Throwable
	{
		final Object dataParam = getParamObj(request, "data");

		new VoidExecutor(request, response, springModel, schemaId, modelName, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable
			{
				Connection cn = getConnection();

				Object data = modelDataConverter.convert(dataParam, model);

				LOBConversionContext.set(buildGetLobConversionSetting());

				data = persistenceManager.get(cn, model, data);

				LOBConversionContext.remove();

				if (data == null)
					throw new RecordNotFoundException();

				springModel.addAttribute("data", data);
				springModel.addAttribute("titleOperationMessageKey", "edit");
				springModel.addAttribute("clientOperation", "false");
				springModel.addAttribute("submitAction", "saveEdit");
			}
		}.execute();

		setParseDateFormats(request, springModel);

		return "/data/data_form";
	}

	@RequestMapping(value = "/{schemaId}/{modelName}/saveEdit", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveEdit(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("modelName") String modelName) throws Throwable
	{
		final Object originalDataParam = getParamObj(request, "originalData");
		final Object dataParam = getParamMap(request, "data");

		Object data = new ReturnExecutor<Object>(request, response, springModel, schemaId, modelName, false)
		{
			@Override
			protected Object execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable
			{
				Connection cn = getConnection();

				Object originalData = modelDataConverter.convert(originalDataParam, model);
				Object data = modelDataConverter.convert(dataParam, model);

				persistenceManager.update(cn, model, originalData, data, false);

				return data;
			}
		}.execute();

		ResponseEntity<OperationMessage> responseEntity = buildOperationMessageSaveSuccessResponseEntity(request);
		responseEntity.getBody().setData(data);

		return responseEntity;
	}

	@RequestMapping(value = "/{schemaId}/{modelName}/delete", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> delete(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("modelName") String modelName) throws Throwable
	{
		final Object dataParam = getParamObj(request, "data");

		int deleteCount = new ReturnExecutor<Integer>(request, response, springModel, schemaId, modelName, false)
		{
			@Override
			protected Integer execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable
			{
				Connection cn = getConnection();

				Object[] datas = modelDataConverter.convertToArray(dataParam, model);

				int count = persistenceManager.delete(cn, model, datas);

				return count;
			}
		}.execute();

		ResponseEntity<OperationMessage> responseEntity = buildOperationMessageDeleteSuccessResponseEntity(request);
		responseEntity.getBody().setData(deleteCount);

		return responseEntity;
	}

	@RequestMapping("/{schemaId}/{modelName}/view")
	public String view(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("modelName") String modelName) throws Throwable
	{
		final Object dataParam = getParamObj(request, "data");

		new VoidExecutor(request, response, springModel, schemaId, modelName, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable
			{
				Connection cn = getConnection();

				Object data = modelDataConverter.convert(dataParam, model);

				LOBConversionContext.set(buildGetLobConversionSetting());

				data = persistenceManager.get(cn, model, data);

				LOBConversionContext.remove();

				if (data == null)
					throw new RecordNotFoundException();

				springModel.addAttribute("data", data);
				springModel.addAttribute("titleOperationMessageKey", "view");
				springModel.addAttribute("readonly", "true");
			}
		}.execute();

		setParseDateFormats(request, springModel);

		return "/data/data_form";
	}

	@RequestMapping("/{schemaId}/{modelName}/selectPropValue")
	public String selectPropValue(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("modelName") String modelName, @RequestParam("propName") final String propName,
			@RequestParam(value = "page", required = false) Integer page,
			@RequestParam(value = "pageSize", required = false) Integer pageSize) throws Throwable
	{
		final Object dataParam = getParamMap(request, "data");

		new VoidExecutor(request, response, springModel, schemaId, modelName, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable
			{
				Connection cn = getConnection();

				Object data = modelDataConverter.convert(dataParam, model);

				PropertyPathInfo propertyPathInfo = ModelUtils.toPropertyPathInfoConcrete(model, propName, data);

				QueryResultMetaInfo queryResultMetaInfo = persistenceManager
						.getQueryPropValueSourceQueryResultMetaInfo(cn, model, data, propertyPathInfo);

				springModel.addAttribute("data", data);
				springModel.addAttribute("propName", propName);
				springModel.addAttribute("conditionSource", getPropertyPathNameLabels(request, queryResultMetaInfo));
			}
		}.execute();

		return "/data/data_select_prop_value";
	}

	@RequestMapping("/{schemaId}/{modelName}/selectPropValueData")
	@ResponseBody
	public PagingData<Object> selectPropValueData(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("modelName") String modelName, @RequestParam("propName") final String propName)
			throws Throwable
	{
		final Object dataParam = getParamMap(request, "data");
		final PagingQuery pagingQuery = getPagingQuery(request);

		PagingData<Object> propValueSourcePagingData = new ReturnExecutor<PagingData<Object>>(request, response,
				springModel, schemaId, modelName, true)
		{
			@Override
			protected PagingData<Object> execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable
			{
				Connection cn = getConnection();

				Object data = modelDataConverter.convert(dataParam, model);
				PropertyPathInfo propertyPathInfo = ModelUtils.toPropertyPathInfoConcrete(model, propName, data);

				LOBConversionContext.set(buildQueryLobConversionSetting());

				PagingData<Object> pagingData = persistenceManager.queryPropValueSource(cn, model, data,
						propertyPathInfo, pagingQuery);

				LOBConversionContext.remove();

				return pagingData;
			}
		}.execute();

		return propValueSourcePagingData;
	}

	@RequestMapping("/{schemaId}/{modelName}/addSinglePropValue")
	public String addSinglePropValue(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("modelName") String modelName, @RequestParam("propName") final String propName)
			throws Throwable
	{
		final Object dataParam = getParamMap(request, "data");

		new VoidExecutor(request, response, springModel, schemaId, modelName, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable
			{
				Object data = modelDataConverter.convert(dataParam, model);

				springModel.addAttribute("data", data);
				springModel.addAttribute("propName", propName);
				springModel.addAttribute("titleOperationMessageKey", "add");
				springModel.addAttribute("clientOperation", "true");
				springModel.addAttribute("submitAction", "saveAddSinglePropValue");
			}
		}.execute();

		setParseDateFormats(request, springModel);

		return "/data/data_prop_value_form";
	}

	@RequestMapping(value = "/{schemaId}/{modelName}/saveAddSinglePropValue", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAddSinglePropValue(HttpServletRequest request,
			HttpServletResponse response, org.springframework.ui.Model springModel,
			@PathVariable("schemaId") String schemaId, @PathVariable("modelName") String modelName,
			@RequestParam("propName") final String propName) throws Throwable
	{
		final Object dataParam = getParamMap(request, "data");
		final Object propValueParam = getParamMap(request, "propValue");

		Object propValue = new ReturnExecutor<Object>(request, response, springModel, schemaId, modelName, false)
		{
			@Override
			protected Object execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable
			{
				Connection cn = getConnection();

				Object data = modelDataConverter.convert(dataParam, model);

				PropertyPathInfo propertyPathInfo = ModelUtils.toPropertyPathInfoConcrete(model, propName, data);
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

	@RequestMapping("/{schemaId}/{modelName}/editSinglePropValue")
	public String editSinglePropValue(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("modelName") String modelName, @RequestParam("propName") final String propName)
			throws Throwable
	{
		final Object dataParam = getParamMap(request, "data");
		final boolean clientOperation = isClientOperation(request);

		new VoidExecutor(request, response, springModel, schemaId, modelName, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable
			{
				Connection cn = getConnection();

				Object data = modelDataConverter.convert(dataParam, model);

				if (!clientOperation)
				{
					PropertyPathInfo propertyPathInfo = ModelUtils.toPropertyPathInfoConcrete(model, propName, data);

					LOBConversionContext.set(buildGetLobConversionSetting());

					Object propValue = persistenceManager.getPropValue(cn, model, data, propertyPathInfo);

					if (propValue == null)
						throw new RecordNotFoundException();

					propertyPathInfo.setValueTail(propValue);
				}

				springModel.addAttribute("data", data);
				springModel.addAttribute("propName", propName);
				springModel.addAttribute("titleOperationMessageKey", "edit");
				springModel.addAttribute("submitAction", "saveEditSinglePropValue");
			}
		}.execute();

		setParseDateFormats(request, springModel);

		return "/data/data_prop_value_form";
	}

	@RequestMapping(value = "/{schemaId}/{modelName}/saveEditSinglePropValue", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveEditSinglePropValue(HttpServletRequest request,
			HttpServletResponse response, org.springframework.ui.Model springModel,
			@PathVariable("schemaId") String schemaId, @PathVariable("modelName") String modelName,
			@RequestParam("propName") final String propName) throws Throwable
	{
		final Object dataParam = getParamMap(request, "data");
		final Object propValueParam = getParamMap(request, "propValue");

		Object propValue = new ReturnExecutor<Object>(request, response, springModel, schemaId, modelName, false)
		{
			@Override
			protected Object execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable
			{
				Connection cn = getConnection();

				Object data = modelDataConverter.convert(dataParam, model);

				PropertyPathInfo propertyPathInfo = ModelUtils.toPropertyPathInfoConcrete(model, propName, data);
				Model propModel = propertyPathInfo.getModelTail();

				Object propValue = modelDataConverter.convert(propValueParam, propModel);

				persistenceManager.updateSinglePropValue(cn, model, data, propertyPathInfo, propValue);

				return propValue;
			}
		}.execute();

		ResponseEntity<OperationMessage> responseEntity = buildOperationMessageSaveSuccessResponseEntity(request);
		responseEntity.getBody().setData(propValue);

		return responseEntity;
	}

	@RequestMapping("/{schemaId}/{modelName}/viewSinglePropValue")
	public String viewSinglePropValue(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("modelName") String modelName, @RequestParam("propName") final String propName)
			throws Throwable
	{
		final Object dataParam = getParamMap(request, "data");
		final boolean clientOperation = isClientOperation(request);

		new VoidExecutor(request, response, springModel, schemaId, modelName, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable
			{
				Connection cn = getConnection();

				Object data = modelDataConverter.convert(dataParam, model);

				if (!clientOperation)
				{
					PropertyPathInfo propertyPathInfo = ModelUtils.toPropertyPathInfoConcrete(model, propName, data);

					LOBConversionContext.set(buildGetLobConversionSetting());

					Object propValue = persistenceManager.getPropValue(cn, model, data, propertyPathInfo);

					if (propValue == null)
						throw new RecordNotFoundException();

					propertyPathInfo.setValueTail(propValue);
				}

				springModel.addAttribute("data", data);
				springModel.addAttribute("propName", propName);
				springModel.addAttribute("readonly", "true");
				springModel.addAttribute("titleOperationMessageKey", "view");
			}
		}.execute();

		setParseDateFormats(request, springModel);

		return "/data/data_prop_value_form";
	}

	@RequestMapping("/{schemaId}/{modelName}/editMultiplePropValue")
	public String editMultiplePropValue(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("modelName") String modelName, @RequestParam("propName") final String propName,
			@RequestParam(value = "clientOperation", required = false) final Boolean clientOperation) throws Throwable
	{
		final Object dataParam = getParamMap(request, "data");

		new VoidExecutor(request, response, springModel, schemaId, modelName, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable
			{
				Connection cn = getConnection();

				Object data = modelDataConverter.convert(dataParam, model);

				PropertyPathInfo propertyPathInfo = ModelUtils.toPropertyPathInfoConcrete(model, propName, data);

				QueryResultMetaInfo queryResultMetaInfo = persistenceManager
						.getQueryMultiplePropValueQueryResultMetaInfo(cn, model, data, propertyPathInfo, true);

				springModel.addAttribute("data", data);
				springModel.addAttribute("propName", propName);
				springModel.addAttribute("titleOperationMessageKey", "edit");
				springModel.addAttribute("conditionSource", getPropertyPathNameLabels(request, queryResultMetaInfo));
			}
		}.execute();

		return "/data/data_prop_value_grid";
	}

	@RequestMapping("/{schemaId}/{modelName}/queryMultiplePropValueData")
	@ResponseBody
	public PagingData<Object> queryMultiplePropValueData(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("modelName") String modelName, @RequestParam("propName") final String propName)
			throws Throwable
	{
		final Object dataParam = getParamMap(request, "data");

		final PagingQuery pagingQuery = getPagingQuery(request);

		PagingData<Object> pagingPropValue = new ReturnExecutor<PagingData<Object>>(request, response, springModel,
				schemaId, modelName, true)
		{
			@Override
			protected PagingData<Object> execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable
			{
				Connection cn = getConnection();

				Object data = modelDataConverter.convert(dataParam, model);

				PropertyPathInfo propertyPathInfo = ModelUtils.toPropertyPathInfoConcrete(model, propName, data);

				LOBConversionContext.set(buildQueryLobConversionSetting());

				PagingData<Object> pagingData = persistenceManager.queryMultiplePropValue(cn, model, data,
						propertyPathInfo, pagingQuery, true);

				LOBConversionContext.remove();

				return pagingData;
			}
		}.execute();

		return pagingPropValue;
	}

	@RequestMapping("/{schemaId}/{modelName}/viewMultiplePropValue")
	public String viewMultiplePropValue(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("modelName") String modelName, @RequestParam("propName") final String propName)
			throws Throwable
	{
		final Object dataParam = getParamMap(request, "data");

		new VoidExecutor(request, response, springModel, schemaId, modelName, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable
			{
				Connection cn = getConnection();

				Object data = modelDataConverter.convert(dataParam, model);

				PropertyPathInfo propertyPathInfo = ModelUtils.toPropertyPathInfoConcrete(model, propName, data);

				QueryResultMetaInfo queryResultMetaInfo = persistenceManager
						.getQueryMultiplePropValueQueryResultMetaInfo(cn, model, data, propertyPathInfo, true);

				springModel.addAttribute("data", data);
				springModel.addAttribute("propName", propName);
				springModel.addAttribute("readonly", "true");
				springModel.addAttribute("titleOperationMessageKey", "view");
				springModel.addAttribute("conditionSource", getPropertyPathNameLabels(request, queryResultMetaInfo));
			}
		}.execute();

		return "/data/data_prop_value_grid";
	}

	@RequestMapping("/{schemaId}/{modelName}/addMultiplePropValueElement")
	public String addMultiplePropValueElement(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("modelName") String modelName, @RequestParam("propName") final String propName)
			throws Throwable
	{
		final Object dataParam = getParamMap(request, "data");

		new VoidExecutor(request, response, springModel, schemaId, modelName, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable
			{
				Object data = modelDataConverter.convert(dataParam, model);

				springModel.addAttribute("data", data);
				springModel.addAttribute("propName", propName);
				springModel.addAttribute("titleOperationMessageKey", "add");
				springModel.addAttribute("clientOperation", "true");
				springModel.addAttribute("submitAction", "saveAddMultiplePropValueElement");
			}
		}.execute();

		setParseDateFormats(request, springModel);

		return "/data/data_prop_value_form";
	}

	@RequestMapping(value = "/{schemaId}/{modelName}/saveAddMultiplePropValueElement", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAddMultiplePropValueElement(HttpServletRequest request,
			HttpServletResponse response, org.springframework.ui.Model springModel,
			@PathVariable("schemaId") String schemaId, @PathVariable("modelName") String modelName,
			@RequestParam("propName") final String propName) throws Throwable
	{
		final Object dataParam = getParamMap(request, "data");
		final Object propValueElementParam = getParamMap(request, "propValue");

		Object propValueElement = new ReturnExecutor<Object>(request, response, springModel, schemaId, modelName, false)
		{
			@Override
			protected Object execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable
			{
				Connection cn = getConnection();

				Object data = modelDataConverter.convert(dataParam, model);
				PropertyPathInfo propertyPathInfo = ModelUtils.toPropertyPathInfoConcrete(model, propName, data);
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

	@RequestMapping(value = "/{schemaId}/{modelName}/saveAddMultiplePropValueElements", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAddMultiplePropValueElements(HttpServletRequest request,
			HttpServletResponse response, org.springframework.ui.Model springModel,
			@PathVariable("schemaId") String schemaId, @PathVariable("modelName") String modelName,
			@RequestParam("propName") final String propName) throws Throwable
	{
		final Object dataParam = getParamMap(request, "data");
		final Object propValueElementsParam = getParamMap(request, "propValueElements");

		int count = new ReturnExecutor<Integer>(request, response, springModel, schemaId, modelName, false)
		{
			@Override
			protected Integer execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable
			{
				Connection cn = getConnection();

				Object data = modelDataConverter.convert(dataParam, model);
				PropertyPathInfo propertyPathInfo = ModelUtils.toPropertyPathInfoConcrete(model, propName, data);
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

	@RequestMapping("/{schemaId}/{modelName}/editMultiplePropValueElement")
	public String editMultiplePropValueElement(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("modelName") String modelName, @RequestParam("propName") final String propName)
			throws Throwable
	{
		final Object dataParam = getParamMap(request, "data");
		final boolean clientOperation = isClientOperation(request);

		new VoidExecutor(request, response, springModel, schemaId, modelName, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable
			{
				Connection cn = getConnection();

				Object data = modelDataConverter.convert(dataParam, model);

				if (!clientOperation)
				{
					PropertyPathInfo propertyPathInfo = ModelUtils.toPropertyPathInfoConcrete(model, propName, data);

					LOBConversionContext.set(buildGetLobConversionSetting());

					Object propValue = persistenceManager.getMultiplePropValueElement(cn, model, data, propertyPathInfo,
							propertyPathInfo.getValueTail());

					if (propValue == null)
						throw new RecordNotFoundException();

					propertyPathInfo.setValueTail(propValue);
				}

				springModel.addAttribute("data", data);
				springModel.addAttribute("propName", propName);
				springModel.addAttribute("titleOperationMessageKey", "edit");
				springModel.addAttribute("submitAction", "saveEditMultiplePropValueElement");
			}
		}.execute();

		setParseDateFormats(request, springModel);

		return "/data/data_prop_value_form";
	}

	@RequestMapping(value = "/{schemaId}/{modelName}/saveEditMultiplePropValueElement", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveEditMultiplePropValueElement(HttpServletRequest request,
			HttpServletResponse response, org.springframework.ui.Model springModel,
			@PathVariable("schemaId") String schemaId, @PathVariable("modelName") String modelName,
			@RequestParam("propName") final String propName) throws Throwable
	{
		final Object dataParam = getParamMap(request, "data");
		final Object propValueElementParam = getParamMap(request, "propValue");

		Object propValueElement = new ReturnExecutor<Object>(request, response, springModel, schemaId, modelName, false)
		{
			@Override
			protected Object execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable
			{
				Connection cn = getConnection();

				Object data = modelDataConverter.convert(dataParam, model);
				PropertyPathInfo propertyPathInfo = ModelUtils.toPropertyPathInfoConcrete(model, propName, data);
				Object propValueElement = modelDataConverter.convert(propValueElementParam,
						propertyPathInfo.getModelTail());

				persistenceManager.updateMultiplePropValueElement(cn, model, data, propertyPathInfo, propValueElement);

				return propValueElement;
			}
		}.execute();

		ResponseEntity<OperationMessage> responseEntity = buildOperationMessageSaveSuccessResponseEntity(request);
		responseEntity.getBody().setData(propValueElement);

		return responseEntity;
	}

	@RequestMapping(value = "/{schemaId}/{modelName}/deleteMultiplePropValueElements", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> deleteMultiplePropValueElements(HttpServletRequest request,
			HttpServletResponse response, org.springframework.ui.Model springModel,
			@PathVariable("schemaId") String schemaId, @PathVariable("modelName") String modelName,
			@RequestParam("propName") final String propName) throws Throwable
	{
		final Object dataParam = getParamMap(request, "data");
		final Object propValueElementsParam = getParamMap(request, "propValueElements");

		int count = new ReturnExecutor<Integer>(request, response, springModel, schemaId, modelName, false)
		{
			@Override
			protected Integer execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable
			{
				Connection cn = getConnection();

				Object data = modelDataConverter.convert(dataParam, model);
				PropertyPathInfo propertyPathInfo = ModelUtils.toPropertyPathInfoConcrete(model, propName, data);
				Object[] propValueElements = modelDataConverter.convertToArray(propValueElementsParam,
						propertyPathInfo.getModelTail());

				persistenceManager.deleteMultiplePropValueElement(cn, model, data, propertyPathInfo, propValueElements);

				return 1;
			}
		}.execute();

		ResponseEntity<OperationMessage> responseEntity = buildOperationMessageDeleteSuccessResponseEntity(request);
		responseEntity.getBody().setData(count);

		return responseEntity;
	}

	@RequestMapping("/{schemaId}/{modelName}/viewMultiplePropValueElement")
	public String viewMultiplePropValueElement(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("modelName") String modelName, @RequestParam("propName") final String propName)
			throws Throwable
	{
		final Object dataParam = getParamMap(request, "data");
		final boolean clientOperation = isClientOperation(request);

		new VoidExecutor(request, response, springModel, schemaId, modelName, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable
			{
				Connection cn = getConnection();

				Object data = modelDataConverter.convert(dataParam, model);

				if (!clientOperation)
				{
					PropertyPathInfo propertyPathInfo = ModelUtils.toPropertyPathInfoConcrete(model, propName, data);

					LOBConversionContext.set(buildGetLobConversionSetting());

					Object propValue = persistenceManager.getMultiplePropValueElement(cn, model, data, propertyPathInfo,
							propertyPathInfo.getValueTail());

					if (propValue == null)
						throw new RecordNotFoundException();

					propertyPathInfo.setValueTail(propValue);
				}

				springModel.addAttribute("data", data);
				springModel.addAttribute("propName", propName);
				springModel.addAttribute("readonly", "true");
				springModel.addAttribute("titleOperationMessageKey", "view");
			}
		}.execute();

		setParseDateFormats(request, springModel);

		return "/data/data_prop_value_form";
	}

	@RequestMapping(value = "/{schemaId}/{modelName}/downloadSinglePropertyValueFile")
	public void downloadSinglePropertyValueFile(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("modelName") String modelName, @RequestParam("propName") final String propName)
			throws Throwable
	{
		final Object dataParam = getParamMap(request, "data");

		Object[] propValueInfo = new ReturnExecutor<Object[]>(request, response, springModel, schemaId, modelName, true)
		{
			@Override
			protected Object[] execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Model model) throws Throwable
			{
				Connection cn = getConnection();

				Object data = modelDataConverter.convert(dataParam, model);
				PropertyPathInfo propertyPathInfo = ModelUtils.toPropertyPathInfoConcrete(model, propName, data);

				Object propValue = persistenceManager.getPropValue(cn, model, data, propertyPathInfo);

				return new Object[] { propValue, propertyPathInfo.getPropertyTail().getName() };
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
							"The property value [" + propName + "] of [" + modelName + "] is not download-able");

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

	/**
	 * 获取{@linkplain QueryResultMetaInfo}的{@linkplain PropertyPathNameLabel}列表。
	 * 
	 * @param request
	 * @param queryResultMetaInfo
	 * @return
	 */
	protected List<PropertyPathNameLabel> getPropertyPathNameLabels(HttpServletRequest request,
			QueryResultMetaInfo queryResultMetaInfo)
	{
		List<PropertyPathNameLabel> propertyPathNameLabels = new ArrayList<PropertyPathNameLabel>();

		List<ColumnPropertyPath> columnPropertyPaths = queryResultMetaInfo.getColumnPropertyPaths();
		for (ColumnPropertyPath columnPropertyPath : columnPropertyPaths)
		{
			PropertyPathNameLabel propertyPathNameLabel = new PropertyPathNameLabel();

			String propertyPath = columnPropertyPath.getPropertyPath();

			propertyPathNameLabel.setPropertyPath(propertyPath);
			propertyPathNameLabel.setNameLabel(ModelUtils.getNameLabelValuePath(queryResultMetaInfo.getModel(),
					PropertyPath.valueOf(propertyPath), WebUtils.getLocale(request), ".", false));

			propertyPathNameLabels.add(propertyPathNameLabel);
		}

		return propertyPathNameLabels;
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
	 * @param propName
	 * @return
	 */
	protected Property getProperty(Model model, String propName)
	{
		return model.getProperty(propName);
	}

	/**
	 * 设置数据库标识符引用符号。
	 * 
	 * @param cn
	 * @param springModel
	 * @return
	 * @throws Throwable
	 */
	protected String setDbIdentifierQuoteAttribute(Connection cn, org.springframework.ui.Model springModel)
			throws Throwable
	{
		String dbIdentifierQuote = this.dialectSource.getDialect(cn).getIdentifierQuote();

		springModel.addAttribute("dbIdentifierQuote", dbIdentifierQuote);

		return dbIdentifierQuote;
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

		springModel.addAttribute("dateFormat", this.dateFormatter.getParsePattern(locale));
		springModel.addAttribute("sqlDateFormat", this.sqlDateFormatter.getParsePattern(locale));
		springModel.addAttribute("sqlTimestampFormat", this.sqlTimestampFormatter.getParsePattern(locale));
		springModel.addAttribute("sqlTimeFormat", this.sqlTimeFormatter.getParsePattern(locale));
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
}
