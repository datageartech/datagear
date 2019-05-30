/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.datagear.connection.IOUtil;
import org.datagear.connection.JdbcUtil;
import org.datagear.dataexchange.DataexchangeTestSupport;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * {@linkplain CsvDataExporter}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class CsvDataExporterTest extends DataexchangeTestSupport
{
	public static final String TABLE_NAME = "T_DATA_EXPORT";

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private CsvDataImporter csvDataImporter;
	private CsvDataExporter csvDataExporter;

	public CsvDataExporterTest()
	{
		super();
		this.csvDataImporter = new CsvDataImporter(databaseInfoResolver);
		this.csvDataExporter = new CsvDataExporter(databaseInfoResolver);
	}

	@Before
	public void initTableData() throws Exception
	{
		DataFormat dataFormat = new DataFormat();

		Connection cn = null;
		Reader reader = null;

		try
		{
			cn = getConnection();
			reader = IOUtil.getReader(getTestResourceInputStream("CsvDataExporterTest.csv"), "UTF-8");

			CsvDataImport impt = new CsvDataImport(buildSimpleDataSource(cn), true, reader, dataFormat, true,
					TABLE_NAME);

			clearTable(cn, TABLE_NAME);
			this.csvDataImporter.impt(impt);
		}
		finally
		{
			JdbcUtil.closeConnection(cn);
			IOUtil.close(reader);
		}
	}

	@Test
	public void exptTest() throws Exception
	{
		DataFormat dataFormat = new DataFormat();

		File outFile = new File("target/CsvDataExporterTest.csv");

		Connection cn = null;
		Writer writer = null;

		try
		{
			cn = getConnection();

			Statement st = cn.createStatement();
			ResultSet rs = st.executeQuery("select * from " + TABLE_NAME + " order by id asc");

			writer = new OutputStreamWriter(new FileOutputStream(outFile), "UTF-8");

			CsvDataExport csvDataExport = new CsvDataExport(buildSimpleDataSource(cn), true, rs, writer, dataFormat);

			this.csvDataExporter.expt(csvDataExport);
		}
		finally
		{
			IOUtil.close(writer);
			JdbcUtil.closeConnection(cn);
		}

		CSVParser csvParser = CSVFormat.DEFAULT.parse(new InputStreamReader(new FileInputStream(outFile), "UTF-8"));

		List<CSVRecord> records = csvParser.getRecords();

		assertEquals(4, records.size());

		{
			CSVRecord cr = records.get(0);

			assertEquals("ID", cr.get(0));
			assertEquals("NAME", cr.get(1));
		}

		{
			CSVRecord cr = records.get(1);

			assertEquals("1", cr.get(0));
		}

		{
			CSVRecord cr = records.get(2);

			assertEquals("2", cr.get(0));
		}

		{
			CSVRecord cr = records.get(3);

			assertEquals("3", cr.get(0));
		}

		csvParser.close();
	}

	@Override
	protected InputStream getTestResourceInputStream(String resourceName) throws IOException
	{
		return CsvDataExporterTest.class.getClassLoader()
				.getResourceAsStream("org/datagear/dataexchange/support/" + resourceName);
	}
}
