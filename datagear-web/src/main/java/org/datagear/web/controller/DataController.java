/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.controller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.Writer;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.management.domain.Schema;
import org.datagear.management.domain.User;
import org.datagear.meta.Column;
import org.datagear.meta.Table;
import org.datagear.persistence.Dialect;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.PagingQuery;
import org.datagear.persistence.PersistenceManager;
import org.datagear.persistence.Query;
import org.datagear.persistence.Row;
import org.datagear.persistence.RowMapper;
import org.datagear.persistence.SqlParamValueMapper;
import org.datagear.persistence.support.ConversionSqlParamValueMapper;
import org.datagear.persistence.support.DefaultLOBRowMapper;
import org.datagear.persistence.support.SqlParamValueSqlExpressionSyntaxException;
import org.datagear.persistence.support.SqlParamValueVariableExpressionSyntaxException;
import org.datagear.persistence.support.expression.ExpressionEvaluationContext;
import org.datagear.util.FileInfo;
import org.datagear.util.FileUtil;
import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;
import org.datagear.web.format.DateFormatter;
import org.datagear.web.format.SqlDateFormatter;
import org.datagear.web.format.SqlTimeFormatter;
import org.datagear.web.format.SqlTimestampFormatter;
import org.datagear.web.freemarker.WriteJsonTemplateDirectiveModel;
import org.datagear.web.json.jackson.ObjectMapperBuilder;
import org.datagear.web.util.OperationMessage;
import org.datagear.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

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

	public static final String KEY_DATA_IS_CLIENT = "dataIsClient";

	public static final String KEY_TITLE_DISPLAY_NAME = "titleDisplayName";

	public static final String KEY_TITLE_DISPLAY_DESC = "titleDisplayDesc";

	public static final String KEY_SQL_IDENTIFIER_QUOTE = "sqlIdentifierQuote";

	@Autowired
	private PersistenceManager persistenceManager;

	@Autowired
	private File tempDirectory;

	@Autowired
	private DateFormatter dateFormatter;

	@Autowired
	private SqlDateFormatter sqlDateFormatter;

	@Autowired
	private SqlTimestampFormatter sqlTimestampFormatter;

	@Autowired
	private SqlTimeFormatter sqlTimeFormatter;

	private ObjectMapperBuilder objectMapperBuilder;

	private ObjectMapper _objectMapper;

	private ObjectMapper _objectMapperForBigNumberToString;

	public DataController()
	{
		super();
	}

	public PersistenceManager getPersistenceManager()
	{
		return persistenceManager;
	}

	public void setPersistenceManager(PersistenceManager persistenceManager)
	{
		this.persistenceManager = persistenceManager;
	}

	public File getTempDirectory()
	{
		return tempDirectory;
	}

	public void setTempDirectory(File tempDirectory)
	{
		this.tempDirectory = tempDirectory;
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

	public ObjectMapperBuilder getObjectMapperBuilder()
	{
		return objectMapperBuilder;
	}

	@Autowired
	public void setObjectMapperBuilder(ObjectMapperBuilder objectMapperBuilder)
	{
		this.objectMapperBuilder = objectMapperBuilder;
		this._objectMapper = this.objectMapperBuilder.build();
		this._objectMapperForBigNumberToString = this.objectMapperBuilder.buildForBigNumberToString();
	}

	@RequestMapping("/{schemaId}/{tableName}/pagingQuery")
	public String pagingQuery(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("tableName") String tableName,
			@RequestParam(value="reloadTable", required = false) Boolean reloadTable) throws Throwable
	{
		final User user = WebUtils.getUser();

		new VoidSchemaConnTableExecutor(request, response, springModel, schemaId, tableName, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Table table) throws Throwable
			{
				checkReadTableDataPermission(schema, user);

				Dialect dialect = persistenceManager.getDialectSource().getDialect(getConnection());

				springModel.addAttribute(KEY_SQL_IDENTIFIER_QUOTE, dialect.getIdentifierQuote());
				springModel.addAttribute("readonly", table.isReadonly());
				springModel.addAttribute("reloadTable", Boolean.TRUE.equals(reloadTable));

				springModel.addAttribute(KEY_REQUEST_ACTION, REQUEST_ACTION_QUERY);
				setGridPageAttributes(request, response, springModel, schema, table, dialect);
			}
		}.execute();

		return "/data/data_table";
	}

	@RequestMapping(value = "/{schemaId}/{tableName}/pagingQueryData", produces = CONTENT_TYPE_JSON)
	public void pagingQueryData(HttpServletRequest request, HttpServletResponse response,
			final org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("tableName") String tableName, @RequestBody(required = false) PagingQuery paramData)
			throws Throwable
	{
		final User user = WebUtils.getUser();
		final PagingQuery pagingQuery = inflatePagingQuery(request, paramData);

		final DefaultLOBRowMapper rowMapper = buildQueryDefaultLOBRowMapper();

		ReturnSchemaConnTableExecutor<PagingData<Row>> executor = new ReturnSchemaConnTableExecutor<PagingData<Row>>(
				request, response, springModel, schemaId, tableName, true)
		{
			@Override
			protected PagingData<Row> execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Table table) throws Throwable
			{
				checkReadTableDataPermission(schema, user);

				PagingData<Row> pagingData = persistenceManager.pagingQuery(getConnection(), null, table, pagingQuery,
						rowMapper);
				return pagingData;
			}
		};

		PagingData<Row> pagingData = executor.execute();

		response.setContentType(CONTENT_TYPE_JSON);
		Writer out = response.getWriter();

		this._objectMapperForBigNumberToString.writeValue(out, pagingData);
	}

	@RequestMapping(value = "/{schemaId}/{tableName}/getQuerySql", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public Map<String, ?> getQuerySql(HttpServletRequest request, HttpServletResponse response,
			final org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("tableName") String tableName, @RequestBody(required = false) Query paramData)
			throws Throwable
	{
		final User user = WebUtils.getUser(request, response);
		final Query query = (paramData == null ? new Query() : paramData);

		String sql = new ReturnSchemaConnTableExecutor<String>(request, response, springModel, schemaId, tableName,
				true)
		{
			@Override
			protected String execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Table table) throws Throwable
			{
				checkReadTableDataPermission(schema, user);

				return persistenceManager.getQuerySql(getConnection(), table, query);
			}
		}.execute();

		Map<String, Object> map = new HashMap<>();
		map.put("query", query);
		map.put("sql", sql);

		return map;
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
				springModel.addAttribute(KEY_DATA_IS_CLIENT, true);
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
			@RequestParam(value = "batchHandleErrorMode", required = false) BatchHandleErrorMode batchHandleErrorMode,
			@RequestBody Map<String, ?> paramData) throws Throwable
	{
		Row row = convertToRow(paramData);

		if (batchCount != null && batchCount >= 0)
			return saveAddBatch(request, response, springModel, schemaId, tableName, row, batchCount,
					batchHandleErrorMode);
		else
			return saveAddSingle(request, response, springModel, schemaId, tableName, row);
	}

	protected ResponseEntity<OperationMessage> saveAddSingle(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, String schemaId, String tableName, Row row) throws Throwable
	{
		final User user = WebUtils.getUser(request, response);

		Row savedRow = new ReturnSchemaConnTableExecutor<Row>(request, response, springModel, schemaId, tableName,
				false)
		{
			@Override
			protected Row execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Table table) throws Throwable
			{
				checkEditTableDataPermission(schema, user);
				return persistenceManager.insert(getConnection(), null, table, row,
						buildSaveSingleSqlParamValueMapper());
			}
		}.execute();

		return optMsgSaveSuccessResponseEntity(request, savedRow);
	}

	protected ResponseEntity<OperationMessage> saveAddBatch(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, String schemaId, String tableName, Row row, int batchCount,
			BatchHandleErrorMode batchHandleErrorMode) throws Throwable
	{
		final User user = WebUtils.getUser(request, response);

		ResponseEntity<OperationMessage> batchResponseEntity = new BatchReturnExecutor(request, response, springModel,
				schemaId, tableName, batchCount, batchHandleErrorMode)
		{
			@Override
			protected void doBatchUnit(HttpServletRequest request, HttpServletResponse response, Model springModel,
					Schema schema, Table table, Connection cn, Dialect dialect,
					ConversionSqlParamValueMapper paramValueMapper) throws Throwable
			{
				checkEditTableDataPermission(schema, user);
				persistenceManager.insert(cn, dialect, table, row, paramValueMapper);
			}
		}.execute();

		return batchResponseEntity;
	}

	@RequestMapping("/{schemaId}/{tableName}/edit")
	public String edit(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("tableName") String tableName, @RequestBody Map<String, ?> paramData) throws Throwable
	{
		final User user = WebUtils.getUser(request, response);
		final Row row = convertToRow(paramData);

		final DefaultLOBRowMapper rowMapper = buildFormDefaultLOBRowMapper();

		new VoidSchemaConnTableExecutor(request, response, springModel, schemaId, tableName, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Table table) throws Throwable
			{
				checkEditTableDataPermission(schema, user);

				Connection cn = getConnection();

				Row myRow = persistenceManager.get(cn, null, table, row, buildConditionSqlParamValueMapper(),
						rowMapper);

				if (myRow == null)
					throw new RecordNotFoundException();

				springModel.addAttribute("data", WriteJsonTemplateDirectiveModel.toWriteJsonTemplateModel(myRow));
				springModel.addAttribute("titleOperationMessageKey", "edit");
				springModel.addAttribute(KEY_TITLE_DISPLAY_NAME, table.getName());
				springModel.addAttribute("submitAction", "saveEdit");
			}
		}.execute();

		setFormPageAttributes(request, springModel);

		return "/data/data_form";
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/{schemaId}/{tableName}/saveEdit", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveEdit(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("tableName") String tableName,
			@RequestParam(value = PARAM_IGNORE_DUPLICATION, required = false) final Boolean ignoreDuplication,
			@RequestBody Map<String, ?> paramData) throws Throwable
	{
		final User user = WebUtils.getUser(request, response);
		final Row originalRow = convertToRow((Map<String, ?>) paramData.get("originalData"));
		final Row updateRow = convertToRow((Map<String, ?>) paramData.get("data"));

		Row updatedRow = new ReturnSchemaConnTableExecutor<Row>(request, response, springModel, schemaId, tableName,
				false)
		{
			@Override
			protected Row execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Table table) throws Throwable
			{
				checkEditTableDataPermission(schema, user);

				Row myUpdateRow = removeBlobPlacehoderValue(table, updateRow);

				Connection cn = getConnection();

				int count = persistenceManager.update(cn, null, table, originalRow, myUpdateRow,
						buildSaveSingleSqlParamValueMapper());

				checkDuplicateRecord(1, count, ignoreDuplication);

				return myUpdateRow;
			}
		}.execute();

		return optMsgSaveSuccessResponseEntity(request, updatedRow);
	}

	@RequestMapping(value = "/{schemaId}/{tableName}/delete", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> delete(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("tableName") String tableName,
			@RequestParam(value = PARAM_IGNORE_DUPLICATION, required = false) final Boolean ignoreDuplication,
			@RequestBody List<Map<String, ?>> paramData) throws Throwable
	{
		final User user = WebUtils.getUser(request, response);
		final Row[] rows = convertToRows(paramData);

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

				int count = persistenceManager.delete(cn, dialect, table, rows, buildConditionSqlParamValueMapper());

				checkDuplicateRecord(rows.length, count, ignoreDuplication);

				ResponseEntity<OperationMessage> responseEntity = optMsgDeleteCountSuccessResponseEntity(
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
			@PathVariable("tableName") String tableName, @RequestBody Map<String, ?> paramData) throws Throwable
	{
		final User user = WebUtils.getUser(request, response);
		final Row row = convertToRow(paramData);

		final DefaultLOBRowMapper rowMapper = buildFormDefaultLOBRowMapper();

		new VoidSchemaConnTableExecutor(request, response, springModel, schemaId, tableName, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Table table) throws Throwable
			{
				checkReadTableDataPermission(schema, user);

				Connection cn = getConnection();

				Row myRow = persistenceManager.get(cn, null, table, row, buildConditionSqlParamValueMapper(),
						rowMapper);

				if (myRow == null)
					throw new RecordNotFoundException();

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
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/{schemaId}/{tableName}/savess", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> savess(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("tableName") String tableName, @RequestBody Map<String, ?> params) throws Throwable
	{
		final User user = WebUtils.getUser(request, response);
		final Row[] updateOriginRows = convertToRows((List<Map<String, ?>>) params.get("updateOrigins"));
		final Row[] updateTargetRows = convertToRows((List<Map<String, ?>>) params.get("updateTargets"));
		final Row[] addRows = convertToRows((List<Map<String, ?>>) params.get("adds"));
		final Row[] deleteRows = convertToRows((List<Map<String, ?>>) params.get("deletes"));

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

				ConversionSqlParamValueMapper paramValueMapper = buildSaveSingleSqlParamValueMapper();

				int acutalUpdateCount = 0, actualAddCount = 0, actualDeleteCount = 0;

				if (updateOriginRows != null && updateOriginRows.length > 0)
				{
					for (int i = 0; i < updateOriginRows.length; i++)
					{
						Row updateRow = updateTargetRows[i];

						int myUpdateCount = persistenceManager.update(cn, dialect, table, updateOriginRows[i],
								updateRow, paramValueMapper);
						acutalUpdateCount += myUpdateCount;
					}
				}

				if (addRows != null && addRows.length > 0)
				{
					for (int i = 0; i < addRows.length; i++)
					{
						persistenceManager.insert(cn, dialect, table, addRows[i], paramValueMapper);
						actualAddCount += 1;
					}
				}

				if (deleteRows != null && deleteRows.length > 0)
				{
					int myDeleteCount = persistenceManager.delete(cn, dialect, table, deleteRows, paramValueMapper);
					actualDeleteCount += myDeleteCount;
				}

				OperationMessage operationMessage = optMsgSuccess(request,
						buildMsgCode("savessSuccess"), expectedUpdateCount, acutalUpdateCount, expectedAddCount,
						actualAddCount, expectedDeleteCount, actualDeleteCount);

				ResponseEntity<OperationMessage> responseEntity = optResponseEntity(HttpStatus.OK,
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

				Dialect dialect = persistenceManager.getDialectSource().getDialect(getConnection());

				springModel.addAttribute(KEY_TITLE_DISPLAY_NAME, table.getName());
				springModel.addAttribute(KEY_SQL_IDENTIFIER_QUOTE, dialect.getIdentifierQuote());
				springModel.addAttribute(KEY_SELECT_OPERATION, true);
				setGridPageAttributes(request, response, springModel, schema, table, dialect);
			}
		}.execute();

		return "/data/data_grid";
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/{schemaId}/{tableName}/getColumnValuess", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public List<List<Object>> getColumnValuess(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("tableName") String tableName, @RequestBody Map<String, ?> params) throws Throwable
	{
		final User user = WebUtils.getUser(request, response);
		final Row[] rows = convertToRows((List<Map<String, ?>>) params.get("datas"));
		final List<List<String>> columnNamess = (List<List<String>>) params.get("columnNamess");

		final ConversionSqlParamValueMapper paramValueMapper = buildConditionSqlParamValueMapper();

		final DefaultLOBRowMapper rowMapper = new DefaultLOBRowMapper();
		rowMapper.setReadActualClobRows(-1);
		rowMapper.setReadActualBinaryRows(0);
		rowMapper.setBinaryEncoder(DefaultLOBRowMapper.BINARY_ENCODER_HEX);

		List<List<Object>> columnValuess = new ReturnSchemaConnTableExecutor<List<List<Object>>>(request, response,
				springModel, schemaId, tableName, true)
		{
			@Override
			protected List<List<Object>> execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Table table) throws Throwable
			{
				checkReadTableDataPermission(schema, user);

				Connection cn = getConnection();
				Dialect dialect = persistenceManager.getDialectSource().getDialect(cn);

				List<List<Object>> columnValuess = new ArrayList<>(rows.length);

				for (int i = 0; i < rows.length; i++)
				{
					Row row = rows[i];
					List<String> columnNames = columnNamess.get(i);
					columnValuess
							.add(loadColumnValues(cn, dialect, table, row, columnNames, paramValueMapper, rowMapper));
				}

				return columnValuess;
			}
		}.execute();

		return columnValuess;
	}

	@RequestMapping(value = "/{schemaId}/{tableName}/downloadColumnValue")
	public void downloadColumnValue(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("tableName") String tableName, @RequestParam("data") String rowJsonStr,
			@RequestParam("columnName") final String columnName) throws Throwable
	{
		final User user = WebUtils.getUser(request, response);
		@SuppressWarnings("unchecked")
		final Row row = convertToRow(this._objectMapper.readValue(rowJsonStr, Map.class));

		final DefaultLOBRowMapper rowMapper = new DefaultLOBRowMapper();
		rowMapper.setReadActualClobRows(0);
		rowMapper.setReadActualBinaryRows(1);
		rowMapper.setBinaryEncoder(DefaultLOBRowMapper.BINARY_ENCODER_NONE);
		rowMapper.setBinaryDirectory(getDataBinaryTmpDirectory());

		Object columnValue = new ReturnSchemaConnTableExecutor<Object>(request, response, springModel, schemaId,
				tableName, true)
		{
			@Override
			protected Object execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Table table) throws Throwable
			{
				checkReadTableDataPermission(schema, user);

				Connection cn = getConnection();

				Row getRow = persistenceManager.get(cn, null, table, row, buildConditionSqlParamValueMapper(),
						rowMapper);
				return (getRow == null ? null : getRow.get(columnName));
			}
		}.execute();

		response.setCharacterEncoding(IOUtil.CHARSET_UTF_8);
		response.setHeader("Content-Disposition",
				"attachment; filename=" + toResponseAttachmentFileName(request, response, columnName));

		InputStream in = null;
		OutputStream out = null;

		try
		{
			out = response.getOutputStream();

			if (!StringUtil.isEmpty(columnValue))
			{
				if (columnValue instanceof String)
					in = IOUtil.getInputStream(rowMapper.getBlobFile((String) columnValue));
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

	@RequestMapping(value = "/uploadFile", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public FileInfo uploadFile(HttpServletRequest request, @RequestParam("file") MultipartFile multipartFile)
			throws Throwable
	{
		File file = FileUtil.generateUniqueFile(buildSaveSingleSqlParamValueMapper().getFilePathValueDirectory());

		multipartFile.transferTo(file);

		FileInfo fileInfo = new FileInfo(file.getName(), file.length());

		return fileInfo;
	}

	@RequestMapping(value = "/downloadFile")
	public void downloadFile(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("file") String fileName) throws Throwable
	{
		response.setCharacterEncoding(RESPONSE_ENCODING);
		response.setHeader("Content-Disposition",
				"attachment; filename=" + toResponseAttachmentFileName(request, response, fileName));

		OutputStream out = null;

		try
		{
			out = response.getOutputStream();

			File file = FileUtil.getFile(buildSaveSingleSqlParamValueMapper().getFilePathValueDirectory(), fileName);

			if (file.exists())
				IOUtil.write(file, out);
		}
		finally
		{
			IOUtil.close(out);
		}
	}

	@RequestMapping(value = "/deleteFile", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public FileInfo deleteFile(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("file") String fileName) throws Throwable
	{
		File file = FileUtil.getFile(buildSaveSingleSqlParamValueMapper().getFilePathValueDirectory(), fileName);

		FileInfo fileInfo = FileUtil.getFileInfo(file);

		FileUtil.deleteFile(file);

		return fileInfo;
	}

	protected Row convertToRow(Map<String, ?> map)
	{
		if (map == null)
			return null;

		return new Row(map);
	}

	protected Row[] convertToRows(List<? extends Map<String, ?>> list)
	{
		if (list == null)
			return null;

		Row[] rows = new Row[list.size()];

		for (int i = 0; i < list.size(); i++)
			rows[i] = convertToRow((list.get(i)));

		return rows;
	}

	protected Row removeBlobPlacehoderValue(Table table, Row row)
	{
		if (row == null || row.isEmpty())
			return row;

		Column[] bcs = Table.getBinaryColumns(table);

		if (StringUtil.isEmpty(bcs))
			return row;

		DefaultLOBRowMapper rowMapper = buildFormDefaultLOBRowMapper();

		Row reRow = new Row();

		for (Map.Entry<String, Object> entry : row.entrySet())
		{
			boolean isBc = false;
			for (Column bc : bcs)
			{
				if (bc.getName().equals(entry.getKey()))
					isBc = true;
			}

			if (isBc && rowMapper.getBinaryPlaceholder().equals(entry.getValue()))
				;
			else
				reRow.put(entry.getKey(), entry.getValue());
		}

		return reRow;
	}

	/**
	 * 获取给定表的多个列值。
	 */
	@SuppressWarnings("unchecked")
	protected List<Object> loadColumnValues(Connection cn, Dialect dialect, Table table, Row row,
			List<String> columnNames, SqlParamValueMapper paramValueMapper, RowMapper rowMapper) throws Throwable
	{
		if (StringUtil.isEmpty(columnNames))
			return Collections.EMPTY_LIST;

		List<Object> columnValues = new ArrayList<>(columnNames.size());

		Row getRow = persistenceManager.get(cn, dialect, table, row, paramValueMapper, rowMapper);

		if (getRow != null)
		{
			for (int i = 0; i < columnNames.size(); i++)
				columnValues.add(getRow.get(columnNames.get(i)));
		}

		return columnValues;
	}

	@Override
	protected String buildMsgCode(String code)
	{
		return super.buildMsgCode("data", code);
	}

	protected DefaultLOBRowMapper buildQueryDefaultLOBRowMapper()
	{
		DefaultLOBRowMapper rowMapper = new DefaultLOBRowMapper();
		rowMapper.setReadActualClobRows(0);
		rowMapper.setReadActualBinaryRows(0);
		rowMapper.setBinaryEncoder(DefaultLOBRowMapper.BINARY_ENCODER_HEX);

		return rowMapper;
	}

	protected DefaultLOBRowMapper buildFormDefaultLOBRowMapper()
	{
		DefaultLOBRowMapper rowMapper = new DefaultLOBRowMapper();
		rowMapper.setReadActualClobRows(1);
		rowMapper.setReadActualBinaryRows(0);
		rowMapper.setBinaryEncoder(DefaultLOBRowMapper.BINARY_ENCODER_HEX);

		return rowMapper;
	}

	protected ConversionSqlParamValueMapper buildSaveSingleSqlParamValueMapper()
	{
		ConversionSqlParamValueMapper mapper = new ConversionSqlParamValueMapper();
		mapper.setConversionService(getConversionService());
		mapper.setFilePathValueDirectory(getDataBinaryTmpDirectory());
		mapper.setEnableSqlExpression(true);
		// 单个保存操作不开启变量表达式，避免不必要的输入转义
		mapper.setEnableVariableExpression(false);

		return mapper;
	}

	protected ConversionSqlParamValueMapper buildSaveBatchSqlParamValueMapper()
	{
		ConversionSqlParamValueMapper mapper = new ConversionSqlParamValueMapper();
		mapper.setConversionService(getConversionService());
		mapper.setFilePathValueDirectory(getDataBinaryTmpDirectory());
		mapper.setEnableSqlExpression(true);
		mapper.setEnableVariableExpression(true);

		return mapper;
	}

	protected ConversionSqlParamValueMapper buildConditionSqlParamValueMapper()
	{
		ConversionSqlParamValueMapper mapper = new ConversionSqlParamValueMapper();
		mapper.setConversionService(getConversionService());
		mapper.setFilePathValueDirectory(getDataBinaryTmpDirectory());

		return mapper;
	}

	protected File getDataBinaryTmpDirectory()
	{
		return FileUtil.getDirectory(this.tempDirectory, "data", true);
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
	protected boolean dataIsClientRequest(HttpServletRequest request)
	{
		return WebUtils.getBooleanValue(request, KEY_DATA_IS_CLIENT, false);
	}

	/**
	 * 设置表格页面属性。
	 * 
	 * @param request
	 * @param springModel
	 */
	protected void setGridPageAttributes(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, Schema schema, Table table, Dialect dialect)
	{
		springModel.addAttribute("queryDefaultLOBRowMapper", buildQueryDefaultLOBRowMapper());
		springModel.addAttribute("keywordQueryColumnCount", dialect.getKeywordQueryColumnCount());

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
		springModel.addAttribute("formDefaultLOBRowMapper", buildFormDefaultLOBRowMapper());

		if (!containsAttributes(request, springModel, KEY_DATA_IS_CLIENT))
		{
			boolean dataIsClient = WebUtils.getBooleanValue(request, KEY_DATA_IS_CLIENT, false);
			springModel.addAttribute(KEY_DATA_IS_CLIENT, dataIsClient);
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

			Dialect dialect = persistenceManager.getDialectSource().getDialect(cn);
			ConversionSqlParamValueMapper paramValueMapper = buildSaveBatchSqlParamValueMapper();
			ExpressionEvaluationContext evaluationContext = paramValueMapper.getExpressionEvaluationContext();

			int index = 0;

			for (; index < this.batchCount; index++)
			{
				try
				{
					doBatchUnit(request, response, springModel, schema, table, cn, dialect, paramValueMapper);

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
					evaluationContext.clearCachedValue();
					evaluationContext.incrementVariableIndex();
				}
			}

			if (BatchHandleErrorMode.IGNORE.equals(this.batchHandleErrorMode))
				commitConnection();

			ResponseEntity<OperationMessage> responseEntity = null;

			if (successCount == this.batchCount)// 全部成功
			{
				OperationMessage operationMessage = optMsgSuccess(request,
						buildMsgCode("batchOperationSuccess"), this.batchCount, this.batchCount, 0);
				operationMessage.setDetail(toBatchUnitResultHtml(request, batchResults));

				responseEntity = optResponseEntity(HttpStatus.OK, operationMessage);
			}
			else if (failCount == this.batchCount)// 全部失败
			{
				OperationMessage operationMessage = optMsgFail(request,
						buildMsgCode("batchOperationFail"), this.batchCount, 0, this.batchCount);
				operationMessage.setDetail(toBatchUnitResultHtml(request, batchResults));

				responseEntity = optResponseEntity(HttpStatus.BAD_REQUEST, operationMessage);
			}
			else
			{
				OperationMessage operationMessage = null;

				if (BatchHandleErrorMode.IGNORE.equals(this.batchHandleErrorMode))
				{
					operationMessage = optMsgFail(request,
							buildMsgCode("batchOperationFinish.ignore"), this.batchCount, successCount, failCount);
				}
				else if (BatchHandleErrorMode.ROLLBACK.equals(this.batchHandleErrorMode))
				{
					operationMessage = optMsgFail(request,
							buildMsgCode("batchOperationFinish.rollback"), this.batchCount, successCount);
				}
				else if (BatchHandleErrorMode.ABORT.equals(this.batchHandleErrorMode))
				{
					operationMessage = optMsgFail(request,
							buildMsgCode("batchOperationFinish.abort"), this.batchCount, successCount,
							this.batchCount - successCount);
				}
				else
					throw new UnsupportedOperationException();

				operationMessage.setDetail(toBatchUnitResultHtml(request, batchResults));

				responseEntity = optResponseEntity(HttpStatus.BAD_REQUEST, operationMessage);
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
			if (e instanceof ConversionException)
				return true;

			// 变量表达式语法错误
			if (e instanceof SqlParamValueVariableExpressionSyntaxException)
				return true;

			// SQL语法错误
			if (e instanceof SqlParamValueSqlExpressionSyntaxException)
				return true;

			return false;
		}

		protected String toBatchUnitResultHtml(HttpServletRequest request, List<BatchUnitResult> batchUnitResults)
		{
			StringBuilder sb = new StringBuilder();

			String mcs = buildMsgCode("batchUnitResult.successHtml");
			String mcf = buildMsgCode("batchUnitResult.failHtml");

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
		 */
		protected abstract void doBatchUnit(HttpServletRequest request, HttpServletResponse response,
				org.springframework.ui.Model springModel, Schema schema, Table table, Connection cn, Dialect dialect,
				ConversionSqlParamValueMapper paramValueMapper) throws Throwable;
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
