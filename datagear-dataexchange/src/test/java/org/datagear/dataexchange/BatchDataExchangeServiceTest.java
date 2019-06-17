/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

import java.io.Reader;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.datagear.connection.JdbcUtil;
import org.datagear.dataexchange.support.CsvBatchDataImport;
import org.datagear.dataexchange.support.CsvDataImport;
import org.datagear.dataexchange.support.CsvDataImportService;
import org.junit.Assert;
import org.junit.Test;

/**
 * {@linkplain CsvBatchDataImportService}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class BatchDataExchangeServiceTest extends DataexchangeTestSupport
{
	public static final String TABLE_NAME = "T_DATA_IMPORT";

	private BatchDataExchangeService<CsvDataImport, CsvBatchDataImport> batchDataExchangeService;

	public BatchDataExchangeServiceTest()
	{
		super();

		CsvDataImportService csvDataImportService = new CsvDataImportService(databaseInfoResolver);
		ExecutorService executorService = Executors.newCachedThreadPool();

		this.batchDataExchangeService = new BatchDataExchangeService<CsvDataImport, CsvBatchDataImport>(
				csvDataImportService, executorService);
	}

	@Test
	public void exchangeTest() throws Throwable
	{
		ConnectionFactory connectionFactory = new DataSourceConnectionFactory(buildTestDataSource());
		DataFormat dataFormat = new DataFormat();
		TextDataImportOption importOption = new TextDataImportOption(true, ExceptionResolve.ABORT, true);
		List<ResourceFactory<Reader>> csvReaderFactories = new ArrayList<ResourceFactory<Reader>>();
		List<String> importTables = new ArrayList<String>();

		csvReaderFactories.add(getTestReaderResourceFactory("BatchDataExchangeServiceTest_1.csv"));
		csvReaderFactories.add(getTestReaderResourceFactory("BatchDataExchangeServiceTest_2.csv"));

		importTables.add(TABLE_NAME);
		importTables.add(TABLE_NAME);

		CsvBatchDataImport csvBatchDataImport = new CsvBatchDataImport(connectionFactory, dataFormat, importOption,
				csvReaderFactories, importTables);

		Connection cn = connectionFactory.get();

		try
		{
			clearTable(cn, TABLE_NAME);

			this.batchDataExchangeService.exchange(csvBatchDataImport);

			List<CsvDataImport> csvDataImports = csvBatchDataImport.getForResult();

			int count = getCount(cn, TABLE_NAME);

			Assert.assertEquals(2, csvDataImports.size());
			Assert.assertEquals(6, count);
		}
		finally
		{
			JdbcUtil.closeConnection(cn);
		}
	}
}
