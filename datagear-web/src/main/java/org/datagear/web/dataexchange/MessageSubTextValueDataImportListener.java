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

import org.datagear.dataexchange.DataExchangeException;
import org.datagear.dataexchange.DataIndex;
import org.datagear.dataexchange.ExceptionResolve;
import org.datagear.dataexchange.ValueDataImportListener;
import org.datagear.web.util.MessageChannel;
import org.springframework.context.MessageSource;

/**
 * 发送消息的子数据导入{@linkplain ValueDataImportListener}。
 * 
 * @author datagear@163.com
 *
 */
public class MessageSubTextValueDataImportListener extends MessageSubDataImportListener implements ValueDataImportListener
{
	public MessageSubTextValueDataImportListener()
	{
		super();
	}

	public MessageSubTextValueDataImportListener(MessageChannel messageChannel,
			String dataExchangeServerChannel, MessageSource messageSource, Locale locale,
			String subDataExchangeId, ExceptionResolve exceptionResolve)
	{
		super(messageChannel, dataExchangeServerChannel, messageSource, locale, subDataExchangeId,
				exceptionResolve);
	}

	@Override
	public void onSetNullColumnValue(DataIndex dataIndex, String columnName, Object columnValue,
			DataExchangeException e)
	{
		String exceptionI18n = resolveDataExchangeExceptionI18n(e);
		this._lastIgnoreException = exceptionI18n;

		if (hasLogFile())
			writeDataLog(dataIndex, exceptionI18n);
	}
}
