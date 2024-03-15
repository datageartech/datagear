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
import org.datagear.dataexchange.DataIndex;
import org.datagear.dataexchange.ExceptionResolve;
import org.datagear.dataexchange.TextDataExportListener;
import org.datagear.web.util.MessageChannel;
import org.datagear.web.util.msg.Message;
import org.springframework.context.MessageSource;

/**
 * 发送消息的子数据导出{@linkplain TextDataExportListener}。
 * 
 * @author datagear@163.com
 *
 */
public class MessageSubTextDataExportListener extends MessageSubDataExchangeListener implements TextDataExportListener
{
	private AtomicInteger _successCount = new AtomicInteger(0);

	public MessageSubTextDataExportListener()
	{
		super();
	}

	public MessageSubTextDataExportListener(MessageChannel messageChannel,
			String dataExchangeServerChannel, MessageSource messageSource, Locale locale,
			String subDataExchangeId)
	{
		super(messageChannel, dataExchangeServerChannel, messageSource, locale, subDataExchangeId);
	}

	@Override
	public void onSuccess(DataIndex dataIndex)
	{
		_successCount.incrementAndGet();

		if (isTimeSendExchangingMessage())
			sendExportingMessage();
	}

	@Override
	public void onSetNullTextValue(DataIndex dataIndex, String columnName, DataExchangeException e)
	{
		String exceptionI18n = resolveDataExchangeExceptionI18n(e);

		if (hasLogFile())
			writeDataLog(dataIndex, exceptionI18n);
	}

	@Override
	protected Message buildExceptionMessage(DataExchangeException e)
	{
		int successCount = this._successCount.intValue();

		return new SubExceptionWithCount(getSubDataExchangeId(), resolveDataExchangeExceptionI18n(e), evalDuration(),
				ExceptionResolve.ABORT, successCount, (successCount > 0 ? 1 : 0));
	}

	@Override
	protected Message buildSuccessMessage()
	{
		SubSuccessWithCount message = new SubSuccessWithCount(getSubDataExchangeId(), evalDuration(),
				this._successCount.intValue(), 0);

		return message;
	}

	@Override
	protected String getStartLog()
	{
		return getI18nMessage("startExport");
	}

	@Override
	protected String getFinishLog()
	{
		return getI18nMessage("finishExport");
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
