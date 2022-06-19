/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.dataexchange.support;

import java.io.Writer;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.datagear.dataexchange.AbstractDevotedDBMetaDataExchangeService;
import org.datagear.dataexchange.DataExchangeContext;
import org.datagear.dataexchange.IndexFormatDataExchangeContext;
import org.datagear.dataexchange.RowDataIndex;
import org.datagear.dataexchange.TextDataExportListener;
import org.datagear.meta.Column;
import org.datagear.meta.PrimaryKey;
import org.datagear.meta.resolver.DBMetaResolver;
import org.datagear.util.JdbcUtil;
import org.datagear.util.QueryResultSet;

/**
 * SQL导出服务。
 * 
 * @author datagear@163.com
 *
 */
public class SqlDataExportService extends AbstractDevotedDBMetaDataExchangeService<SqlDataExport>
{
	public static final String LINE_SEPARATOR = "\r\n";

	public SqlDataExportService()
	{
		super();
	}

	public SqlDataExportService(DBMetaResolver dbMetaResolver)
	{
		super(dbMetaResolver);
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

		QueryResultSet qrs = dataExchange.getQuery().execute(cn);
		context.addContextCloseable(qrs);

		ResultSet rs = qrs.getResultSet();
		List<Column> columns = getColumns(cn, rs);

		writeRecords(dataExchange, cn, columns, rs, sqlWriter, exportContext);
	}

	/**
	 * 写记录。
	 * 
	 * @param dataExchange
	 * @param cn
	 * @param columns
	 * @param rs
	 * @param out
	 * @param exportContext
	 */
	protected void writeRecords(SqlDataExport dataExchange, Connection cn, List<Column> columns, ResultSet rs,
			Writer out, IndexFormatDataExchangeContext exportContext) throws Throwable
	{
		TextDataExportListener listener = dataExchange.getListener();
		SqlDataExportOption exportOption = dataExchange.getExportOption();
		int columnCount = columns.size();

		DatabaseMetaData metaData = cn.getMetaData();
		String quote = metaData.getIdentifierQuoteString();

		if (exportOption.isExportCreationSql())
			writeCreationSql(dataExchange, cn, columns, rs, quote, out, exportContext);

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
				Column column = columns.get(i);

				if (i > 0)
					out.write(",");

				out.write(quote);
				out.write(column.getName());
				out.write(quote);
			}

			out.write(") VALUES(");

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

				if (i > 0)
					out.write(",");

				if (value == null)
				{
					out.write("NULL");
				}
				else if (isSqlStringType(column.getType()))
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
	 * @param columns
	 * @param rs
	 * @param quote
	 * @param out
	 * @param exportContext
	 * @throws Throwable
	 */
	protected void writeCreationSql(SqlDataExport dataExchange, Connection cn, List<Column> columns, ResultSet rs,
			String quote, Writer out, IndexFormatDataExchangeContext exportContext) throws Throwable
	{
		out.write("CREATE TABLE ");
		out.write(quote);
		out.write(dataExchange.getTableName());
		out.write(quote);
		out.write(LINE_SEPARATOR);
		out.write('(');
		out.write(LINE_SEPARATOR);

		PrimaryKey primaryKey = getDbMetaResolver().getPrimaryKey(cn, dataExchange.getTableName());
		List<String> filterPkNames = filterPrimaryColumnNames(primaryKey, columns);

		for (int i = 0, len = columns.size(); i < len; i++)
		{
			Column column = columns.get(i);

			out.write("  ");
			out.write(quote);
			out.write(column.getName());
			out.write(quote);
			out.write(' ');

			out.write(column.getTypeName());

			if (column.getSize() > 0)
			{
				out.write('(');
				out.write(Integer.toString(column.getSize()));

				if (column.getDecimalDigits() > 0)
				{
					out.write(',');
					out.write(Integer.toString(column.getDecimalDigits()));
				}

				out.write(')');
			}

			if (!column.isNullable())
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
	 * 过滤主键列名，仅保留在{@code columns}包含的。
	 * 
	 * @param primaryKey
	 * @param columns
	 * @return
	 */
	protected List<String> filterPrimaryColumnNames(PrimaryKey primaryKey, List<Column> columns)
	{
		List<String> names = new ArrayList<>();

		if (primaryKey == null)
			return names;

		String[] pkColumnNames = primaryKey.getColumnNames();

		for (int i = 0; i < pkColumnNames.length; i++)
		{
			if (columns == null)
				names.add(pkColumnNames[i]);
			else
			{
				for (Column column : columns)
				{
					if (column.getName().equals(pkColumnNames[i]))
					{
						names.add(pkColumnNames[i]);
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
