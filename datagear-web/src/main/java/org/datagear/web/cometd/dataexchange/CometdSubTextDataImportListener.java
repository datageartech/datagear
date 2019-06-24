/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.web.cometd.dataexchange;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import org.cometd.bayeux.server.ServerChannel;
import org.datagear.dataexchange.DataExchangeException;
import org.datagear.dataexchange.ExceptionResolve;
import org.datagear.dataexchange.TextDataImportListener;
import org.springframework.context.MessageSource;

/**
 * 基于Cometd的子数据导入{@linkplain TextDataImportListener}。
 * 
 * @author datagear@163.com
 *
 */
public class CometdSubTextDataImportListener extends CometdSubDataExchangeListener implements TextDataImportListener
{
	private ExceptionResolve exceptionResolve;

	private AtomicInteger _successCount = new AtomicInteger(0);
	private AtomicInteger _ignoreCount = new AtomicInteger(0);
	private volatile String _lastIgnoreException = "";

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

	@Override
	public void onSuccess(int dataIndex)
	{
		_successCount.incrementAndGet();
	}

	@Override
	public void onIgnore(int dataIndex, DataExchangeException e)
	{
		_ignoreCount.incrementAndGet();
		this._lastIgnoreException = resolveDataExchangeExceptionI18n(e);
	}

	@Override
	public void onSetNullColumnValue(int dataIndex, String columnName, String rawColumnValue, DataExchangeException e)
	{
		// TODO Auto-generated method stub
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
