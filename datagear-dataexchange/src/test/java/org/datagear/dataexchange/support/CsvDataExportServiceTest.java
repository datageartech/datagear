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
import org.datagear.dataexchange.DataFormat;
import org.datagear.dataexchange.DataexchangeTestSupport;
import org.datagear.dataexchange.ExceptionResolve;
import org.datagear.dataexchange.TableQuery;
import org.datagear.dataexchange.TextDataExportOption;
import org.datagear.dataexchange.ValueDataImportOption;
import org.datagear.util.FileUtil;
import org.datagear.util.IOUtil;
import org.datagear.util.JdbcUtil;
import org.datagear.util.resource.ClasspathReaderResourceFactory;
import org.datagear.util.resource.FileWriterResourceFactory;
import org.datagear.util.resource.ResourceFactory;
import org.datagear.util.resource.SimpleConnectionFactory;
import org.junit.Before;
import org.junit.Test;

/**
 * {@linkplain CsvDataExportService}单元测试类。
 * 
 * @author datagear@163.com
 *
 */
public class CsvDataExportServiceTest extends DataexchangeTestSupport
{
	private CsvDataImportService csvDataImportService;
	private CsvDataExportService csvDataExportService;

	public CsvDataExportServiceTest()
	{
		super();
		this.csvDataImportService = new CsvDataImportService(dbMetaResolver);
		this.csvDataExportService = new CsvDataExportService(dbMetaResolver);
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
					.valueOf(getResourceClasspath("support/CsvDataExportServiceTest.csv"), IOUtil.CHARSET_UTF_8);

			ValueDataImportOption valueDataImportOption = new ValueDataImportOption(ExceptionResolve.ABORT, true, true);
			CsvDataImport impt = new CsvDataImport(new SimpleConnectionFactory(cn, false), dataFormat,
					valueDataImportOption, TABLE_NAME_DATA_EXPORT, readerFactory);

			clearTable(cn, TABLE_NAME_DATA_EXPORT);

			this.csvDataImportService.exchange(impt);
		}
		finally
		{
			JdbcUtil.closeConnection(cn);
			IOUtil.close(reader);
		}

		try
		{
			cn = getConnection();
			ResourceFactory<Reader> readerFactory = ClasspathReaderResourceFactory
					.valueOf(getResourceClasspath("support/CsvDataImportServiceTest_unsigned_number.csv"),
							IOUtil.CHARSET_UTF_8);

			ValueDataImportOption valueDataImportOption = new ValueDataImportOption(ExceptionResolve.ABORT, true, true);
			CsvDataImport impt = new CsvDataImport(new SimpleConnectionFactory(cn, false), dataFormat,
					valueDataImportOption, TABLE_NAME_UNSIGNED_NUMBER, readerFactory);

			clearTable(cn, TABLE_NAME_UNSIGNED_NUMBER);

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

		File outFile = FileUtil.getFile("target/CsvDataExportServiceTest.csv");

		Connection cn = null;
		Writer writer = null;

		try
		{
			cn = getConnection();

			ResourceFactory<Writer> writerFactory = FileWriterResourceFactory.valueOf(outFile, IOUtil.CHARSET_UTF_8);

			CsvDataExport csvDataExport = new CsvDataExport(new SimpleConnectionFactory(cn, false), dataFormat,
					new TextDataExportOption(true), new TableQuery(TABLE_NAME_DATA_EXPORT), writerFactory);

			this.csvDataExportService.exchange(csvDataExport);
		}
		finally
		{
			IOUtil.close(writer);
			JdbcUtil.closeConnection(cn);
		}

		CSVParser sourceCsvParser = CSVFormat.DEFAULT.parse(ClasspathReaderResourceFactory
				.valueOf(getResourceClasspath("support/CsvDataExportServiceTest.csv"), IOUtil.CHARSET_UTF_8).get());

		CSVParser exportCsvParser = CSVFormat.DEFAULT
				.parse(new InputStreamReader(new FileInputStream(outFile), IOUtil.CHARSET_UTF_8));

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

	@Test
	public void exptTest_unsigned_number() throws Exception
	{
		DataFormat dataFormat = new DataFormat();
		dataFormat.setBinaryFormat("0x${Hex}");
		dataFormat.setNumberFormat("#.##");

		File outFile = FileUtil.getFile("target/CsvDataExportServiceTest_unsigned_number.csv");

		Connection cn = null;
		Writer writer = null;

		try
		{
			cn = getConnection();

			ResourceFactory<Writer> writerFactory = FileWriterResourceFactory.valueOf(outFile, IOUtil.CHARSET_UTF_8);

			CsvDataExport csvDataExport = new CsvDataExport(new SimpleConnectionFactory(cn, false), dataFormat,
					new TextDataExportOption(true), new TableQuery(TABLE_NAME_UNSIGNED_NUMBER), writerFactory);

			this.csvDataExportService.exchange(csvDataExport);
		}
		finally
		{
			IOUtil.close(writer);
			JdbcUtil.closeConnection(cn);
		}

		CSVParser sourceCsvParser = CSVFormat.DEFAULT.parse(ClasspathReaderResourceFactory
				.valueOf(getResourceClasspath("support/CsvDataImportServiceTest_unsigned_number.csv"),
						IOUtil.CHARSET_UTF_8)
				.get());

		CSVParser exportCsvParser = CSVFormat.DEFAULT
				.parse(new InputStreamReader(new FileInputStream(outFile), IOUtil.CHARSET_UTF_8));

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
