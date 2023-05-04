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

package org.datagear.web.dataexchange;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Locale;

import org.datagear.dataexchange.DataExchangeException;
import org.datagear.dataexchange.DataExchangeListener;
import org.datagear.dataexchange.DataIndex;
import org.datagear.dataexchange.ExceptionResolve;
import org.datagear.util.IOUtil;
import org.datagear.web.dataexchange.MessageBatchDataExchangeListener.SubSubmitSuccess;
import org.datagear.web.util.MessageChannel;
import org.springframework.context.MessageSource;

/**
 * 发送消息的子数据交换{@linkplain DataExchangeListener}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class MessageSubDataExchangeListener extends MessageDataExchangeListener
{
	public static final String LINE_SEPARATOR = System.getProperty("line.separator", "\n");

	public static final String LOG_FILE_CHARSET = IOUtil.CHARSET_UTF_8;

	public static final String LOG_FILE_CONTENT_DIV = "----------------------------------------";

	private String subDataExchangeId;

	private File logFile;

	/** 发送交换中消息的间隔毫秒数 */
	private int sendExchangingMessageInterval = 500;

	private volatile long _prevSendExchangingMessageTime = 0;
	private volatile Writer _logWriter;

	public MessageSubDataExchangeListener()
	{
		super();
	}

	public MessageSubDataExchangeListener(MessageChannel messageChannel,
			String dataExchangeServerChannel, MessageSource messageSource, Locale locale,
			String subDataExchangeId)
	{
		super(messageChannel, dataExchangeServerChannel, messageSource, locale);
		this.subDataExchangeId = subDataExchangeId;
	}

	public String getSubDataExchangeId()
	{
		return subDataExchangeId;
	}

	public void setSubDataExchangeId(String subDataExchangeId)
	{
		this.subDataExchangeId = subDataExchangeId;
	}

	public boolean hasLogFile()
	{
		return (this.logFile != null);
	}

	public File getLogFile()
	{
		return logFile;
	}

	public void setLogFile(File logFile)
	{
		this.logFile = logFile;
	}

	public int getSendExchangingMessageInterval()
	{
		return sendExchangingMessageInterval;
	}

	public void setSendExchangingMessageInterval(int sendExchangingMessageInterval)
	{
		this.sendExchangingMessageInterval = sendExchangingMessageInterval;
	}

	@Override
	public void onStart()
	{
		super.onStart();

		if (hasLogFile())
		{
			try
			{
				this._logWriter = new BufferedWriter(
						new OutputStreamWriter(new FileOutputStream(this.logFile), LOG_FILE_CHARSET));
			}
			catch (Throwable t)
			{
				this._logWriter = null;
				LOGGER.error("create log writer error", t);
			}

			writeStartLog();
		}
	}

	@Override
	public void onException(DataExchangeException e)
	{
		super.onException(e);

		if (hasLogFile())
			writeLogLine(resolveDataExchangeExceptionI18n(e));
	}

	@Override
	public void onFinish()
	{
		super.onFinish();

		if (hasLogFile())
		{
			writeFinishLog();
			IOUtil.close(this._logWriter);
		}
	}

	@Override
	protected DataExchangeMessage buildStartMessage()
	{
		return new SubStart(this.subDataExchangeId);
	}

	@Override
	protected DataExchangeMessage buildExceptionMessage(DataExchangeException e)
	{
		return new SubException(this.subDataExchangeId, resolveDataExchangeExceptionI18n(e), evalDuration());
	}

	@Override
	protected DataExchangeMessage buildSuccessMessage()
	{
		return new SubSuccess(this.subDataExchangeId, evalDuration());
	}

	@Override
	protected DataExchangeMessage buildFinishMessage()
	{
		return new SubFinish(this.subDataExchangeId);
	}

	/**
	 * 是否可以发送数据交换中消息了。
	 * 
	 * @return
	 */
	protected boolean isTimeSendExchangingMessage()
	{
		long currentTime = System.currentTimeMillis();

		if (currentTime - this._prevSendExchangingMessageTime < this.sendExchangingMessageInterval)
			return false;

		return true;
	}

	/**
	 * 发送交换中消息。
	 * 
	 * @return
	 */
	protected void sendExchangingMessage(DataExchangeMessage message)
	{
		sendMessage(message);
		this._prevSendExchangingMessageTime = System.currentTimeMillis();
	}

	/**
	 * 写开始日志。
	 * 
	 * @param log
	 */
	protected void writeStartLog()
	{
		writeLogLine(getStartLog());
		writeLogLine(LOG_FILE_CONTENT_DIV);
	}

	/**
	 * 获取开始日志。
	 * 
	 * @return
	 */
	protected abstract String getStartLog();

	/**
	 * 写结束日志。
	 * 
	 * @param log
	 */
	protected void writeFinishLog()
	{
		writeLogLine(LOG_FILE_CONTENT_DIV);
		writeLogLine(getFinishLog());
	}

	/**
	 * 获取结束日志。
	 * 
	 * @return
	 */
	protected abstract String getFinishLog();

	/**
	 * 写一条数据日志。
	 * 
	 * @param dataIndex
	 * @param log
	 */
	protected void writeDataLog(DataIndex dataIndex, String log)
	{
		writeLogLine("[" + dataIndex + "] " + log);
	}

	/**
	 * 写一行日志。
	 * 
	 * @param log
	 * @return
	 */
	protected boolean writeLogLine(String log)
	{
		if (this._logWriter == null)
			return false;

		try
		{
			this._logWriter.write(log);
			this._logWriter.write(LINE_SEPARATOR);

			return true;
		}
		catch (Throwable t)
		{
			LOGGER.error("write log error", t);

			return false;
		}
	}

	/**
	 * 子数据交换开始。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class SubStart extends SubDataExchangeMessage
	{
		public static final int ORDER = SubSubmitSuccess.ORDER + 1;

		public SubStart()
		{
			super();
		}

		public SubStart(String subDataExchangeId)
		{
			super(subDataExchangeId, ORDER);
		}
	}

	/**
	 * 子数据交换异常。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class SubException extends SubDataExchangeMessage
	{
		public static final int ORDER = SubStart.ORDER + 2;

		private String content;

		private long duration;

		public SubException()
		{
			super();
		}

		public SubException(String subDataExchangeId, String content, long duration)
		{
			super(subDataExchangeId, ORDER);
			this.content = content;
			this.duration = duration;
		}

		public String getContent()
		{
			return content;
		}

		public void setContent(String content)
		{
			this.content = content;
		}

		public long getDuration()
		{
			return duration;
		}

		public void setDuration(long duration)
		{
			this.duration = duration;
		}
	}

	/**
	 * 子数据交换成功。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class SubSuccess extends SubDataExchangeMessage
	{
		public static final int ORDER = SubStart.ORDER + 2;

		private long duration;

		public SubSuccess()
		{
			super();
		}

		public SubSuccess(String subDataExchangeId, long duration)
		{
			super(subDataExchangeId, ORDER);
			this.duration = duration;
		}

		public long getDuration()
		{
			return duration;
		}

		public void setDuration(long duration)
		{
			this.duration = duration;
		}
	}

	/**
	 * 子数据交换完成。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class SubFinish extends SubDataExchangeMessage
	{
		public static final int ORDER = SubSuccess.ORDER + 1;

		public SubFinish()
		{
			super();
		}

		public SubFinish(String subDataExchangeId)
		{
			super(subDataExchangeId, ORDER);
		}
	}

	/**
	 * 带有成功、失败数量统计的子数据交换进行中。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class SubExchangingWithCount extends SubDataExchangeMessage
	{
		public static final int ORDER = SubStart.ORDER + 1;

		private int successCount;

		private int failCount;

		public SubExchangingWithCount()
		{
			super();
		}

		public SubExchangingWithCount(String subDataExchangeId, int successCount, int failCount)
		{
			super(subDataExchangeId, ORDER);
			this.successCount = successCount;
			this.failCount = failCount;
		}

		public int getSuccessCount()
		{
			return successCount;
		}

		public void setSuccessCount(int successCount)
		{
			this.successCount = successCount;
		}

		public int getFailCount()
		{
			return failCount;
		}

		public void setFailCount(int failCount)
		{
			this.failCount = failCount;
		}
	}

	/**
	 * 带有成功、失败数量统计的子数据交换异常。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class SubExceptionWithCount extends SubException
	{
		private ExceptionResolve exceptionResolve;

		private int successCount;

		private int failCount;

		public SubExceptionWithCount()
		{
			super();
		}

		public SubExceptionWithCount(String subDataExchangeId, String content, long duration,
				ExceptionResolve exceptionResolve, int successCount, int failCount)
		{
			super(subDataExchangeId, content, duration);
			this.exceptionResolve = exceptionResolve;
			this.successCount = successCount;
			this.failCount = failCount;
		}

		public ExceptionResolve getExceptionResolve()
		{
			return exceptionResolve;
		}

		public void setExceptionResolve(ExceptionResolve exceptionResolve)
		{
			this.exceptionResolve = exceptionResolve;
		}

		public int getSuccessCount()
		{
			return successCount;
		}

		public void setSuccessCount(int successCount)
		{
			this.successCount = successCount;
		}

		public int getFailCount()
		{
			return failCount;
		}

		public void setFailCount(int failCount)
		{
			this.failCount = failCount;
		}
	}

	/**
	 * 子数据交换成功。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class SubSuccessWithCount extends SubSuccess
	{
		private int successCount;

		private int failCount;

		private String ignoreException;

		public SubSuccessWithCount()
		{
			super();
		}

		public SubSuccessWithCount(String subDataExchangeId, long duration, int successCount, int failCount)
		{
			super(subDataExchangeId, duration);
			this.successCount = successCount;
			this.failCount = failCount;
		}

		public int getSuccessCount()
		{
			return successCount;
		}

		public void setSuccessCount(int successCount)
		{
			this.successCount = successCount;
		}

		public int getFailCount()
		{
			return failCount;
		}

		public void setFailCount(int failCount)
		{
			this.failCount = failCount;
		}

		public String getIgnoreException()
		{
			return ignoreException;
		}

		public void setIgnoreException(String ignoreException)
		{
			this.ignoreException = ignoreException;
		}
	}
}
