/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.dataexchange.support;

import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.datagear.dataexchange.AbstractDevotedDBMetaDataExchangeService;
import org.datagear.dataexchange.DataExchangeContext;
import org.datagear.dataexchange.DataExchangeException;
import org.datagear.dataexchange.IndexFormatDataExchangeContext;
import org.datagear.dataexchange.RowDataIndex;
import org.datagear.dataexchange.TextDataExportListener;
import org.datagear.dataexchange.TextDataExportOption;
import org.datagear.meta.Column;
import org.datagear.meta.resolver.DBMetaResolver;
import org.datagear.util.JdbcUtil;
import org.datagear.util.QueryResultSet;

/**
 * CSV导出服务。
 * 
 * @author datagear@163.com
 *
 */
public class CsvDataExportService extends AbstractDevotedDBMetaDataExchangeService<CsvDataExport>
{
	public CsvDataExportService()
	{
		super();
	}

	public CsvDataExportService(DBMetaResolver dbMetaResolver)
	{
		super(dbMetaResolver);
	}

	@Override
	protected DataExchangeContext createDataExchangeContext(CsvDataExport dataExchange)
	{
		return IndexFormatDataExchangeContext.valueOf(dataExchange);
	}

	@Override
	protected void exchange(CsvDataExport dataExchange, DataExchangeContext context) throws Throwable
	{
		IndexFormatDataExchangeContext exportContext = IndexFormatDataExchangeContext.cast(context);

		TextDataExportListener listener = dataExchange.getListener();
		TextDataExportOption exportOption = dataExchange.getExportOption();

		Writer csvWriter = getResource(dataExchange.getWriterFactory(), exportContext);

		Connection cn = exportContext.getConnection();
		JdbcUtil.setReadonlyIfSupports(cn, true);

		QueryResultSet qrs = dataExchange.getQuery().execute(cn);
		context.addContextCloseable(qrs);

		ResultSet rs = qrs.getResultSet();

		List<Column> columns = getColumns(cn, rs);
		int columnCount = columns.size();

		CSVPrinter csvPrinter = buildCSVPrinter(csvWriter);

		writeColumns(csvPrinter, columns);

		long row = 0;

		while (rs.next())
		{
			exportContext.setDataIndex(RowDataIndex.valueOf(row));

			for (int i = 0; i < columnCount; i++)
			{
				Column column = columns.get(i);

				String value = null;

				try
				{
					value = getStringValue(cn, rs, i + 1, column.getType(), exportContext.getDataFormatContext());
				}
				catch (Throwable t)
				{
					if (exportOption.isNullForIllegalColumnValue())
					{
						value = null;

						if (listener != null)
							listener.onSetNullTextValue(exportContext.getDataIndex(), column.getName(),
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

	protected void writeColumns(CSVPrinter csvPrinter, List<Column> columns) throws DataExchangeException
	{
		try
		{
			for (Column column : columns)
				csvPrinter.print(column.getName());

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
