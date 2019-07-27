/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import org.datagear.dataexchange.AbstractDevotedDbInfoAwareDataExchangeService;
import org.datagear.dataexchange.DataExchangeContext;
import org.datagear.dataexchange.IndexFormatDataExchangeContext;
import org.datagear.dbinfo.DatabaseInfoResolver;

/**
 * Excel导入服务。
 * 
 * @author datagear@163.com
 *
 */
public class ExcelDataImportService extends AbstractDevotedDbInfoAwareDataExchangeService<ExcelDataImport>
{
	public ExcelDataImportService()
	{
		super();
	}

	public ExcelDataImportService(DatabaseInfoResolver databaseInfoResolver)
	{
		super(databaseInfoResolver);
	}

	@Override
	protected DataExchangeContext createDataExchangeContext(ExcelDataImport dataExchange)
	{
		return IndexFormatDataExchangeContext.valueOf(dataExchange);
	}

	@Override
	protected void exchange(ExcelDataImport dataExchange, DataExchangeContext context) throws Throwable
	{
		IndexFormatDataExchangeContext importContext = IndexFormatDataExchangeContext.cast(context);

		// TODO
	}
}
