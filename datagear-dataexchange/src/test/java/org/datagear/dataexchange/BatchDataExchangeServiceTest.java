/*
 * Copyright 2018-2024 datagear.tech
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

package org.datagear.dataexchange;

import java.io.Writer;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.datagear.dataexchange.support.CsvDataExport;
import org.datagear.dataexchange.support.CsvDataExportService;
import org.datagear.dataexchange.support.CsvDataImport;
import org.datagear.dataexchange.support.CsvDataImportService;
import org.datagear.util.FileUtil;
import org.datagear.util.IOUtil;
import org.datagear.util.JdbcUtil;
import org.datagear.util.resource.ConnectionFactory;
import org.datagear.util.resource.DataSourceConnectionFactory;
import org.datagear.util.resource.FileWriterResourceFactory;
import org.datagear.util.resource.ResourceFactory;
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
	private BatchDataExchangeService<BatchDataExchange> batchDataExchangeService;

	public BatchDataExchangeServiceTest()
	{
		super();

		GenericDataExchangeService genericDataExchangeService = new GenericDataExchangeService();
		CsvDataImportService csvDataImportService = new CsvDataImportService(dbMetaResolver);
		CsvDataExportService csvDataExportService = new CsvDataExportService(dbMetaResolver);
		List<DevotedDataExchangeService<?>> devotedDataExchangeServices = new ArrayList<>();
		devotedDataExchangeServices.add(csvDataImportService);
		devotedDataExchangeServices.add(csvDataExportService);
		genericDataExchangeService.setDevotedDataExchangeServices(devotedDataExchangeServices);

		this.batchDataExchangeService = new BatchDataExchangeService<>(genericDataExchangeService);
	}

	@Test
	public void exchangeTest() throws Throwable
	{
		ConnectionFactory connectionFactory = new DataSourceConnectionFactory(buildTestDataSource());
		DataFormat dataFormat = new DataFormat();
		ValueDataImportOption importOption = new ValueDataImportOption(ExceptionResolve.ABORT, true, true, true);

		Set<SubDataExchange> subDataExchanges = new HashSet<>();

		{
			CsvDataImport csvDataImport = new CsvDataImport(connectionFactory, dataFormat, importOption,
					TABLE_NAME_DATA_IMPORT,
					getTestReaderResourceFactory("BatchDataExchangeServiceTest_1.csv"));

			SubDataExchange subDataExchange = new SubDataExchange("import-0", csvDataImport);
			subDataExchanges.add(subDataExchange);
		}

		{
			CsvDataImport csvDataImport = new CsvDataImport(connectionFactory, dataFormat, importOption,
					TABLE_NAME_DATA_IMPORT,
					getTestReaderResourceFactory("BatchDataExchangeServiceTest_2.csv"));

			SubDataExchange subDataExchange = new SubDataExchange("import-1", csvDataImport);
			subDataExchanges.add(subDataExchange);
		}

		final AtomicInteger exportDataCount = new AtomicInteger(0);

		{
			final String subDataExchangeId = "export-1";

			ResourceFactory<Writer> writerFactory = FileWriterResourceFactory
					.valueOf(FileUtil.getFile("target/BatchDataExchangeServiceTest.csv"), IOUtil.CHARSET_UTF_8);
			CsvDataExport csvDataExport = new CsvDataExport(connectionFactory, dataFormat,
					new TextDataExportOption(true), new TableQuery(TABLE_NAME_DATA_IMPORT), writerFactory);
			csvDataExport.setListener(new TextDataExportListener()
			{
				@Override
				public void onSuccess()
				{
					println(subDataExchangeId + " : onSuccess");
				}

				@Override
				public void onStart()
				{
					println(subDataExchangeId + " : onStart");
				}

				@Override
				public void onFinish()
				{
					println(subDataExchangeId + " : onFinish");
				}

				@Override
				public void onException(DataExchangeException e)
				{
				}

				@Override
				public void onSuccess(DataIndex dataIndex)
				{
					exportDataCount.incrementAndGet();
					println(subDataExchangeId + " : onSuccess(" + dataIndex + ")");
				}

				@Override
				public void onSetNullTextValue(DataIndex dataIndex, String columnName, DataExchangeException e)
				{
				}
			});

			SubDataExchange subDataExchange = new SubDataExchange(subDataExchangeId, csvDataExport);

			Set<SubDataExchange> dependents = new HashSet<>();
			dependents.addAll(subDataExchanges);
			subDataExchange.setDependencies(dependents);

			subDataExchanges.add(subDataExchange);
		}

		final AtomicInteger submitSuccessCount = new AtomicInteger(0);

		BatchDataExchange batchDataExchange = new SimpleBatchDataExchange(connectionFactory, subDataExchanges);
		batchDataExchange.setListener(new BatchDataExchangeListener()
		{
			@Override
			public void onStart()
			{
				println("onStart");
			}

			@Override
			public void onFinish()
			{
				println("onFinish");
			}

			@Override
			public void onException(DataExchangeException e)
			{
				println("onException");
			}

			@Override
			public void onSuccess()
			{
				println("onSuccess");
			}

			@Override
			public void onSubmitSuccess(SubDataExchange subDataExchange)
			{
				println("onSubmitSuccess : " + subDataExchange.getId());

				submitSuccessCount.incrementAndGet();
			}

			@Override
			public void onSubmitFail(SubDataExchange subDataExchange, SubmitFailException exception)
			{
				println("onSubmitFail : " + subDataExchange.getId());
			}

			@Override
			public void onCancel(SubDataExchange subDataExchange, CancelReason reason)
			{
				println("onCancel : " + subDataExchange.getId());
			}
		});

		Connection cn = connectionFactory.get();

		try
		{
			clearTable(cn, TABLE_NAME_DATA_IMPORT);

			this.batchDataExchangeService.exchange(batchDataExchange);

			batchDataExchange.getResult().waitForFinish();

			int count = getCount(cn, TABLE_NAME_DATA_IMPORT);

			Assert.assertEquals(6, count);
			Assert.assertEquals(3, submitSuccessCount.intValue());
			Assert.assertEquals(6, exportDataCount.get());
		}
		finally
		{
			JdbcUtil.closeConnection(cn);
		}
	}

	@Override
	protected void println()
	{
	}

	@Override
	protected void println(Object o)
	{
	}

	@Override
	protected void print(Object o)
	{
	}
}
