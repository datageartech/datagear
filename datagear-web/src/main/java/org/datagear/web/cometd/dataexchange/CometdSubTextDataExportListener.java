/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.web.cometd.dataexchange;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import org.cometd.bayeux.server.ServerChannel;
import org.datagear.dataexchange.DataExchangeException;
import org.datagear.dataexchange.ExceptionResolve;
import org.datagear.dataexchange.TextDataExportListener;
import org.springframework.context.MessageSource;

/**
 * 基于Cometd的子数据导出{@linkplain TextDataExportListener}。
 * 
 * @author datagear@163.com
 *
 */
public class CometdSubTextDataExportListener extends CometdSubDataExchangeListener implements TextDataExportListener
{
	private AtomicInteger _successCount = new AtomicInteger(0);

	public CometdSubTextDataExportListener()
	{
		super();
	}

	public CometdSubTextDataExportListener(DataExchangeCometdService dataExchangeCometdService,
			ServerChannel dataExchangeServerChannel, MessageSource messageSource, Locale locale,
			String subDataExchangeId)
	{
		super(dataExchangeCometdService, dataExchangeServerChannel, messageSource, locale, subDataExchangeId);
	}

	@Override
	public void onSuccess(int dataIndex)
	{
		_successCount.incrementAndGet();

		if (isTimeSendExchangingMessage())
			sendExportingMessage();
	}

	@Override
	public void onSetNullTextValue(int dataIndex, String columnName, DataExchangeException e)
	{
		String exceptionI18n = resolveDataExchangeExceptionI18n(e);

		if (hasLogFile())
			writeDataLog(dataIndex, exceptionI18n);
	}

	@Override
	protected DataExchangeMessage buildExceptionMessage(DataExchangeException e)
	{
		return new SubExceptionWithCount(getSubDataExchangeId(), resolveDataExchangeExceptionI18n(e), evalDuration(),
				ExceptionResolve.ABORT, this._successCount.intValue(), 1);
	}

	@Override
	protected DataExchangeMessage buildSuccessMessage()
	{
		SubSuccessWithCount message = new SubSuccessWithCount(getSubDataExchangeId(), evalDuration(),
				this._successCount.intValue(), 0);

		return message;
	}

	@Override
	protected String getStartLog()
	{
		return getI18nMessage("dataExport.startExport");
	}

	@Override
	protected String getFinishLog()
	{
		return getI18nMessage("dataExport.finishExport");
	}

	/**
	 * 发送导出中消息。
	 * 
	 * @return
	 */
	protected void sendExportingMessage()
	{
		sendExchangingMessage(new SubExchangingWithCount(getSubDataExchangeId(), this._successCount.intValue(), 0));
	}
}
