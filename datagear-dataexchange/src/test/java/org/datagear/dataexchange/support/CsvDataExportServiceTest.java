/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.sql.Connection;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.datagear.dataexchange.ClasspathReaderResourceFactory;
import org.datagear.dataexchange.DataFormat;
import org.datagear.dataexchange.DataexchangeTestSupport;
import org.datagear.dataexchange.ExceptionResolve;
import org.datagear.dataexchange.FileWriterResourceFactory;
import org.datagear.dataexchange.ResourceFactory;
import org.datagear.dataexchange.SimpleConnectionFactory;
import org.datagear.dataexchange.TableQuery;
import org.datagear.dataexchange.TextDataExportOption;
import org.datagear.dataexchange.TextValueDataImportOption;
import org.datagear.util.IOUtil;
import org.datagear.util.JdbcUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * {@linkplain CsvDataExportService}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class CsvDataExportServiceTest extends DataexchangeTestSupport
{
	public static final String TABLE_NAME = "T_DATA_EXPORT";

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private CsvDataImportService csvDataImportService;
	private CsvDataExportService csvDataExportService;

	public CsvDataExportServiceTest()
	{
		super();
		this.csvDataImportService = new CsvDataImportService(databaseInfoResolver);
		this.csvDataExportService = new CsvDataExportService(databaseInfoResolver);
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
			ResourceFactory<Reader> readerFactory = ClasspathReaderResourceFactory
					.valueOf(getResourceClasspath("support/CsvDataExportServiceTest.csv"), "UTF-8");

			TextValueDataImportOption textValueDataImportOption = new TextValueDataImportOption(ExceptionResolve.ABORT,
					true, true);
			CsvDataImport impt = new CsvDataImport(new SimpleConnectionFactory(cn, false), dataFormat,
					textValueDataImportOption, TABLE_NAME, readerFactory);

			clearTable(cn, TABLE_NAME);

			this.csvDataImportService.exchange(impt);
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
		dataFormat.setBinaryFormat("0x${Hex}");

		File outFile = new File("target/CsvDataExportServiceTest.csv");

		Connection cn = null;
		Writer writer = null;

		try
		{
			cn = getConnection();

			ResourceFactory<Writer> writerFactory = FileWriterResourceFactory.valueOf(outFile, "UTF-8");

			CsvDataExport csvDataExport = new CsvDataExport(new SimpleConnectionFactory(cn, false), dataFormat,
					new TextDataExportOption(true), new TableQuery(TABLE_NAME), writerFactory);

			this.csvDataExportService.exchange(csvDataExport);
		}
		finally
		{
			IOUtil.close(writer);
			JdbcUtil.closeConnection(cn);
		}

		CSVParser sourceCsvParser = CSVFormat.DEFAULT.parse(ClasspathReaderResourceFactory
				.valueOf(getResourceClasspath("support/CsvDataExportServiceTest.csv"), "UTF-8").get());

		CSVParser exportCsvParser = CSVFormat.DEFAULT
				.parse(new InputStreamReader(new FileInputStream(outFile), "UTF-8"));

		List<CSVRecord> sourceRecords = sourceCsvParser.getRecords();
		List<CSVRecord> exportRecords = exportCsvParser.getRecords();

		assertEquals(sourceRecords.size(), exportRecords.size());

		for (int i = 0; i < sourceRecords.size(); i++)
		{
			CSVRecord sourceRecord = sourceRecords.get(i);
			CSVRecord exportRecord = exportRecords.get(i);

			assertEquals(sourceRecord.size(), exportRecord.size());

			for (int j = 0; j < sourceRecord.size(); j++)
			{
				String sourceValue = sourceRecord.get(j);
				String exportValue = exportRecord.get(j);

				assertEquals(sourceValue, exportValue);
			}
		}

		sourceCsvParser.close();
		exportCsvParser.close();
	}
}
