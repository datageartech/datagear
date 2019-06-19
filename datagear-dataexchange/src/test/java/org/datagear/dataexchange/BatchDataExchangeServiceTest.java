/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

import java.io.Reader;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.datagear.connection.JdbcUtil;
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

	private BatchDataExchangeService<CsvDataImport, BatchDataExchange<CsvDataImport>> batchDataExchangeService;

	public BatchDataExchangeServiceTest()
	{
		super();

		CsvDataImportService csvDataImportService = new CsvDataImportService(databaseInfoResolver);

		this.batchDataExchangeService = new BatchDataExchangeService<CsvDataImport, BatchDataExchange<CsvDataImport>>(
				csvDataImportService);
	}

	@Test
	public void exchangeTest() throws Throwable
	{
		ConnectionFactory connectionFactory = new DataSourceConnectionFactory(buildTestDataSource());
		DataFormat dataFormat = new DataFormat();
		TextDataImportOption importOption = new TextDataImportOption(true, ExceptionResolve.ABORT, true);
		List<ResourceFactory<Reader>> readerFactories = new ArrayList<ResourceFactory<Reader>>();
		List<String> tables = new ArrayList<String>();

		readerFactories.add(getTestReaderResourceFactory("BatchDataExchangeServiceTest_1.csv"));
		readerFactories.add(getTestReaderResourceFactory("BatchDataExchangeServiceTest_2.csv"));

		tables.add(TABLE_NAME);
		tables.add(TABLE_NAME);

		List<CsvDataImport> csvDataImports = CsvDataImport.valuesOf(connectionFactory, dataFormat, importOption, tables,
				readerFactories);

		BatchDataExchange<CsvDataImport> csvBatchDataImport = new SimpleBatchDataExchange<CsvDataImport>(
				connectionFactory, csvDataImports);

		Connection cn = connectionFactory.get();

		try
		{
			clearTable(cn, TABLE_NAME);

			this.batchDataExchangeService.exchange(csvBatchDataImport);

			csvDataImports = csvBatchDataImport.waitForResults();

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
