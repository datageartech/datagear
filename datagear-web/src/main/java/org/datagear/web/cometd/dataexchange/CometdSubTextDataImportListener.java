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

	/** 发送导入中消息的间隔毫秒数 */
	private int sendImportingMessageInterval = 500;

	private AtomicInteger _successCount = new AtomicInteger(0);
	private AtomicInteger _failCount = new AtomicInteger(0);
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

	public int getSendImportingMessageInterval()
	{
		return sendImportingMessageInterval;
	}

	public void setSendImportingMessageInterval(int sendImportingMessageInterval)
	{
		this.sendImportingMessageInterval = sendImportingMessageInterval;
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
		_failCount.incrementAndGet();
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
		return new SubExceptionWithCount(getSubDataExchangeId(), resolveDataExchangeExceptionI18n(e), evalDuration(),
				this.exceptionResolve, this._successCount.intValue(), this._failCount.intValue());
	}

	@Override
	protected DataExchangeMessage buildSuccessMessage()
	{
		SubSuccessWithCount message = new SubSuccessWithCount(getSubDataExchangeId(), evalDuration(),
				this._successCount.intValue(), this._failCount.intValue());

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
		if (currentTime - this._prevSendImportingMessageTime < this.sendImportingMessageInterval)
			return false;

		this._prevSendImportingMessageTime = currentTime;

		sendMessage(new SubExchangingWithCount(getSubDataExchangeId(), this._successCount.intValue(),
				this._failCount.intValue()));

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
}
