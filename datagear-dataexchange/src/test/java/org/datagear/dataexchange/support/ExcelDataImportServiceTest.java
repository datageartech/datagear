/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.io.InputStream;
import java.sql.Connection;
import java.util.concurrent.atomic.AtomicInteger;

import org.datagear.dataexchange.DataExchangeException;
import org.datagear.dataexchange.DataFormat;
import org.datagear.dataexchange.DataIndex;
import org.datagear.dataexchange.DataexchangeTestSupport;
import org.datagear.dataexchange.ExceptionResolve;
import org.datagear.dataexchange.ResourceFactory;
import org.datagear.dataexchange.SimpleConnectionFactory;
import org.datagear.dataexchange.ValueDataImportOption;
import org.datagear.util.JdbcUtil;
import org.junit.Test;

/**
 * {@linkplain ExcelDataImportService}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class ExcelDataImportServiceTest extends DataexchangeTestSupport
{
	public static final String TABLE_NAME = "T_DATA_IMPORT";

	private ExcelDataImportService excelDataImportService;

	public ExcelDataImportServiceTest()
	{
		super();
		this.excelDataImportService = new ExcelDataImportService(databaseInfoResolver);
	}

	@Test
	public void exchangeTest() throws Throwable
	{
		DataFormat dataFormat = new DataFormat();

		Connection cn = getConnection();

		try
		{
			cn = getConnection();
			ResourceFactory<InputStream> inputFactory = getTestInputStreamResourceFactory(
					"support/ExcelDataImportServiceTest.xls");

			final AtomicInteger successCount = new AtomicInteger(0);
			final AtomicInteger ignoreCount = new AtomicInteger(0);

			ValueDataImportOption valueDataImportOption = new ValueDataImportOption(ExceptionResolve.ABORT, false,
					true);

			ExcelDataImport impt = new ExcelDataImport(new SimpleConnectionFactory(cn, false), dataFormat,
					valueDataImportOption, inputFactory);
			impt.setUnifiedTable(TABLE_NAME);

			impt.setListener(new MockValueDataImportListener()
			{
				@Override
				public void onSuccess(DataIndex dataIndex)
				{
					super.onSuccess(dataIndex);
					successCount.incrementAndGet();
				}

				@Override
				public void onIgnore(DataIndex dataIndex, DataExchangeException e)
				{
					super.onIgnore(dataIndex, e);
					ignoreCount.incrementAndGet();
				}
			});

			clearTable(cn, TABLE_NAME);

			this.excelDataImportService.exchange(impt);
		}
		finally
		{
			JdbcUtil.closeConnection(cn);
		}
	}
}
