/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.datagear.connection.IOUtil;
import org.datagear.connection.JdbcUtil;
import org.datagear.dataexchange.DataexchangeTestSupport;
import org.datagear.dbinfo.DatabaseInfoResolver;
import org.datagear.dbinfo.DevotedDatabaseInfoResolver;
import org.datagear.dbinfo.GenericDatabaseInfoResolver;
import org.datagear.dbinfo.WildcardDevotedDatabaseInfoResolver;
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
	public static final String TABLE_NAME = "T_DATAEXCHANGE";

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	protected DatabaseInfoResolver databaseInfoResolver;

	private CsvDataImporter csvDataImporter;

	public CsvDataImporterTest()
	{
		super();

		List<DevotedDatabaseInfoResolver> devotedDatabaseInfoResolver = new ArrayList<DevotedDatabaseInfoResolver>();
		devotedDatabaseInfoResolver.add(new WildcardDevotedDatabaseInfoResolver());
		this.databaseInfoResolver = new GenericDatabaseInfoResolver(devotedDatabaseInfoResolver);

		this.csvDataImporter = new CsvDataImporter(this.databaseInfoResolver);
	}

	@Test
	public void imptTest_ignoreInexistentColumn_false() throws Exception
	{
		Reader reader = IOUtil.getReader(getResourceInputStream("CsvDataImporterTest_ignoreInexistentColumn.csv"),
				"UTF-8");
		DataFormat dataFormat = new DataFormat();

		Connection cn = getConnection();

		CsvImport impt = new CsvImport(cn, true, null, reader, dataFormat, false, TABLE_NAME);

		try
		{
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
		Reader reader = IOUtil.getReader(getResourceInputStream("CsvDataImporterTest_ignoreInexistentColumn.csv"),
				"UTF-8");
		DataFormat dataFormat = new DataFormat();

		Connection cn = getConnection();

		CsvImport impt = new CsvImport(cn, true, null, reader, dataFormat, true, TABLE_NAME);

		try
		{
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
		Reader reader = IOUtil.getReader(getResourceInputStream("CsvDataImporterTest_abortOnError.csv"), "UTF-8");
		DataFormat dataFormat = new DataFormat();

		Connection cn = getConnection();

		CsvImport impt = new CsvImport(cn, false, null, reader, dataFormat, true, TABLE_NAME);

		try
		{
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
		Reader reader = IOUtil.getReader(getResourceInputStream("CsvDataImporterTest_abortOnError.csv"), "UTF-8");
		DataFormat dataFormat = new DataFormat();

		Connection cn = getConnection();

		CsvImport impt = new CsvImport(cn, true, null, reader, dataFormat, true, TABLE_NAME);

		try
		{
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

	protected InputStream getResourceInputStream(String resourceName) throws IOException
	{
		return CsvDataImporterTest.class.getClassLoader()
				.getResourceAsStream("org/datagear/dataexchange/support/" + resourceName);
	}
}
