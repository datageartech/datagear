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
import org.datagear.connection.JdbcUtil;
import org.datagear.dataexchange.DataExportException;
import org.datagear.dataexchange.DataExportReporter;
import org.datagear.dataexchange.DataExportResult;
import org.datagear.dataexchange.DataExporter;
import org.datagear.dbinfo.ColumnInfo;
import org.datagear.dbinfo.DatabaseInfoResolver;

/**
 * CSV {@linkplain DataExporter}。
 * 
 * @author datagear@163.com
 *
 */
public class CsvDataExporter extends AbstractTextDevotedDataExporter<CsvDataExport>
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
	public DataExportResult expt(CsvDataExport expt) throws DataExportException
	{
		DataExportResult dataExportResult = new DataExportResult();

		long startTime = System.currentTimeMillis();

		boolean abortOnError = expt.isAbortOnError();
		DataExportReporter dataExportReporter = (expt.hasDataExportReporter() ? expt.getDataExportReporter() : null);

		SelectContext selectContext = buildSelectContext(expt);

		Connection cn = null;

		try
		{
			cn = expt.getDataSource().getConnection();

			ResultSet rs = expt.getQuery().execute(cn);

			ColumnInfo[] columnInfos = getColumnInfos(cn, rs);
			CSVPrinter csvPrinter = buildCSVPrinter(expt);

			writeColumnInfos(csvPrinter, columnInfos);
			int dataIndex = 0;

			while (rs.next())
			{
				for (int i = 0; i < columnInfos.length; i++)
				{
					String value = null;

					try
					{
						value = getStringValue(expt, cn, rs, i + 1, columnInfos[i].getType(), selectContext);
					}
					catch (SQLTransientException e)
					{
						GetColumnValueException e1 = new GetColumnValueException(dataIndex, columnInfos[i].getName(),
								e);

						if (abortOnError)
							throw e1;

						if (dataExportReporter != null)
							dataExportReporter.report(e1);
					}
					catch (IOException e)
					{
						GetColumnValueException e1 = new GetColumnValueException(dataIndex, columnInfos[i].getName(),
								e);

						if (abortOnError)
							throw e1;

						if (dataExportReporter != null)
							dataExportReporter.report(e1);
					}
					catch (UnsupportedSqlTypeException e)
					{
						GetColumnValueException e1 = new GetColumnValueException(dataIndex, columnInfos[i].getName(),
								e);

						if (abortOnError)
							throw e1;

						if (dataExportReporter != null)
							dataExportReporter.report(e1);
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
		finally
		{
			JdbcUtil.closeConnection(cn);
		}

		dataExportResult.setDuration(System.currentTimeMillis() - startTime);

		return dataExportResult;
	}

	/**
	 * 构建{@linkplain CSVPrinter}。
	 * 
	 * @param expt
	 * @return
	 * @throws DataExportException
	 */
	protected CSVPrinter buildCSVPrinter(CsvDataExport expt) throws DataExportException
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
