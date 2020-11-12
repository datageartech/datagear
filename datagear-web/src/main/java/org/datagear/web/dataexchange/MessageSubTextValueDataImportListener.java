/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
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
