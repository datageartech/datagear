/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.NumberFormat;
import java.text.ParseException;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.datagear.dataexchange.DataImportReporter;
import org.datagear.dataexchange.DevotedDataImporter;
import org.datagear.dataexchange.support.DataFormat.BinaryFormat;
import org.datagear.dbinfo.ColumnInfo;
import org.datagear.dbinfo.DatabaseInfoResolver;

/**
 * 抽象文本{@linkplain DevotedDataImporter}。
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public abstract class AbstractTextDevotedDataImporter<T extends TextDataImport> extends AbstractDevotedDataImporter<T>
{
	private DatabaseInfoResolver databaseInfoResolver;

	public AbstractTextDevotedDataImporter()
	{
		super();
	}

	public AbstractTextDevotedDataImporter(DatabaseInfoResolver databaseInfoResolver)
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
	 * 构建{@linkplain TextInsertContext}。
	 * 
	 * @param table
	 * @param impt
	 * @return
	 */
	protected TextInsertContext buildTextInsertContext(String table, T impt)
	{
		return new TextInsertContext(table, new DataFormatContext(impt.getDataFormat()));
	}

	/**
	 * 设置插入预编译SQL语句{@linkplain PreparedStatement}参数。
	 * <p>
	 * 如果{@linkplain AbstractTextDataImport#isAbortOnError()}为{@code false}，此方法将不会抛出{@linkplain SetInsertPreparedColumnValueException}。
	 * </p>
	 * 
	 * @param impt
	 * @param cn
	 * @param st
	 * @param columnInfos
	 * @param columnValues
	 * @param textInsertContext
	 * @throws SetInsertPreparedColumnValueException
	 */
	protected void setInsertPreparedColumnValues(T impt, Connection cn, PreparedStatement st, ColumnInfo[] columnInfos,
			String[] columnValues, TextInsertContext textInsertContext) throws SetInsertPreparedColumnValueException
	{
		boolean abortOnError = impt.isAbortOnError();
		DataImportReporter dataImportReporter = (impt.hasDataImportReporter() ? impt.getDataImportReporter() : null);
		String table = textInsertContext.getTable();
		int dataIndex = textInsertContext.getDataIndex();

		for (int i = 0; i < columnInfos.length; i++)
		{
			ColumnInfo columnInfo = columnInfos[i];
			String columnName = columnInfo.getName();
			int sqlType = columnInfo.getType();
			int parameterIndex = i + 1;
			String rawValue = (columnValues == null || columnValues.length - 1 < i ? null : columnValues[i]);

			try
			{
				setInsertPreparedColumnValue(impt, cn, st, parameterIndex, sqlType, rawValue, textInsertContext);
			}
			catch (SQLException e)
			{
				SetInsertPreparedColumnValueException e1 = new SetInsertPreparedColumnValueException(table, dataIndex,
						columnName, rawValue, e);

				if (abortOnError)
					throw e1;
				else
				{
					setParameterNull(st, parameterIndex, sqlType);

					if (dataImportReporter != null)
						dataImportReporter.report(e1);
				}
			}
			catch (ParseException e)
			{
				IllegalSourceValueException e1 = new IllegalSourceValueException(table, dataIndex, columnName, rawValue,
						e);

				if (abortOnError)
					throw e1;
				else
				{
					setParameterNull(st, parameterIndex, sqlType);

					if (dataImportReporter != null)
						dataImportReporter.report(e1);
				}
			}
			catch (DecoderException e)
			{
				IllegalSourceValueException e1 = new IllegalSourceValueException(table, dataIndex, columnName, rawValue,
						e);

				if (abortOnError)
					throw e1;
				else
				{
					setParameterNull(st, parameterIndex, sqlType);

					if (dataImportReporter != null)
						dataImportReporter.report(e1);
				}
			}
			catch (UnsupportedSqlTypeException e)
			{
				SetInsertPreparedColumnValueException e1 = new SetInsertPreparedColumnValueException(table, dataIndex,
						columnName, rawValue, e);

				if (abortOnError)
					throw e1;
				else
				{
					setParameterNull(st, parameterIndex, sqlType);

					if (dataImportReporter != null)
						dataImportReporter.report(e1);
				}
			}
			catch (Exception e)
			{
				SetInsertPreparedColumnValueException e1 = new SetInsertPreparedColumnValueException(table, dataIndex,
						columnName, rawValue, e);

				if (abortOnError)
					throw e1;
				else
				{
					setParameterNull(st, parameterIndex, sqlType);

					if (dataImportReporter != null)
						dataImportReporter.report(e1);
				}
			}
		}
	}

	/**
	 * 设置插入预编译SQL语句{@linkplain PreparedStatement}参数，并进行必要的数据类型转换。
	 * <p>
	 * 此方法实现参考自JDBC4.0规范“Data Type Conversion Tables”章节中的“Java Types Mapper to
	 * JDBC Types”表。
	 * </p>
	 * 
	 * @param impt
	 * @param cn
	 * @param st
	 * @param parameterIndex
	 * @param sqlType
	 * @param parameterValue
	 * @param textInsertContext
	 * @throws SQLException
	 * @throws ParseException
	 * @throws DecoderException
	 * @throws UnsupportedSqlTypeException
	 */
	protected void setInsertPreparedColumnValue(T impt, Connection cn, PreparedStatement st, int parameterIndex,
			int sqlType, String parameterValue, TextInsertContext textInsertContext)
			throws SQLException, ParseException, DecoderException, UnsupportedSqlTypeException
	{
		if (parameterValue == null)
		{
			st.setNull(parameterIndex, sqlType);
			return;
		}

		DataFormatContext dataFormatContext = textInsertContext.getDataFormatContext();
		DataFormat dataFormat = dataFormatContext.getDataFormat();
		NumberFormat numberFormat = dataFormatContext.getNumberFormatter();
		BinaryFormat binaryFormat = dataFormat.getBinaryFormat();

		switch (sqlType)
		{
			case Types.CHAR:
			case Types.VARCHAR:
			case Types.LONGVARCHAR:

				st.setString(parameterIndex, parameterValue);
				break;

			case Types.NUMERIC:
			case Types.DECIMAL:

				BigDecimal bdv = new BigDecimal(parameterValue);
				st.setBigDecimal(parameterIndex, bdv);
				break;

			case Types.BIT:
			case Types.BOOLEAN:

				boolean bv = ("true".equalsIgnoreCase(parameterValue) || "1".equals(parameterValue)
						|| "on".equalsIgnoreCase(parameterValue));
				st.setBoolean(parameterIndex, bv);
				break;

			case Types.TINYINT:
			case Types.SMALLINT:
			case Types.INTEGER:

				numberFormat.setParseIntegerOnly(true);
				int iv = numberFormat.parse(parameterValue).intValue();
				st.setInt(parameterIndex, iv);
				break;

			case Types.BIGINT:

				numberFormat.setParseIntegerOnly(true);
				long lv = numberFormat.parse(parameterValue).longValue();
				st.setLong(parameterIndex, lv);
				break;

			case Types.REAL:

				numberFormat.setParseIntegerOnly(false);
				float fv = numberFormat.parse(parameterValue).floatValue();
				st.setFloat(parameterIndex, fv);
				break;

			case Types.FLOAT:
			case Types.DOUBLE:

				numberFormat.setParseIntegerOnly(false);
				double dv = numberFormat.parse(parameterValue).doubleValue();
				st.setDouble(parameterIndex, dv);
				break;

			case Types.BINARY:
			case Types.VARBINARY:
			case Types.LONGVARBINARY:

				if (BinaryFormat.NULL.equals(binaryFormat))
				{
					st.setNull(parameterIndex, sqlType);
				}
				else if (BinaryFormat.HEX.equals(binaryFormat))
				{
					byte[] btv = convertToBytesForHex(parameterValue);
					st.setBytes(parameterIndex, btv);
				}
				else if (BinaryFormat.BASE64.equals(binaryFormat))
				{
					byte[] btv = convertToBytesForBase64(parameterValue);
					st.setBytes(parameterIndex, btv);
				}
				else
					throw new UnsupportedOperationException();

				break;

			case Types.DATE:

				java.util.Date dtv = dataFormatContext.getDateFormatter().parse(parameterValue);
				java.sql.Date sdtv = new java.sql.Date(dtv.getTime());
				st.setDate(parameterIndex, sdtv);
				break;

			case Types.TIME:

				java.util.Date tdv = dataFormatContext.getTimeFormatter().parse(parameterValue);
				java.sql.Time tv = new java.sql.Time(tdv.getTime());
				st.setTime(parameterIndex, tv);
				break;

			case Types.TIMESTAMP:

				// 如果是默认格式，则直接使用Timestamp.valueOf，这样可以避免丢失纳秒精度
				if (DataFormat.DEFAULT_TIMESTAMP_FORMAT.equals(dataFormat.getTimestampFormat()))
				{
					java.sql.Timestamp tsv = Timestamp.valueOf(parameterValue);
					st.setTimestamp(parameterIndex, tsv);
				}
				else
				{
					// XXX 这种处理方式会丢失纳秒数据，待以后版本升级至jdk1.8库时采用java.time可解决
					java.util.Date tsdv = dataFormatContext.getTimestampFormatter().parse(parameterValue);
					java.sql.Timestamp tsv = new Timestamp(tsdv.getTime());
					st.setTimestamp(parameterIndex, tsv);
				}
				break;

			case Types.CLOB:

				Clob clob = cn.createClob();
				clob.setString(1, parameterValue);
				st.setClob(parameterIndex, clob);
				break;

			case Types.BLOB:

				if (BinaryFormat.NULL.equals(binaryFormat))
				{
					st.setNull(parameterIndex, sqlType);
				}
				else if (BinaryFormat.HEX.equals(binaryFormat))
				{
					Blob blob = cn.createBlob();
					byte[] btv = convertToBytesForHex(parameterValue);
					blob.setBytes(1, btv);
					st.setBlob(parameterIndex, blob);
				}
				else if (BinaryFormat.BASE64.equals(binaryFormat))
				{
					Blob blob = cn.createBlob();
					byte[] btv = convertToBytesForBase64(parameterValue);
					blob.setBytes(1, btv);
					st.setBlob(parameterIndex, blob);
				}
				else
					throw new UnsupportedOperationException();

				break;

			case Types.NCHAR:
			case Types.NVARCHAR:
			case Types.LONGNVARCHAR:

				st.setNString(parameterIndex, parameterValue);
				break;

			case Types.NCLOB:

				NClob nclob = cn.createNClob();
				nclob.setString(1, parameterValue);
				st.setNClob(parameterIndex, nclob);
				break;

			case Types.SQLXML:

				SQLXML sqlxml = cn.createSQLXML();
				sqlxml.setString(parameterValue);
				st.setSQLXML(parameterIndex, sqlxml);
				break;

			default:

				throw new UnsupportedSqlTypeException(sqlType);
		}
	}

	/**
	 * 将HEX编码的字符串转换为字节数组。
	 * 
	 * @param value
	 * @return
	 * @throws DecoderException
	 */
	protected byte[] convertToBytesForHex(String value) throws DecoderException
	{
		if (value == null || value.isEmpty())
			return null;

		return Hex.decodeHex(value);
	}

	/**
	 * 将Base64编码的字符串转换为字节数组。
	 * 
	 * @param value
	 * @return
	 */
	protected byte[] convertToBytesForBase64(String value)
	{
		if (value == null || value.isEmpty())
			return null;

		return Base64.decodeBase64(value);
	}

	/**
	 * 获取表指定列信息数组。
	 * <p>
	 * 当指定位置的列不存在时，如果{@code nullIfInexistentColumn}为{@code true}，返回数组对应位置将为{@code null}，
	 * 否则，将立刻抛出{@linkplain ColumnNotFoundException}。
	 * </p>
	 * 
	 * @param cn
	 * @param table
	 * @param columnNames
	 * @param nullIfInexistentColumn
	 * @return
	 * @throws ColumnNotFoundException
	 */
	protected ColumnInfo[] getColumnInfos(Connection cn, String table, String[] columnNames,
			boolean nullIfInexistentColumn) throws ColumnNotFoundException
	{
		return getColumnInfos(cn, table, columnNames, nullIfInexistentColumn, this.databaseInfoResolver);
	}

	/**
	 * 文本SQL插入操作上下文。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static class TextInsertContext extends InsertContext
	{
		private DataFormatContext dataFormatContext;

		public TextInsertContext()
		{
			super();
		}

		public TextInsertContext(String table, DataFormatContext dataFormatContext)
		{
			super(table);
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
	}
}
