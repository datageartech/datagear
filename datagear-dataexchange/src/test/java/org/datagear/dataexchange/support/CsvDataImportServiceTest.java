/*
 * Copyright 2018-2023 datagear.tech
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

package org.datagear.dataexchange.support;

import java.io.Reader;
import java.sql.Connection;
import java.util.concurrent.atomic.AtomicInteger;

import org.datagear.dataexchange.ColumnNotFoundException;
import org.datagear.dataexchange.DataExchangeException;
import org.datagear.dataexchange.DataFormat;
import org.datagear.dataexchange.DataIndex;
import org.datagear.dataexchange.DataexchangeTestSupport;
import org.datagear.dataexchange.ExceptionResolve;
import org.datagear.dataexchange.IllegalImportSourceValueException;
import org.datagear.dataexchange.ValueDataImportOption;
import org.datagear.util.IOUtil;
import org.datagear.util.JdbcUtil;
import org.datagear.util.resource.ResourceFactory;
import org.datagear.util.resource.SimpleConnectionFactory;
import org.junit.Assert;
import org.junit.Test;

/**
 * {@linkplain CsvDataImportService}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class CsvDataImportServiceTest extends DataexchangeTestSupport
{
	private CsvDataImportService csvDataImportService;

	public CsvDataImportServiceTest()
	{
		super();
		this.csvDataImportService = new CsvDataImportService(dbMetaResolver);
	}

	@Test
	public void exchangeTest_ignoreInexistentColumn_false() throws Exception
	{
		DataFormat dataFormat = new DataFormat();

		ColumnNotFoundException exception = Assert.assertThrows(ColumnNotFoundException.class, () ->
		{
			Connection cn = null;

			try
			{
				cn = getConnection();
				ResourceFactory<Reader> readerFactory = getTestReaderResourceFactory(
						"support/CsvDataImportServiceTest_ignoreInexistentColumn.csv");

				ValueDataImportOption valueDataImportOption = new ValueDataImportOption(ExceptionResolve.ABORT, false,
						true, true);
				CsvDataImport impt = new CsvDataImport(new SimpleConnectionFactory(cn, false), dataFormat,
						valueDataImportOption, TABLE_NAME_DATA_IMPORT, readerFactory);

				clearTable(cn, TABLE_NAME_DATA_IMPORT);

				this.csvDataImportService.exchange(impt);
			}
			finally
			{
				JdbcUtil.closeConnection(cn);
			}
		});

		Assert.assertTrue(exception.getMessage().contains("Column [INEXISTENT_COLUMN] not found"));
	}

	@Test
	public void exchangeTest_ignoreInexistentColumn_true() throws Exception
	{
		DataFormat dataFormat = new DataFormat();

		Connection cn = null;
		Reader reader = null;

		try
		{
			cn = getConnection();

			ResourceFactory<Reader> readerFactory = getTestReaderResourceFactory(
					"support/CsvDataImportServiceTest_ignoreInexistentColumn.csv");

			final AtomicInteger importCountInListener = new AtomicInteger(0);

			ValueDataImportOption valueDataImportOption = new ValueDataImportOption(ExceptionResolve.ABORT, true, true,
					true);
			CsvDataImport impt = new CsvDataImport(new SimpleConnectionFactory(cn, false), dataFormat,
					valueDataImportOption, TABLE_NAME_DATA_IMPORT, readerFactory);

			impt.setListener(new MockValueDataImportListener()
			{
				@Override
				public void onSuccess(DataIndex dataIndex)
				{
					println("onSuccess : " + dataIndex);
					importCountInListener.incrementAndGet();
				}
			});

			clearTable(cn, TABLE_NAME_DATA_IMPORT);

			this.csvDataImportService.exchange(impt);

			int count = getCount(cn, TABLE_NAME_DATA_IMPORT);

			Assert.assertEquals(3, count);

			Assert.assertEquals(3, importCountInListener.intValue());
		}
		finally
		{
			JdbcUtil.closeConnection(cn);
			IOUtil.close(reader);
		}
	}

	@Test
	public void exchangeTest_ExceptionResolve_ignore() throws Exception
	{
		DataFormat dataFormat = new DataFormat();

		Connection cn = null;
		Reader reader = null;

		try
		{
			cn = getConnection();

			ResourceFactory<Reader> readerFactory = getTestReaderResourceFactory(
					"support/CsvDataImportServiceTest__ExceptionResolve.csv");

			final AtomicInteger successCount = new AtomicInteger(0);
			final AtomicInteger ignoreCount = new AtomicInteger(0);

			ValueDataImportOption valueDataImportOption = new ValueDataImportOption(ExceptionResolve.IGNORE, true,
					true, true);
			CsvDataImport impt = new CsvDataImport(new SimpleConnectionFactory(cn, false), dataFormat,
					valueDataImportOption, TABLE_NAME_DATA_IMPORT, readerFactory);

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

			clearTable(cn, TABLE_NAME_DATA_IMPORT);

			this.csvDataImportService.exchange(impt);

			int count = getCount(cn, TABLE_NAME_DATA_IMPORT);

			Assert.assertEquals(2, count);
			Assert.assertEquals(2, successCount.intValue());
			Assert.assertEquals(1, ignoreCount.intValue());
		}
		finally
		{
			JdbcUtil.closeConnection(cn);
			IOUtil.close(reader);
		}
	}

	@Test
	public void exchangeTest_ExceptionResolve_abort() throws Exception
	{
		DataFormat dataFormat = new DataFormat();

		Assert.assertThrows(IllegalImportSourceValueException.class, () ->
		{
			Connection cn = null;
			Reader reader = null;

			try
			{
				cn = getConnection();

				ResourceFactory<Reader> readerFactory = getTestReaderResourceFactory(
						"support/CsvDataImportServiceTest__ExceptionResolve.csv");

				ValueDataImportOption valueDataImportOption = new ValueDataImportOption(ExceptionResolve.ABORT, true,
						false, true);
				CsvDataImport impt = new CsvDataImport(new SimpleConnectionFactory(cn, false), dataFormat,
						valueDataImportOption, TABLE_NAME_DATA_IMPORT, readerFactory);

				clearTable(cn, TABLE_NAME_DATA_IMPORT);

				this.csvDataImportService.exchange(impt);
			}
			finally
			{
				JdbcUtil.closeConnection(cn);
				IOUtil.close(reader);
			}
		});
	}

	@Test
	public void exchangeTest_unsigned_number() throws Exception
	{
		DataFormat dataFormat = new DataFormat();

		Connection cn = null;
		Reader reader = null;

		try
		{
			cn = getConnection();

			ResourceFactory<Reader> readerFactory = getTestReaderResourceFactory(
					"support/CsvDataImportServiceTest_unsigned_number.csv");

			final AtomicInteger successCount = new AtomicInteger(0);
			final AtomicInteger ignoreCount = new AtomicInteger(0);

			ValueDataImportOption valueDataImportOption = new ValueDataImportOption(ExceptionResolve.IGNORE, true,
					true, true);
			CsvDataImport impt = new CsvDataImport(new SimpleConnectionFactory(cn, false), dataFormat,
					valueDataImportOption, TABLE_NAME_UNSIGNED_NUMBER, readerFactory);

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

			clearTable(cn, TABLE_NAME_UNSIGNED_NUMBER);

			this.csvDataImportService.exchange(impt);

			int count = getCount(cn, TABLE_NAME_UNSIGNED_NUMBER);

			Assert.assertEquals(2, count);
			Assert.assertEquals(2, successCount.intValue());
		}
		finally
		{
			JdbcUtil.closeConnection(cn);
			IOUtil.close(reader);
		}
	}
}
