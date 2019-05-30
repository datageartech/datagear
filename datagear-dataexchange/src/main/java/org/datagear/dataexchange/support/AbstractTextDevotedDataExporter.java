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
import java.text.NumberFormat;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.datagear.connection.IOUtil;
import org.datagear.dataexchange.DevotedDataExporter;
import org.datagear.dataexchange.support.DataFormat.BinaryFormat;
import org.datagear.dbinfo.DatabaseInfoResolver;

/**
 * 抽象文本{@linkplain DevotedDataExporter}。
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public abstract class AbstractTextDevotedDataExporter<T extends AbstractTextDataExport>
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
	 * <p>
	 * 此方法实现参考自JDBC4.0规范“Data Type Conversion Tables”章节中的“Type Conversions
	 * Supported by ResultSet getter Methods”表，并且使用其中的最佳方法。
	 * </p>
	 * 
	 * @param cn
	 * @param rs
	 * @param columnIndex
	 * @param sqlType
	 * @param selectContext
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 * @throws UnsupportedSqlTypeException
	 */
	protected String getStringValue(Connection cn, ResultSet rs, int columnIndex, int sqlType,
			SelectContext selectContext) throws SQLException, IOException, UnsupportedSqlTypeException
	{
		String value = null;

		DataFormat dataFormat = selectContext.getDataFormat();
		NumberFormat numberFormat = selectContext.getNumberFormatter();
		BinaryFormat binaryFormat = dataFormat.getBinaryFormat();

		switch (sqlType)
		{
			case Types.TINYINT:

				int byteValue = rs.getByte(columnIndex);

				if (!rs.wasNull())
					value = numberFormat.format(byteValue);

				break;

			case Types.SMALLINT:

				int shortValue = rs.getShort(columnIndex);

				if (!rs.wasNull())
					value = numberFormat.format(shortValue);

				break;

			case Types.INTEGER:

				int intValue = rs.getInt(columnIndex);

				if (!rs.wasNull())
					value = numberFormat.format(intValue);

				break;

			case Types.BIGINT:

				long longValue = rs.getLong(columnIndex);

				if (!rs.wasNull())
					value = numberFormat.format(longValue);

				break;

			case Types.REAL:

				double floatValue = rs.getFloat(columnIndex);

				if (!rs.wasNull())
					value = numberFormat.format(floatValue);

				break;

			case Types.FLOAT:
			case Types.DOUBLE:

				double doubleValue = rs.getDouble(columnIndex);

				if (!rs.wasNull())
					value = numberFormat.format(doubleValue);

				break;

			case Types.DECIMAL:
			case Types.NUMERIC:

				BigDecimal bigDecimalValue = rs.getBigDecimal(columnIndex);

				if (!rs.wasNull())
					value = bigDecimalValue.toString();

				break;

			case Types.BIT:

				boolean bitValue = rs.getBoolean(columnIndex);

				if (!rs.wasNull())
					value = (bitValue ? "1" : "0");

				break;

			case Types.BOOLEAN:

				boolean boolValue = rs.getBoolean(columnIndex);

				if (!rs.wasNull())
					value = Boolean.toString(boolValue);

				break;

			case Types.CHAR:
			case Types.VARCHAR:

				value = rs.getString(columnIndex);

				if (rs.wasNull())
					value = null;

				break;

			case Types.LONGVARCHAR:

				Reader lvValue = rs.getCharacterStream(columnIndex);

				try
				{
					if (rs.wasNull())
						value = null;
					else
						value = readToString(lvValue);
				}
				finally
				{
					IOUtil.close(lvValue);
				}

				break;

			case Types.BINARY:
			case Types.VARBINARY:

				if (!BinaryFormat.NULL.equals(binaryFormat))
				{
					byte[] binaryValue = rs.getBytes(columnIndex);

					if (rs.wasNull())
						value = null;
					else
						value = convertToString(binaryValue, binaryFormat);
				}

				break;

			case Types.LONGVARBINARY:

				if (!BinaryFormat.NULL.equals(binaryFormat))
				{
					InputStream lbValue = rs.getBinaryStream(columnIndex);

					try
					{
						if (!rs.wasNull())
						{
							byte[] bytes = readToBytes(lbValue);
							value = convertToString(bytes, binaryFormat);
						}
					}
					finally
					{
						IOUtil.close(lbValue);
					}
				}

				break;

			case Types.DATE:

				java.sql.Date dateValue = rs.getDate(columnIndex);

				if (!rs.wasNull())
					value = selectContext.getDateFormatter().format(dateValue);

				break;

			case Types.TIME:

				java.sql.Time timeValue = rs.getTime(columnIndex);

				if (!rs.wasNull())
					value = selectContext.getTimeFormatter().format(timeValue);

				break;

			case Types.TIMESTAMP:

				java.sql.Timestamp timestampValue = rs.getTimestamp(columnIndex);

				// 如果是默认格式，则直接使用Timestamp.valueOf，这样可以避免丢失纳秒精度
				if (DataFormat.DEFAULT_TIMESTAMP_FORMAT.equals(dataFormat.getTimestampFormat()))
				{
					if (!rs.wasNull())
						value = timestampValue.toString();
				}
				else
				{
					// XXX 这种处理方式会丢失纳秒数据，待以后版本升级至jdk1.8库时采用java.time可解决

					if (!rs.wasNull())
						value = selectContext.getTimestampFormatter().format(timestampValue);
				}

				break;

			case Types.CLOB:

				Clob clobValue = rs.getClob(columnIndex);

				if (!rs.wasNull())
				{
					Reader reader = clobValue.getCharacterStream();

					try
					{
						value = readToString(reader);
					}
					finally
					{
						IOUtil.close(reader);
					}
				}

				break;

			case Types.BLOB:

				if (!BinaryFormat.NULL.equals(binaryFormat))
				{
					Blob blobValue = rs.getBlob(columnIndex);

					if (!rs.wasNull())
					{
						InputStream inputStream = blobValue.getBinaryStream();

						try
						{
							byte[] bytes = readToBytes(inputStream);
							value = convertToString(bytes, binaryFormat);
						}
						finally
						{
							IOUtil.close(inputStream);
						}
					}
				}

				break;

			case Types.NCHAR:
			case Types.NVARCHAR:

				value = rs.getNString(columnIndex);

				if (rs.wasNull())
					value = null;

				break;

			case Types.LONGNVARCHAR:

				Reader lnvValue = rs.getNCharacterStream(columnIndex);

				try
				{
					if (!rs.wasNull())
						value = readToString(lnvValue);
				}
				finally
				{
					IOUtil.close(lnvValue);
				}

				break;

			case Types.NCLOB:

				NClob nclobValue = rs.getNClob(columnIndex);

				if (!rs.wasNull())
				{
					Reader reader = nclobValue.getCharacterStream();

					try
					{
						value = readToString(reader);
					}
					finally
					{
						IOUtil.close(reader);
					}
				}

				break;

			case Types.SQLXML:

				SQLXML sqlxmlValue = rs.getSQLXML(columnIndex);

				if (!rs.wasNull())
				{
					Reader reader = sqlxmlValue.getCharacterStream();

					try
					{
						value = readToString(reader);
					}
					finally
					{
						IOUtil.close(reader);
					}
				}

				break;

			default:

				throw new UnsupportedSqlTypeException(sqlType);
		}

		return value;
	}

	/**
	 * 将字节数组编码转换为字符串。
	 * 
	 * @param bytes
	 * @param binaryFormat
	 * @return
	 */
	protected String convertToString(byte[] bytes, BinaryFormat binaryFormat)
	{
		String value = null;

		if (bytes == null)
			;
		else if (BinaryFormat.NULL.equals(binaryFormat))
			;
		else if (BinaryFormat.HEX.equals(binaryFormat))
			value = convertToHex(bytes);
		else if (BinaryFormat.BASE64.equals(binaryFormat))
			value = convertToBase64(bytes);
		else
			throw new UnsupportedOperationException();

		return value;
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
	 * 将字节数组转换为Hex字符串。
	 * 
	 * @param bytes
	 * @return
	 */
	protected String convertToHex(byte[] bytes)
	{
		if (bytes == null)
			return null;

		return Hex.encodeHexString(bytes);
	}

	/**
	 * 将字节数组转换为Base64字符串。
	 * 
	 * @param bytes
	 * @return
	 */
	protected String convertToBase64(byte[] bytes)
	{
		if (bytes == null)
			return null;

		return Base64.encodeBase64String(bytes);
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
