/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import org.datagear.dataexchange.AbstractDevotedDataExchangeService;
import org.datagear.dataexchange.DataExchangeException;
import org.datagear.dataexchange.DataImportListener;

/**
 * SQL导入服务。
 * 
 * @author datagear@163.com
 *
 */
public class SqlDataImportService extends AbstractDevotedDataExchangeService<SqlDataImport>
{
	public SqlDataImportService()
	{
		super();
	}

	@Override
	public void exchange(SqlDataImport dataExchange) throws DataExchangeException
	{
		DataImportListener listener = dataExchange.getListener();

		if (listener != null)
			listener.onStart();

		try
		{
			// TODO
			if (listener != null)
				listener.onSuccess();
		}
		finally
		{
			if (listener != null)
				listener.onFinish();
		}
	}
}
