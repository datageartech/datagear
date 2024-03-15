/*
 * Copyright 2018-present datagear.tech
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

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import org.datagear.dataexchange.DataExchangeException;
import org.datagear.dataexchange.DataImportListener;
import org.datagear.dataexchange.DataIndex;
import org.datagear.dataexchange.ExceptionResolve;
import org.datagear.web.util.MessageChannel;
import org.datagear.web.util.msg.Message;
import org.springframework.context.MessageSource;

/**
 * 发送消息的子数据导入{@linkplain DataImportListener}。
 * 
 * @author datagear@163.com
 *
 */
public class MessageSubDataImportListener extends MessageSubDataExchangeListener implements DataImportListener
{
	private ExceptionResolve exceptionResolve;

	protected final AtomicInteger _successCount = new AtomicInteger(0);
	protected final AtomicInteger _failCount = new AtomicInteger(0);
	protected volatile String _lastIgnoreException = "";

	public MessageSubDataImportListener()
	{
		super();
	}

	public MessageSubDataImportListener(MessageChannel messageChannel,
			String dataExchangeServerChannel, MessageSource messageSource, Locale locale,
			String subDataExchangeId, ExceptionResolve exceptionResolve)
	{
		super(messageChannel, dataExchangeServerChannel, messageSource, locale, subDataExchangeId);
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
	protected Message buildExceptionMessage(DataExchangeException e)
	{
		return new SubExceptionWithCount(getSubDataExchangeId(), resolveDataExchangeExceptionI18n(e), evalDuration(),
				this.exceptionResolve, this._successCount.intValue(), this._failCount.intValue());
	}

	@Override
	protected Message buildSuccessMessage()
	{
		SubSuccessWithCount message = new SubSuccessWithCount(getSubDataExchangeId(), evalDuration(),
				this._successCount.intValue(), this._failCount.intValue());

		message.setIgnoreException(this._lastIgnoreException);

		return message;
	}

	@Override
	protected String getStartLog()
	{
		return getI18nMessage("startImport");
	}

	@Override
	protected String getFinishLog()
	{
		return getI18nMessage("finishImport");
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
