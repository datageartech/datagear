/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.web.sqlpad;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.cometd.bayeux.server.ServerChannel;
import org.datagear.connection.ConnectionOption;
import org.datagear.connection.ConnectionSource;
import org.datagear.connection.ConnectionSourceException;
import org.datagear.connection.DriverEntity;
import org.datagear.connection.JdbcUtil;
import org.datagear.management.domain.Schema;
import org.datagear.web.util.SqlScriptParser.SqlStatement;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

/**
 * SQL工作台执行SQL服务。
 * 
 * @author datagear@163.com
 *
 */
public class SqlpadExecutionService
{
	private ConnectionSource connectionSource;

	private MessageSource messageSource;

	private SqlpadCometdService sqlpadCometdService;

	private ExecutorService _executorService = Executors.newCachedThreadPool();

	private ConcurrentMap<String, SqlpadExecutionRunnable> _sqlpadExecutionRunnableMap = new ConcurrentHashMap<String, SqlpadExecutionRunnable>();

	public SqlpadExecutionService()
	{
		super();
	}

	public SqlpadExecutionService(ConnectionSource connectionSource, MessageSource messageSource,
			SqlpadCometdService sqlpadCometdService)
	{
		super();
		this.connectionSource = connectionSource;
		this.messageSource = messageSource;
		this.sqlpadCometdService = sqlpadCometdService;
	}

	public ConnectionSource getConnectionSource()
	{
		return connectionSource;
	}

	public void setConnectionSource(ConnectionSource connectionSource)
	{
		this.connectionSource = connectionSource;
	}

	public MessageSource getMessageSource()
	{
		return messageSource;
	}

	public void setMessageSource(MessageSource messageSource)
	{
		this.messageSource = messageSource;
	}

	public SqlpadCometdService getSqlpadCometdService()
	{
		return sqlpadCometdService;
	}

	public void setSqlpadCometdService(SqlpadCometdService sqlpadCometdService)
	{
		this.sqlpadCometdService = sqlpadCometdService;
	}

	/**
	 * 提交SQL执行。
	 * 
	 * @param sqlpadId
	 * @param schema
	 * @param sqlStatements
	 * @param commitMode
	 * @param exceptionHandleMode
	 * @param locale
	 * @return
	 */
	public boolean submit(String sqlpadId, Schema schema, List<SqlStatement> sqlStatements, CommitMode commitMode,
			ExceptionHandleMode exceptionHandleMode, Locale locale)
	{
		String sqlpadChannelId = getSqlpadChannelId(sqlpadId);

		SqlpadExecutionRunnable sqlpadExecutionRunnable = new SqlpadExecutionRunnable(schema, sqlpadId, sqlpadChannelId,
				sqlpadCometdService, sqlStatements, commitMode, exceptionHandleMode, locale);

		SqlpadExecutionRunnable old = this._sqlpadExecutionRunnableMap.putIfAbsent(sqlpadId, sqlpadExecutionRunnable);

		if (old != null)
			return false;

		sqlpadExecutionRunnable.init();

		this._executorService.submit(sqlpadExecutionRunnable);

		return true;
	}

	/**
	 * 发送SQL命令。
	 * 
	 * @param sqlpadId
	 * @param sqlCommand
	 * @return
	 */
	public boolean command(String sqlpadId, SqlCommand sqlCommand)
	{
		SqlpadExecutionRunnable sqlpadExecutionRunnable = this._sqlpadExecutionRunnableMap.get(sqlpadId);

		if (sqlpadExecutionRunnable == null)
			return false;

		sqlpadExecutionRunnable.setSqlCommand(sqlCommand);

		return true;
	}

	/**
	 * 获取指定SQL工作台ID对应的cometd通道ID。
	 * 
	 * @param sqlpadId
	 * @return
	 */
	public String getSqlpadChannelId(String sqlpadId)
	{
		return "/sqlpad/channel/" + sqlpadId;
	}

	/**
	 * 获取指定{@linkplain Schema}的{@linkplain Connection}。
	 * 
	 * @param schema
	 * @return
	 * @throws ConnectionSourceException
	 */
	protected Connection getSchemaConnection(Schema schema) throws ConnectionSourceException
	{
		Connection cn = null;

		ConnectionOption connectionOption = ConnectionOption.valueOf(schema.getUrl(), schema.getUser(),
				schema.getPassword());

		if (schema.hasDriverEntity())
		{
			DriverEntity driverEntity = schema.getDriverEntity();

			cn = this.connectionSource.getConnection(driverEntity, connectionOption);
		}
		else
		{
			cn = this.connectionSource.getConnection(connectionOption);
		}

		return cn;
	}

	/**
	 * 获取I18N消息内容。
	 * <p>
	 * 如果找不到对应消息码的消息，则返回<code>"???[code]???"<code>（例如：{@code "???error???"}）。
	 * </p>
	 * 
	 * @param locale
	 * @param code
	 * @param args
	 * @return
	 */
	protected String getMessage(Locale locale, String code, Object... args)
	{
		try
		{
			return this.messageSource.getMessage(code, args, locale);
		}
		catch (NoSuchMessageException e)
		{
			return "???" + code + "???";
		}
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

		private String sqlpadId;

		private String sqlpadChannelId;

		private SqlpadCometdService sqlpadCometdService;

		private List<SqlStatement> sqlStatements;

		private CommitMode commitMode;

		private ExceptionHandleMode exceptionHandleMode;

		private Locale locale;

		/** 发送给此Runnable的SQL命令 */
		private volatile SqlCommand sqlCommand;

		private ServerChannel _sqlpadServerChannel;

		public SqlpadExecutionRunnable()
		{
			super();
		}

		public SqlpadExecutionRunnable(Schema schema, String sqlpadId, String sqlpadChannelId,
				SqlpadCometdService sqlpadCometdService, List<SqlStatement> sqlStatements, CommitMode commitMode,
				ExceptionHandleMode exceptionHandleMode, Locale locale)
		{
			super();
			this.schema = schema;
			this.sqlpadId = sqlpadId;
			this.sqlpadChannelId = sqlpadChannelId;
			this.sqlpadCometdService = sqlpadCometdService;
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

		public String getSqlpadId()
		{
			return sqlpadId;
		}

		public void setSqlpadId(String sqlpadId)
		{
			this.sqlpadId = sqlpadId;
		}

		public String getSqlpadChannelId()
		{
			return sqlpadChannelId;
		}

		public void setSqlpadChannelId(String sqlpadChannelId)
		{
			this.sqlpadChannelId = sqlpadChannelId;
		}

		public SqlpadCometdService getSqlpadCometdService()
		{
			return sqlpadCometdService;
		}

		public void setSqlpadCometdService(SqlpadCometdService sqlpadCometdService)
		{
			this.sqlpadCometdService = sqlpadCometdService;
		}

		public List<SqlStatement> getSqlStatements()
		{
			return sqlStatements;
		}

		public void setSqlStatements(List<SqlStatement> sqlStatements)
		{
			this.sqlStatements = sqlStatements;
		}

		public CommitMode getCommitMode()
		{
			return commitMode;
		}

		public void setCommitMode(CommitMode commitMode)
		{
			this.commitMode = commitMode;
		}

		public ExceptionHandleMode getExceptionHandleMode()
		{
			return exceptionHandleMode;
		}

		public void setExceptionHandleMode(ExceptionHandleMode exceptionHandleMode)
		{
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

		public SqlCommand getSqlCommand()
		{
			return sqlCommand;
		}

		public void setSqlCommand(SqlCommand sqlCommand)
		{
			this.sqlCommand = sqlCommand;
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

			this.sqlpadCometdService.sendStartMessage(this._sqlpadServerChannel);

			try
			{
				cn = getSchemaConnection(this.schema);
				cn.setAutoCommit(false);
				st = cn.createStatement();
			}
			catch (Throwable t)
			{
				this.sqlpadCometdService.sendExceptionMessage(_sqlpadServerChannel, t,
						getMessage(this.locale, "sqlpad.executionConnectionException"), false);

				this.sqlpadCometdService.sendFinishMessage(this._sqlpadServerChannel);

				return;
			}

			int totalCount = this.sqlStatements.size();
			int successCount = 0, exceptionCount = 0;
			long durationMs = System.currentTimeMillis();

			try
			{
				for (int i = 0; i < totalCount; i++)
				{
					if (handleSqlCommandInExecution(cn))
						break;

					SqlStatement sqlStatement = sqlStatements.get(i);

					try
					{
						execute(cn, st, sqlStatement.getSql());

						successCount++;

						this.sqlpadCometdService.sendSuccessMessage(_sqlpadServerChannel, sqlStatement, i);
					}
					catch (SQLException e)
					{
						exceptionCount++;

						this.sqlpadCometdService.sendExecuteSQLExceptionMessage(_sqlpadServerChannel, sqlStatement, i,
								e, getMessage(this.locale, "sqlpad.executionSQLException"));

						if (ExceptionHandleMode.IGNORE.equals(this.exceptionHandleMode))
							;
						else
						{
							break;
						}
					}
				}

				if (SqlCommand.STOP.equals(this.sqlCommand))
					;
				else
				{
					if (CommitMode.AUTO.equals(this.commitMode))
					{
						if (exceptionCount > 0 && ExceptionHandleMode.ROLLBACK.equals(this.exceptionHandleMode))
							this.sqlCommand = SqlCommand.ROLLBACK;
						else
							this.sqlCommand = SqlCommand.COMMIT;
					}

					waitForCommitOrRollbackCommand(cn);
				}
			}
			catch (Throwable t)
			{
				this.sqlpadCometdService.sendExceptionMessage(_sqlpadServerChannel, t,
						getMessage(this.locale, "sqlpad.executionErrorOccure"), true);
			}
			finally
			{
				JdbcUtil.closeStatement(st);
				JdbcUtil.closeConnection(cn);

				durationMs = System.currentTimeMillis() - durationMs;

				this.sqlpadCometdService.sendFinishMessage(this._sqlpadServerChannel,
						new SQLExecutionStat(totalCount, successCount, exceptionCount, durationMs));

				_sqlpadExecutionRunnableMap.remove(this.sqlpadId);
			}
		}

		/**
		 * 处理执行时命令。
		 * 
		 * @param cn
		 * @return true 退出执行循环；false 不退出执行循环。
		 * @throws SQLException
		 * @throws InterruptedException
		 */
		protected boolean handleSqlCommandInExecution(Connection cn) throws SQLException, InterruptedException
		{
			boolean breakLoop = false;

			if (SqlCommand.PAUSE.equals(this.sqlCommand))
			{
				sendSqlCommandMessage(this.sqlCommand);

				// TODO 添加超时处理逻辑
				while (SqlCommand.PAUSE.equals(this.sqlCommand))
					sleepForSqlCommand();
			}

			if (SqlCommand.RESUME.equals(this.sqlCommand))
				;
			else if (SqlCommand.STOP.equals(this.sqlCommand))
			{
				cn.rollback();
				sendSqlCommandMessage(this.sqlCommand);

				breakLoop = true;
			}
			else if (SqlCommand.COMMIT.equals(this.sqlCommand))
			{
				cn.commit();
				sendSqlCommandMessage(this.sqlCommand);

				this.sqlCommand = null;
			}
			else if (SqlCommand.ROLLBACK.equals(this.sqlCommand))
			{
				cn.rollback();
				sendSqlCommandMessage(this.sqlCommand);

				this.sqlCommand = null;
			}

			return breakLoop;
		}

		/**
		 * 等待执行提交或者是回滚命令。
		 * 
		 * @param cn
		 * @param commitMode
		 * @throws SQLException
		 * @throws InterruptedException
		 */
		protected void waitForCommitOrRollbackCommand(Connection cn) throws SQLException, InterruptedException
		{
			boolean sendWatingMessage = false;

			// TODO 添加超时处理逻辑
			while (!SqlCommand.COMMIT.equals(this.sqlCommand) && !SqlCommand.ROLLBACK.equals(this.sqlCommand))
			{
				if (!sendWatingMessage)
				{
					this.sqlpadCometdService.sendTextMessage(this._sqlpadServerChannel,
							getMessage(this.locale, "sqlpad.waitingForCommitOrRollback"), "message-content-highlight");

					sendWatingMessage = true;
				}

				sleepForSqlCommand();
			}

			if (SqlCommand.COMMIT.equals(this.sqlCommand))
			{
				cn.commit();
				sendSqlCommandMessage(this.sqlCommand);

				this.sqlCommand = null;
			}
			else if (SqlCommand.ROLLBACK.equals(this.sqlCommand))
			{
				cn.rollback();
				sendSqlCommandMessage(this.sqlCommand);

				this.sqlCommand = null;
			}
		}

		/**
		 * 执行SQL，出现异常时应该抛出{@linkplain SQLException}。
		 * 
		 * @param cn
		 * @param st
		 * @param sql
		 * @throws SQLException
		 */
		protected void execute(Connection cn, Statement st, String sql) throws SQLException
		{
			// TODO 执行SQL

			st.execute(sql);

			try
			{
				Thread.sleep(500);
			}
			catch (InterruptedException e)
			{
			}
		}

		/**
		 * 发送命令已执行消息。
		 * 
		 * @param sqlCommand
		 */
		protected void sendSqlCommandMessage(SqlCommand sqlCommand)
		{
			String messageKey = "sqlpad.SqlCommand." + sqlCommand.toString() + ".ok";

			this.sqlpadCometdService.sendSqlCommandMessage(this._sqlpadServerChannel, sqlCommand,
					getMessage(this.locale, messageKey));
		}

		/**
		 * 睡眠等待SQL命令。
		 * 
		 * @throws InterruptedException
		 */
		protected void sleepForSqlCommand() throws InterruptedException
		{
			Thread.sleep(10);
		}
	}

	/**
	 * SQL执行统计信息。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class SQLExecutionStat implements Serializable
	{
		private static final long serialVersionUID = 1L;

		/** 总计SQL语句数 */
		private int totalCount;

		/** 执行成功数 */
		private int successCount = 0;

		/** 执行失败数 */
		private int exceptionCount = 0;

		/** 执行持续毫秒数 */
		private long durationMs = 0;

		public SQLExecutionStat()
		{
			super();
		}

		public SQLExecutionStat(int totalCount)
		{
			super();
			this.totalCount = totalCount;
		}

		public SQLExecutionStat(int totalCount, int successCount, int exceptionCount, long durationMs)
		{
			super();
			this.totalCount = totalCount;
			this.successCount = successCount;
			this.exceptionCount = exceptionCount;
			this.durationMs = durationMs;
		}

		public int getTotalCount()
		{
			return totalCount;
		}

		public void setTotalCount(int totalCount)
		{
			this.totalCount = totalCount;
		}

		public int getSuccessCount()
		{
			return successCount;
		}

		public void setSuccessCount(int successCount)
		{
			this.successCount = successCount;
		}

		public int getExceptionCount()
		{
			return exceptionCount;
		}

		public void setExceptionCount(int exceptionCount)
		{
			this.exceptionCount = exceptionCount;
		}

		public long getDurationMs()
		{
			return durationMs;
		}

		public void setDurationMs(long durationMs)
		{
			this.durationMs = durationMs;
		}
	}

	/**
	 * 提交模式。
	 * 
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
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static enum ExceptionHandleMode
	{
		ABORT,

		IGNORE,

		ROLLBACK
	}

	/**
	 * SQL执行命令。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static enum SqlCommand
	{
		/** 提交 */
		COMMIT,

		/** 回滚 */
		ROLLBACK,

		/** 暂停 */
		PAUSE,

		/** 继续 */
		RESUME,

		/** 停止 */
		STOP
	}
}
