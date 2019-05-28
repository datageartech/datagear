/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

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
import org.junit.Test;

/**
 * {@linkplain CsvDataImporter}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class CsvDataImporterTest extends DataexchangeTestSupport
{
	public static final String TABLE_NAME = "T_DATAEXCHANGE";

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
	public void imptTest() throws Exception
	{
		Reader reader = IOUtil.getReader(CsvDataImporterTest.class.getClassLoader()
				.getResourceAsStream("org/datagear/dataexchange/support/CsvDataImporterTest.csv"), "UTF-8");
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
		}
	}
}
