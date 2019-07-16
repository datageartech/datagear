/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Types;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.DecoderException;
import org.datagear.dbinfo.ColumnInfo;
import org.datagear.dbinfo.DatabaseInfoResolver;
import org.datagear.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 抽象{@linkplain DevotedDataExchangeService}。
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public abstract class AbstractDevotedDataExchangeService<T extends DataExchange>
		implements DevotedDataExchangeService<T>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDevotedDataExchangeService.class);

	public AbstractDevotedDataExchangeService()
	{
		super();
	}

	@Override
	public boolean supports(T dataExchange)
	{
		return true;
	}

	/**
	 * 回滚。
	 * 
	 * @param cn
	 * @throws DataExchangeException
	 */
	protected void rollback(Connection cn) throws DataExchangeException
	{
		try
		{
			cn.rollback();
		}
		catch (SQLException e)
		{
			throw new DataExchangeException(e);
		}
	}

	/**
	 * 提交。
	 * 
	 * @param cn
	 * @throws DataExchangeException
	 */
	protected void commit(Connection cn) throws DataExchangeException
	{
		try
		{
			cn.commit();
		}
		catch (SQLException e)
		{
			throw new DataExchangeException(e);
		}
	}

	/**
	 * 静默回滚。
	 * 
	 * @param cn
	 */
	protected void rollbackSilently(Connection cn)
	{
		try
		{
			cn.rollback();
		}
		catch (Throwable t)
		{
			LOGGER.error("rollback connection exception", t);
		}
	}

	/**
	 * 静默提交。
	 * 
	 * @param cn
	 */
	protected void commitSilently(Connection cn)
	{
		try
		{
			cn.commit();
		}
		catch (Throwable t)
		{
			LOGGER.error("commit connection exception", t);
		}
	}

	/**
	 * 处理数据交换异常。
	 * 
	 * @param cn
	 * @param exceptionResolve
	 * @param t
	 * @param listener
	 * @throws DataExchangeException
	 */
	protected void handleExchangeThrowable(Connection cn, ExceptionResolve exceptionResolve, Throwable t,
			DataExchangeListener listener) throws DataExchangeException
	{
		DataExchangeException e = wrapToDataExchangeException(t);

		if (ExceptionResolve.ABORT.equals(exceptionResolve))
			commitSilently(cn);
		else if (ExceptionResolve.ROLLBACK.equals(exceptionResolve))
			rollbackSilently(cn);
		else if (ExceptionResolve.IGNORE.equals(exceptionResolve))
			commitSilently(cn);
		else
			throw new UnsupportedOperationException();

		if (listener != null)
			listener.onException(e);
		else
			throw e;
	}

	/**
	 * 获取资源。
	 * 
	 * @param <R>
	 * @param resourceFactory
	 * @return
	 * @throws DataExchangeException
	 */
	protected <R> R getResource(ResourceFactory<R> resourceFactory) throws DataExchangeException
	{
		try
		{
			return resourceFactory.get();
		}
		catch (Exception e)
		{
			throw new DataExchangeException(e);
		}
	}

	/**
	 * 释放资源。
	 * <p>
	 * 此方法不会抛出任何{@linkplain Throwable}。
	 * </p>
	 * 
	 * @param <R>
	 * @param resourceFactory
	 * @param resource
	 */
	protected <R> void releaseResource(ResourceFactory<R> resourceFactory, R resource)
	{
		if (resource == null)
			return;

		try
		{
			resourceFactory.release(resource);
		}
		catch (Throwable e)
		{
			LOGGER.error("release connection error", e);
		}
	}

	/**
	 * 将异常包装为{@linkplain DataExchangeException}。
	 * 
	 * @param t
	 * @return
	 */
	protected DataExchangeException wrapToDataExchangeException(Throwable t)
	{
		if (t instanceof DataExchangeException)
			return (DataExchangeException) t;
		else
			return new DataExchangeException(t);
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
	protected String buildInsertPreparedSql(Connection cn, String table, List<ColumnInfo> columnInfos)
			throws SQLException
	{
		String quote = cn.getMetaData().getIdentifierQuoteString();

		StringBuilder sql = new StringBuilder("INSERT INTO ");
		sql.append(quote).append(table).append(quote);
		sql.append(" (");

		int size = columnInfos.size();

		for (int i = 0; i < size; i++)
		{
			if (i != 0)
				sql.append(',');

			sql.append(quote).append(columnInfos.get(i).getName()).append(quote);
		}

		sql.append(") VALUES (");

		for (int i = 0; i < size; i++)
		{
			if (i != 0)
				sql.append(',');

			sql.append('?');
		}

		sql.append(")");

		return sql.toString();
	}

	/**
	 * 移除{@code null}列信息位置对应的列值。
	 * <p>
	 * 如果没有{@code null}列信息，将返回原列值列表。
	 * </p>
	 * 
	 * @param rawColumnInfos
	 * @param noNullColumnInfos
	 * @param columnValues
	 * @return
	 */
	protected <G> List<G> removeNullColumnValues(List<ColumnInfo> rawColumnInfos, List<ColumnInfo> noNullColumnInfos,
			List<G> columnValues)
	{
		if (noNullColumnInfos == rawColumnInfos || noNullColumnInfos.size() == rawColumnInfos.size())
			return columnValues;

		List<G> newColumnValues = new ArrayList<G>(noNullColumnInfos.size());

		for (G ele : columnValues)
		{
			if (ele == null)
				continue;

			newColumnValues.add(ele);
		}

		return newColumnValues;
	}

	/**
	 * 移除{@linkplain ColumnInfo}列表中的{@code null}元素。
	 * <p>
	 * 如果没有{@code null}元素，将返回原列表。
	 * </p>
	 * 
	 * @param columnInfos
	 * @return
	 */
	protected List<ColumnInfo> removeNullColumnInfos(List<ColumnInfo> columnInfos)
	{
		boolean noNull = true;

		for (ColumnInfo columnInfo : columnInfos)
		{
			if (columnInfo == null)
			{
				noNull = false;
				break;
			}
		}

		if (noNull)
			return columnInfos;

		List<ColumnInfo> list = new ArrayList<ColumnInfo>(columnInfos.size());

		for (ColumnInfo columnInfo : columnInfos)
		{
			if (columnInfo != null)
				list.add(columnInfo);
		}

		return list;
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
	 * @param databaseInfoResolver
	 * @return
	 * @throws TableNotFoundException
	 * @throws ColumnNotFoundException
	 */
	protected List<ColumnInfo> getColumnInfos(Connection cn, String table, List<String> columnNames,
			boolean nullIfColumnNotFound, DatabaseInfoResolver databaseInfoResolver)
			throws TableNotFoundException, ColumnNotFoundException
	{
		int size = columnNames.size();

		List<ColumnInfo> columnInfos = new ArrayList<ColumnInfo>(size);

		ColumnInfo[] allColumnInfos = databaseInfoResolver.getColumnInfos(cn, table);

		if (allColumnInfos == null || allColumnInfos.length == 0)
			throw new TableNotFoundException(table);

		for (int i = 0; i < size; i++)
		{
			String columnName = columnNames.get(i);

			ColumnInfo columnInfo = null;

			for (int j = 0; j < allColumnInfos.length; j++)
			{
				if (allColumnInfos[j].getName().equals(columnName))
				{
					columnInfo = allColumnInfos[j];
					break;
				}
			}

			if (!nullIfColumnNotFound && columnInfo == null)
				throw new ColumnNotFoundException(table, columnName);

			columnInfos.add(columnInfo);
		}

		return columnInfos;
	}

	/**
	 * 获取{@linkplain ResultSet}列信息。
	 * 
	 * @param cn
	 * @param rs
	 * @param databaseInfoResolver
	 * @return
	 * @throws SQLException
	 */
	protected List<ColumnInfo> getColumnInfos(Connection cn, ResultSet rs, DatabaseInfoResolver databaseInfoResolver)
			throws SQLException
	{
		ResultSetMetaData resultSetMetaData = rs.getMetaData();
		ColumnInfo[] columnInfos = databaseInfoResolver.getColumnInfos(cn, resultSetMetaData);

		List<ColumnInfo> list = new ArrayList<ColumnInfo>(columnInfos.length);

		for (ColumnInfo columnInfo : columnInfos)
			list.add(columnInfo);

		return list;
	}

	/**
	 * 设置{@linkplain PreparedStatement}的文本数据参数，并进行必要的数据类型转换。
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
	 * @param dataFormatContext
	 * @throws SQLException
	 * @throws ParseException
	 * @throws DecoderException
	 * @throws UnsupportedSqlTypeException
	 */
	protected void setStringParameterValue(Connection cn, PreparedStatement st, int parameterIndex, int sqlType,
			String parameterValue, DataFormatContext dataFormatContext)
			throws SQLException, ParseException, DecoderException, UnsupportedSqlTypeException
	{
		if (parameterValue == null)
		{
			st.setNull(parameterIndex, sqlType);
			return;
		}

		switch (sqlType)
		{
			case Types.CHAR:
			case Types.VARCHAR:
			case Types.LONGVARCHAR:
			{
				st.setString(parameterIndex, parameterValue);

				break;
			}

			case Types.NUMERIC:
			case Types.DECIMAL:
			{
				BigDecimal value = new BigDecimal(parameterValue);
				st.setBigDecimal(parameterIndex, value);

				break;
			}

			case Types.BIT:
			case Types.BOOLEAN:
			{
				boolean value = ("true".equalsIgnoreCase(parameterValue) || "1".equals(parameterValue)
						|| "on".equalsIgnoreCase(parameterValue));
				st.setBoolean(parameterIndex, value);

				break;
			}

			case Types.TINYINT:
			case Types.SMALLINT:
			case Types.INTEGER:
			{
				Integer value = dataFormatContext.parseInt(parameterValue);

				if (value == null)
					st.setNull(parameterIndex, sqlType);
				else
					st.setInt(parameterIndex, value);

				break;
			}

			case Types.BIGINT:
			{
				Long value = dataFormatContext.parseLong(parameterValue);

				if (value == null)
					st.setNull(parameterIndex, sqlType);
				else
					st.setLong(parameterIndex, value);

				break;
			}

			case Types.REAL:
			{
				Float value = dataFormatContext.parseFloat(parameterValue);

				if (value == null)
					st.setNull(parameterIndex, sqlType);
				else
					st.setFloat(parameterIndex, value);

				break;
			}

			case Types.FLOAT:
			case Types.DOUBLE:
			{
				Double value = dataFormatContext.parseDouble(parameterValue);

				if (value == null)
					st.setNull(parameterIndex, sqlType);
				else
					st.setDouble(parameterIndex, value);

				break;
			}

			case Types.BINARY:
			case Types.VARBINARY:
			case Types.LONGVARBINARY:
			{
				byte[] value = dataFormatContext.parseBytes(parameterValue);

				if (value == null)
					st.setNull(parameterIndex, sqlType);
				else
					st.setBytes(parameterIndex, value);

				break;
			}

			case Types.DATE:
			{
				java.sql.Date value = dataFormatContext.parseDate(parameterValue);

				if (value == null)
					st.setNull(parameterIndex, sqlType);
				else
					st.setDate(parameterIndex, value);

				break;
			}

			case Types.TIME:
			{
				java.sql.Time value = dataFormatContext.parseTime(parameterValue);

				if (value == null)
					st.setNull(parameterIndex, sqlType);
				else
					st.setTime(parameterIndex, value);

				break;
			}

			case Types.TIMESTAMP:
			{
				java.sql.Timestamp value = dataFormatContext.parseTimestamp(parameterValue);

				if (value == null)
					st.setNull(parameterIndex, sqlType);
				else
					st.setTimestamp(parameterIndex, value);

				break;
			}

			case Types.CLOB:
			{
				Clob clob = cn.createClob();
				clob.setString(1, parameterValue);
				st.setClob(parameterIndex, clob);

				break;
			}

			case Types.BLOB:
			{
				byte[] value = dataFormatContext.parseBytes(parameterValue);

				if (value == null)
				{
					st.setNull(parameterIndex, sqlType);
				}
				else
				{
					Blob blob = cn.createBlob();
					blob.setBytes(1, value);
					st.setBlob(parameterIndex, blob);
				}

				break;
			}

			case Types.NCHAR:
			case Types.NVARCHAR:
			case Types.LONGNVARCHAR:
			{
				st.setNString(parameterIndex, parameterValue);
				break;
			}

			case Types.NCLOB:
			{
				NClob nclob = cn.createNClob();
				nclob.setString(1, parameterValue);
				st.setNClob(parameterIndex, nclob);
				break;
			}

			case Types.SQLXML:
			{
				SQLXML sqlxml = cn.createSQLXML();
				sqlxml.setString(parameterValue);
				st.setSQLXML(parameterIndex, sqlxml);
				break;
			}

			default:

				throw new UnsupportedSqlTypeException(sqlType);
		}
	}

	/**
	 * 获取字段的的字符串值。
	 * <p>
	 * 此方法实现参考自JDBC4.0规范“Data Type Conversion Tables”章节中的“Type Conversions
	 * Supported by ResultSet getter Methods”表，并且使用其中的最佳方法。
	 * </p>
	 * 
	 * @param cn
	 * @param rs
	 * @param columnIndex
	 * @param sqlType
	 * @param dataFormatContext
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 * @throws UnsupportedSqlTypeException
	 */
	protected String getStringValue(Connection cn, ResultSet rs, int columnIndex, int sqlType,
			DataFormatContext dataFormatContext) throws SQLException, IOException, UnsupportedSqlTypeException
	{
		String valueStr = null;

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
}
