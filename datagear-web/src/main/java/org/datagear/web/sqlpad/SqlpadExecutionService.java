/*
 * Copyright 2018-2023 datagear.tech
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

package org.datagear.web.sqlpad;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.datagear.connection.ConnectionSource;
import org.datagear.connection.ConnectionSourceException;
import org.datagear.management.domain.Schema;
import org.datagear.management.domain.User;
import org.datagear.management.service.SqlHistoryService;
import org.datagear.management.util.SchemaConnectionSupport;
import org.datagear.persistence.support.PersistenceSupport;
import org.datagear.persistence.support.SqlSelectManager;
import org.datagear.persistence.support.SqlSelectResult;
import org.datagear.util.JdbcUtil;
import org.datagear.util.SqlScriptParser.SqlStatement;
import org.datagear.util.sqlvalidator.DatabaseProfile;
import org.datagear.util.sqlvalidator.SqlValidation;
import org.datagear.web.controller.SqlpadController.SqlpadFileDirectory;
import org.datagear.web.util.MessageChannel;
import org.datagear.web.util.OperationMessage;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

/**
 * SQL工作台执行SQL服务。
 * 
 * @author datagear@163.com
 *
 */
public class SqlpadExecutionService extends PersistenceSupport
{
	private ConnectionSource connectionSource;

	private MessageSource messageSource;

	private SqlHistoryService sqlHistoryService;

	private SqlSelectManager sqlSelectManager;

	private MessageChannel messageChannel;

	private SqlPermissionValidator sqlPermissionValidator = null;

	private SchemaConnectionSupport schemaConnectionSupport = new SchemaConnectionSupport();

	private ExecutorService _executorService = Executors.newCachedThreadPool();

	private ConcurrentMap<String, SqlpadExecutionRunnable> _sqlpadExecutionRunnableMap = new ConcurrentHashMap<>();

	public SqlpadExecutionService()
	{
		super();
	}

	public SqlpadExecutionService(ConnectionSource connectionSource, MessageSource messageSource,
			SqlHistoryService sqlHistoryService, SqlSelectManager sqlSelectManager, MessageChannel messageChannel)
	{
		super();
		this.connectionSource = connectionSource;
		this.messageSource = messageSource;
		this.sqlHistoryService = sqlHistoryService;
		this.sqlSelectManager = sqlSelectManager;
		this.messageChannel = messageChannel;
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

	public SqlHistoryService getSqlHistoryService()
	{
		return sqlHistoryService;
	}

	public void setSqlHistoryService(SqlHistoryService sqlHistoryService)
	{
		this.sqlHistoryService = sqlHistoryService;
	}

	public SqlSelectManager getSqlSelectManager()
	{
		return sqlSelectManager;
	}

	public void setSqlSelectManager(SqlSelectManager sqlSelectManager)
	{
		this.sqlSelectManager = sqlSelectManager;
	}

	public MessageChannel getMessageChannel()
	{
		return messageChannel;
	}

	public void setMessageChannel(MessageChannel messageChannel)
	{
		this.messageChannel = messageChannel;
	}

	public SqlPermissionValidator getSqlPermissionValidator()
	{
		return sqlPermissionValidator;
	}

	public void setSqlPermissionValidator(SqlPermissionValidator sqlPermissionValidator)
	{
		this.sqlPermissionValidator = sqlPermissionValidator;
	}

	public SchemaConnectionSupport getSchemaConnectionSupport()
	{
		return schemaConnectionSupport;
	}

	public void setSchemaConnectionSupport(SchemaConnectionSupport schemaConnectionSupport)
	{
		this.schemaConnectionSupport = schemaConnectionSupport;
	}

	/**
	 * 提交SQL执行。
	 * 
	 * @param submit
	 * @return
	 */
	public boolean submit(SqlpadExecutionSubmit submit)
	{
		SqlpadExecutionRunnable sqlpadExecutionRunnable = new SqlpadExecutionRunnable(submit);

		SqlpadExecutionRunnable old = this._sqlpadExecutionRunnableMap.putIfAbsent(submit.getSqlpadId(),
				sqlpadExecutionRunnable);

		if (old != null)
			return false;

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
	 * 获取反馈消息。
	 * 
	 * @param <T>
	 * @param sqlpadId
	 * @param count
	 * @return
	 */
	public <T> List<T> message(String sqlpadId, int count)
	{
		return this.messageChannel.poll(sqlpadId, count);
	}

	/**
	 * 关闭。
	 */
	public void shutdown()
	{
		this._executorService.shutdown();
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
		return this.schemaConnectionSupport.getSchemaConnection(this.connectionSource, schema);
	}

	/**
	 * 发送执行开始消息。
	 * 
	 * @param channel
	 * @param sqlCount
	 */
	protected void sendStartMessage(String channel, int sqlCount)
	{
		this.messageChannel.push(channel, new StartMessageData(sqlCount));
	}

	/**
	 * 发送SQL执行成功消息。
	 * 
	 * @param channel
	 * @param sqlStatement
	 * @param sqlStatementCount
	 */
	protected void sendSqlSuccessMessage(String channel, SqlStatement sqlStatement, int sqlStatementIndex)
	{
		this.messageChannel.push(channel,
				new SqlSuccessMessageData(sqlStatement, sqlStatementIndex, SqlResultType.NONE));
	}

	/**
	 * 发送SQL执行成功消息。
	 * 
	 * @param channel
	 * @param sqlStatement
	 * @param sqlStatementIndex
	 * @param updateCount
	 */
	protected void sendSqlSuccessMessage(String channel, SqlStatement sqlStatement, int sqlStatementIndex,
			int updateCount)
	{
		SqlSuccessMessageData sqlSuccessMessageData = new SqlSuccessMessageData(sqlStatement, sqlStatementIndex,
				SqlResultType.UPDATE_COUNT);
		sqlSuccessMessageData.setUpdateCount(updateCount);

		this.messageChannel.push(channel, sqlSuccessMessageData);
	}

	/**
	 * 发送SQL执行成功消息。
	 * 
	 * @param channel
	 * @param sqlStatement
	 * @param sqlStatementIndex
	 * @param sqlSelectResult
	 */
	protected void sendSqlSuccessMessage(String channel, SqlStatement sqlStatement, int sqlStatementIndex,
			SqlSelectResult sqlSelectResult)
	{
		SqlSuccessMessageData sqlSuccessMessageData = new SqlSuccessMessageData(sqlStatement, sqlStatementIndex,
				SqlResultType.RESULT_SET);
		sqlSuccessMessageData.setSqlSelectResult(sqlSelectResult);

		this.messageChannel.push(channel, sqlSuccessMessageData);
	}

	/**
	 * 发送SQL异常消息。
	 * 
	 * @param channel
	 * @param sqlStatement
	 * @param sqlStatementIndex
	 * @param e
	 * @param content
	 */
	protected void sendSqlExceptionMessage(String channel, SqlStatement sqlStatement, int sqlStatementIndex,
			SQLException e, String content)
	{
		SQLExceptionMessageData messageData = new SQLExceptionMessageData(sqlStatement, sqlStatementIndex, content);

		this.messageChannel.push(channel, messageData);
	}

	/**
	 * 发送SQL异常消息。
	 * 
	 * @param channel
	 * @param sqlStatement
	 * @param sqlStatementIndex
	 * @param content
	 */
	protected void sendSqlExceptionMessage(String channel, SqlStatement sqlStatement, int sqlStatementIndex,
			String content)
	{
		SQLExceptionMessageData messageData = new SQLExceptionMessageData(sqlStatement, sqlStatementIndex, content);

		this.messageChannel.push(channel, messageData);
	}

	/**
	 * 发送异常消息。
	 * 
	 * @param channel
	 * @param t
	 * @param content
	 * @param trace
	 */
	protected void sendExceptionMessage(String channel, Throwable t, String content, boolean trace)
	{
		ExceptionMessageData messageData = new ExceptionMessageData(content);
		if (trace)
			messageData.setDetailTrace(t);

		this.messageChannel.push(channel, messageData);
	}

	/**
	 * 发送异常消息。
	 * 
	 * @param channel
	 * @param content
	 */
	protected void sendExceptionMessage(String channel, String content)
	{
		ExceptionMessageData messageData = new ExceptionMessageData(content);
		this.messageChannel.push(channel, messageData);
	}

	/**
	 * 发送执行命令消息。
	 * 
	 * @param channel
	 * @param sqlCommand
	 * @param content
	 */
	protected void sendSqlCommandMessage(String channel, SqlCommand sqlCommand, String content)
	{
		this.messageChannel.push(channel, new SqlCommandMessageData(sqlCommand, content));
	}

	/**
	 * 发送执行命令消息。
	 * 
	 * @param channel
	 * @param sqlCommand
	 * @param content
	 * @param sqlExecutionStat
	 */
	protected void sendSqlCommandMessage(String channel, SqlCommand sqlCommand, String content,
			SQLExecutionStat sqlExecutionStat)
	{
		SqlCommandMessageData sqlCommandMessageData = new SqlCommandMessageData(sqlCommand, content);
		sqlCommandMessageData.setSqlExecutionStat(sqlExecutionStat);

		this.messageChannel.push(channel, sqlCommandMessageData);
	}

	/**
	 * 发送文本消息。
	 * 
	 * @param channel
	 * @param text
	 */
	protected void sendTextMessage(String channel, String text)
	{
		this.messageChannel.push(channel, new TextMessageData(text));
	}

	/**
	 * 发送文本消息。
	 * 
	 * @param channel
	 * @param text
	 * @param textType
	 */
	protected void sendTextMessage(String channel, String text, String textType)
	{
		TextMessageData textMessageData = new TextMessageData(text);
		textMessageData.setTextType(textType);

		this.messageChannel.push(channel, textMessageData);
	}

	/**
	 * 发送文本消息。
	 * 
	 * @param channel
	 * @param text
	 * @param textType
	 * @param sqlExecutionStat
	 */
	protected void sendTextMessage(String channel, String text, String textType, SQLExecutionStat sqlExecutionStat)
	{
		TextMessageData textMessageData = new TextMessageData(text);
		textMessageData.setTextType(textType);
		textMessageData.setSqlExecutionStat(sqlExecutionStat);

		this.messageChannel.push(channel, textMessageData);
	}

	/**
	 * 发送执行完成消息。
	 * <p>
	 * 无论是否出现异常，都要发送此消息。
	 * </p>
	 * 
	 * @param channel
	 */
	protected void sendFinishMessage(String channel)
	{
		this.messageChannel.push(channel, new FinishMessageData());
	}

	/**
	 * 发送执行完成消息。
	 * <p>
	 * 无论是否出现异常，都要发送此消息。
	 * </p>
	 * 
	 * @param channel
	 * @param sqlExecutionStat
	 */
	protected void sendFinishMessage(String channel, SQLExecutionStat sqlExecutionStat)
	{
		FinishMessageData finishMessageData = new FinishMessageData();
		finishMessageData.setSqlExecutionStat(sqlExecutionStat);

		this.messageChannel.push(channel, finishMessageData);
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
	protected class SqlpadExecutionRunnable extends SqlpadExecutionSubmit implements Runnable
	{
		/** 发送给此Runnable的SQL命令 */
		private volatile SqlCommand sqlCommand;

		public SqlpadExecutionRunnable()
		{
			super();
		}

		public SqlpadExecutionRunnable(SqlpadExecutionSubmit submit)
		{
			super(submit);
		}

		public SqlCommand getSqlCommand()
		{
			return sqlCommand;
		}

		public void setSqlCommand(SqlCommand sqlCommand)
		{
			this.sqlCommand = sqlCommand;
		}

		@Override
		public void run()
		{
			Connection cn = null;
			Statement st = null;

			sendStartMessage(getSqlpadId(), getSqlStatements().size());

			try
			{
				cn = getSchemaConnection(getSchema());
				JdbcUtil.setAutoCommitIfSupports(cn, false);
				JdbcUtil.setReadonlyIfSupports(cn, false);
				st = createStatement(cn);
			}
			catch (Throwable t)
			{
				sendExceptionMessage(getSqlpadId(), t, getMessage(getLocale(), "sqlpad.executionConnectionException"),
						false);

				sendFinishMessage(getSqlpadId());

				_sqlpadExecutionRunnableMap.remove(getSqlpadId());

				return;
			}

			long startTime = System.currentTimeMillis();
			int totalCount = getSqlStatements().size();
			SQLExecutionStat sqlExecutionStat = new SQLExecutionStat(totalCount);
			SqlpadFileDirectory sqlpadFileDirectory = SqlpadFileDirectory.valueOf(getSqlpadFileDirectory());
			SqlPermissionValidator sqlPermissionValidator = SqlpadExecutionService.this.sqlPermissionValidator;
			DatabaseProfile databaseProfile = DatabaseProfile.valueOf(cn);

			List<String> sqlHistories = new ArrayList<>();

			try
			{
				for (int i = 0; i < totalCount; i++)
				{
					if (handleSqlCommandInExecution(cn, true, sqlExecutionStat))
						break;

					SqlStatement sqlStatement = getSqlStatements().get(i);
					SqlValidation sqlValidation = (sqlPermissionValidator == null ? null
							: sqlPermissionValidator.validate(getUser(), getSchema(), sqlStatement, databaseProfile));

					if (sqlValidation != null && !sqlValidation.isValid())
					{
						sendSqlExceptionMessage(getSqlpadId(), sqlStatement, i,
								getMessage(getLocale(), "sqlpad.executionSQLPermissionDenied",
										sqlValidation.getInvalidValue()));

						sqlExecutionStat.increaseExceptionCount();
					}
					else
					{
						try
						{
							execute(sqlExecutionStat, sqlpadFileDirectory, cn, st, sqlStatement, i);
							sqlExecutionStat.increaseSuccessCount();

							sqlHistories.add(sqlStatement.getSql());
						}
						catch (SQLException e)
						{
							sqlExecutionStat.increaseExceptionCount();

							sendSqlExceptionMessage(getSqlpadId(), sqlStatement, i, e,
									getMessage(getLocale(), "sqlpad.executionSQLException", e.getMessage()));

							if (ExceptionHandleMode.IGNORE.equals(getExceptionHandleMode()))
								;
							else
							{
								break;
							}
						}
					}
				}

				if (SqlCommand.STOP.equals(this.sqlCommand))
					;
				else
				{
					if (CommitMode.AUTO.equals(getCommitMode()))
					{
						if (sqlExecutionStat.getExceptionCount() > 0
								&& ExceptionHandleMode.ROLLBACK.equals(getExceptionHandleMode()))
							this.sqlCommand = SqlCommand.ROLLBACK;
						else
							this.sqlCommand = SqlCommand.COMMIT;
					}

					waitForCommitOrRollbackCommand(cn, sqlExecutionStat);
				}
			}
			catch (Throwable t)
			{
				sendExceptionMessage(getSqlpadId(), t, getMessage(getLocale(), "sqlpad.executionErrorOccure"), true);
			}
			finally
			{
				JdbcUtil.closeStatement(st);
				JdbcUtil.closeConnection(cn);

				sqlExecutionStat.setTaskDuration(System.currentTimeMillis() - startTime);

				sendFinishMessage(getSqlpadId(), sqlExecutionStat);

				_sqlpadExecutionRunnableMap.remove(getSqlpadId());
			}

			User user = getUser();
			
			if (!user.isAnonymous() && !sqlHistories.isEmpty())
				SqlpadExecutionService.this.sqlHistoryService.addForRemain(getSchema().getId(), user.getId(),
						sqlHistories);
		}

		/**
		 * 处理执行时命令。
		 * 
		 * @param cn
		 * @param sendMessageIfPause
		 * @param sqlExecutionStat
		 * @return true 退出执行循环；false 不退出执行循环。
		 * @throws SQLException
		 * @throws InterruptedException
		 */
		protected boolean handleSqlCommandInExecution(Connection cn, boolean sendMessageIfPause,
				SQLExecutionStat sqlExecutionStat) throws SQLException, InterruptedException
		{
			boolean breakLoop = false;

			boolean hasPaused = false;

			if (SqlCommand.PAUSE.equals(this.sqlCommand))
			{
				hasPaused = true;

				if (sendMessageIfPause)
					sendSqlCommandMessage(this.sqlCommand, getOverTimeThreashold());

				long waitStartTime = System.currentTimeMillis();

				while (SqlCommand.PAUSE.equals(this.sqlCommand)
						&& (System.currentTimeMillis() - waitStartTime) <= getOverTimeThreashold() * 60 * 1000)
					sleepForSqlCommand();

				// 暂停超时
				if (SqlCommand.PAUSE.equals(this.sqlCommand))
				{
					sendTextMessage(getSqlpadId(), getMessage(getLocale(), "sqlpad.pauseOverTime"));

					this.sqlCommand = SqlCommand.RESUME;
				}
			}

			if (SqlCommand.RESUME.equals(this.sqlCommand))
			{
				sendSqlCommandMessage(this.sqlCommand);

				this.sqlCommand = null;
			}
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

				// 提交操作不打断暂停
				if (hasPaused)
				{
					this.sqlCommand = SqlCommand.PAUSE;
					breakLoop = handleSqlCommandInExecution(cn, false, sqlExecutionStat);
				}
				else
					this.sqlCommand = null;
			}
			else if (SqlCommand.ROLLBACK.equals(this.sqlCommand))
			{
				cn.rollback();
				sendSqlCommandMessage(this.sqlCommand);

				// 回滚操作不打断暂停
				if (hasPaused)
				{
					this.sqlCommand = SqlCommand.PAUSE;
					breakLoop = handleSqlCommandInExecution(cn, false, sqlExecutionStat);
				}
				else
					this.sqlCommand = null;
			}

			return breakLoop;
		}

		/**
		 * 等待执行提交或者是回滚命令。
		 * 
		 * @param cn
		 * @param sqlExecutionStat
		 * @throws SQLException
		 * @throws InterruptedException
		 */
		protected void waitForCommitOrRollbackCommand(Connection cn, SQLExecutionStat sqlExecutionStat)
				throws SQLException, InterruptedException
		{
			boolean sendWatingMessage = false;

			long waitStartTime = System.currentTimeMillis();

			while (!SqlCommand.COMMIT.equals(this.sqlCommand) && !SqlCommand.ROLLBACK.equals(this.sqlCommand)
					&& (System.currentTimeMillis() - waitStartTime) <= getOverTimeThreashold() * 60 * 1000)
			{
				if (!sendWatingMessage)
				{
					sendTextMessage(getSqlpadId(),
							getMessage(getLocale(), "sqlpad.waitingForCommitOrRollback", getOverTimeThreashold()),
							TextMessageData.TEXT_TYPE_WAIT_COR, sqlExecutionStat);

					sendWatingMessage = true;
				}

				sleepForSqlCommand();
			}

			// 等待超时
			if (!SqlCommand.COMMIT.equals(this.sqlCommand) && !SqlCommand.ROLLBACK.equals(this.sqlCommand))
			{
				sendTextMessage(getSqlpadId(), getMessage(getLocale(), "sqlpad.waitOverTime"));

				this.sqlCommand = (sqlExecutionStat.getExceptionCount() > 0 ? SqlCommand.ROLLBACK : SqlCommand.COMMIT);
			}

			if (SqlCommand.COMMIT.equals(this.sqlCommand))
			{
				JdbcUtil.commitIfSupports(cn);
				sendSqlCommandMessage(this.sqlCommand);

				this.sqlCommand = null;
			}
			else if (SqlCommand.ROLLBACK.equals(this.sqlCommand))
			{
				JdbcUtil.rollbackIfSupports(cn);
				sendSqlCommandMessage(this.sqlCommand);

				this.sqlCommand = null;
			}
		}

		/**
		 * 执行SQL，出现异常时应该抛出{@linkplain SQLException}。
		 * 
		 * @param sqlExecutionStat
		 * @param sqlpadFileDirectory
		 * @param cn
		 * @param st
		 * @param sqlStatement
		 * @param sqlStatementIndex
		 * @throws SQLException
		 */
		protected void execute(SQLExecutionStat sqlExecutionStat, SqlpadFileDirectory sqlpadFileDirectory,
				Connection cn, Statement st, SqlStatement sqlStatement, int sqlStatementIndex) throws SQLException
		{
			long startTime = System.currentTimeMillis();

			// 禁用插入文件功能，因为没有应用场景
			// String sql =
			// sqlpadFileDirectory.replaceNameToAbsolutePath(sqlStatement.getSql());
			String sql = sqlStatement.getSql();

			boolean isResultSet = st.execute(sql);

			sqlExecutionStat.increaseSqlDuration(System.currentTimeMillis() - startTime);

			// 查询操作
			if (isResultSet)
			{
				ResultSet rs = st.getResultSet();

				SqlSelectResult sqlSelectResult = SqlpadExecutionService.this.sqlSelectManager.select(cn, sql, rs, 1,
						getResultsetFetchSize(), getResultsetRowMapper());

				sendSqlSuccessMessage(getSqlpadId(), sqlStatement, sqlStatementIndex, sqlSelectResult);
			}
			else
			{
				int updateCount = st.getUpdateCount();

				// 更新操作
				if (updateCount > -1)
				{
					sendSqlSuccessMessage(getSqlpadId(), sqlStatement, sqlStatementIndex, updateCount);
				}
				// 其他操作
				else
				{
					sendSqlSuccessMessage(getSqlpadId(), sqlStatement, sqlStatementIndex);
				}
			}
		}

		/**
		 * 发送命令已执行消息。
		 * 
		 * @param sqlCommand
		 * @param messageArgs
		 */
		protected void sendSqlCommandMessage(SqlCommand sqlCommand, Object... messageArgs)
		{
			String messageKey = "sqlpad.SqlCommand." + sqlCommand.toString() + ".ok";

			SqlpadExecutionService.this.sendSqlCommandMessage(getSqlpadId(), sqlCommand,
					getMessage(getLocale(), messageKey, messageArgs));
		}

		/**
		 * 创建执行SQL语句所需要的{@linkplain Statement}。
		 * 
		 * @param cn
		 * @return
		 * @throws SQLException
		 */
		protected Statement createStatement(Connection cn) throws SQLException
		{
			// 某些查询SQL语句并不支持ResultSet.TYPE_SCROLL_*（比如SQLServer的聚集列存储索引），
			// 而这里调用的结果集都是从第一行开始，不会用到ResultSet.TYPE_SCROLL_*特性，
			// 因而采用ResultSet.TYPE_FORWARD_ONLY，避免遇到上述情况而抛出异常
			Statement st = createUpdateStatement(cn);
			JdbcUtil.setFetchSizeIfSupports(st, getResultsetFetchSize());

			return st;
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

		/** SQL执行持续毫秒数，-1表示未记录 */
		private long sqlDuration = -1;

		/** 任务执行持续毫秒数，-1表示未记录 */
		private long taskDuration = -1;

		public SQLExecutionStat()
		{
			super();
		}

		public SQLExecutionStat(int totalCount)
		{
			super();
			this.totalCount = totalCount;
		}

		public SQLExecutionStat(int totalCount, int successCount, int exceptionCount, long sqlDuration)
		{
			super();
			this.totalCount = totalCount;
			this.successCount = successCount;
			this.exceptionCount = exceptionCount;
			this.sqlDuration = sqlDuration;
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

		public long getSqlDuration()
		{
			return sqlDuration;
		}

		public void setSqlDuration(long sqlDuration)
		{
			this.sqlDuration = sqlDuration;
		}

		public long getTaskDuration()
		{
			return taskDuration;
		}

		public void setTaskDuration(long taskDuration)
		{
			this.taskDuration = taskDuration;
		}

		public int getAbortCount()
		{
			return this.totalCount - this.successCount - this.exceptionCount;
		}

		public void increaseSuccessCount()
		{
			this.successCount += 1;
		}

		public void increaseExceptionCount()
		{
			this.exceptionCount += 1;
		}

		public void increaseSqlDuration(long increment)
		{
			if (this.sqlDuration < 0)
				this.sqlDuration = 0;

			this.sqlDuration += increment;
		}

		public void increaseTaskDuration(long increment)
		{
			if (this.taskDuration < 0)
				this.taskDuration = 0;

			this.taskDuration += increment;
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

	/**
	 * SQL执行结果类型。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static enum SqlResultType
	{
		/** 结果集 */
		RESULT_SET,

		/** 更新数目 */
		UPDATE_COUNT,

		/** 无结果 */
		NONE
	}

	protected static abstract class MessageData
	{
		protected static final String TIME_PATTERN = "HH:mm:ss";

		private String type;

		private Date date;

		public MessageData()
		{
			super();
			this.date = new Date();
		}

		public MessageData(String type)
		{
			super();
			this.type = type;
			this.date = new Date();
		}

		public String getType()
		{
			return type;
		}

		protected void setType(String type)
		{
			this.type = type;
		}

		public Date getDate()
		{
			return date;
		}

		public void setDate(Date date)
		{
			this.date = date;
		}

		public String getTimeText()
		{
			return new SimpleDateFormat(TIME_PATTERN).format(this.date);
		}
	}

	protected static class StartMessageData extends MessageData
	{
		public static final String TYPE = "START";

		private int sqlCount = 0;

		public StartMessageData()
		{
			super(TYPE);
		}

		public StartMessageData(int sqlCount)
		{
			super(TYPE);
			this.sqlCount = sqlCount;
		}

		public int getSqlCount()
		{
			return sqlCount;
		}

		public void setSqlCount(int sqlCount)
		{
			this.sqlCount = sqlCount;
		}
	}

	protected static class SqlSuccessMessageData extends MessageData
	{
		public static final String TYPE = "SQLSUCCESS";

		private SqlStatement sqlStatement;

		/** SQL语句索引 */
		private int sqlStatementIndex;

		/** SQL结果类型 */
		private SqlResultType sqlResultType = SqlResultType.NONE;

		/** 更新数目 */
		private int updateCount = -1;

		private SqlSelectResult sqlSelectResult;

		public SqlSuccessMessageData()
		{
			super(TYPE);
		}

		public SqlSuccessMessageData(SqlStatement sqlStatement, int sqlStatementIndex, SqlResultType sqlResultType)
		{
			super(TYPE);
			this.sqlStatement = sqlStatement;
			this.sqlStatementIndex = sqlStatementIndex;
			this.sqlResultType = sqlResultType;
		}

		public SqlStatement getSqlStatement()
		{
			return sqlStatement;
		}

		public void setSqlStatement(SqlStatement sqlStatement)
		{
			this.sqlStatement = sqlStatement;
		}

		public int getSqlStatementIndex()
		{
			return sqlStatementIndex;
		}

		public void setSqlStatementIndex(int sqlStatementIndex)
		{
			this.sqlStatementIndex = sqlStatementIndex;
		}

		public SqlResultType getSqlResultType()
		{
			return sqlResultType;
		}

		public void setSqlResultType(SqlResultType sqlResultType)
		{
			this.sqlResultType = sqlResultType;
		}

		public int getUpdateCount()
		{
			return updateCount;
		}

		public void setUpdateCount(int updateCount)
		{
			this.updateCount = updateCount;
		}

		public SqlSelectResult getSqlSelectResult()
		{
			return sqlSelectResult;
		}

		public void setSqlSelectResult(SqlSelectResult sqlSelectResult)
		{
			this.sqlSelectResult = sqlSelectResult;
		}
	}

	protected static class ExceptionMessageData extends MessageData
	{
		public static final String TYPE = "EXCEPTION";

		private String content;

		private String detailTrace;

		public ExceptionMessageData()
		{
			super(TYPE);
		}

		public ExceptionMessageData(String content)
		{
			super(TYPE);
			this.content = content;
		}

		public String getContent()
		{
			return content;
		}

		public void setContent(String content)
		{
			this.content = content;
		}

		public String getDetailTrace()
		{
			return detailTrace;
		}

		public void setDetailTrace(String detailTrace)
		{
			this.detailTrace = detailTrace;
		}

		public void setDetailTrace(Throwable t)
		{
			this.detailTrace = OperationMessage.printThrowableTrace(t);
		}
	}

	protected static class SQLExceptionMessageData extends ExceptionMessageData
	{
		public static final String TYPE = "SQLEXCEPTION";

		private SqlStatement sqlStatement;

		/** SQL语句索引 */
		private int sqlStatementIndex;

		public SQLExceptionMessageData()
		{
			super();
			super.setType(TYPE);
		}

		public SQLExceptionMessageData(SqlStatement sqlStatement, int sqlStatementIndex, String content)
		{
			super(content);
			super.setType(TYPE);
			this.sqlStatement = sqlStatement;
			this.sqlStatementIndex = sqlStatementIndex;
		}

		public SqlStatement getSqlStatement()
		{
			return sqlStatement;
		}

		public void setSqlStatement(SqlStatement sqlStatement)
		{
			this.sqlStatement = sqlStatement;
		}

		public int getSqlStatementIndex()
		{
			return sqlStatementIndex;
		}

		public void setSqlStatementIndex(int sqlStatementIndex)
		{
			this.sqlStatementIndex = sqlStatementIndex;
		}
	}

	protected static class SqlCommandMessageData extends MessageData
	{
		public static final String TYPE = "SQLCOMMAND";

		private SqlCommand sqlCommand;

		private String content;

		private SQLExecutionStat sqlExecutionStat;

		public SqlCommandMessageData()
		{
			super(TYPE);
		}

		public SqlCommandMessageData(SqlCommand sqlCommand, String content)
		{
			super(TYPE);
			this.sqlCommand = sqlCommand;
			this.content = content;
		}

		public SqlCommand getSqlCommand()
		{
			return sqlCommand;
		}

		public void setSqlCommand(SqlCommand sqlCommand)
		{
			this.sqlCommand = sqlCommand;
		}

		public String getContent()
		{
			return content;
		}

		public void setContent(String content)
		{
			this.content = content;
		}

		public SQLExecutionStat getSqlExecutionStat()
		{
			return sqlExecutionStat;
		}

		public void setSqlExecutionStat(SQLExecutionStat sqlExecutionStat)
		{
			this.sqlExecutionStat = sqlExecutionStat;
		}
	}

	protected static class TextMessageData extends MessageData
	{
		public static final String TYPE = "TEXT";
		
		public static final String TEXT_TYPE_WAIT_COR = "WAIT_COMMIT_OR_ROLLBACK";

		private String text;

		private String textType;

		private SQLExecutionStat sqlExecutionStat;

		public TextMessageData()
		{
			super(TYPE);
		}

		public TextMessageData(String text)
		{
			super(TYPE);
			this.text = text;
		}

		public String getText()
		{
			return text;
		}

		public void setText(String text)
		{
			this.text = text;
		}

		public String getTextType()
		{
			return textType;
		}

		public void setTextType(String textType)
		{
			this.textType = textType;
		}

		public SQLExecutionStat getSqlExecutionStat()
		{
			return sqlExecutionStat;
		}

		public void setSqlExecutionStat(SQLExecutionStat sqlExecutionStat)
		{
			this.sqlExecutionStat = sqlExecutionStat;
		}
	}
	
	protected static class FinishMessageData extends MessageData
	{
		public static final String TYPE = "FINISH";

		private SQLExecutionStat sqlExecutionStat;

		public FinishMessageData()
		{
			super(TYPE);
		}

		public SQLExecutionStat getSqlExecutionStat()
		{
			return sqlExecutionStat;
		}

		public void setSqlExecutionStat(SQLExecutionStat sqlExecutionStat)
		{
			this.sqlExecutionStat = sqlExecutionStat;
		}
	}
}
