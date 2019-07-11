package org.datagear.web.sqlpad;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.cometd.bayeux.MarkedReference;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.ServerChannel;
import org.cometd.server.AbstractService;
import org.datagear.dbmodel.ModelSqlSelectService.ModelSqlResult;
import org.datagear.util.SqlScriptParser.SqlStatement;
import org.datagear.web.OperationMessage;
import org.datagear.web.sqlpad.SqlpadExecutionService.SQLExecutionStat;
import org.datagear.web.sqlpad.SqlpadExecutionService.SqlCommand;
import org.datagear.web.sqlpad.SqlpadExecutionService.SqlResultType;

/**
 * SQL工作台cometd服务。
 * 
 * @author datagear@163.com
 *
 */
public class SqlpadCometdService extends AbstractService
{
	public SqlpadCometdService(BayeuxServer bayeux)
	{
		super(bayeux, "sqlpadCometdService");

		getServerSession();
	}

	/**
	 * 发送执行开始消息。
	 * 
	 * @param channel
	 * @param sqlCount
	 */
	public void sendStartMessage(ServerChannel channel, int sqlCount)
	{
		channel.publish(getServerSession(), new StartMessageData(sqlCount));
	}

	/**
	 * 发送SQL执行成功消息。
	 * 
	 * @param channel
	 * @param sqlStatement
	 * @param sqlStatementCount
	 */
	public void sendSqlSuccessMessage(ServerChannel channel, SqlStatement sqlStatement, int sqlStatementIndex)
	{
		channel.publish(getServerSession(),
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
	public void sendSqlSuccessMessage(ServerChannel channel, SqlStatement sqlStatement, int sqlStatementIndex,
			int updateCount)
	{
		SqlSuccessMessageData sqlSuccessMessageData = new SqlSuccessMessageData(sqlStatement, sqlStatementIndex,
				SqlResultType.UPDATE_COUNT);
		sqlSuccessMessageData.setUpdateCount(updateCount);

		channel.publish(getServerSession(), sqlSuccessMessageData);
	}

	/**
	 * 发送SQL执行成功消息。
	 * 
	 * @param channel
	 * @param sqlStatement
	 * @param sqlStatementIndex
	 * @param modelSqlResult
	 */
	public void sendSqlSuccessMessage(ServerChannel channel, SqlStatement sqlStatement, int sqlStatementIndex,
			ModelSqlResult modelSqlResult)
	{
		SqlSuccessMessageData sqlSuccessMessageData = new SqlSuccessMessageData(sqlStatement, sqlStatementIndex,
				SqlResultType.RESULT_SET);
		sqlSuccessMessageData.setModelSqlResult(modelSqlResult);

		channel.publish(getServerSession(), sqlSuccessMessageData);
	}

	/**
	 * 发送执行SQL异常消息。
	 * 
	 * @param channel
	 * @param sqlStatement
	 * @param sqlStatementIndex
	 * @param e
	 * @param content
	 */
	public void sendSqlExceptionMessage(ServerChannel channel, SqlStatement sqlStatement, int sqlStatementIndex,
			SQLException e, String content)
	{
		SQLExceptionMessageData messageData = new SQLExceptionMessageData(sqlStatement, sqlStatementIndex, content);

		channel.publish(getServerSession(), messageData);
	}

	/**
	 * 发送异常消息。
	 * 
	 * @param channel
	 * @param t
	 * @param content
	 * @param trace
	 */
	public void sendExceptionMessage(ServerChannel channel, Throwable t, String content, boolean trace)
	{
		ExceptionMessageData messageData = new ExceptionMessageData(content);
		if (trace)
			messageData.setDetailTrace(t);

		channel.publish(getServerSession(), messageData);
	}

	/**
	 * 发送执行命令消息。
	 * 
	 * @param channel
	 * @param sqlCommand
	 * @param content
	 */
	public void sendSqlCommandMessage(ServerChannel channel, SqlCommand sqlCommand, String content)
	{
		channel.publish(getServerSession(), new SqlCommandMessageData(sqlCommand, content));
	}

	/**
	 * 发送执行命令消息。
	 * 
	 * @param channel
	 * @param sqlCommand
	 * @param content
	 * @param sqlExecutionStat
	 */
	public void sendSqlCommandMessage(ServerChannel channel, SqlCommand sqlCommand, String content,
			SQLExecutionStat sqlExecutionStat)
	{
		SqlCommandMessageData sqlCommandMessageData = new SqlCommandMessageData(sqlCommand, content);
		sqlCommandMessageData.setSqlExecutionStat(sqlExecutionStat);

		channel.publish(getServerSession(), sqlCommandMessageData);
	}

	/**
	 * 发送文本消息。
	 * 
	 * @param channel
	 * @param text
	 */
	public void sendTextMessage(ServerChannel channel, String text)
	{
		channel.publish(getServerSession(), new TextMessageData(text));
	}

	/**
	 * 发送文本消息。
	 * 
	 * @param channel
	 * @param text
	 * @param cssClass
	 */
	public void sendTextMessage(ServerChannel channel, String text, String cssClass)
	{
		TextMessageData textMessageData = new TextMessageData(text);
		textMessageData.setCssClass(cssClass);

		channel.publish(getServerSession(), textMessageData);
	}

	/**
	 * 发送文本消息。
	 * 
	 * @param channel
	 * @param text
	 * @param cssClass
	 * @param sqlExecutionStat
	 */
	public void sendTextMessage(ServerChannel channel, String text, String cssClass, SQLExecutionStat sqlExecutionStat)
	{
		TextMessageData textMessageData = new TextMessageData(text);
		textMessageData.setCssClass(cssClass);
		textMessageData.setSqlExecutionStat(sqlExecutionStat);

		channel.publish(getServerSession(), textMessageData);
	}

	/**
	 * 发送执行完成消息。
	 * <p>
	 * 无论是否出现异常，都要发送此消息。
	 * </p>
	 * 
	 * @param channel
	 */
	public void sendFinishMessage(ServerChannel channel)
	{
		channel.publish(getServerSession(), new FinishMessageData());
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
	public void sendFinishMessage(ServerChannel channel, SQLExecutionStat sqlExecutionStat)
	{
		FinishMessageData finishMessageData = new FinishMessageData();
		finishMessageData.setSqlExecutionStat(sqlExecutionStat);

		channel.publish(getServerSession(), finishMessageData);
	}

	/**
	 * 获取指定ID的消息通道。
	 * 
	 * @param channelId
	 * @return
	 */
	public ServerChannel getChannel(String channelId)
	{
		return getBayeux().getChannel(channelId);
	}

	/**
	 * 获取指定ID的消息通道，必要时建立通道。
	 * 
	 * @param channelId
	 * @return
	 */
	public ServerChannel getChannelWithCreation(String channelId)
	{
		MarkedReference<ServerChannel> markedReference = getBayeux().createChannelIfAbsent(channelId);

		return markedReference.getReference();
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

		private ModelSqlResult modelSqlResult;

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

		public ModelSqlResult getModelSqlResult()
		{
			return modelSqlResult;
		}

		public void setModelSqlResult(ModelSqlResult modelSqlResult)
		{
			this.modelSqlResult = modelSqlResult;
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

		private String text;

		private String cssClass;

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

		public String getCssClass()
		{
			return cssClass;
		}

		public void setCssClass(String cssClass)
		{
			this.cssClass = cssClass;
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
