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
import java.util.List;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.datagear.dataexchange.DataExchangeException;
import org.datagear.dataexchange.DataFormat;
import org.datagear.dataexchange.DataFormat.BinaryFormat;
import org.datagear.dataexchange.ExceptionResolve;
import org.datagear.dataexchange.TextDataImport;
import org.datagear.dataexchange.TextDataImportListener;
import org.datagear.dbinfo.ColumnInfo;
import org.datagear.dbinfo.DatabaseInfoResolver;

/**
 * 抽象文本导入服务。
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public abstract class AbstractDevotedTextDataImportService<T extends TextDataImport>
		extends AbstractDevotedDataExchangeService<T>
{
	private DatabaseInfoResolver databaseInfoResolver;

	public AbstractDevotedTextDataImportService()
	{
		super();
	}

	public AbstractDevotedTextDataImportService(DatabaseInfoResolver databaseInfoResolver)
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
	 * 构建{@linkplain TextDataImportContext}。
	 * 
	 * @param impt
	 * @param table
	 * @return
	 */
	protected TextDataImportContext buildTextDataImportContext(T impt, String table)
	{
		return new TextDataImportContext(new DataFormatContext(impt.getDataFormat()), table);
	}

	/**
	 * 执行下一个导入操作。
	 * 
	 * @param impt
	 * @param st
	 * @param textDataImportContext
	 * @return true 成功；false 失败
	 * @throws DataExchangeException
	 */
	protected boolean executeNextImport(T impt, PreparedStatement st, TextDataImportContext textDataImportContext)
			throws DataExchangeException
	{
		TextDataImportListener listener = impt.getListener();

		try
		{
			st.executeUpdate();

			if (listener != null)
				listener.onSuccess(textDataImportContext.getDataIndex());

			return true;
		}
		catch (SQLException e)
		{
			DataExchangeException de = wrapToDataExchangeException(e);

			if (ExceptionResolve.IGNORE.equals(impt.getImportOption().getExceptionResolve()))
			{
				if (listener != null)
					listener.onFail(textDataImportContext.getDataIndex(), de);

				return false;
			}
			else
				throw de;
		}
		finally
		{
			textDataImportContext.incrementDataIndex();
			textDataImportContext.clearCloseResources();
		}
	}

	/**
	 * 设置文本导入数据参数，并进行必要的数据类型转换。
	 * 
	 * @param impt
	 * @param cn
	 * @param st
	 * @param columnInfos
	 * @param columnValues
	 * @param textDataImportContext
	 * @throws SetImportColumnValueException
	 */
	protected void setImportColumnValues(T impt, Connection cn, PreparedStatement st, List<ColumnInfo> columnInfos,
			List<String> columnValues, TextDataImportContext textDataImportContext) throws SetImportColumnValueException
	{
		String table = textDataImportContext.getTable();
		int dataIndex = textDataImportContext.getDataIndex();

		int columnCount = columnInfos.size();
		int columnValueCount = columnValues.size();

		for (int i = 0; i < columnCount; i++)
		{
			ColumnInfo columnInfo = columnInfos.get(i);
			String columnName = columnInfo.getName();
			int sqlType = columnInfo.getType();
			int parameterIndex = i + 1;
			String rawValue = (columnValues == null || columnValueCount - 1 < i ? null : columnValues.get(i));

			try
			{
				setImportColumnValue(impt, cn, st, parameterIndex, sqlType, rawValue, textDataImportContext);
			}
			catch (Exception e)
			{
				if (impt.getImportOption().isNullForIllegalColumnValue())
				{
					try
					{
						st.setNull(parameterIndex, sqlType);
					}
					catch (SQLException e1)
					{
						throw new SetImportColumnValueException(table, dataIndex, columnName, null);
					}

					TextDataImportListener listener = impt.getListener();
					if (listener != null)
					{
						DataExchangeException de = null;

						if ((e instanceof ParseException) || (e instanceof DecoderException))
							de = new IllegalSourceValueException(table, dataIndex, columnName, rawValue, e);
						else if (e instanceof UnsupportedSqlTypeException)
							de = (UnsupportedSqlTypeException) e;
						else
							de = new SetImportColumnValueException(table, dataIndex, columnName, rawValue);

						listener.onSetNullColumnValue(dataIndex, columnName, rawValue, de);
					}
				}
				else
				{
					if ((e instanceof ParseException) || (e instanceof DecoderException))
					{
						throw new IllegalSourceValueException(table, dataIndex, columnName, rawValue, e);
					}
					else if (e instanceof UnsupportedSqlTypeException)
					{
						throw (UnsupportedSqlTypeException) e;
					}
					else
					{
						throw new SetImportColumnValueException(table, dataIndex, columnName, rawValue);
					}
				}
			}
		}
	}

	/**
	 * 设置文本导入数据参数，并进行必要的数据类型转换。
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
	 * @param textDataImportContext
	 * @throws SQLException
	 * @throws ParseException
	 * @throws DecoderException
	 * @throws UnsupportedSqlTypeException
	 */
	protected void setImportColumnValue(T impt, Connection cn, PreparedStatement st, int parameterIndex, int sqlType,
			String parameterValue, TextDataImportContext textDataImportContext)
			throws SQLException, ParseException, DecoderException, UnsupportedSqlTypeException
	{
		if (parameterValue == null)
		{
			st.setNull(parameterIndex, sqlType);
			return;
		}

		DataFormatContext dataFormatContext = textDataImportContext.getDataFormatContext();
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
	 * 获取表指定列信息列表。
	 * <p>
	 * 当指定位置的列不存在时，如果{@code nullIfColumnNotFound}为{@code true}，返回列表对应位置将为{@code null}，
	 * 否则，将立刻抛出{@linkplain ColumnNotFoundException}。
	 * </p>
	 * 
	 * @param cn
	 * @param table
	 * @param columnNames
	 * @param nullIfColumnNotFound
	 * @return
	 * @throws TableNotFoundException
	 * @throws ColumnNotFoundException
	 */
	protected List<ColumnInfo> getColumnInfos(Connection cn, String table, List<String> columnNames,
			boolean nullIfColumnNotFound) throws TableNotFoundException, ColumnNotFoundException
	{
		return getColumnInfos(cn, table, columnNames, nullIfColumnNotFound, this.databaseInfoResolver);
	}

	/**
	 * 文本数据导入上下文。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static class TextDataImportContext extends DataExchangeContext
	{
		private DataFormatContext dataFormatContext;

		/** 导入表名 */
		private String table;

		/** 导入数据索引 */
		private int dataIndex = 0;

		public TextDataImportContext()
		{
			super();
		}

		public TextDataImportContext(DataFormatContext dataFormatContext, String table)
		{
			super();
			this.dataFormatContext = dataFormatContext;
			this.table = table;
		}

		public DataFormatContext getDataFormatContext()
		{
			return dataFormatContext;
		}

		public void setDataFormatContext(DataFormatContext dataFormatContext)
		{
			this.dataFormatContext = dataFormatContext;
		}

		public String getTable()
		{
			return table;
		}

		public void setTable(String table)
		{
			this.table = table;
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
