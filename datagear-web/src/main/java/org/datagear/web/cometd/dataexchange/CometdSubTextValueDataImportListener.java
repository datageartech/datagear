/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.web.cometd.dataexchange;

import java.util.Locale;

import org.cometd.bayeux.server.ServerChannel;
import org.datagear.dataexchange.DataExchangeException;
import org.datagear.dataexchange.DataIndex;
import org.datagear.dataexchange.ExceptionResolve;
import org.datagear.dataexchange.ValueDataImportListener;
import org.springframework.context.MessageSource;

/**
 * 基于Cometd的子数据导入{@linkplain ValueDataImportListener}。
 * 
 * @author datagear@163.com
 *
 */
public class CometdSubTextValueDataImportListener extends CometdSubDataImportListener implements ValueDataImportListener
{
	public CometdSubTextValueDataImportListener()
	{
		super();
	}

	public CometdSubTextValueDataImportListener(DataExchangeCometdService dataExchangeCometdService,
			ServerChannel dataExchangeServerChannel, MessageSource messageSource, Locale locale,
			String subDataExchangeId, ExceptionResolve exceptionResolve)
	{
		super(dataExchangeCometdService, dataExchangeServerChannel, messageSource, locale, subDataExchangeId,
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
