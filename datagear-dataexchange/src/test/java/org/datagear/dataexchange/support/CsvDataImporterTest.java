/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.io.Reader;
import java.sql.Connection;

import org.datagear.connection.IOUtil;
import org.datagear.connection.JdbcUtil;
import org.datagear.dataexchange.DataexchangeTestSupport;
import org.datagear.dataexchange.SimpleConnectionFactory;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * {@linkplain CsvDataImporter}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class CsvDataImporterTest extends DataexchangeTestSupport
{
	public static final String TABLE_NAME = "T_DATA_IMPORT";

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private CsvDataImporter csvDataImporter;

	public CsvDataImporterTest()
	{
		super();
		this.csvDataImporter = new CsvDataImporter(databaseInfoResolver);
	}

	@Test
	public void imptTest_ignoreInexistentColumn_false() throws Exception
	{
		DataFormat dataFormat = new DataFormat();

		Connection cn = getConnection();
		Reader reader = null;

		try
		{
			cn = getConnection();
			reader = IOUtil.getReader(getTestResourceInputStream("CsvDataImporterTest_ignoreInexistentColumn.csv"),
					"UTF-8");

			CsvDataImport impt = new CsvDataImport(new SimpleConnectionFactory(cn, false), true, dataFormat, TABLE_NAME,
					false, reader);

			clearTable(cn, TABLE_NAME);

			this.thrown.expect(ColumnNotFoundException.class);
			this.thrown.expectMessage("Column [INEXISTENT_COLUMN] not found");

			this.csvDataImporter.impt(impt);
		}
		finally
		{
			JdbcUtil.closeConnection(cn);
			IOUtil.close(reader);
		}
	}

	@Test
	public void imptTest_ignoreInexistentColumn_true() throws Exception
	{
		DataFormat dataFormat = new DataFormat();

		Connection cn = null;
		Reader reader = null;

		try
		{
			cn = getConnection();
			reader = IOUtil.getReader(getTestResourceInputStream("CsvDataImporterTest_ignoreInexistentColumn.csv"),
					"UTF-8");

			CsvDataImport impt = new CsvDataImport(new SimpleConnectionFactory(cn, false), true, dataFormat, TABLE_NAME,
					true, reader);

			clearTable(cn, TABLE_NAME);

			this.csvDataImporter.impt(impt);

			int count = getCount(cn, TABLE_NAME);

			Assert.assertEquals(3, count);
		}
		finally
		{
			JdbcUtil.closeConnection(cn);
			IOUtil.close(reader);
		}
	}

	@Test
	public void imptTest_abortOnError_false() throws Exception
	{
		DataFormat dataFormat = new DataFormat();

		Connection cn = null;
		Reader reader = null;

		try
		{
			cn = getConnection();
			reader = IOUtil.getReader(getTestResourceInputStream("CsvDataImporterTest_abortOnError.csv"), "UTF-8");

			CsvDataImport impt = new CsvDataImport(new SimpleConnectionFactory(cn, false), false, dataFormat,
					TABLE_NAME, true, reader);

			clearTable(cn, TABLE_NAME);

			this.csvDataImporter.impt(impt);

			int count = getCount(cn, TABLE_NAME);

			Assert.assertEquals(2, count);
		}
		finally
		{
			JdbcUtil.closeConnection(cn);
			IOUtil.close(reader);
		}
	}

	@Test
	public void imptTest_abortOnError_true() throws Exception
	{
		DataFormat dataFormat = new DataFormat();

		Connection cn = null;
		Reader reader = null;

		try
		{
			cn = getConnection();
			reader = IOUtil.getReader(getTestResourceInputStream("CsvDataImporterTest_abortOnError.csv"), "UTF-8");

			CsvDataImport impt = new CsvDataImport(new SimpleConnectionFactory(cn, false), true, dataFormat, TABLE_NAME,
					true, reader);

			clearTable(cn, TABLE_NAME);

			this.thrown.expect(IllegalSourceValueException.class);

			this.csvDataImporter.impt(impt);
		}
		finally
		{
			JdbcUtil.closeConnection(cn);
			IOUtil.close(reader);
		}
	}
}
