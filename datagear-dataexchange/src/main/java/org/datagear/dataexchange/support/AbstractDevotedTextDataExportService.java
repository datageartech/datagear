/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.NClob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Types;
import java.util.List;

import org.datagear.dataexchange.TextDataExport;
import org.datagear.dbinfo.ColumnInfo;
import org.datagear.dbinfo.DatabaseInfoResolver;
import org.datagear.util.IOUtil;

/**
 * 抽象文本导出服务。
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public abstract class AbstractDevotedTextDataExportService<T extends TextDataExport>
		extends AbstractDevotedDataExchangeService<T>
{
	private DatabaseInfoResolver databaseInfoResolver;

	public AbstractDevotedTextDataExportService()
	{
		super();
	}

	public AbstractDevotedTextDataExportService(DatabaseInfoResolver databaseInfoResolver)
	{
		super();
		this.databaseInfoResolver = databaseInfoResolver;
	}

	public DatabaseInfoResolver getDatabaseInfoResolver()
	{
		return databaseInfoResolver;
	}

	public void setDatabaseInfoResolver(DatabaseInfoResolver databaseInfoResolver)
	{
		this.databaseInfoResolver = databaseInfoResolver;
	}

	/**
	 * 构建{@linkplain TextDataExportContext}。
	 * 
	 * @param expt
	 * @return
	 */
	protected TextDataExportContext buildTextDataExportContext(T expt)
	{
		return new TextDataExportContext(new DataFormatContext(expt.getDataFormat()));
	}

	/**
	 * 读取导出列的字符串值。
	 * <p>
	 * 此方法实现参考自JDBC4.0规范“Data Type Conversion Tables”章节中的“Type Conversions
	 * Supported by ResultSet getter Methods”表，并且使用其中的最佳方法。
	 * </p>
	 * 
	 * @param expt
	 * @param cn
	 * @param rs
	 * @param columnIndex
	 * @param sqlType
	 * @param textDataExportContext
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 * @throws UnsupportedSqlTypeException
	 */
	protected String getExportColumnValue(T expt, Connection cn, ResultSet rs, int columnIndex, int sqlType,
			TextDataExportContext textDataExportContext) throws SQLException, IOException, UnsupportedSqlTypeException
	{
		String valueStr = null;

		DataFormatContext dataFormatContext = textDataExportContext.getDataFormatContext();

		switch (sqlType)
		{
			case Types.TINYINT:
			{
				int value = rs.getByte(columnIndex);

				if (!rs.wasNull())
					valueStr = dataFormatContext.formatInt(value);

				break;
			}

			case Types.SMALLINT:
			{
				int value = rs.getShort(columnIndex);

				if (!rs.wasNull())
					valueStr = dataFormatContext.formatInt(value);

				break;
			}

			case Types.INTEGER:
			{
				int value = rs.getInt(columnIndex);

				if (!rs.wasNull())
					valueStr = dataFormatContext.formatInt(value);

				break;
			}

			case Types.BIGINT:
			{
				long value = rs.getLong(columnIndex);

				if (!rs.wasNull())
					valueStr = dataFormatContext.formatLong(value);

				break;
			}

			case Types.REAL:
			{
				double value = rs.getFloat(columnIndex);

				if (!rs.wasNull())
					valueStr = dataFormatContext.formatDouble(value);

				break;
			}

			case Types.FLOAT:
			case Types.DOUBLE:
			{
				double value = rs.getDouble(columnIndex);

				if (!rs.wasNull())
					valueStr = dataFormatContext.formatDouble(value);

				break;
			}

			case Types.DECIMAL:
			case Types.NUMERIC:
			{
				BigDecimal value = rs.getBigDecimal(columnIndex);

				if (!rs.wasNull())
					valueStr = value.toString();

				break;
			}

			case Types.BIT:
			{
				boolean value = rs.getBoolean(columnIndex);

				if (!rs.wasNull())
					valueStr = (value ? "1" : "0");

				break;
			}

			case Types.BOOLEAN:
			{
				boolean value = rs.getBoolean(columnIndex);

				if (!rs.wasNull())
					valueStr = Boolean.toString(value);

				break;
			}

			case Types.CHAR:
			case Types.VARCHAR:
			{
				valueStr = rs.getString(columnIndex);

				if (rs.wasNull())
					valueStr = null;

				break;
			}

			case Types.LONGVARCHAR:
			{
				Reader value = rs.getCharacterStream(columnIndex);

				try
				{
					if (rs.wasNull())
						valueStr = null;
					else
						valueStr = readToString(value);
				}
				finally
				{
					IOUtil.close(value);
				}

				break;
			}

			case Types.BINARY:
			case Types.VARBINARY:
			{
				byte[] value = rs.getBytes(columnIndex);

				if (rs.wasNull())
					valueStr = null;
				else
					valueStr = dataFormatContext.formatBytes(value);

				break;
			}

			case Types.LONGVARBINARY:
			{
				InputStream value = rs.getBinaryStream(columnIndex);

				try
				{
					if (!rs.wasNull())
					{
						byte[] bytes = readToBytes(value);
						valueStr = dataFormatContext.formatBytes(bytes);
					}
				}
				finally
				{
					IOUtil.close(value);
				}

				break;
			}

			case Types.DATE:
			{
				java.sql.Date value = rs.getDate(columnIndex);

				if (!rs.wasNull())
					valueStr = dataFormatContext.formatDate(value);

				break;
			}

			case Types.TIME:
			{
				java.sql.Time value = rs.getTime(columnIndex);

				if (!rs.wasNull())
					valueStr = dataFormatContext.formatTime(value);

				break;
			}

			case Types.TIMESTAMP:
			{
				java.sql.Timestamp value = rs.getTimestamp(columnIndex);

				if (!rs.wasNull())
					valueStr = dataFormatContext.formatTimestamp(value);

				break;
			}

			case Types.CLOB:
			{
				Clob value = rs.getClob(columnIndex);

				if (!rs.wasNull())
				{
					Reader reader = value.getCharacterStream();

					try
					{
						valueStr = readToString(reader);
					}
					finally
					{
						IOUtil.close(reader);
					}
				}

				break;
			}

			case Types.BLOB:
			{
				Blob value = rs.getBlob(columnIndex);

				if (!rs.wasNull())
				{
					InputStream inputStream = value.getBinaryStream();

					try
					{
						byte[] bytes = readToBytes(inputStream);
						valueStr = dataFormatContext.formatBytes(bytes);
					}
					finally
					{
						IOUtil.close(inputStream);
					}
				}

				break;
			}

			case Types.NCHAR:
			case Types.NVARCHAR:
			{
				valueStr = rs.getNString(columnIndex);

				if (rs.wasNull())
					valueStr = null;

				break;
			}

			case Types.LONGNVARCHAR:
			{
				Reader value = rs.getNCharacterStream(columnIndex);

				try
				{
					if (!rs.wasNull())
						valueStr = readToString(value);
				}
				finally
				{
					IOUtil.close(value);
				}

				break;
			}

			case Types.NCLOB:
			{
				NClob value = rs.getNClob(columnIndex);

				if (!rs.wasNull())
				{
					Reader reader = value.getCharacterStream();

					try
					{
						valueStr = readToString(reader);
					}
					finally
					{
						IOUtil.close(reader);
					}
				}

				break;
			}

			case Types.SQLXML:
			{
				SQLXML value = rs.getSQLXML(columnIndex);

				if (!rs.wasNull())
				{
					Reader reader = value.getCharacterStream();

					try
					{
						valueStr = readToString(reader);
					}
					finally
					{
						IOUtil.close(reader);
					}
				}

				break;
			}

			default:

				throw new UnsupportedSqlTypeException(sqlType);
		}

		return valueStr;

	}

	/**
	 * 将字节流读入字节数组。
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	protected byte[] readToBytes(InputStream in) throws IOException
	{
		ByteArrayOutputStream bout = new ByteArrayOutputStream();

		byte[] buf = new byte[32];
		int count = -1;
		while ((count = in.read(buf)) > -1)
			bout.write(buf, 0, count);

		return bout.toByteArray();
	}

	/**
	 * 将字符流读入字符串。
	 * 
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	protected String readToString(Reader reader) throws IOException
	{
		StringBuilder sb = new StringBuilder();

		char[] buf = new char[32];

		int count = -1;
		while ((count = reader.read(buf)) > -1)
			sb.append(buf, 0, count);

		return sb.toString();
	}

	/**
	 * 获取{@linkplain ResultSet}列信息。
	 * 
	 * @param cn
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	protected List<ColumnInfo> getColumnInfos(Connection cn, ResultSet rs) throws SQLException
	{
		return super.getColumnInfos(cn, rs, this.databaseInfoResolver);
	}

	/**
	 * 文本导出上下文。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static class TextDataExportContext extends DataExchangeContext
	{
		private DataFormatContext dataFormatContext;

		private int dataIndex = 0;

		public TextDataExportContext()
		{
			super();
		}

		public TextDataExportContext(DataFormatContext dataFormatContext)
		{
			super();
			this.dataFormatContext = dataFormatContext;
		}

		public DataFormatContext getDataFormatContext()
		{
			return dataFormatContext;
		}

		public void setDataFormatContext(DataFormatContext dataFormatContext)
		{
			this.dataFormatContext = dataFormatContext;
		}

		public int getDataIndex()
		{
			return dataIndex;
		}

		public void setDataIndex(int dataIndex)
		{
			this.dataIndex = dataIndex;
		}

		public void incrementDataIndex()
		{
			this.dataIndex++;
		}
	}
}
