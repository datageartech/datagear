/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.web.controller;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.ServerChannel;
import org.datagear.connection.ConnectionSource;
import org.datagear.connection.JdbcUtil;
import org.datagear.management.domain.Schema;
import org.datagear.management.service.SchemaService;
import org.datagear.persistence.support.UUID;
import org.datagear.web.OperationMessage;
import org.datagear.web.cometd.SqlpadCometdService;
import org.datagear.web.convert.ClassDataConverter;
import org.datagear.web.util.SqlScriptParser;
import org.datagear.web.util.SqlScriptParser.SqlStatement;
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
	@Autowired
	private BayeuxServer bayeuxServer;

	@Autowired
	private SqlpadCometdService sqlpadCometdService;

	public SqlpadController()
	{
		super();
	}

	public SqlpadController(MessageSource messageSource, ClassDataConverter classDataConverter,
			SchemaService schemaService, ConnectionSource connectionSource)
	{
		super(messageSource, classDataConverter, schemaService, connectionSource);
	}

	public BayeuxServer getBayeuxServer()
	{
		return bayeuxServer;
	}

	public void setBayeuxServer(BayeuxServer bayeuxServer)
	{
		this.bayeuxServer = bayeuxServer;
	}

	public SqlpadCometdService getSqlpadCometdService()
	{
		return sqlpadCometdService;
	}

	public void setSqlpadCometdService(SqlpadCometdService sqlpadCometdService)
	{
		this.sqlpadCometdService = sqlpadCometdService;
	}

	@RequestMapping("/{schemaId}")
	public String index(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId) throws Throwable
	{
		new VoidSchemaConnExecutor(request, response, springModel, schemaId, true)
		{
			@Override
			protected void execute(HttpServletRequest request, HttpServletResponse response, Model springModel,
					Schema schema) throws Throwable
			{
			}
		}.execute();

		String sqlpadChannelId = generateSqlpadChannelId(request, response);

		springModel.addAttribute("sqlpadChannelId", sqlpadChannelId);

		return "/sqlpad/sqlpad";
	}

	@RequestMapping(value = "/{schemaId}/execute", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> executeSql(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model springModel, @PathVariable("schemaId") String schemaId,
			@RequestParam("sqlpadChannelId") String sqlpadChannelId, @RequestParam("sql") String sql,
			@RequestParam(value = "sqlStartRow", required = false) Integer sqlStartRow,
			@RequestParam(value = "sqlStartColumn", required = false) Integer sqlStartColumn,
			@RequestParam(value = "commitMode", required = false) CommitMode commitMode,
			@RequestParam(value = "exceptionHandleMode", required = false) ExceptionHandleMode exceptionHandleMode) throws Throwable
	{
		Schema schema = getSchemaNotNull(request, response, schemaId);

		SqlScriptParser sqlScriptParser = new SqlScriptParser(new StringReader(sql));
		if (sqlStartRow != null)
			sqlScriptParser.setContextStartRow(sqlStartRow);
		if (sqlStartColumn != null)
			sqlScriptParser.setContextStartColumn(sqlStartColumn);
		
		if(commitMode == null)
			commitMode = CommitMode.AUTO;
		
		if(exceptionHandleMode == null)
			exceptionHandleMode = ExceptionHandleMode.ABORT;
		
		List<SqlStatement> sqlStatements = sqlScriptParser.parse();

		SqlpadExecutionRunnable sqlpadExecutionRunnable = new SqlpadExecutionRunnable(schema, sqlpadCometdService,
				sqlpadChannelId, sqlStatements, commitMode, exceptionHandleMode, WebUtils.getLocale(request));
		sqlpadExecutionRunnable.init();

		new Thread(sqlpadExecutionRunnable).start();

		return buildOperationMessageSuccessEmptyResponseEntity();
	}

	/**
	 * 生成SQL工作台cometd通道ID。
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	protected String generateSqlpadChannelId(HttpServletRequest request, HttpServletResponse response)
	{
		return "/sqlpad/channel/" + UUID.gen();
	}

	/**
	 * 用于执行SQL的{@linkplain Runnable}。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected class SqlpadExecutionRunnable implements Runnable
	{
		private Schema schema;

		private SqlpadCometdService sqlpadCometdService;

		private String sqlpadChannelId;

		private List<SqlStatement> sqlStatements;
		
		private CommitMode commitMode;
		
		private ExceptionHandleMode exceptionHandleMode;

		private Locale locale;

		private ServerChannel _sqlpadServerChannel;

		public SqlpadExecutionRunnable()
		{
			super();
		}

		public SqlpadExecutionRunnable(Schema schema, SqlpadCometdService sqlpadCometdService, String sqlpadChannelId,
				List<SqlStatement> sqlStatements, CommitMode commitMode, ExceptionHandleMode exceptionHandleMode, Locale locale)
		{
			super();
			this.schema = schema;
			this.sqlpadCometdService = sqlpadCometdService;
			this.sqlpadChannelId = sqlpadChannelId;
			this.sqlStatements = sqlStatements;
			this.commitMode = commitMode;
			this.exceptionHandleMode = exceptionHandleMode;
			this.locale = locale;
		}

		public Schema getSchema()
		{
			return schema;
		}

		public void setSchema(Schema schema)
		{
			this.schema = schema;
		}

		public SqlpadCometdService getSqlpadCometdService()
		{
			return sqlpadCometdService;
		}

		public void setSqlpadCometdService(SqlpadCometdService sqlpadCometdService)
		{
			this.sqlpadCometdService = sqlpadCometdService;
		}

		public String getSqlpadChannelId()
		{
			return sqlpadChannelId;
		}

		public void setSqlpadChannelId(String sqlpadChannelId)
		{
			this.sqlpadChannelId = sqlpadChannelId;
		}

		public List<SqlStatement> getSqlStatements()
		{
			return sqlStatements;
		}

		public void setSqlStatements(List<SqlStatement> sqlStatements)
		{
			this.sqlStatements = sqlStatements;
		}

		public CommitMode getCommitMode() {
			return commitMode;
		}

		public void setCommitMode(CommitMode commitMode) {
			this.commitMode = commitMode;
		}

		public ExceptionHandleMode getExceptionHandleMode() {
			return exceptionHandleMode;
		}

		public void setExceptionHandleMode(ExceptionHandleMode exceptionHandleMode) {
			this.exceptionHandleMode = exceptionHandleMode;
		}

		public Locale getLocale()
		{
			return locale;
		}

		public void setLocale(Locale locale)
		{
			this.locale = locale;
		}

		/**
		 * 初始化。
		 * <p>
		 * 此方法应该在{@linkplain #run()}之前调用。
		 * </p>
		 */
		public void init()
		{
			this._sqlpadServerChannel = this.sqlpadCometdService.getChannelWithCreation(this.sqlpadChannelId);
		}

		@Override
		public void run()
		{
			Connection cn = null;
			Statement st = null;

			try
			{
				this.sqlpadCometdService.sendStartMessage(this._sqlpadServerChannel);

				try
				{
					cn = getSchemaConnection(this.schema);
					st = cn.createStatement();
				}
				catch (Exception e)
				{
					this.sqlpadCometdService.sendExceptionMessage(_sqlpadServerChannel, e,
							getMessage(this.locale, "sqlpad.ConnectionException"), false);

					return;
				}

				for (int i = 0, len = sqlStatements.size(); i < len; i++)
				{
					SqlStatement sqlStatement = sqlStatements.get(i);

					// TODO 执行SQL
					// st.execute(sqlStatement.getSql());

					this.sqlpadCometdService.sendSuccessMessage(_sqlpadServerChannel, sqlStatement, i);

					try
					{
						Thread.sleep(500);
					}
					catch (InterruptedException e)
					{
						break;
					}
				}
			}
			catch (Exception e)
			{
				this.sqlpadCometdService.sendExceptionMessage(_sqlpadServerChannel, e,
						getMessage(this.locale, "sqlpad.executionErrorOccure"), true);
			}
			finally
			{
				JdbcUtil.closeStatement(st);
				JdbcUtil.closeConnection(cn);

				this.sqlpadCometdService.sendFinishMessage(this._sqlpadServerChannel);
			}
		}
	}
	
	/**
	 * 提交模式。
	 * @author datagear@163.com
	 *
	 */
	public static enum CommitMode
	{
		AUTO,
		
		MANUAL
	}
	
	/**
	 * 错误处理模式。
	 * @author datagear@163.com
	 *
	 */
	public static enum ExceptionHandleMode
	{
		ABORT,
		
		IGNORE,
		
		ROLLBACK
	}
}
