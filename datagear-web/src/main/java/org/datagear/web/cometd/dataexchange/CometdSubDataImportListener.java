/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.web.cometd.dataexchange;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import org.cometd.bayeux.server.ServerChannel;
import org.datagear.dataexchange.DataExchangeException;
import org.datagear.dataexchange.DataImportListener;
import org.datagear.dataexchange.DataIndex;
import org.datagear.dataexchange.ExceptionResolve;
import org.springframework.context.MessageSource;

/**
 * 基于Cometd的子数据导入{@linkplain DataImportListener}。
 * 
 * @author datagear@163.com
 *
 */
public class CometdSubDataImportListener extends CometdSubDataExchangeListener implements DataImportListener
{
	private ExceptionResolve exceptionResolve;

	protected final AtomicInteger _successCount = new AtomicInteger(0);
	protected final AtomicInteger _failCount = new AtomicInteger(0);
	protected volatile String _lastIgnoreException = "";

	public CometdSubDataImportListener()
	{
		super();
	}

	public CometdSubDataImportListener(DataExchangeCometdService dataExchangeCometdService,
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
	public void onSuccess(DataIndex dataIndex)
	{
		_successCount.incrementAndGet();

		if (isTimeSendExchangingMessage())
			sendImportingMessage();
	}

	@Override
	public void onIgnore(DataIndex dataIndex, DataExchangeException e)
	{
		_failCount.incrementAndGet();

		if (isTimeSendExchangingMessage())
			sendImportingMessage();

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

	@Override
	protected String getStartLog()
	{
		return getI18nMessage("dataImport.startImport");
	}

	@Override
	protected String getFinishLog()
	{
		return getI18nMessage("dataImport.finishImport");
	}

	/**
	 * 发送导入中消息。
	 * 
	 * @return
	 */
	protected void sendImportingMessage()
	{
		sendExchangingMessage(new SubExchangingWithCount(getSubDataExchangeId(), this._successCount.intValue(),
				this._failCount.intValue()));
	}
}
