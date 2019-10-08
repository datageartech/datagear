/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.DecoderException;
import org.datagear.dbinfo.ColumnInfo;
import org.datagear.dbinfo.DatabaseInfoResolver;
import org.datagear.util.IOUtil;
import org.datagear.util.JdbcUtil;
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

	@Override
	public void exchange(T dataExchange) throws DataExchangeException
	{
		onStart(dataExchange);

		DataExchangeContext context = createDataExchangeContext(dataExchange);

		try
		{
			Throwable throwable = null;

			try
			{
				exchange(dataExchange, context);
			}
			catch (Throwable t)
			{
				throwable = t;
			}

			if (throwable == null)
			{
				onSuccess(dataExchange, context);
			}
			else
			{
				DataExchangeException e = wrapToDataExchangeException(throwable);

				onException(dataExchange, context, e);
			}
		}
		finally
		{
			context.closeConnection();
			context.closeContextCloseables();

			onFinish(dataExchange, context);
		}
	}

	/**
	 * 执行数据交换。
	 * 
	 * @param dataExchange
	 * @param context
	 * @throws Throwable
	 */
	protected abstract void exchange(T dataExchange, DataExchangeContext context) throws Throwable;

	/**
	 * 数据交换开始回调。
	 * 
	 * @param dataExchange
	 */
	protected void onStart(T dataExchange)
	{
		DataExchangeListener listener = dataExchange.getListener();

		if (listener != null)
			listener.onStart();
	}

	/**
	 * 数据交换成功回调。
	 * 
	 * @param dataExchange
	 * @param context
	 */
	protected void onSuccess(T dataExchange, DataExchangeContext context)
	{
		DataExchangeListener listener = dataExchange.getListener();

		if (listener != null)
			listener.onSuccess();
	}

	/**
	 * 数据交换异常回调。
	 * 
	 * @param dataExchange
	 * @param context
	 * @param e
	 * @throws DataExchangeException
	 */
	protected void onException(T dataExchange, DataExchangeContext context, DataExchangeException e)
			throws DataExchangeException
	{
		DataExchangeListener listener = dataExchange.getListener();

		if (listener != null)
			listener.onException(e);
		else
			throw e;
	}

	/**
	 * 数据交换完成。
	 * 
	 * @param dataExchange
	 * @param context
	 */
	protected void onFinish(T dataExchange, DataExchangeContext context)
	{
		DataExchangeListener listener = dataExchange.getListener();

		if (listener != null)
			listener.onFinish();
	}

	/**
	 * 构建{@linkplain DataExchangeContext}。
	 * 
	 * @param dataExchange
	 * @return
	 */
	protected DataExchangeContext createDataExchangeContext(T dataExchange)
	{
		return new DataExchangeContext(dataExchange.getConnectionFactory());
	}

	/**
	 * 处理数据交换异常的事务逻辑。
	 * 
	 * @param context
	 * @param e
	 * @param exceptionResolve
	 * @throws DataExchangeException
	 */
	protected void processTransactionForDataExchangeException(DataExchangeContext context, DataExchangeException e,
			ExceptionResolve exceptionResolve) throws DataExchangeException
	{
		Connection cn = getConnection(context);

		if (ExceptionResolve.ABORT.equals(exceptionResolve) || ExceptionResolve.IGNORE.equals(exceptionResolve))
			commitSilently(cn);
		else if (ExceptionResolve.ROLLBACK.equals(exceptionResolve))
			rollbackSilently(cn);
		else
			rollbackSilently(cn);
	}

	protected Connection getConnection(DataExchangeContext context) throws DataExchangeException
	{
		try
		{
			return context.getConnection();
		}
		catch (Throwable t)
		{
			throw wrapToDataExchangeException(t);
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
			JdbcUtil.commitIfSupports(cn);
		}
		catch (SQLException e)
		{
			throw new DataExchangeException(e);
		}
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
			JdbcUtil.rollbackIfSupports(cn);
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
			JdbcUtil.rollbackIfSupports(cn);
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
			JdbcUtil.commitIfSupports(cn);
		}
		catch (Throwable t)
		{
			LOGGER.error("commit connection exception", t);
		}
	}

	/**
	 * 获取资源。
	 * 
	 * @param <R>
	 * @param resourceFactory
	 * @param dataExchangeContext
	 * @return
	 * @throws DataExchangeException
	 */
	protected <R> R getResource(ResourceFactory<R> resourceFactory, DataExchangeContext dataExchangeContext)
			throws DataExchangeException
	{
		try
		{
			R resource = resourceFactory.get();

			dataExchangeContext.addContextCloseable(resourceFactory, resource);

			return resource;
		}
		catch (Exception e)
		{
			throw new DataExchangeException(e);
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
	 * @throws DataExchangeException
	 */
	protected String buildInsertPreparedSqlUnchecked(Connection cn, String table, List<ColumnInfo> columnInfos)
			throws DataExchangeException
	{
		try
		{
			return buildInsertPreparedSql(cn, table, columnInfos);
		}
		catch (SQLException e)
		{
			throw new DataExchangeException(e);
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
	 * 创建{@linkplain PreparedStatement}。
	 * 
	 * @param cn
	 * @param sql
	 * @return
	 * @throws SQLException
	 */
	protected PreparedStatement createPreparedStatementUnchecked(Connection cn, String sql) throws DataExchangeException
	{
		try
		{
			return cn.prepareStatement(sql);
		}
		catch (SQLException e)
		{
			throw new DataExchangeException(e);
		}
	}

	/**
	 * 创建{@linkplain PreparedStatement}。
	 * 
	 * @param cn
	 * @param sql
	 * @return
	 * @throws SQLException
	 */
	protected PreparedStatement createPreparedStatement(Connection cn, String sql) throws SQLException
	{
		return cn.prepareStatement(sql);
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
	 * 设置{@linkplain PreparedStatement}的参数值，并在必要时进行数据类型转换。
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
	 *            当{@code parameterValue}为字符串且需要类型转换时使用，允许为{@code null}
	 * @throws SQLException
	 * @throws ParseException
	 * @throws DecoderException
	 * @throws UnsupportedSqlValueException
	 * @throws UnsupportedSqlTypeException
	 */
	protected void setParameterValue(Connection cn, PreparedStatement st, int parameterIndex, int sqlType,
			Object parameterValue, DataFormatContext dataFormatContext) throws SQLException, ParseException,
			DecoderException, UnsupportedSqlValueException, UnsupportedSqlTypeException
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
				String value = null;

				if (parameterValue instanceof String)
					value = (String) parameterValue;
				else
					value = parameterValue.toString();

				st.setString(parameterIndex, value);

				break;
			}

			case Types.NUMERIC:
			case Types.DECIMAL:
			{
				BigDecimal value = null;

				if (parameterValue instanceof BigDecimal)
					value = (BigDecimal) parameterValue;
				else if (parameterValue instanceof BigInteger)
					value = new BigDecimal((BigInteger) parameterValue);
				else if (parameterValue instanceof Number)
					value = new BigDecimal(((Number) parameterValue).doubleValue());
				else if (parameterValue instanceof String)
					value = new BigDecimal((String) parameterValue);
				else
					throw new UnsupportedSqlValueException(sqlType, parameterValue);

				st.setBigDecimal(parameterIndex, value);

				break;
			}

			case Types.BIT:
			case Types.BOOLEAN:
			{
				boolean value = false;

				if (parameterValue instanceof Boolean)
					value = (Boolean) parameterValue;
				else if (parameterValue instanceof String)
				{
					String str = (String) parameterValue;
					value = ("true".equalsIgnoreCase(str) || "1".equals(str) || "on".equalsIgnoreCase(str));
				}
				else if (parameterValue instanceof Number)
					value = (((Number) parameterValue).intValue() > 0);
				else
					throw new UnsupportedSqlValueException(sqlType, parameterValue);

				st.setBoolean(parameterIndex, value);

				break;
			}

			case Types.TINYINT:
			case Types.SMALLINT:
			case Types.INTEGER:
			{
				Integer value = null;

				if (parameterValue instanceof Integer)
					value = (Integer) parameterValue;
				else if (parameterValue instanceof Number)
					value = ((Number) parameterValue).intValue();
				else if (parameterValue instanceof String && dataFormatContext != null)
					value = dataFormatContext.parseInt((String) parameterValue);
				else
					throw new UnsupportedSqlValueException(sqlType, parameterValue);

				if (value == null)
					st.setNull(parameterIndex, sqlType);
				else
					st.setInt(parameterIndex, value);

				break;
			}

			case Types.BIGINT:
			{
				Long value = null;

				if (parameterValue instanceof Long)
					value = (Long) parameterValue;
				else if (parameterValue instanceof Number)
					value = ((Number) parameterValue).longValue();
				else if (parameterValue instanceof String && dataFormatContext != null)
					value = dataFormatContext.parseLong((String) parameterValue);
				else
					throw new UnsupportedSqlValueException(sqlType, parameterValue);

				if (value == null)
					st.setNull(parameterIndex, sqlType);
				else
					st.setLong(parameterIndex, value);

				break;
			}

			case Types.REAL:
			{
				Float value = null;

				if (parameterValue instanceof Float)
					value = (Float) parameterValue;
				else if (parameterValue instanceof Number)
					value = ((Number) parameterValue).floatValue();
				else if (parameterValue instanceof String && dataFormatContext != null)
					value = dataFormatContext.parseFloat((String) parameterValue);
				else
					throw new UnsupportedSqlValueException(sqlType, parameterValue);

				if (value == null)
					st.setNull(parameterIndex, sqlType);
				else
					st.setFloat(parameterIndex, value);

				break;
			}

			case Types.FLOAT:
			case Types.DOUBLE:
			{
				Double value = null;

				if (parameterValue instanceof Double)
					value = (Double) parameterValue;
				else if (parameterValue instanceof Number)
					value = ((Number) parameterValue).doubleValue();
				else if (parameterValue instanceof String && dataFormatContext != null)
					value = dataFormatContext.parseDouble((String) parameterValue);
				else
					throw new UnsupportedSqlValueException(sqlType, parameterValue);

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
				byte[] value = null;

				if (parameterValue instanceof byte[])
					value = (byte[]) parameterValue;
				else if (parameterValue instanceof String && dataFormatContext != null)
					value = dataFormatContext.parseBytes((String) parameterValue);
				else
					throw new UnsupportedSqlValueException(sqlType, parameterValue);

				if (value == null)
					st.setNull(parameterIndex, sqlType);
				else
					st.setBytes(parameterIndex, value);

				break;
			}

			case Types.DATE:
			{
				java.sql.Date value = null;

				if (parameterValue instanceof java.sql.Date)
					value = (java.sql.Date) parameterValue;
				else if (parameterValue instanceof java.util.Date)
					value = new java.sql.Date(((java.util.Date) parameterValue).getTime());
				else if (parameterValue instanceof Number)
					value = new java.sql.Date(((Number) parameterValue).longValue());
				else if (parameterValue instanceof String && dataFormatContext != null)
					value = dataFormatContext.parseDate((String) parameterValue);
				else
					throw new UnsupportedSqlValueException(sqlType, parameterValue);

				if (value == null)
					st.setNull(parameterIndex, sqlType);
				else
					st.setDate(parameterIndex, value);

				break;
			}

			case Types.TIME:
			{
				java.sql.Time value = null;

				if (parameterValue instanceof java.sql.Time)
					value = (java.sql.Time) parameterValue;
				else if (parameterValue instanceof java.util.Date)
					value = new java.sql.Time(((java.util.Date) parameterValue).getTime());
				else if (parameterValue instanceof Number)
					value = new java.sql.Time(((Number) parameterValue).longValue());
				else if (parameterValue instanceof String && dataFormatContext != null)
					value = dataFormatContext.parseTime((String) parameterValue);
				else
					throw new UnsupportedSqlValueException(sqlType, parameterValue);

				if (value == null)
					st.setNull(parameterIndex, sqlType);
				else
					st.setTime(parameterIndex, value);

				break;
			}

			case Types.TIMESTAMP:
			{
				java.sql.Timestamp value = null;

				if (parameterValue instanceof java.sql.Timestamp)
					value = (java.sql.Timestamp) parameterValue;
				else if (parameterValue instanceof java.util.Date)
					value = new java.sql.Timestamp(((java.util.Date) parameterValue).getTime());
				else if (parameterValue instanceof Number)
					value = new java.sql.Timestamp(((Number) parameterValue).longValue());
				else if (parameterValue instanceof String && dataFormatContext != null)
					value = dataFormatContext.parseTimestamp((String) parameterValue);
				else
					throw new UnsupportedSqlValueException(sqlType, parameterValue);

				if (value == null)
					st.setNull(parameterIndex, sqlType);
				else
					st.setTimestamp(parameterIndex, value);

				break;
			}

			case Types.CLOB:
			{
				String value = null;

				if (parameterValue instanceof String)
					value = (String) parameterValue;
				else
					value = parameterValue.toString();

				Clob clob = cn.createClob();
				clob.setString(1, value);
				st.setClob(parameterIndex, clob);

				break;
			}

			case Types.BLOB:
			{
				byte[] value = null;

				if (parameterValue instanceof byte[])
					value = (byte[]) parameterValue;
				else if (parameterValue instanceof String && dataFormatContext != null)
					value = dataFormatContext.parseBytes((String) parameterValue);
				else
					throw new UnsupportedSqlValueException(sqlType, parameterValue);

				if (value == null)
					st.setNull(parameterIndex, sqlType);
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
				String value = null;

				if (parameterValue instanceof String)
					value = (String) parameterValue;
				else
					value = parameterValue.toString();

				st.setNString(parameterIndex, value);
				break;
			}

			case Types.NCLOB:
			{
				String value = null;

				if (parameterValue instanceof String)
					value = (String) parameterValue;
				else
					value = parameterValue.toString();

				NClob nclob = cn.createNClob();
				nclob.setString(1, value);
				st.setNClob(parameterIndex, nclob);
				break;
			}

			case Types.SQLXML:
			{
				String value = null;

				if (parameterValue instanceof String)
					value = (String) parameterValue;
				else
					value = parameterValue.toString();

				SQLXML sqlxml = cn.createSQLXML();
				sqlxml.setString(value);
				st.setSQLXML(parameterIndex, sqlxml);
				break;
			}

			default:

				throw new UnsupportedSqlTypeException(sqlType);
		}
	}

	/**
	 * 获取字段值。
	 * <p>
	 * 此方法会对部分JDBC数据进行转换，仅返回如下类型的对象：
	 * </p>
	 * <p>
	 * {@linkplain String}、{@linkplain Number}子类、{@linkplain Boolean}、{@linkplain java.sql.Date}、
	 * {@linkplain java.sql.Timestamp}、{@linkplain java.sql.Time}、{@code byte[]}。
	 * </p>
	 * <p>
	 * 此方法实现参考自JDBC4.0规范“Data Type Conversion Tables”章节中的“Type Conversions
	 * Supported by ResultSet getter Methods”表，并且使用其中的最佳方法。
	 * </p>
	 * 
	 * @param cn
	 * @param rs
	 * @param columnIndex
	 * @param sqlType
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 * @throws UnsupportedSqlTypeException
	 */
	protected Object getValue(Connection cn, ResultSet rs, int columnIndex, int sqlType)
			throws SQLException, IOException, UnsupportedSqlTypeException
	{
		Object value = null;

		switch (sqlType)
		{
			case Types.TINYINT:
			{
				value = rs.getByte(columnIndex);
				break;
			}

			case Types.SMALLINT:
			{
				value = rs.getShort(columnIndex);
				break;
			}

			case Types.INTEGER:
			{
				value = rs.getInt(columnIndex);
				break;
			}

			case Types.BIGINT:
			{
				value = rs.getLong(columnIndex);
				break;
			}

			case Types.REAL:
			{
				value = rs.getFloat(columnIndex);
				break;
			}

			case Types.FLOAT:
			case Types.DOUBLE:
			{
				value = rs.getDouble(columnIndex);
				break;
			}

			case Types.DECIMAL:
			case Types.NUMERIC:
			{
				value = rs.getBigDecimal(columnIndex);
				break;
			}

			case Types.BIT:
			{
				value = rs.getBoolean(columnIndex);
				break;
			}

			case Types.BOOLEAN:
			{
				value = rs.getBoolean(columnIndex);
				break;
			}

			case Types.CHAR:
			case Types.VARCHAR:
			{
				value = rs.getString(columnIndex);
				break;
			}

			case Types.LONGVARCHAR:
			{
				Reader reader = rs.getCharacterStream(columnIndex);

				try
				{
					if (!rs.wasNull())
						value = readToString(reader);
				}
				finally
				{
					IOUtil.close(reader);
				}

				break;
			}

			case Types.BINARY:
			case Types.VARBINARY:
			{
				value = rs.getBytes(columnIndex);
				break;
			}

			case Types.LONGVARBINARY:
			{
				InputStream in = rs.getBinaryStream(columnIndex);

				try
				{
					if (!rs.wasNull())
					{
						value = readToBytes(in);
					}
				}
				finally
				{
					IOUtil.close(in);
				}

				break;
			}

			case Types.DATE:
			{
				value = rs.getDate(columnIndex);
				break;
			}

			case Types.TIME:
			{
				value = rs.getTime(columnIndex);
				break;
			}

			case Types.TIMESTAMP:
			{
				value = rs.getTimestamp(columnIndex);
				break;
			}

			case Types.CLOB:
			{
				Clob clob = rs.getClob(columnIndex);

				if (!rs.wasNull())
				{
					Reader reader = clob.getCharacterStream();

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
			}

			case Types.BLOB:
			{
				Blob blob = rs.getBlob(columnIndex);

				if (!rs.wasNull())
				{
					InputStream inputStream = blob.getBinaryStream();

					try
					{
						value = readToBytes(inputStream);
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
				value = rs.getNString(columnIndex);
				break;
			}

			case Types.LONGNVARCHAR:
			{
				Reader reader = rs.getNCharacterStream(columnIndex);

				try
				{
					if (!rs.wasNull())
						value = readToString(reader);
				}
				finally
				{
					IOUtil.close(reader);
				}

				break;
			}

			case Types.NCLOB:
			{
				NClob nclob = rs.getNClob(columnIndex);

				if (!rs.wasNull())
				{
					Reader reader = nclob.getCharacterStream();

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
			}

			case Types.SQLXML:
			{
				SQLXML sqlXml = rs.getSQLXML(columnIndex);

				if (!rs.wasNull())
				{
					Reader reader = sqlXml.getCharacterStream();

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
			}

			default:

				throw new UnsupportedSqlTypeException(sqlType);
		}

		if (rs.wasNull())
			value = null;

		return value;
	}

	/**
	 * 获取字段的的字符串值。
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
		Object value = getValue(cn, rs, columnIndex, sqlType);
		String valueStr = null;

		if (value == null)
			;
		else if (value instanceof Number)
		{
			Number number = (Number) value;

			if (number instanceof Float || number instanceof Double)
				valueStr = dataFormatContext.formatDouble(number.doubleValue());
			else if (number instanceof BigDecimal || value instanceof BigInteger)
				valueStr = number.toString();
			else
				valueStr = dataFormatContext.formatLong(number.longValue());
		}
		else if (value instanceof Date)
		{
			if (value instanceof java.sql.Date)
				valueStr = dataFormatContext.formatDate((java.sql.Date) value);
			else if (value instanceof java.sql.Time)
				valueStr = dataFormatContext.formatTime((Time) value);
			else if (value instanceof java.sql.Timestamp)
				valueStr = dataFormatContext.formatTimestamp((Timestamp) value);
			else
				valueStr = dataFormatContext.formatDate((java.sql.Date) value);
		}
		else if (value instanceof String)
		{
			valueStr = (String) value;
		}
		else if (value instanceof Boolean)
		{
			if (Types.BIT == sqlType)
				valueStr = Boolean.TRUE.equals(value) ? "1" : "0";
			else
				valueStr = value.toString();
		}
		else if (value instanceof byte[])
		{
			valueStr = dataFormatContext.formatBytes((byte[]) value);
		}
		else
			valueStr = value.toString();

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
	 * 导入一条值数据。
	 * 
	 * @param cn
	 * @param st
	 * @param columnInfos
	 * @param columnValues
	 * @param dataIndex
	 * @param nullForIllegalColumnValue
	 * @param exceptionResolve
	 * @param dataFormatContext
	 * @param listener
	 * @return
	 * @throws DataExchangeException
	 */
	protected boolean importValueData(Connection cn, PreparedStatement st, List<ColumnInfo> columnInfos,
			List<? extends Object> columnValues, DataIndex dataIndex, boolean nullForIllegalColumnValue,
			ExceptionResolve exceptionResolve, DataFormatContext dataFormatContext, ValueDataImportListener listener)
			throws DataExchangeException
	{
		DataExchangeException exception = null;

		try
		{
			setImportParameterValues(cn, st, columnInfos, columnValues, dataIndex, nullForIllegalColumnValue,
					dataFormatContext, listener);

			executeImportPreparedStatement(st, dataIndex);
		}
		catch (Throwable t)
		{
			exception = wrapToDataExchangeException(t);
		}

		if (exception == null)
		{
			if (listener != null)
				listener.onSuccess(dataIndex);

			return true;
		}
		else
		{
			if (ExceptionResolve.IGNORE.equals(exceptionResolve))
			{
				if (listener != null)
					listener.onIgnore(dataIndex, exception);

				return false;
			}
			else
				throw exception;
		}
	}

	/**
	 * 执行导入SQL。
	 * 
	 * @param impt
	 * @param st
	 * @param context
	 * @throws ExecuteDataImportSqlException
	 */
	protected void executeImportPreparedStatement(PreparedStatement st, DataIndex dataIndex)
			throws ExecuteDataImportSqlException
	{
		try
		{
			st.executeUpdate();
		}
		catch (SQLException e)
		{
			throw new ExecuteDataImportSqlException(dataIndex, e);
		}
	}

	/**
	 * 设置导入{@linkplain PreparedStatement}参数值。
	 * 
	 * @param cn
	 * @param st
	 * @param columnInfos
	 * @param columnValues
	 * @param dataIndex
	 * @param nullForIllegalColumnValue
	 * @param dataFormatContext
	 * @param listener
	 * @throws SetImportColumnValueException
	 */
	protected void setImportParameterValues(Connection cn, PreparedStatement st, List<ColumnInfo> columnInfos,
			List<? extends Object> columnValues, DataIndex dataIndex, boolean nullForIllegalColumnValue,
			DataFormatContext dataFormatContext, ValueDataImportListener listener) throws SetImportColumnValueException
	{
		int columnCount = columnInfos.size();
		int columnValueCount = columnValues.size();

		for (int i = 0; i < columnCount; i++)
		{
			ColumnInfo columnInfo = columnInfos.get(i);
			String columnName = columnInfo.getName();
			int sqlType = columnInfo.getType();
			int parameterIndex = i + 1;
			Object rawValue = (columnValues == null || columnValueCount - 1 < i ? null : columnValues.get(i));

			try
			{
				setParameterValue(cn, st, parameterIndex, sqlType, rawValue, dataFormatContext);
			}
			catch (Throwable t)
			{
				SetImportColumnValueException e = null;

				if ((t instanceof ParseException) || (t instanceof DecoderException)
						|| (t instanceof UnsupportedSqlValueException))
					e = new IllegalImportSourceValueException(dataIndex, columnName, rawValue, t);
				else
					e = new SetImportColumnValueException(dataIndex, columnName, rawValue, t);

				if (nullForIllegalColumnValue)
				{
					try
					{
						st.setNull(parameterIndex, sqlType);
					}
					catch (SQLException e1)
					{
						throw new SetImportColumnValueException(dataIndex, columnName, null);
					}

					if (listener != null)
					{
						listener.onSetNullColumnValue(dataIndex, columnName, rawValue, e);
					}
				}
				else
					throw e;
			}
		}
	}
}
