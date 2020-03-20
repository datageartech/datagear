/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.controller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.connection.ConnectionSource;
import org.datagear.management.domain.Schema;
import org.datagear.management.domain.User;
import org.datagear.management.service.SchemaService;
import org.datagear.meta.Table;
import org.datagear.meta.resolver.DBMetaResolver;
import org.datagear.persistence.Dialect;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.PagingQuery;
import org.datagear.persistence.PersistenceManager;
import org.datagear.persistence.Row;
import org.datagear.persistence.support.ConversionSqlParamValueMapper;
import org.datagear.persistence.support.DefaultLOBRowMapper;
import org.datagear.persistence.support.expression.ExpressionEvaluationContext;
import org.datagear.persistence.support.expression.SqlExpressionSyntaxErrorException;
import org.datagear.persistence.support.expression.VariableExpressionSyntaxErrorException;
import org.datagear.util.FileInfo;
import org.datagear.util.FileUtil;
import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;
import org.datagear.web.OperationMessage;
import org.datagear.web.convert.ClassDataConverter;
import org.datagear.web.convert.ConverterException;
import org.datagear.web.convert.ModelDataConverter;
import org.datagear.web.format.DateFormatter;
import org.datagear.web.format.SqlDateFormatter;
import org.datagear.web.format.SqlTimeFormatter;
import org.datagear.web.format.SqlTimestampFormatter;
import org.datagear.web.freemarker.WriteJsonTemplateDirectiveModel;
import org.datagear.web.util.TableCache;
import org.datagear.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
public class DataController extends AbstractSchemaConnTableController
{
	public static final String PARAM_IGNORE_DUPLICATION = "ignoreDuplication";

	public static final String PARAM_IS_LOAD_PAGE_DATA = "isLoadPageData";

	public static final String KEY_IS_CLIENT_PAGE_DATA = "isClientPageData";

	public static final String KEY_TITLE_DISPLAY_NAME = "titleDisplayName";

	public static final String KEY_TITLE_DISPLAY_DESC = "titleDisplayDesc";

	public static final String KEY_CONDITION_SOURCE = "conditionSource";

	@Autowired
	private PersistenceManager persistenceManager;

	@Autowired
	private DefaultLOBRowMapper dataGetRowMapper;

	@Autowired
	private DefaultLOBRowMapper dataQueryRowMapper;

	@Autowired
	private ModelDataConverter modelDataConverter;

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
			SchemaService schemaService, ConnectionSource connectionSource, DBMetaResolver dbMetaResolver,
			TableCache tableCache, PersistenceManager persistenceManager, DefaultLOBRowMapper dataGetRowMapper,
			DefaultLOBRowMapper dataQueryRowMapper, ModelDataConverter modelDataConverter, DateFormatter dateFormatter,
			SqlDateFormatter sqlDateFormatter, SqlTimestampFormatter sqlTimestampFormatter,
			SqlTimeFormatter sqlTimeFormatter)
	{
		super(messageSource, classDataConverter, schemaService, connectionSource, dbMetaResolver, tableCache);
		this.persistenceManager = persistenceManager;
		this.dataGetRowMapper = dataGetRowMapper;
		this.dataQueryRowMapper = dataQueryRowMapper;
		this.modelDataConverter = modelDataConverter;
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

	public DefaultLOBRowMapper getDataGetRowMapper()
	{
		return dataGetRowMapper;
	}

	public void setDataGetRowMapper(DefaultLOBRowMapper dataGetRowMapper)
	{
		this.dataGetRowMapper = dataGetRowMapper;
	}

	public DefaultLOBRowMapper getDataQueryRowMapper()
	{
		return dataQueryRowMapper;
	}

	public void setDataQueryRowMapper(DefaultLOBRowMapper dataQueryRowMapper)
	{
		this.dataQueryRowMapper = dataQueryRowMapper;
	}

	public ModelDataConverter getModelDataConverter()
	{
		return modelDataConverter;
	}

	public void setModelDataConverter(ModelDataConverter modelDataConverter)
	{
		this.modelDataConverter = modelDataConverter;
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
		final User user = WebUtils.getUser(request, response);

		new VoidSchemaConnTableExecutor(request, response, springModel, schemaId, tableName, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Table table) throws Throwable
			{
				checkReadTableDataPermission(schema, user);

				springModel.addAttribute(KEY_TITLE_DISPLAY_NAME, table.getName());
				setGridPageAttributes(request, response, springModel, schema, table);
			}
		}.execute();

		return "/data/data_grid";
	}

	@RequestMapping(value = "/{schemaId}/{tableName}/queryData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public PagingData<Row> queryData(HttpServletRequest request, HttpServletResponse response,
			final org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("tableName") String tableName) throws Throwable
	{
		final User user = WebUtils.getUser(request, response);
		final PagingQuery pagingQuery = getPagingQuery(request);

		ReturnSchemaConnTableExecutor<PagingData<Row>> executor = new ReturnSchemaConnTableExecutor<PagingData<Row>>(
				request, response, springModel, schemaId, tableName, true)
		{
			@Override
			protected PagingData<Row> execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Table table) throws Throwable
			{
				checkReadTableDataPermission(schema, user);

				PagingData<Row> pagingData = persistenceManager.pagingQuery(getConnection(), null, table, pagingQuery,
						getDataQueryRowMapper());
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
		final User user = WebUtils.getUser(request, response);
		new VoidSchemaConnTableExecutor(request, response, springModel, schemaId, tableName, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Table table) throws Throwable
			{
				checkEditTableDataPermission(schema, user);

				springModel.addAttribute("titleOperationMessageKey", "add");
				springModel.addAttribute(KEY_TITLE_DISPLAY_NAME, table.getName());
				springModel.addAttribute(KEY_IS_CLIENT_PAGE_DATA, true);
				springModel.addAttribute("submitAction", "saveAdd");
			}
		}.execute();

		setFormPageAttributes(request, springModel);

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
		final User user = WebUtils.getUser(request, response);
		final Row row = paramMapToRow(request, getParamMap(request, "data"));

		new VoidSchemaConnTableExecutor(request, response, springModel, schemaId, tableName, false)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Table table) throws Throwable
			{
				checkEditTableDataPermission(schema, user);

				persistenceManager.insert(getConnection(), null, table, row, buildSqlParamValueMapper());
			}
		}.execute();

		ResponseEntity<OperationMessage> responseEntity = buildOperationMessageSaveSuccessResponseEntity(request);
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
		final User user = WebUtils.getUser(request, response);
		final Row row = paramMapToRow(request, getParamMap(request, "data"));

		ResponseEntity<OperationMessage> batchResponseEntity = new BatchReturnExecutor(request, response, springModel,
				schemaId, tableName, batchCount, batchHandleErrorMode)
		{
			@Override
			protected void doBatchUnit(HttpServletRequest request, HttpServletResponse response, Model springModel,
					Schema schema, Table table, Connection cn, ExpressionEvaluationContext context) throws Throwable
			{
				checkEditTableDataPermission(schema, user);
				persistenceManager.insert(cn, null, table, row, buildSqlParamValueMapper(context));
			}
		}.execute();

		return batchResponseEntity;
	}

	@RequestMapping("/{schemaId}/{tableName}/edit")
	public String edit(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("tableName") String tableName) throws Throwable
	{
		final User user = WebUtils.getUser(request, response);
		final Row row = paramMapToRow(request, getParamMap(request, "data"));
		final boolean isLoadPageData = isLoadPageDataRequest(request);

		new VoidSchemaConnTableExecutor(request, response, springModel, schemaId, tableName, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Table table) throws Throwable
			{
				checkEditTableDataPermission(schema, user);

				Connection cn = getConnection();

				Row myRow = row;

				if (isLoadPageData)
				{
					myRow = persistenceManager.get(cn, null, table, row, buildSqlParamValueMapper(),
							getDataGetRowMapper());

					if (myRow == null)
						throw new RecordNotFoundException();
				}

				springModel.addAttribute("data", WriteJsonTemplateDirectiveModel.toWriteJsonTemplateModel(myRow));
				springModel.addAttribute("titleOperationMessageKey", "edit");
				springModel.addAttribute(KEY_TITLE_DISPLAY_NAME, table.getName());
				springModel.addAttribute("submitAction", "saveEdit");
			}
		}.execute();

		setFormPageAttributes(request, springModel);

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
		final User user = WebUtils.getUser(request, response);
		final Row originalRow = paramMapToRow(request, getParamMap(request, "originalData"));
		final Row updateRow = paramMapToRow(request, getParamMap(request, "data"));

		ResponseEntity<OperationMessage> responseEntity = new ReturnSchemaConnTableExecutor<ResponseEntity<OperationMessage>>(
				request, response, springModel, schemaId, tableName, false)
		{
			@Override
			protected ResponseEntity<OperationMessage> execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Table table) throws Throwable
			{
				checkEditTableDataPermission(schema, user);

				Connection cn = getConnection();

				int count = persistenceManager.update(cn, null, table, originalRow, updateRow,
						buildSqlParamValueMapper());

				checkDuplicateRecord(1, count, ignoreDuplication);

				ResponseEntity<OperationMessage> responseEntity = buildOperationMessageSaveCountResponseEntity(request,
						count);
				responseEntity.getBody().setData(updateRow);

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
		final User user = WebUtils.getUser(request, response);
		final Row[] rows = paramToRows(request, getParamObj(request, "data"));

		ResponseEntity<OperationMessage> responseEntity = new ReturnSchemaConnTableExecutor<ResponseEntity<OperationMessage>>(
				request, response, springModel, schemaId, tableName, false)
		{
			@Override
			protected ResponseEntity<OperationMessage> execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Table table) throws Throwable
			{
				checkEditTableDataPermission(schema, user);

				int count = persistenceManager.delete(getConnection(), table, rows);

				checkDuplicateRecord(rows.length, count, ignoreDuplication);

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
		final User user = WebUtils.getUser(request, response);
		final Row row = paramMapToRow(request, getParamMap(request, "data"));
		final boolean isLoadPageData = isLoadPageDataRequest(request);

		new VoidSchemaConnTableExecutor(request, response, springModel, schemaId, tableName, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Table table) throws Throwable
			{
				checkReadTableDataPermission(schema, user);

				Connection cn = getConnection();

				Row myRow = row;

				if (isLoadPageData)
				{
					myRow = persistenceManager.get(cn, null, table, row, buildSqlParamValueMapper(),
							getDataGetRowMapper());

					if (myRow == null)
						throw new RecordNotFoundException();
				}

				springModel.addAttribute("data", WriteJsonTemplateDirectiveModel.toWriteJsonTemplateModel(myRow));
				springModel.addAttribute("titleOperationMessageKey", "view");
				springModel.addAttribute(KEY_TITLE_DISPLAY_NAME, table.getName());
				springModel.addAttribute("readonly", true);
			}
		}.execute();

		setFormPageAttributes(request, springModel);

		return "/data/data_form";
	}

	/**
	 * 混合保存，包括添加、修改、删除。
	 * 
	 * @param request
	 * @param response
	 * @param springModel
	 * @param schemaId
	 * @param tableName
	 * @return
	 * @throws Throwable
	 */
	@RequestMapping(value = "/{schemaId}/{tableName}/savess", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> savess(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("tableName") String tableName) throws Throwable
	{
		final User user = WebUtils.getUser(request, response);
		final Row[] updateOriginRows = paramToRows(request, getParamObj(request, "updates"));
		final Object updatePropertyNamessParam = getParamMap(request, "updatePropertyNamess");
		final Object updatePropertyValuessParam = getParamMap(request, "updatePropertyValuess");
		final Row[] addRows = paramToRows(request, getParamObj(request, "adds"));
		final Row[] deleteRows = paramToRows(request, getParamObj(request, "deletes"));

		final String[][] updateColumnNamess = (StringUtil.isEmpty(updateOriginRows) ? null
				: getClassDataConverter().convertToArray(updatePropertyNamessParam, String[].class));

		final Object[][] updateColumnValuess = (StringUtil.isEmpty(updateOriginRows) ? null
				: getClassDataConverter().convertToArray(updatePropertyValuessParam, Object[].class));

		ResponseEntity<OperationMessage> responseEntity = new ReturnSchemaConnTableExecutor<ResponseEntity<OperationMessage>>(
				request, response, springModel, schemaId, tableName, false)
		{
			@Override
			protected ResponseEntity<OperationMessage> execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Table table) throws Throwable
			{
				checkEditTableDataPermission(schema, user);

				Connection cn = getConnection();
				Dialect dialect = persistenceManager.getDialectSource().getDialect(cn);

				int expectedUpdateCount = (updateOriginRows == null ? 0 : updateOriginRows.length);
				int expectedAddCount = (addRows == null ? 0 : addRows.length);
				int expectedDeleteCount = (deleteRows == null ? 0 : deleteRows.length);

				if (expectedDeleteCount > 0)
					checkDeleteTableDataPermission(schema, user);

				ConversionSqlParamValueMapper paramValueMapper = buildSqlParamValueMapper();

				int acutalUpdateCount = 0, actualAddCount = 0, actualDeleteCount = 0;

				if (updateOriginRows != null && updateOriginRows.length > 0)
				{
					for (int i = 0; i < updateOriginRows.length; i++)
					{
						String[] updateColumnNames = updateColumnNamess[i];
						Object[] updateColumnValues = updateColumnValuess[i];

						Row updateRow = buildRow(updateColumnNames, updateColumnValues);

						int myUpdateCount = persistenceManager.update(cn, dialect, table, updateOriginRows[i],
								updateRow,
								paramValueMapper);
						acutalUpdateCount += myUpdateCount;
					}
				}

				if (addRows != null && addRows.length > 0)
				{
					for (int i = 0; i < addRows.length; i++)
					{
						int myAddCount = persistenceManager.insert(cn, dialect, table, addRows[i], paramValueMapper);
						actualAddCount += myAddCount;
					}
				}

				if (deleteRows != null && deleteRows.length > 0)
				{
					int myDeleteCount = persistenceManager.delete(cn, dialect, table, deleteRows, paramValueMapper);
					actualDeleteCount += myDeleteCount;
				}

				OperationMessage operationMessage = buildOperationMessageSuccess(request,
						buildMessageCode("savessSuccess"), expectedUpdateCount, acutalUpdateCount, expectedAddCount,
						actualAddCount, expectedDeleteCount, actualDeleteCount);

				ResponseEntity<OperationMessage> responseEntity = buildOperationMessageResponseEntity(HttpStatus.OK,
						operationMessage);

				return responseEntity;
			}
		}.execute();

		return responseEntity;
	}

	@RequestMapping("/{schemaId}/{tableName}/select")
	public String select(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("tableName") String tableName, @RequestParam(value = "page", required = false) Integer page,
			@RequestParam(value = "pageSize", required = false) Integer pageSize) throws Throwable
	{
		final User user = WebUtils.getUser(request, response);

		new VoidSchemaConnTableExecutor(request, response, springModel, schemaId, tableName, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Table table) throws Throwable
			{
				checkReadTableDataPermission(schema, user);

				springModel.addAttribute(KEY_TITLE_DISPLAY_NAME, table.getName());
				setGridPageAttributes(request, response, springModel, schema, table);
			}
		}.execute();

		return "/data/data_grid";
	}

	@RequestMapping(value = "/{schemaId}/{tableName}/getColumnValuess", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public Object[][] getColumnValuess(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("tableName") String tableName) throws Throwable
	{
		final User user = WebUtils.getUser(request, response);
		final Row[] rows = paramToRows(request, getParamMap(request, "datas"));
		Object columnNamessParam = getParamMap(request, "columnNamess");
		final String[][] columnNamess = getClassDataConverter().convertToArray(columnNamessParam, String[].class);

		Object[][] columnValuess = new ReturnSchemaConnTableExecutor<Object[][]>(request, response, springModel,
				schemaId, tableName, true)
		{
			@Override
			protected Object[][] execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Table table) throws Throwable
			{
				checkReadTableDataPermission(schema, user);

				Connection cn = getConnection();

				Object[][] columnValuess = new Object[rows.length][];

				for (int i = 0; i < rows.length; i++)
				{
					Row row = rows[i];
					String[] columnNames = columnNamess[i];
					columnValuess[i] = loadColumnValues(cn, table, row, columnNames);
				}

				return columnValuess;
			}
		}.execute();

		return columnValuess;
	}

	@RequestMapping(value = "/{schemaId}/{tableName}/downloadColumnValue")
	public void downloadColumnValue(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("tableName") String tableName, @RequestParam("columnName") final String columnName)
			throws Throwable
	{
		final User user = WebUtils.getUser(request, response);
		final Row row = paramMapToRow(request, getParamMap(request, "data"));

		Object columnValue = new ReturnSchemaConnTableExecutor<Object>(request, response, springModel, schemaId,
				tableName, true)
		{
			@Override
			protected Object execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Table table) throws Throwable
			{
				checkReadTableDataPermission(schema, user);

				Connection cn = getConnection();

				Row getRow = persistenceManager.get(cn, null, table, row, buildSqlParamValueMapper(),
						getDataGetRowMapper());

				return (getRow == null ? null : getRow.get(columnName));
			}
		}.execute();

		response.setCharacterEncoding("utf-8");
		response.setHeader("Content-Disposition", "attachment; filename=" + columnName + "");

		InputStream in = null;
		OutputStream out = null;

		try
		{
			out = response.getOutputStream();
			
			if (!StringUtil.isEmpty(columnValue))
			{
				if (columnValue instanceof String)
					in = IOUtil.getInputStream(getDataGetRowMapper().getBigBinaryFile((String) columnValue));
				else if (columnValue instanceof byte[])
					in = new ByteArrayInputStream((byte[]) columnValue);
				else
					throw new IllegalArgumentException(
							"Table '" + tableName + "' column '" + columnValue + "' 's value is not downloadable");
				
				IOUtil.write(in, out);
			}
		}
		finally
		{
			IOUtil.close(in);
			IOUtil.close(out);
		}
	}

	@RequestMapping(value = "/file/upload", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public FileInfo fileUpload(HttpServletRequest request, @RequestParam("file") MultipartFile multipartFile)
			throws Throwable
	{
		File file = FileUtil.generateUniqueFile(buildSqlParamValueMapper().getFilePathValueDirectory());

		multipartFile.transferTo(file);

		FileInfo fileInfo = new FileInfo(file.getName(), file.length());

		return fileInfo;
	}

	@RequestMapping(value = "/file/delete", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public FileInfo fileDelete(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("file") String fileName) throws Throwable
	{
		File file = FileUtil.getFile(buildSqlParamValueMapper().getFilePathValueDirectory(), fileName);

		FileInfo fileInfo = FileUtil.getFileInfo(file);

		FileUtil.deleteFile(file);

		return fileInfo;
	}

	protected Row buildRow(String[] names, Object[] values)
	{
		Row row = new Row();

		if (!StringUtil.isEmpty(names))
		{
			for (int i = 0; i < names.length; i++)
				row.put(names[i], values[i]);
		}

		return row;
	}

	/**
	 * 获取给定表的多个列值。
	 * 
	 * @param cn
	 * @param table
	 * @param row
	 * @param columnNames
	 * @return
	 * @throws Throwable
	 */
	protected Object[] loadColumnValues(Connection cn, Table table, Row row, String[] columnNames)
			throws Throwable
	{
		if (columnNames == null || columnNames.length == 0)
			return null;

		Object[] columnValues = new Object[columnNames.length];

		Row getRow = persistenceManager.get(cn, null, table, row, buildSqlParamValueMapper(), getDataGetRowMapper());

		if (getRow != null)
		{
			for (int i = 0; i < columnNames.length; i++)
				columnValues[i] = getRow.get(columnNames[i]);
		}

		return columnValues;
	}

	@Override
	protected String buildMessageCode(String code)
	{
		return super.buildMessageCode("data", code);
	}

	protected Row paramMapToRow(HttpServletRequest request, Map<String, ?> paramMap)
	{
		// TODO
		return new Row();
	}

	protected Row[] paramToRows(HttpServletRequest request, Object param)
	{
		// TODO
		return new Row[0];
	}

	protected ConversionSqlParamValueMapper buildSqlParamValueMapper()
	{
		return buildSqlParamValueMapper(null);
	}

	/**
	 * 
	 * @param evaluationContext
	 *            允许为{@code null}
	 * @return
	 */
	protected ConversionSqlParamValueMapper buildSqlParamValueMapper(ExpressionEvaluationContext evaluationContext)
	{
		if (evaluationContext == null)
			evaluationContext = new ExpressionEvaluationContext();

		ConversionSqlParamValueMapper mapper = new ConversionSqlParamValueMapper();
		// TODO 设置
		// mapper.setConversionService(conversionService);
		// mapper.setFilePathValueDirectory(filePathValueDirectory);
		mapper.setExpressionEvaluationContext(evaluationContext);
		return mapper;
	}

	protected void checkDuplicateRecord(int expectedCount, int actualCount, Boolean ignoreDuplication)
			throws DuplicateRecordException
	{
		if (actualCount > expectedCount && !Boolean.TRUE.equals(ignoreDuplication))
			throw new DuplicateRecordException(expectedCount, actualCount);
	}

	/**
	 * 是否客户端数据请求。
	 * 
	 * @param request
	 * @return
	 */
	protected boolean isClientPageDataRequest(HttpServletRequest request)
	{
		return WebUtils.getBooleanValue(request, KEY_IS_CLIENT_PAGE_DATA, false);
	}

	/**
	 * 是否是加载页面数据请求。
	 * 
	 * @param request
	 * @return
	 */
	protected boolean isLoadPageDataRequest(HttpServletRequest request)
	{
		if (isClientPageDataRequest(request))
			return false;

		return WebUtils.getBooleanValue(request, PARAM_IS_LOAD_PAGE_DATA, true);
	}

	/**
	 * 设置表格页面属性。
	 * 
	 * @param request
	 * @param springModel
	 */
	protected void setGridPageAttributes(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, Schema schema, Table table)
	{
		// 编辑表格需要表单属性
		setFormPageAttributes(request, springModel);
	}

	/**
	 * 设置表单页面属性。
	 * 
	 * @param request
	 * @param springModel
	 */
	protected void setFormPageAttributes(HttpServletRequest request, org.springframework.ui.Model springModel)
	{
		Locale locale = WebUtils.getLocale(request);

		springModel.addAttribute("dateFormat", this.dateFormatter.getParsePatternDesc(locale));
		springModel.addAttribute("sqlDateFormat", this.sqlDateFormatter.getParsePatternDesc(locale));
		springModel.addAttribute("sqlTimestampFormat", this.sqlTimestampFormatter.getParsePatternDesc(locale));
		springModel.addAttribute("sqlTimeFormat", this.sqlTimeFormatter.getParsePatternDesc(locale));
		springModel.addAttribute("queryRowMapper", getDataQueryRowMapper());

		if (!containsAttributes(request, springModel, KEY_IS_CLIENT_PAGE_DATA))
		{
			boolean isClientPageData = WebUtils.getBooleanValue(request, KEY_IS_CLIENT_PAGE_DATA, false);
			springModel.addAttribute(KEY_IS_CLIENT_PAGE_DATA, isClientPageData);
		}

		if (!containsAttributes(request, springModel, "batchSet"))
		{
			boolean batchSet = WebUtils.getBooleanValue(request, "batchSet", false);
			springModel.addAttribute("batchSet", batchSet);
		}

		if (!containsAttributes(request, springModel, "ignorePropertyName"))
		{
			String ignorePropertyName = WebUtils.getStringValue(request, "ignorePropertyName", "");
			springModel.addAttribute("ignorePropertyName", ignorePropertyName);
		}
	}

	protected boolean containsAttributes(HttpServletRequest request, org.springframework.ui.Model springModel,
			String name)
	{
		if (request.getAttribute(name) != null)
			return true;

		if (springModel.containsAttribute(name))
			return true;

		return false;
	}

	/**
	 * 批量执行器。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected abstract class BatchReturnExecutor extends ReturnSchemaConnTableExecutor<ResponseEntity<OperationMessage>>
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
				org.springframework.ui.Model springModel, Schema schema, Table table) throws Throwable
		{
			Connection cn = getConnection();

			List<BatchUnitResult> batchResults = new ArrayList<>();
			int successCount = 0;
			int failCount = 0;

			ExpressionEvaluationContext context = new ExpressionEvaluationContext();

			int index = 0;

			for (; index < this.batchCount; index++)
			{
				try
				{
					doBatchUnit(request, response, springModel, schema, table, cn, context);

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
		 * @param table
		 * @param cn
		 * @param context
		 * @throws Throwable
		 */
		protected abstract void doBatchUnit(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, Schema schema, Table table, Connection cn,
				ExpressionEvaluationContext context) throws Throwable;
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
