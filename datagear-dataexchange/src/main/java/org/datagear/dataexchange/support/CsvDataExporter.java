/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLTransientException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.datagear.dataexchange.DataExportException;
import org.datagear.dataexchange.DataExporter;
import org.datagear.dataexchange.ExportReporter;
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

		boolean abortOnError = expt.isAbortOnError();
		ExportReporter exportReporter = (expt.hasExportReporter() ? expt.getExportReporter() : null);

		Connection cn = expt.getConnection();
		ResultSet rs = expt.getResultSet();

		ColumnInfo[] columnInfos = getColumnInfos(cn, rs);
		CSVPrinter csvPrinter = buildCSVPrinter(expt);
		SelectContext selectContext = new SelectContext(expt.getDataFormat());

		writeColumnInfos(csvPrinter, columnInfos);

		try
		{
			int dataIndex = 0;

			while (rs.next())
			{
				for (int i = 0; i < columnInfos.length; i++)
				{
					String value = null;

					try
					{
						value = getStringValue(cn, rs, i + 1, columnInfos[i].getType(), selectContext);
					}
					catch (SQLTransientException e)
					{
						GetColumnValueException e1 = new GetColumnValueException(dataIndex, columnInfos[i].getName(),
								e);

						if (abortOnError)
							throw e1;

						if (exportReporter != null)
							exportReporter.report(e1);
					}
					catch (IOException e)
					{
						GetColumnValueException e1 = new GetColumnValueException(dataIndex, columnInfos[i].getName(),
								e);

						if (abortOnError)
							throw e1;

						if (exportReporter != null)
							exportReporter.report(e1);
					}
					catch (UnsupportedSqlTypeException e)
					{
						GetColumnValueException e1 = new GetColumnValueException(dataIndex, columnInfos[i].getName(),
								e);

						if (abortOnError)
							throw e1;

						if (exportReporter != null)
							exportReporter.report(e1);
					}

					csvPrinter.print(value);
				}

				csvPrinter.println();

				dataIndex++;
			}
		}
		catch (SQLException e)
		{
			throw new DataExportException(e);
		}
		catch (IOException e)
		{
			throw new DataExportException(e);
		}

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

	protected void writeColumnInfos(CSVPrinter csvPrinter, ColumnInfo[] columnInfos) throws DataExportException
	{
		try
		{
			for (int i = 0; i < columnInfos.length; i++)
				csvPrinter.print(columnInfos[i].getName());

			csvPrinter.println();
		}
		catch (IOException e)
		{
			throw new DataExportException(e);
		}
	}

	protected void writeDataRecord(CSVPrinter csvPrinter, String[] values) throws IOException
	{
		for (int i = 0; i < values.length; i++)
			csvPrinter.print(values[i]);

		csvPrinter.println();
	}
}
