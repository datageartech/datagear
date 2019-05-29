/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.datagear.dataexchange.DataExportException;
import org.datagear.dataexchange.DataExporter;
import org.datagear.dataexchange.ExportResult;
import org.datagear.dbinfo.ColumnInfo;
import org.datagear.dbinfo.DatabaseInfoResolver;

/**
 * CSV {@linkplain DataExporter}。
 * 
 * @author datagear@163.com
 *
 */
public class CsvDataExporter extends AbstractTextDevotedDataExporter<CsvExport>
{
	public CsvDataExporter()
	{
		super();
	}

	public CsvDataExporter(DatabaseInfoResolver databaseInfoResolver)
	{
		super(databaseInfoResolver);
	}

	@Override
	public ExportResult expt(CsvExport expt) throws DataExportException
	{
		ExportResult exportResult = new ExportResult();

		long startTime = System.currentTimeMillis();

		Connection cn = expt.getConnection();
		ResultSet rs = expt.getResultSet();

		ColumnInfo[] columnInfos = getColumnInfos(cn, rs);
		CSVPrinter csvPrinter = buildCSVPrinter(expt);
		SelectContext selectContext = new SelectContext(expt.getDataFormat());

		exportResult.setDuration(System.currentTimeMillis() - startTime);

		return exportResult;
	}

	/**
	 * 构建{@linkplain CSVPrinter}。
	 * 
	 * @param expt
	 * @return
	 * @throws DataExportException
	 */
	protected CSVPrinter buildCSVPrinter(CsvExport expt) throws DataExportException
	{
		try
		{
			return new CSVPrinter(expt.getWriter(), CSVFormat.DEFAULT);
		}
		catch (IOException e)
		{
			throw new DataExportException(e);
		}
	}

	protected void writeColumnInfos(CSVPrinter csvPrinter, ColumnInfo[] columnInfos) throws IOException
	{
		for (int i = 0; i < columnInfos.length; i++)
			csvPrinter.print(columnInfos[i].getName());

		csvPrinter.println();
	}

	protected void writeDataRecord(CSVPrinter csvPrinter, String[] values) throws IOException
	{
		for (int i = 0; i < values.length; i++)
			csvPrinter.print(values[i]);

		csvPrinter.println();
	}
}
