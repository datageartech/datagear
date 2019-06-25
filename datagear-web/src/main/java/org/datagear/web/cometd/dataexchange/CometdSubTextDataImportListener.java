/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.web.cometd.dataexchange;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import org.cometd.bayeux.server.ServerChannel;
import org.datagear.connection.IOUtil;
import org.datagear.dataexchange.DataExchangeException;
import org.datagear.dataexchange.ExceptionResolve;
import org.datagear.dataexchange.TextDataImportListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;

/**
 * 基于Cometd的子数据导入{@linkplain TextDataImportListener}。
 * 
 * @author datagear@163.com
 *
 */
public class CometdSubTextDataImportListener extends CometdSubDataExchangeListener implements TextDataImportListener
{
	protected static final Logger LOGGER = LoggerFactory.getLogger(CometdSubTextDataImportListener.class);

	public static final String LINE_SEPARATOR = System.getProperty("line.separator", "\n");

	public static final String LOG_FILE_CHARSET = "UTF-8";

	public static final String LOG_FILE_CONTENT_DIV = "----------------------------------------";

	private ExceptionResolve exceptionResolve;

	private File logFile;

	private AtomicInteger _successCount = new AtomicInteger(0);
	private AtomicInteger _ignoreCount = new AtomicInteger(0);
	private volatile String _lastIgnoreException = "";
	private volatile Writer _logWriter;
	private volatile long _prevSendImportingMessageTime = 0;

	public CometdSubTextDataImportListener()
	{
		super();
	}

	public CometdSubTextDataImportListener(DataExchangeCometdService dataExchangeCometdService,
			ServerChannel dataExchangeServerChannel, MessageSource messageSource, Locale locale,
			String subDataExchangeId, ExceptionResolve exceptionResolve)
	{
		super(dataExchangeCometdService, dataExchangeServerChannel, messageSource, locale, subDataExchangeId);
		this.exceptionResolve = exceptionResolve;
	}

	public ExceptionResolve getExceptionResolve()
	{
		return exceptionResolve;
	}

	public void setExceptionResolve(ExceptionResolve exceptionResolve)
	{
		this.exceptionResolve = exceptionResolve;
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

				writeStartLog();
			}
			catch (Throwable t)
			{
				this._logWriter = null;

				LOGGER.error("create log writer error", t);
			}
		}
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
	public void onException(DataExchangeException e)
	{
		super.onException(e);

		if (hasLogFile())
			writeLogLine(resolveDataExchangeExceptionI18n(e));
	}

	@Override
	public void onSuccess(int dataIndex)
	{
		_successCount.incrementAndGet();
		sendImportingMessage();
	}

	@Override
	public void onIgnore(int dataIndex, DataExchangeException e)
	{
		_ignoreCount.incrementAndGet();
		sendImportingMessage();

		String exceptionI18n = resolveDataExchangeExceptionI18n(e);
		this._lastIgnoreException = exceptionI18n;

		if (hasLogFile())
			writeDataLog(dataIndex, exceptionI18n);
	}

	@Override
	public void onSetNullColumnValue(int dataIndex, String columnName, String rawColumnValue, DataExchangeException e)
	{
		String exceptionI18n = resolveDataExchangeExceptionI18n(e);
		this._lastIgnoreException = exceptionI18n;

		if (hasLogFile())
			writeDataLog(dataIndex, exceptionI18n);
	}

	@Override
	protected DataExchangeMessage buildExceptionMessage(DataExchangeException e)
	{
		return new TextImportSubException(getSubDataExchangeId(), resolveDataExchangeExceptionI18n(e), evalDuration(),
				this.exceptionResolve, this._successCount.intValue(), this._ignoreCount.intValue());
	}

	@Override
	protected DataExchangeMessage buildSuccessMessage()
	{
		TextImportSubSuccess message = new TextImportSubSuccess(getSubDataExchangeId(), evalDuration(),
				this._successCount.intValue(), this._ignoreCount.intValue());

		message.setIgnoreException(this._lastIgnoreException);

		return message;
	}

	/**
	 * 发送导入中消息。
	 * 
	 * @return
	 */
	protected boolean sendImportingMessage()
	{
		long currentTime = System.currentTimeMillis();
		if (currentTime - this._prevSendImportingMessageTime < 200)
			return false;

		this._prevSendImportingMessageTime = currentTime;

		sendMessage(new TextImportSubImporting(getSubDataExchangeId(), this._successCount.intValue(),
				this._ignoreCount.intValue()));

		return true;
	}

	/**
	 * 写一条数据日志。
	 * 
	 * @param dataIndex
	 * @param log
	 */
	protected void writeStartLog()
	{
		writeLogLine(getI18nMessage("dataImport.startImport"));
		writeLogLine(LOG_FILE_CONTENT_DIV);
	}

	/**
	 * 写一条数据日志。
	 * 
	 * @param dataIndex
	 * @param log
	 */
	protected void writeFinishLog()
	{
		writeLogLine(LOG_FILE_CONTENT_DIV);
		writeLogLine(getI18nMessage("dataImport.finishImport"));
	}

	/**
	 * 写一条数据日志。
	 * 
	 * @param dataIndex
	 * @param log
	 */
	protected void writeDataLog(int dataIndex, String log)
	{
		writeLogLine("[" + (dataIndex + 1) + "] " + log);
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
	 * 导入中。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class TextImportSubImporting extends SubDataExchangeMessage
	{
		private int successCount;

		private int ignoreCount;

		public TextImportSubImporting()
		{
			super();
		}

		public TextImportSubImporting(String subDataExchangeId, int successCount, int ignoreCount)
		{
			super(subDataExchangeId);
			this.successCount = successCount;
			this.ignoreCount = ignoreCount;
		}

		public int getSuccessCount()
		{
			return successCount;
		}

		public void setSuccessCount(int successCount)
		{
			this.successCount = successCount;
		}

		public int getIgnoreCount()
		{
			return ignoreCount;
		}

		public void setIgnoreCount(int ignoreCount)
		{
			this.ignoreCount = ignoreCount;
		}
	}

	/**
	 * 子文本导入异常。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class TextImportSubException extends SubException
	{
		private ExceptionResolve exceptionResolve;

		private int successCount;

		private int ignoreCount;

		public TextImportSubException()
		{
			super();
		}

		public TextImportSubException(String subDataExchangeId, String content, long duration,
				ExceptionResolve exceptionResolve, int successCount, int ignoreCount)
		{
			super(subDataExchangeId, content, duration);
			this.exceptionResolve = exceptionResolve;
			this.successCount = successCount;
			this.ignoreCount = ignoreCount;
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

		public int getIgnoreCount()
		{
			return ignoreCount;
		}

		public void setIgnoreCount(int ignoreCount)
		{
			this.ignoreCount = ignoreCount;
		}
	}

	/**
	 * 子文本导入成功。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class TextImportSubSuccess extends SubSuccess
	{
		private int successCount;

		private int ignoreCount;

		private String ignoreException;

		public TextImportSubSuccess()
		{
			super();
		}

		public TextImportSubSuccess(String subDataExchangeId, long duration, int successCount, int ignoreCount)
		{
			super(subDataExchangeId, duration);
			this.successCount = successCount;
			this.ignoreCount = ignoreCount;
		}

		public int getSuccessCount()
		{
			return successCount;
		}

		public void setSuccessCount(int successCount)
		{
			this.successCount = successCount;
		}

		public int getIgnoreCount()
		{
			return ignoreCount;
		}

		public void setIgnoreCount(int ignoreCount)
		{
			this.ignoreCount = ignoreCount;
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
