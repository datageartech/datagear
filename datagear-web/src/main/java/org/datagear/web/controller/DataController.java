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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
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
import org.datagear.util.FileInfo;
import org.datagear.util.FileUtil;
import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;
import org.datagear.web.format.DateFormatter;
import org.datagear.web.format.SqlDateFormatter;
import org.datagear.web.format.SqlTimeFormatter;
import org.datagear.web.format.SqlTimestampFormatter;
import org.datagear.web.json.jackson.ObjectMapperBuilder;
import org.datagear.web.util.OperationMessage;
import org.datagear.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
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
		this._objectMapper = this.objectMapperBuilder.std().build();
		this._objectMapperForBigNumberToString = this.objectMapperBuilder.std().bigNumberToString()
				.build();
	}

	@RequestMapping("/{schemaId}/{tableName}/pagingQuery")
	public String pagingQuery(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("tableName") String tableName,
			@RequestParam(value="reloadTable", required = false) Boolean reloadTable) throws Throwable
	{
		final User user = getCurrentUser();

		new VoidSchemaConnTableExecutor(request, response, springModel, schemaId, tableName, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Table table) throws Throwable
			{
				checkReadTableDataPermission(schema, user);

				Dialect dialect = persistenceManager.getDialectSource().getDialect(getConnection());

				springModel.addAttribute(KEY_REQUEST_ACTION, REQUEST_ACTION_QUERY);
				springModel.addAttribute(KEY_SQL_IDENTIFIER_QUOTE, dialect.getIdentifierQuote());
				setReadonlyAction(springModel, table.isReadonly());
				springModel.addAttribute("reloadTable", Boolean.TRUE.equals(reloadTable));
				springModel.addAttribute("queryDefaultLOBRowMapper", buildQueryDefaultLOBRowMapper());
				springModel.addAttribute("keywordQueryColumnCount", dialect.getKeywordQueryColumnCount());
			}
		}.execute();

		return "/data/data_table";
	}
	
	@RequestMapping("/{schemaId}/{tableName}/select")
	public String select(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("tableName") String tableName, @RequestParam(value = "page", required = false) Integer page,
			@RequestParam(value = "pageSize", required = false) Integer pageSize) throws Throwable
	{
		final User user = getCurrentUser();

		new VoidSchemaConnTableExecutor(request, response, springModel, schemaId, tableName, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Table table) throws Throwable
			{
				checkReadTableDataPermission(schema, user);

				Dialect dialect = persistenceManager.getDialectSource().getDialect(getConnection());

				springModel.addAttribute(KEY_REQUEST_ACTION, REQUEST_ACTION_SELECT);
				springModel.addAttribute(KEY_SQL_IDENTIFIER_QUOTE, dialect.getIdentifierQuote());
				springModel.addAttribute("queryDefaultLOBRowMapper", buildQueryDefaultLOBRowMapper());
				springModel.addAttribute("keywordQueryColumnCount", dialect.getKeywordQueryColumnCount());
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
		final User user = getCurrentUser();
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
		final User user = getCurrentUser();
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
		final User user = getCurrentUser();
		new VoidSchemaConnTableExecutor(request, response, springModel, schemaId, tableName, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema, Table table) throws Throwable
			{
				checkEditTableDataPermission(schema, user);

				Row formModel = new Row();
				setFormModel(springModel, formModel, REQUEST_ACTION_ADD, SUBMIT_ACTION_SAVE_ADD);
			}
		}.execute();

		setFormPageAttributes(request, springModel);

		return "/data/data_form";
	}

	@RequestMapping(value = "/{schemaId}/{tableName}/saveAdd", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAdd(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("tableName") String tableName, @RequestBody Map<String, ?> paramData) throws Throwable
	{
		final User user = getCurrentUser();
		
		Row row = convertToRow(paramData);

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

		return optSuccessDataResponseEntity(request, savedRow);
	}

	@RequestMapping("/{schemaId}/{tableName}/edit")
	public String edit(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("tableName") String tableName, @RequestBody Map<String, ?> paramData) throws Throwable
	{
		final User user = getCurrentUser();
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

				setFormModel(springModel, myRow, REQUEST_ACTION_EDIT, SUBMIT_ACTION_SAVE_EDIT);
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
		final User user = getCurrentUser();
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

		return optSuccessDataResponseEntity(request, updatedRow);
	}

	@RequestMapping(value = "/{schemaId}/{tableName}/delete", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> delete(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("tableName") String tableName,
			@RequestParam(value = PARAM_IGNORE_DUPLICATION, required = false) final Boolean ignoreDuplication,
			@RequestBody List<Map<String, ?>> paramData) throws Throwable
	{
		final User user = getCurrentUser();
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

				ResponseEntity<OperationMessage> responseEntity = optDeleteCountSuccessResponseEntity(
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
		final User user = getCurrentUser();
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

				Row formModel = persistenceManager.get(cn, null, table, row, buildConditionSqlParamValueMapper(),
						rowMapper);

				if (formModel == null)
					throw new RecordNotFoundException();

				setFormModel(springModel, formModel, REQUEST_ACTION_VIEW, SUBMIT_ACTION_NONE);
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
		final User user = getCurrentUser();
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
						"data.savessSuccess", expectedUpdateCount, acutalUpdateCount, expectedAddCount,
						actualAddCount, expectedDeleteCount, actualDeleteCount);

				ResponseEntity<OperationMessage> responseEntity = optResponseEntity(HttpStatus.OK,
						operationMessage);

				return responseEntity;
			}
		}.execute();

		return responseEntity;
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/{schemaId}/{tableName}/getColumnValuess", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public List<List<Object>> getColumnValuess(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@PathVariable("tableName") String tableName, @RequestBody Map<String, ?> params) throws Throwable
	{
		final User user = getCurrentUser();
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
		final User user = getCurrentUser();
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
		setDownloadResponseHeader(request, response, columnName);

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
		FileInfo fileInfo = FileInfo.valueOfFile(file.getName(), file.length());

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

		Column[] bcs = table.getBinaryColumns();

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
	 * 设置表单页面属性。
	 * 
	 * @param request
	 * @param springModel
	 */
	protected void setFormPageAttributes(HttpServletRequest request, org.springframework.ui.Model springModel)
	{
		Locale locale = WebUtils.getLocale(request);

		long cm = System.currentTimeMillis();
		java.util.Date date = new java.util.Date(cm); 
		java.sql.Date sqlDate = new java.sql.Date(cm);
		java.sql.Time sqlTime = new java.sql.Time(cm);
		java.sql.Timestamp sqlTimestamp = new java.sql.Timestamp(cm);
		
		springModel.addAttribute("dateFormat", this.dateFormatter.getParsePatternDesc(locale));
		springModel.addAttribute("sqlDateFormat", this.sqlDateFormatter.getParsePatternDesc(locale));
		springModel.addAttribute("sqlTimestampFormat", this.sqlTimestampFormatter.getParsePatternDesc(locale));
		springModel.addAttribute("sqlTimeFormat", this.sqlTimeFormatter.getParsePatternDesc(locale));
		
		springModel.addAttribute("dateFormatExample", this.dateFormatter.print(date, locale));
		springModel.addAttribute("sqlDateFormatExample", this.sqlDateFormatter.print(sqlDate, locale));
		springModel.addAttribute("sqlTimestampFormatExample", this.sqlTimestampFormatter.print(sqlTimestamp,locale, false));
		springModel.addAttribute("sqlTimeFormatExample", this.sqlTimeFormatter.print(sqlTime, locale));
		
		springModel.addAttribute("formDefaultLOBRowMapper", buildFormDefaultLOBRowMapper());
	}
}
