/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.io.Closeable;
import java.io.IOException;
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
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.datagear.dataexchange.DataImportException;
import org.datagear.dataexchange.DevotedDataImporter;
import org.datagear.dataexchange.Import;
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
public abstract class AbstractTextDevotedDataImporter<T extends Import> extends AbstractDevotedDataImporter<T>
{
	public AbstractTextDevotedDataImporter()
	{
		super();
	}

	/**
	 * 构建{@linkplain DataFormatContext}。
	 * 
	 * @param impt
	 * @return
	 */
	protected SetParameterContext buildSetParameterContext(AbstractTextImport impt)
	{
		return new SetParameterContext(impt.getDataFormat());
	}

	/**
	 * 设置插入预编译SQL语句{@linkplain PreparedStatement}参数。
	 * 
	 * @param impt
	 * @param st
	 * @param parameterColumnInfos
	 * @param parameterValues
	 * @param setParameterContext
	 * @throws SQLException
	 */
	protected void setInsertPreparedStatementParameters(AbstractTextImport impt, PreparedStatement st,
			ColumnInfo[] parameterColumnInfos, String[] parameterValues, SetParameterContext setParameterContext)
			throws SQLException
	{
		for (int i = 0; i < parameterColumnInfos.length; i++)
		{
			ColumnInfo columnInfo = parameterColumnInfos[i];
			String rawValue = (parameterValues == null || parameterValues.length - 1 < i ? null : parameterValues[i]);

			setPreparedStatementParameter(impt.getConnection(), st, i + 1, columnInfo.getType(), rawValue,
					setParameterContext);
		}
	}

	/**
	 * 设置{@linkplain PreparedStatement}参数，并进行必要的数据类型转换。
	 * <p>
	 * 此方法实现参考自JDBC4.0规范“Data Type Conversion Tables”章节中的“Java Types Mapper to
	 * JDBC Types”表。
	 * </p>
	 * 
	 * @param cn
	 * @param st
	 * @param parameterIndex
	 * @param sqlType
	 * @param parameterValue
	 * @param setParameterContext
	 * @throws Exception
	 */
	protected void setPreparedStatementParameter(Connection cn, PreparedStatement st, int parameterIndex, int sqlType,
			String parameterValue, SetParameterContext setParameterContext) throws Exception
	{
		if (parameterValue == null)
		{
			st.setNull(parameterIndex, sqlType);
			return;
		}

		DataFormat dataFormat = setParameterContext.getDataFormat();
		NumberFormat numberFormat = setParameterContext.getNumberFormatter();

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

				if (BinaryFormat.HEX.equals(dataFormat.getBinaryFormat()))
				{
					byte[] btv = convertToBytesForHex(parameterValue);
					st.setBytes(parameterIndex, btv);
				}
				else if (BinaryFormat.BASE64.equals(dataFormat.getBinaryFormat()))
				{
					byte[] btv = convertToBytesForBase64(parameterValue);
					st.setBytes(parameterIndex, btv);
				}
				else
					throw new UnsupportedOperationException(
							"Binary type [" + dataFormat.getBinaryFormat() + "] is not supported");

				break;

			case Types.DATE:

				java.util.Date dtv = setParameterContext.getDateFormatter().parse(parameterValue);
				java.sql.Date sdtv = new java.sql.Date(dtv.getTime());
				st.setDate(parameterIndex, sdtv);
				break;

			case Types.TIME:

				java.util.Date tdv = setParameterContext.getTimeFormatter().parse(parameterValue);
				java.sql.Time tv = new java.sql.Time(tdv.getTime());
				st.setTime(parameterIndex, tv);
				break;

			case Types.TIMESTAMP:

				// XXX 这种处理方式会丢失纳秒数据，待以后版本升级至jdk1.8库时采用java.time可解决
				java.util.Date tsdv = setParameterContext.getTimestampFormatter().parse(parameterValue);
				java.sql.Timestamp tsv = new Timestamp(tsdv.getTime());
				st.setTimestamp(parameterIndex, tsv);
				break;

			case Types.CLOB:

				Clob clob = cn.createClob();
				clob.setString(1, parameterValue);
				st.setClob(parameterIndex, clob);
				break;

			case Types.BLOB:

				if (BinaryFormat.HEX.equals(dataFormat.getBinaryFormat()))
				{
					Blob blob = cn.createBlob();
					byte[] btv = convertToBytesForHex(parameterValue);
					blob.setBytes(1, btv);
					st.setBlob(parameterIndex, blob);
				}
				else if (BinaryFormat.BASE64.equals(dataFormat.getBinaryFormat()))
				{
					Blob blob = cn.createBlob();
					byte[] btv = convertToBytesForBase64(parameterValue);
					blob.setBytes(1, btv);
					st.setBlob(parameterIndex, blob);
				}
				else
					throw new UnsupportedOperationException(
							"Binary type [" + dataFormat.getBinaryFormat() + "] is not supported");

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

				throw new DataImportException("The JDBC sql type [" + sqlType + "] is not supported");
		}
	}

	/**
	 * 构建插入预编译SQL语句。
	 * 
	 * @param cn
	 * @param table
	 * @param columnInfos
	 * @return
	 * @throws SQLException
	 */
	protected String buildInsertPreparedSql(Connection cn, String table, ColumnInfo[] columnInfos) throws SQLException
	{
		String quote = cn.getMetaData().getIdentifierQuoteString();

		StringBuilder sql = new StringBuilder("INSERT INTO ");
		sql.append(quote).append(table).append(quote);
		sql.append(" (");

		for (int i = 0; i < columnInfos.length; i++)
		{
			if (i != 0)
				sql.append(',');

			sql.append(quote).append(columnInfos[i].getName()).append(quote);
		}

		sql.append(") VALUES (");

		for (int i = 0; i < columnInfos.length; i++)
		{
			if (i != 0)
				sql.append(',');

			sql.append('?');
		}

		sql.append(")");

		return sql.toString();
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
	 * 如果指定位置的列不存在，返回数组对应位置将为{@code null}。
	 * </p>
	 * 
	 * @param cn
	 * @param table
	 * @param columnNames
	 * @param databaseInfoResolver
	 * @return
	 */
	protected ColumnInfo[] getColumnInfos(Connection cn, String table, String[] columnNames,
			DatabaseInfoResolver databaseInfoResolver)
	{
		ColumnInfo[] columnInfos = new ColumnInfo[columnNames.length];

		ColumnInfo[] allColumnInfos = databaseInfoResolver.getColumnInfos(cn, table);

		for (int i = 0; i < columnNames.length; i++)
		{
			ColumnInfo columnInfo = null;

			for (int j = 0; j < allColumnInfos.length; j++)
			{
				if (allColumnInfos[j].getName().equals(columnNames[i]))
				{
					columnInfo = allColumnInfos[j];
					break;
				}
			}

			columnInfos[i] = columnInfo;
		}

		return columnInfos;
	}

	/**
	 * 设置{@linkplain PreparedStatement}参数支持上下文。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static class SetParameterContext extends DataFormatContext
	{
		private List<Closeable> closeResources = new LinkedList<Closeable>();

		public SetParameterContext()
		{
			super();
		}

		public SetParameterContext(DataFormat dataFormat)
		{
			super(dataFormat);
		}

		/**
		 * 添加一个待关闭的{@linkplain Closeable}。
		 * 
		 * @param closeable
		 */
		public void addCloseResource(Closeable closeable)
		{
			this.closeResources.add(closeable);
		}

		/**
		 * 清除并关闭所有{@linkplain Closeable}。
		 * 
		 * @return
		 */
		public int clearCloseResources()
		{
			int size = closeResources.size();

			for (int i = 0; i < size; i++)
			{
				Closeable closeable = this.closeResources.get(i);

				try
				{
					closeable.close();
				}
				catch (IOException e)
				{
				}
			}

			return size;
		}
	}
}
