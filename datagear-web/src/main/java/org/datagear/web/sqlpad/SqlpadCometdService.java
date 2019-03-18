package org.datagear.web.sqlpad;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.cometd.bayeux.MarkedReference;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.ServerChannel;
import org.cometd.server.AbstractService;
import org.datagear.web.util.SqlScriptParser.SqlStatement;

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
	 */
	public void sendStartMessage(ServerChannel channel)
	{
		channel.publish(getServerSession(), new StartMessageData());
	}

	/**
	 * 发送SQL执行成功消息。
	 * 
	 * @param channel
	 * @param sqlStatement
	 * @param sqlStatementCount
	 */
	public void sendSuccessMessage(ServerChannel channel, SqlStatement sqlStatement, int sqlStatementIndex)
	{
		channel.publish(getServerSession(), new SqlSuccessMessageData(sqlStatement, sqlStatementIndex));
	}

	/**
	 * 发送解析SQL时出现{@linkplain IOException}的消息。
	 * 
	 * @param channel
	 * @param e
	 */
	public void sendParserIOExceptionMessage(ServerChannel channel, IOException e)
	{
		channel.publish(getServerSession(), new ExceptionMessageData(e.getMessage()));
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

		public StartMessageData()
		{
			super(TYPE);
		}
	}

	protected static class SqlSuccessMessageData extends MessageData
	{
		public static final String TYPE = "SUCCESS";

		private SqlStatement sqlStatement;

		/** SQL语句索引 */
		private int sqlStatementIndex;

		public SqlSuccessMessageData()
		{
			super(TYPE);
		}

		public SqlSuccessMessageData(SqlStatement sqlStatement, int sqlStatementIndex)
		{
			super(TYPE);
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

	protected static class ExceptionMessageData extends MessageData
	{
		public static final String TYPE = "EXCEPTION";

		private String content;

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
	}

	protected static class FinishMessageData extends MessageData
	{
		public static final String TYPE = "FINISH";

		public FinishMessageData()
		{
			super(TYPE);
		}
	}
}
