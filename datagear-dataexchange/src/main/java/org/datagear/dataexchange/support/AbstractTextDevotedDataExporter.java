/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.NumberFormat;

import org.datagear.dataexchange.DevotedDataExporter;
import org.datagear.dbinfo.DatabaseInfoResolver;

/**
 * 抽象文本{@linkplain DevotedDataExporter}。
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public abstract class AbstractTextDevotedDataExporter<T extends AbstractTextExport>
		extends AbstractDevotedDataExporter<T>
{
	public AbstractTextDevotedDataExporter()
	{
		super();
	}

	public AbstractTextDevotedDataExporter(DatabaseInfoResolver databaseInfoResolver)
	{
		super(databaseInfoResolver);
	}

	/**
	 * 读取指定列的字符串值。
	 * 
	 * @param cn
	 * @param rs
	 * @param columnIndex
	 * @param sqlType
	 * @param selectContext
	 * @return
	 * @throws SQLException
	 * @throws UnsupportedSqlTypeException
	 */
	protected String getStringValue(Connection cn, ResultSet rs, int columnIndex, int sqlType,
			SelectContext selectContext) throws SQLException, UnsupportedSqlTypeException
	{
		String value = null;

		DataFormat dataFormat = selectContext.getDataFormat();
		NumberFormat numberFormat = selectContext.getNumberFormatter();

		switch (sqlType)
		{
			case Types.CHAR:
			case Types.VARCHAR:
			case Types.LONGVARCHAR:

				value = rs.getString(columnIndex);

				if (rs.wasNull())
					value = null;

				break;

			case Types.NUMERIC:
			case Types.DECIMAL:

				value = rs.getString(columnIndex);
				if (rs.wasNull())
					value = null;

				break;

			case Types.BIT:
			case Types.BOOLEAN:

				value = rs.getString(columnIndex);

				if (rs.wasNull())
					value = null;

				break;

			case Types.TINYINT:
			case Types.SMALLINT:
			case Types.INTEGER:
			case Types.BIGINT:

				long lv = rs.getInt(columnIndex);

				if (!rs.wasNull())
					value = numberFormat.format(lv);

				break;

			case Types.REAL:
			case Types.FLOAT:
			case Types.DOUBLE:

				double dv = rs.getDouble(columnIndex);

				if (!rs.wasNull())
					value = numberFormat.format(dv);

				break;

			case Types.BINARY:
			case Types.VARBINARY:
			case Types.LONGVARBINARY:

				// TODO

				break;

			case Types.DATE:

				java.sql.Date sdtv = rs.getDate(columnIndex);

				if (!rs.wasNull())
					value = selectContext.getDateFormatter().format(sdtv);

				break;

			case Types.TIME:

				java.sql.Time tv = rs.getTime(columnIndex);

				if (!rs.wasNull())
					value = selectContext.getTimeFormatter().format(tv);

				break;

			case Types.TIMESTAMP:

				java.sql.Timestamp tsv = rs.getTimestamp(columnIndex);

				// 如果是默认格式，则直接使用Timestamp.valueOf，这样可以避免丢失纳秒精度
				if (DataFormat.DEFAULT_TIMESTAMP_FORMAT.equals(dataFormat.getTimestampFormat()))
				{
					if (!rs.wasNull())
						value = tsv.toString();
				}
				else
				{
					// XXX 这种处理方式会丢失纳秒数据，待以后版本升级至jdk1.8库时采用java.time可解决

					if (!rs.wasNull())
						value = selectContext.getTimestampFormatter().format(tsv);
				}

				break;

			case Types.CLOB:

				// TODO

				break;

			case Types.BLOB:

				// TODO

				break;

			case Types.NCHAR:
			case Types.NVARCHAR:
			case Types.LONGNVARCHAR:

				value = rs.getNString(columnIndex);

				if (rs.wasNull())
					value = null;

				break;

			case Types.NCLOB:

				// TODO
				break;

			case Types.SQLXML:

				// TODO
				break;

			default:

				throw new UnsupportedSqlTypeException(sqlType);
		}

		return value;
	}

	protected static class SelectContext extends DataFormatContext
	{
		public SelectContext()
		{
			super();
		}

		public SelectContext(DataFormat dataFormat)
		{
			super(dataFormat);
		}
	}
}
