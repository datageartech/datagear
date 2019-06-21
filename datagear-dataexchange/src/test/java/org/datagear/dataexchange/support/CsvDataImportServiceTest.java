/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.io.Reader;
import java.sql.Connection;
import java.util.concurrent.atomic.AtomicInteger;

import org.datagear.connection.IOUtil;
import org.datagear.connection.JdbcUtil;
import org.datagear.dataexchange.DataExchangeException;
import org.datagear.dataexchange.DataFormat;
import org.datagear.dataexchange.DataexchangeTestSupport;
import org.datagear.dataexchange.ExceptionResolve;
import org.datagear.dataexchange.ResourceFactory;
import org.datagear.dataexchange.SimpleConnectionFactory;
import org.datagear.dataexchange.TextDataImportListener;
import org.datagear.dataexchange.TextDataImportOption;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * {@linkplain CsvDataImportService}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class CsvDataImportServiceTest extends DataexchangeTestSupport
{
	public static final String TABLE_NAME = "T_DATA_IMPORT";

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private CsvDataImportService csvDataImportService;

	public CsvDataImportServiceTest()
	{
		super();
		this.csvDataImportService = new CsvDataImportService(databaseInfoResolver);
	}

	@Test
	public void exchangeTest_ignoreInexistentColumn_false() throws Exception
	{
		DataFormat dataFormat = new DataFormat();

		Connection cn = getConnection();

		try
		{
			cn = getConnection();
			ResourceFactory<Reader> readerFactory = getTestReaderResourceFactory(
					"support/CsvDataImportServiceTest_ignoreInexistentColumn.csv");

			TextDataImportOption textDataImportOption = new TextDataImportOption(false, ExceptionResolve.ABORT, true);
			CsvDataImport impt = new CsvDataImport(new SimpleConnectionFactory(cn, false), dataFormat,
					textDataImportOption, TABLE_NAME, readerFactory);

			clearTable(cn, TABLE_NAME);

			this.thrown.expect(ColumnNotFoundException.class);
			this.thrown.expectMessage("Column [INEXISTENT_COLUMN] not found");

			this.csvDataImportService.exchange(impt);
		}
		finally
		{
			JdbcUtil.closeConnection(cn);
		}
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

			TextDataImportOption textDataImportOption = new TextDataImportOption(true, ExceptionResolve.ABORT, true);
			CsvDataImport impt = new CsvDataImport(new SimpleConnectionFactory(cn, false), dataFormat,
					textDataImportOption, TABLE_NAME, readerFactory);

			impt.setListener(new MockTextDataImportListener()
			{
				@Override
				public void onSuccess(int dataIndex)
				{
					println("onSuccess : " + dataIndex);
					importCountInListener.incrementAndGet();
				}
			});

			clearTable(cn, TABLE_NAME);

			this.csvDataImportService.exchange(impt);

			int count = getCount(cn, TABLE_NAME);

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

			TextDataImportOption textDataImportOption = new TextDataImportOption(true, ExceptionResolve.IGNORE, true);
			CsvDataImport impt = new CsvDataImport(new SimpleConnectionFactory(cn, false), dataFormat,
					textDataImportOption, TABLE_NAME, readerFactory);

			impt.setListener(new MockTextDataImportListener()
			{
				@Override
				public void onSuccess(int dataIndex)
				{
					super.onSuccess(dataIndex);
					successCount.incrementAndGet();
				}

				@Override
				public void onIgnore(int dataIndex, DataExchangeException e)
				{
					super.onIgnore(dataIndex, e);
					ignoreCount.incrementAndGet();
				}
			});

			clearTable(cn, TABLE_NAME);

			this.csvDataImportService.exchange(impt);

			int count = getCount(cn, TABLE_NAME);

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

		Connection cn = null;
		Reader reader = null;

		try
		{
			cn = getConnection();

			ResourceFactory<Reader> readerFactory = getTestReaderResourceFactory(
					"support/CsvDataImportServiceTest__ExceptionResolve.csv");

			TextDataImportOption textDataImportOption = new TextDataImportOption(true, ExceptionResolve.ABORT, false);
			CsvDataImport impt = new CsvDataImport(new SimpleConnectionFactory(cn, false), dataFormat,
					textDataImportOption, TABLE_NAME, readerFactory);

			clearTable(cn, TABLE_NAME);

			this.thrown.expect(IllegalSourceValueException.class);

			this.csvDataImportService.exchange(impt);
		}
		finally
		{
			JdbcUtil.closeConnection(cn);
			IOUtil.close(reader);
		}
	}

	protected class MockTextDataImportListener implements TextDataImportListener
	{
		@Override
		public void onStart()
		{
			println("onStart");
		}

		@Override
		public void onException(DataExchangeException e)
		{
			println("onException : " + e.getMessage());
		}

		@Override
		public void onSuccess()
		{
			println("onSuccess");
		}

		@Override
		public void onFinish()
		{
			println("onFinish");
		}

		@Override
		public void onSuccess(int dataIndex)
		{
			println("onSuccess : " + dataIndex);
		}

		@Override
		public void onIgnore(int dataIndex, DataExchangeException e)
		{
			println("onIgnore : " + dataIndex);
		}

		@Override
		public void onSetNullColumnValue(int dataIndex, String columnName, String rawColumnValue,
				DataExchangeException e)
		{
			println("onSetNullColumnValue : " + dataIndex + ", " + columnName);
		}
	}
}
