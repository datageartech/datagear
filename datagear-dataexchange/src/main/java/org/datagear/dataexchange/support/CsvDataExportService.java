/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.datagear.dataexchange.AbstractDevotedTextDataExportService;
import org.datagear.dataexchange.ConnectionFactory;
import org.datagear.dataexchange.DataExchangeException;
import org.datagear.dataexchange.RowDataIndex;
import org.datagear.dataexchange.TextDataExportListener;
import org.datagear.dataexchange.TextDataExportOption;
import org.datagear.dbinfo.ColumnInfo;
import org.datagear.dbinfo.DatabaseInfoResolver;

/**
 * CSV导出服务。
 * 
 * @author datagear@163.com
 *
 */
public class CsvDataExportService extends AbstractDevotedTextDataExportService<CsvDataExport>
{
	public CsvDataExportService()
	{
		super();
	}

	public CsvDataExportService(DatabaseInfoResolver databaseInfoResolver)
	{
		super(databaseInfoResolver);
	}

	@Override
	public void exchange(CsvDataExport dataExchange) throws DataExchangeException
	{
		TextDataExportListener listener = dataExchange.getListener();

		if (listener != null)
			listener.onStart();

		ConnectionFactory connectionFactory = dataExchange.getConnectionFactory();

		TextDataExportOption exportOption = dataExchange.getExportOption();

		TextDataExportContext exportContext = createTextDataExportContext(dataExchange);

		Writer csvWriter = null;
		Connection cn = null;

		try
		{
			csvWriter = dataExchange.getWriterFactory().get();
			cn = connectionFactory.get();

			ResultSet rs = dataExchange.getQuery().execute(cn);

			List<ColumnInfo> columnInfos = getColumnInfos(cn, rs);
			int columnCount = columnInfos.size();

			CSVPrinter csvPrinter = buildCSVPrinter(csvWriter);

			writeColumnInfos(csvPrinter, columnInfos);

			long row = 0;

			while (rs.next())
			{
				exportContext.setDataIndex(RowDataIndex.valueOf(row));

				for (int i = 0; i < columnCount; i++)
				{
					ColumnInfo columnInfo = columnInfos.get(i);

					String value = null;

					try
					{
						value = getStringValue(cn, rs, i + 1, columnInfo.getType(),
								exportContext.getDataFormatContext());
					}
					catch (Throwable t)
					{
						if (exportOption.isNullForIllegalColumnValue())
						{
							value = null;

							if (listener != null)
								listener.onSetNullTextValue(exportContext.getDataIndex(), columnInfo.getName(),
										wrapToDataExchangeException(t));
						}
						else
							throw t;
					}

					csvPrinter.print(value);
				}

				csvPrinter.println();

				if (listener != null)
					listener.onSuccess(exportContext.getDataIndex());

				row++;
			}

			if (listener != null)
				listener.onSuccess();
		}
		catch (Throwable t)
		{
			DataExchangeException e = wrapToDataExchangeException(t);

			if (listener != null)
				listener.onException(e);
			else
				throw e;
		}
		finally
		{
			releaseResource(dataExchange.getWriterFactory(), csvWriter);
			releaseResource(connectionFactory, cn);

			if (listener != null)
				listener.onFinish();
		}
	}

	/**
	 * 构建{@linkplain CSVPrinter}。
	 * 
	 * @param writer
	 * @return
	 * @throws DataExportException
	 */
	protected CSVPrinter buildCSVPrinter(Writer writer) throws DataExchangeException
	{
		try
		{
			return new CSVPrinter(writer, CSVFormat.DEFAULT);
		}
		catch (IOException e)
		{
			throw new DataExchangeException(e);
		}
	}

	protected void writeColumnInfos(CSVPrinter csvPrinter, List<ColumnInfo> columnInfos) throws DataExchangeException
	{
		try
		{
			for (ColumnInfo columnInfo : columnInfos)
				csvPrinter.print(columnInfo.getName());

			csvPrinter.println();
		}
		catch (IOException e)
		{
			throw new DataExchangeException(e);
		}
	}

	protected void writeDataRecord(CSVPrinter csvPrinter, String[] values) throws IOException
	{
		for (int i = 0; i < values.length; i++)
			csvPrinter.print(values[i]);

		csvPrinter.println();
	}
}
