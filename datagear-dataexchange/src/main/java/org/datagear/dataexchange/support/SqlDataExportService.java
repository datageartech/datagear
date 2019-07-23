/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.io.Writer;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.List;

import org.datagear.dataexchange.AbstractDevotedTextDataExportService;
import org.datagear.dataexchange.DataExchangeContext;
import org.datagear.dataexchange.RowDataIndex;
import org.datagear.dataexchange.TextDataExportListener;
import org.datagear.dataexchange.TextDataExportOption;
import org.datagear.dbinfo.ColumnInfo;
import org.datagear.dbinfo.DatabaseInfoResolver;

/**
 * SQL导出服务。
 * 
 * @author datagear@163.com
 *
 */
public class SqlDataExportService extends AbstractDevotedTextDataExportService<SqlDataExport>
{
	public static final String LINE_SEPARATOR = "\r\n";

	public SqlDataExportService()
	{
		super();
	}

	public SqlDataExportService(DatabaseInfoResolver databaseInfoResolver)
	{
		super(databaseInfoResolver);
	}

	@Override
	protected void exchange(SqlDataExport dataExchange, DataExchangeContext context) throws Throwable
	{
		TextDataExportContext exportContext = (TextDataExportContext) context;

		Writer sqlWriter = getResource(dataExchange.getWriterFactory(), exportContext);

		Connection cn = context.getConnection();
		cn.setReadOnly(true);

		ResultSet rs = dataExchange.getQuery().execute(cn);
		List<ColumnInfo> columnInfos = getColumnInfos(cn, rs);

		writeRecords(dataExchange, cn, columnInfos, rs, sqlWriter, exportContext);
	}

	/**
	 * 写记录。
	 * 
	 * @param dataExchange
	 * @param cn
	 * @param columnInfos
	 * @param rs
	 * @param out
	 * @param exportContext
	 */
	protected void writeRecords(SqlDataExport dataExchange, Connection cn, List<ColumnInfo> columnInfos, ResultSet rs,
			Writer out, TextDataExportContext exportContext) throws Throwable
	{
		TextDataExportListener listener = dataExchange.getListener();
		TextDataExportOption exportOption = dataExchange.getExportOption();
		int columnCount = columnInfos.size();

		DatabaseMetaData metaData = cn.getMetaData();
		String quote = metaData.getIdentifierQuoteString();

		long row = 0;

		while (rs.next())
		{
			exportContext.setDataIndex(RowDataIndex.valueOf(row));

			out.write("INSERT INTO ");
			out.write(quote);
			out.write(dataExchange.getTableName());
			out.write(quote);
			out.write(" (");

			for (int i = 0; i < columnCount; i++)
			{
				ColumnInfo columnInfo = columnInfos.get(i);

				if (i > 0)
					out.write(",");

				out.write(quote);
				out.write(columnInfo.getName());
				out.write(quote);
			}

			out.write(") VALUES(");

			for (int i = 0; i < columnCount; i++)
			{
				ColumnInfo columnInfo = columnInfos.get(i);

				String value = null;

				try
				{
					value = getStringValue(cn, rs, i + 1, columnInfo.getType(), exportContext.getDataFormatContext());
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

				if (i > 0)
					out.write(",");

				if (value == null)
				{
					out.write("NULL");
				}
				else if (isSqlStringType(columnInfo.getType()))
				{
					out.write('\'');
					out.write(escapeSqlStringValue(value));
					out.write('\'');
				}
				else
					out.write(value);
			}

			out.write(");");

			out.write(LINE_SEPARATOR);

			if (listener != null)
				listener.onSuccess(exportContext.getDataIndex());

			row++;
		}
	}

	protected String escapeSqlStringValue(String value)
	{
		StringBuilder sb = new StringBuilder();

		char[] cs = value.toCharArray();

		for (char c : cs)
		{
			if (c == '\'')
				sb.append("''");
			else
				sb.append(c);
		}

		return sb.toString();
	}

	/**
	 * 是否是SQL字符串类型。
	 * 
	 * @param sqlType
	 * @return
	 */
	protected boolean isSqlStringType(int sqlType)
	{
		return (Types.CHAR == sqlType || Types.VARCHAR == sqlType || Types.LONGVARCHAR == sqlType
				|| Types.CLOB == sqlType || Types.NCHAR == sqlType || Types.NVARCHAR == sqlType
				|| Types.LONGNVARCHAR == sqlType || Types.NCLOB == sqlType || Types.SQLXML == sqlType);
	}
}
