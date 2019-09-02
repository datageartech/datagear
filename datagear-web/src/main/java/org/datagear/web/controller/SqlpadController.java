/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.web.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.connection.ConnectionSource;
import org.datagear.dbinfo.ColumnInfo;
import org.datagear.dbinfo.DatabaseInfoResolver;
import org.datagear.dbinfo.TableInfo;
import org.datagear.dbmodel.DatabaseModelResolver;
import org.datagear.dbmodel.ModelSqlSelectService;
import org.datagear.dbmodel.ModelSqlSelectService.ModelSqlResult;
import org.datagear.management.domain.Schema;
import org.datagear.management.domain.User;
import org.datagear.management.service.SchemaService;
import org.datagear.util.IDUtil;
import org.datagear.util.IOUtil;
import org.datagear.util.SqlScriptParser;
import org.datagear.util.SqlScriptParser.SqlStatement;
import org.datagear.web.OperationMessage;
import org.datagear.web.convert.ClassDataConverter;
import org.datagear.web.sqlpad.SqlpadExecutionService;
import org.datagear.web.sqlpad.SqlpadExecutionService.CommitMode;
import org.datagear.web.sqlpad.SqlpadExecutionService.ExceptionHandleMode;
import org.datagear.web.sqlpad.SqlpadExecutionService.SqlCommand;
import org.datagear.web.util.KeywordMatcher;
import org.datagear.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * SQL工作台控制器。
 * 
 * @author datagear@163.com
 *
 */
@Controller
@RequestMapping("/sqlpad")
public class SqlpadController extends AbstractSchemaConnController
{
	public static final int DEFAULT_SQL_RESULTSET_FETCH_SIZE = 20;

	@Autowired
	private ModelSqlSelectService modelSqlSelectService;

	@Autowired
	private DatabaseModelResolver databaseModelResolver;

	@Autowired
	private SqlpadExecutionService sqlpadExecutionService;

	@Autowired
	private DatabaseInfoResolver databaseInfoResolver;

	public SqlpadController()
	{
		super();
	}

	public SqlpadController(MessageSource messageSource, ClassDataConverter classDataConverter,
			SchemaService schemaService, ConnectionSource connectionSource, ModelSqlSelectService modelSqlSelectService,
			DatabaseModelResolver databaseModelResolver, SqlpadExecutionService sqlpadExecutionService,
			DatabaseInfoResolver databaseInfoResolver)
	{
		super(messageSource, classDataConverter, schemaService, connectionSource);
		this.modelSqlSelectService = modelSqlSelectService;
		this.databaseModelResolver = databaseModelResolver;
		this.sqlpadExecutionService = sqlpadExecutionService;
		this.databaseInfoResolver = databaseInfoResolver;
	}

	public ModelSqlSelectService getModelSqlSelectService()
	{
		return modelSqlSelectService;
	}

	public void setModelSqlSelectService(ModelSqlSelectService modelSqlSelectService)
	{
		this.modelSqlSelectService = modelSqlSelectService;
	}

	public DatabaseModelResolver getDatabaseModelResolver()
	{
		return databaseModelResolver;
	}

	public void setDatabaseModelResolver(DatabaseModelResolver databaseModelResolver)
	{
		this.databaseModelResolver = databaseModelResolver;
	}

	public SqlpadExecutionService getSqlpadExecutionService()
	{
		return sqlpadExecutionService;
	}

	public void setSqlpadExecutionService(SqlpadExecutionService sqlpadExecutionService)
	{
		this.sqlpadExecutionService = sqlpadExecutionService;
	}

	public DatabaseInfoResolver getDatabaseInfoResolver()
	{
		return databaseInfoResolver;
	}

	public void setDatabaseInfoResolver(DatabaseInfoResolver databaseInfoResolver)
	{
		this.databaseInfoResolver = databaseInfoResolver;
	}

	@RequestMapping("/{schemaId}")
	public String index(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId) throws Throwable
	{
		final User user = WebUtils.getUser(request, response);

		new VoidSchemaConnExecutor(request, response, springModel, schemaId, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response, Model springModel,
					Schema schema) throws Throwable
			{
				checkReadTableDataPermission(schema, user);
			}
		}.execute();

		String sqlpadId = generateSqlpadId(request, response);
		String sqlpadChannelId = this.sqlpadExecutionService.getSqlpadChannelId(sqlpadId);

		springModel.addAttribute("sqlpadId", sqlpadId);
		springModel.addAttribute("sqlpadChannelId", sqlpadChannelId);
		springModel.addAttribute("sqlResultFullLoadingLobMaxRow", this.modelSqlSelectService.getFullLoadingLobMaxRow());

		return "/sqlpad/sqlpad";
	}

	@RequestMapping(value = "/{schemaId}/execute", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> executeSql(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@RequestParam("sqlpadId") String sqlpadId, @RequestParam("sql") String sql,
			@RequestParam(value = "sqlStartRow", required = false) Integer sqlStartRow,
			@RequestParam(value = "sqlStartColumn", required = false) Integer sqlStartColumn,
			@RequestParam(value = "commitMode", required = false) CommitMode commitMode,
			@RequestParam(value = "exceptionHandleMode", required = false) ExceptionHandleMode exceptionHandleMode,
			@RequestParam(value = "overTimeThreashold", required = false) Integer overTimeThreashold,
			@RequestParam(value = "resultsetFetchSize", required = false) Integer resultsetFetchSize) throws Throwable
	{
		final User user = WebUtils.getUser(request, response);

		Schema schema = getSchemaNotNull(request, response, schemaId);

		checkReadTableDataPermission(schema, user);

		SqlScriptParser sqlScriptParser = new SqlScriptParser(new StringReader(sql));
		if (sqlStartRow != null)
			sqlScriptParser.setContextStartRow(sqlStartRow);
		if (sqlStartColumn != null)
			sqlScriptParser.setContextStartColumn(sqlStartColumn);

		if (commitMode == null)
			commitMode = CommitMode.AUTO;

		if (exceptionHandleMode == null)
			exceptionHandleMode = ExceptionHandleMode.ABORT;

		if (overTimeThreashold == null)
			overTimeThreashold = 10;
		else if (overTimeThreashold < 1)
			overTimeThreashold = 1;
		else if (overTimeThreashold > 60)
			overTimeThreashold = 60;

		if (resultsetFetchSize == null)
			resultsetFetchSize = DEFAULT_SQL_RESULTSET_FETCH_SIZE;

		List<SqlStatement> sqlStatements = sqlScriptParser.parseAll();

		// TODO 处理SQL语句执行权限

		this.sqlpadExecutionService.submit(sqlpadId, schema, sqlStatements, commitMode, exceptionHandleMode,
				overTimeThreashold, resultsetFetchSize, WebUtils.getLocale(request));

		return buildOperationMessageSuccessEmptyResponseEntity();
	}

	@RequestMapping(value = "/{schemaId}/command", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> command(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@RequestParam("sqlpadId") String sqlpadId, @RequestParam("command") SqlCommand sqlCommand) throws Throwable
	{
		this.sqlpadExecutionService.command(sqlpadId, sqlCommand);

		return buildOperationMessageSuccessEmptyResponseEntity();
	}

	@RequestMapping(value = "/{schemaId}/select", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ModelSqlResult select(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@RequestParam("sqlpadId") String sqlpadId, @RequestParam("sql") final String sql,
			@RequestParam(value = "startRow", required = false) Integer startRow,
			@RequestParam(value = "fetchSize", required = false) Integer fetchSize,
			@RequestParam(value = "returnModel", required = false) Boolean returnModel) throws Throwable
	{
		final User user = WebUtils.getUser(request, response);

		if (startRow == null)
			startRow = 1;
		if (fetchSize == null)
			fetchSize = DEFAULT_SQL_RESULTSET_FETCH_SIZE;
		if (returnModel == null)
			returnModel = false;

		if (fetchSize < 1)
			fetchSize = 1;
		if (fetchSize > 1000)
			fetchSize = 1000;

		final int startRowFinal = startRow;
		final int fetchSizeFinal = fetchSize;

		ModelSqlResult modelSqlResult = new ReturnSchemaConnExecutor<ModelSqlResult>(request, response, springModel,
				schemaId, true)
		{
			@Override
			protected ModelSqlResult execute(HttpServletRequest request, HttpServletResponse response,
					Model springModel, Schema schema) throws Throwable
			{
				checkReadTableDataPermission(schema, user);

				ModelSqlResult modelSqlResult = modelSqlSelectService.select(getConnection(), sql, startRowFinal,
						fetchSizeFinal, databaseModelResolver);

				return modelSqlResult;
			}
		}.execute();

		if (!Boolean.TRUE.equals(returnModel))
			modelSqlResult.setModel(null);

		return modelSqlResult;
	}

	@RequestMapping("/{schemaId}/downloadResultField")
	public void downloadResultField(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@RequestParam("sqlpadId") String sqlpadId, @RequestParam("value") String value) throws Throwable
	{
		File directory = this.modelSqlSelectService.getBlobFileManagerDirectory();
		File blobFile = new File(directory, value);

		if (!blobFile.exists())
			throw new FileNotFoundException("BLOB");

		response.setCharacterEncoding("utf-8");
		response.setHeader("Content-Disposition", "attachment; filename=BLOB");

		InputStream in = null;
		OutputStream out = null;

		try
		{
			in = new FileInputStream(blobFile);

			out = response.getOutputStream();

			byte[] buffer = new byte[1024];

			int readLen = 0;
			while ((readLen = in.read(buffer)) > 0)
				out.write(buffer, 0, readLen);
		}
		finally
		{
			IOUtil.close(in);
			IOUtil.close(out);
		}
	}

	@RequestMapping(value = "/{schemaId}/findTableNames", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public List<String> findTableNames(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@RequestParam("sqlpadId") String sqlpadId,
			@RequestParam(value = "keyword", required = false) String keyword) throws Throwable
	{
		final User user = WebUtils.getUser(request, response);

		TableInfo[] tableInfos = new ReturnSchemaConnExecutor<TableInfo[]>(request, response, springModel, schemaId,
				true)
		{
			@Override
			protected TableInfo[] execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema) throws Throwable
			{
				checkReadTableDataPermission(schema, user);

				return getDatabaseInfoResolver().getTableInfos(getConnection());
			}
		}.execute();

		List<TableInfo> tableInfoList = SchemaController.findByKeyword(tableInfos, keyword);
		Collections.sort(tableInfoList, SchemaController.TABLE_INFO_SORT_BY_NAME_COMPARATOR);

		List<String> tableNames = new ArrayList<String>();

		for (TableInfo tableInfo : tableInfoList)
			tableNames.add(tableInfo.getName());

		return tableNames;
	}

	@RequestMapping(value = "/{schemaId}/findColumnNames", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public List<String> findColumnNames(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@RequestParam("sqlpadId") String sqlpadId, @RequestParam("table") final String table,
			@RequestParam(value = "keyword", required = false) String keyword) throws Throwable
	{
		final User user = WebUtils.getUser(request, response);

		ColumnInfo[] columnInfos = new ReturnSchemaConnExecutor<ColumnInfo[]>(request, response, springModel, schemaId,
				true)
		{
			@Override
			protected ColumnInfo[] execute(HttpServletRequest request, HttpServletResponse response,
					org.springframework.ui.Model springModel, Schema schema) throws Throwable
			{
				checkReadTableDataPermission(schema, user);

				return getDatabaseInfoResolver().getColumnInfos(getConnection(), table);
			}
		}.execute();

		List<ColumnInfo> columnInfoList = findByKeyword(columnInfos, keyword);
		Collections.sort(columnInfoList, COLUMNINFO_INFO_SORT_BY_NAME_COMPARATOR);

		List<String> columnNames = new ArrayList<String>();

		for (ColumnInfo columnInfo : columnInfoList)
			columnNames.add(columnInfo.getName());

		return columnNames;
	}

	/**
	 * 根据列名称关键字查询{@linkplain ColumnInfo}列表。
	 * 
	 * @param columnInfos
	 * @param columnNameKeyword
	 * @return
	 */
	public static List<ColumnInfo> findByKeyword(ColumnInfo[] columnInfos, String columnNameKeyword)
	{
		return KeywordMatcher.<ColumnInfo> match(columnInfos, columnNameKeyword,
				new KeywordMatcher.MatchValue<ColumnInfo>()
				{
					@Override
					public String[] get(ColumnInfo t)
					{
						return new String[] { t.getName() };
					}
				});
	}

	public static Comparator<ColumnInfo> COLUMNINFO_INFO_SORT_BY_NAME_COMPARATOR = new Comparator<ColumnInfo>()
	{
		@Override
		public int compare(ColumnInfo o1, ColumnInfo o2)
		{
			return o1.getName().compareTo(o2.getName());
		}
	};

	protected String generateSqlpadId(HttpServletRequest request, HttpServletResponse response)
	{
		return IDUtil.uuid();
	}
}
