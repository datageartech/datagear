/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.io.Writer;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.datagear.dataexchange.AbstractDevotedDbInfoAwareDataExchangeService;
import org.datagear.dataexchange.DataExchangeContext;
import org.datagear.dataexchange.IndexFormatDataExchangeContext;
import org.datagear.dataexchange.RowDataIndex;
import org.datagear.dataexchange.TextDataExportListener;
import org.datagear.dbinfo.ColumnInfo;
import org.datagear.dbinfo.DatabaseInfoResolver;
import org.datagear.util.JdbcUtil;

/**
 * SQL导出服务。
 * 
 * @author datagear@163.com
 *
 */
public class SqlDataExportService extends AbstractDevotedDbInfoAwareDataExchangeService<SqlDataExport>
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
	protected DataExchangeContext createDataExchangeContext(SqlDataExport dataExchange)
	{
		return IndexFormatDataExchangeContext.valueOf(dataExchange);
	}

	@Override
	protected void exchange(SqlDataExport dataExchange, DataExchangeContext context) throws Throwable
	{
		IndexFormatDataExchangeContext exportContext = IndexFormatDataExchangeContext.cast(context);

		Writer sqlWriter = getResource(dataExchange.getWriterFactory(), exportContext);

		Connection cn = context.getConnection();
		JdbcUtil.setReadonlyIfSupports(cn, true);

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
			Writer out, IndexFormatDataExchangeContext exportContext) throws Throwable
	{
		TextDataExportListener listener = dataExchange.getListener();
		SqlDataExportOption exportOption = dataExchange.getExportOption();
		int columnCount = columnInfos.size();

		DatabaseMetaData metaData = cn.getMetaData();
		String quote = metaData.getIdentifierQuoteString();

		if (exportOption.isExportCreationSql())
			writeCreationSql(dataExchange, cn, columnInfos, rs, quote, out, exportContext);

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

	/**
	 * 写建表语句。
	 * 
	 * @param dataExchange
	 * @param cn
	 * @param columnInfos
	 * @param rs
	 * @param quote
	 * @param out
	 * @param exportContext
	 * @throws Throwable
	 */
	protected void writeCreationSql(SqlDataExport dataExchange, Connection cn, List<ColumnInfo> columnInfos,
			ResultSet rs, String quote, Writer out, IndexFormatDataExchangeContext exportContext) throws Throwable
	{
		out.write("CREATE TABLE ");
		out.write(quote);
		out.write(dataExchange.getTableName());
		out.write(quote);
		out.write(LINE_SEPARATOR);
		out.write('(');
		out.write(LINE_SEPARATOR);

		String[] primaryColumnNames = getDatabaseInfoResolver().getPrimaryKeyColumnNames(cn,
				dataExchange.getTableName());
		List<String> filterPkNames = filterPrimaryColumnNames(primaryColumnNames, columnInfos);

		for (int i = 0, len = columnInfos.size(); i < len; i++)
		{
			ColumnInfo columnInfo = columnInfos.get(i);

			out.write("  ");
			out.write(quote);
			out.write(columnInfo.getName());
			out.write(quote);
			out.write(' ');

			out.write(columnInfo.getTypeName());

			if (columnInfo.getSize() > 0)
			{
				out.write('(');
				out.write(Integer.toString(columnInfo.getSize()));

				if (columnInfo.getDecimalDigits() > 0)
				{
					out.write(',');
					out.write(Integer.toString(columnInfo.getDecimalDigits()));
				}

				out.write(')');
			}

			if (!columnInfo.isNullable())
				out.write(" NOT NULL");

			if (i < len - 1)
				out.write(',');
			else if (i == len - 1 && !filterPkNames.isEmpty())
				out.write(',');

			out.write(LINE_SEPARATOR);
		}

		if (!filterPkNames.isEmpty())
		{
			out.write("  ");
			out.write("PRIMARY KEY (");

			for (int i = 0, len = filterPkNames.size(); i < len; i++)
			{
				out.write(quote);
				out.write(filterPkNames.get(i));
				out.write(quote);

				if (i < len - 1)
					out.write(',');
			}

			out.write(")");
			out.write(LINE_SEPARATOR);
		}

		out.write(");");
		out.write(LINE_SEPARATOR);
		out.write(LINE_SEPARATOR);
	}

	/**
	 * 过滤主键列名，仅保留在{@code columnInfos}包含的。
	 * 
	 * @param primaryColumnNames
	 * @param columnInfos
	 * @return
	 */
	protected List<String> filterPrimaryColumnNames(String[] primaryColumnNames, List<ColumnInfo> columnInfos)
	{
		List<String> names = new ArrayList<String>(3);

		if (primaryColumnNames == null)
			return names;

		for (int i = 0; i < primaryColumnNames.length; i++)
		{
			if (columnInfos == null)
				names.add(primaryColumnNames[i]);
			else
			{
				for (ColumnInfo columnInfo : columnInfos)
				{
					if (columnInfo.getName().equals(primaryColumnNames[i]))
					{
						names.add(primaryColumnNames[i]);
						break;
					}
				}
			}
		}

		return names;
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
