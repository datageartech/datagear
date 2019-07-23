/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import org.datagear.dataexchange.AbstractDevotedTextDataExportService;
import org.datagear.dataexchange.DataExchangeContext;
import org.datagear.dbinfo.DatabaseInfoResolver;

/**
 * Excel导出服务。
 * 
 * @author datagear@163.com
 *
 */
public class ExcelDataExportService extends AbstractDevotedTextDataExportService<ExcelDataExport>
{
	public ExcelDataExportService()
	{
		super();
	}

	public ExcelDataExportService(DatabaseInfoResolver databaseInfoResolver)
	{
		super(databaseInfoResolver);
	}

	@Override
	protected void exchange(ExcelDataExport dataExchange, DataExchangeContext context) throws Throwable
	{
		// TODO Auto-generated method stub
	}
}
