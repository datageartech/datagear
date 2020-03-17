/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.util;

import java.io.File;
import java.io.FileNotFoundException;
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
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLSyntaxErrorException;
import java.sql.SQLTimeoutException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC操作支持类。
 * 
 * @author datagear@163.com
 *
 */
public class JdbcSupport
{
	/**
	 * 执行数目查询。
	 * 
	 * @param cn
	 * @param query
	 * @return
	 * @throws SQLException
	 */
	public long executeCountQuery(Connection cn, Sql query) throws SQLException
	{
		QueryResultSet qrs = null;

		try
		{
			long count = 0;

			qrs = executeQuery(cn, query, ResultSet.TYPE_FORWARD_ONLY);

			ResultSet rs = qrs.getResultSet();

			if (rs.next())
				count = rs.getInt(1);

			return count;
		}
		finally
		{
			QueryResultSet.close(qrs);
		}
	}

	/**
	 * 执行查询。
	 * 
	 * @param cn
	 * @param sql
	 * @param resultSetType
	 *            {@code ResultSet.TYPE_*}
	 * @return
	 * @throws SQLException
	 */
	public QueryResultSet executeQuery(Connection cn, Sql sql, int resultSetType) throws SQLException
	{
		PreparedStatement pst = null;
		ResultSet rs = null;
		List<Object> setParams = null;

		try
		{
			pst = createQueryPreparedStatement(cn, sql.getSqlValue(), resultSetType);
			setParams = setParamValues(cn, pst, sql);
			rs = pst.executeQuery();

			return new QueryResultSet(pst, rs, setParams);
		}
		catch (SQLSyntaxErrorException | SQLDataException | SQLTimeoutException | SQLWarning e)
		{
			@JDBCCompatiblity("这些异常必定不是驱动程序的ResultSet.TYPE_SCROLL_*支持与否问题，不需要再降级处理")
			SQLException e1 = e;
			throw e1;
		}
		catch (SQLException e)
		{
			@JDBCCompatiblity("某些不支持ResultSet.TYPE_SCROLL_*的驱动程序不是在创建PreparedStatemen时报错，"
					+ "而是在执行SQL的时候（比如SQLServer的聚集列存储索引），所以在这里检查，必要时降级重新执行查询")
			int actualResultSetType = pst.getResultSetType();

			if (ResultSet.TYPE_FORWARD_ONLY == actualResultSetType)
			{
				IOUtil.closeIf(setParams);
				JdbcUtil.closeResultSet(rs);
				JdbcUtil.closeStatement(pst);

				throw e;
			}
			else
			{
				JdbcUtil.closeResultSet(rs);
				JdbcUtil.closeStatement(pst);

				@JDBCCompatiblity("降级为ResultSet.TYPE_FORWARD_ONLY重新执行")
				QueryResultSet qrs = executeQuery(cn, sql, ResultSet.TYPE_FORWARD_ONLY);
				return qrs;
			}
		}
	}

	/**
	 * 执行更新。
	 * 
	 * @param cn
	 * @param sql
	 * @return
	 * @throws SQLException
	 */
	public int executeUpdate(Connection cn, Sql sql) throws SQLException
	{
		PreparedStatement pst = null;
		List<Object> setParams = null;

		try
		{
			pst = createUpdatePreparedStatement(cn, sql.getSqlValue());
			setParams = setParamValues(cn, pst, sql);

			return pst.executeUpdate();
		}
		finally
		{
			IOUtil.closeIf(setParams);
			JdbcUtil.closeStatement(pst);
		}
	}

	/**
	 * 设置预编译SQL参数。
	 * 
	 * @param cn
	 * @param st
	 * @param sql
	 * @return
	 * @throws SQLException
	 */
	public List<Object> setParamValues(Connection cn, PreparedStatement st, Sql sql) throws SQLException
	{
		return setParamValues(cn, st, sql.getParamValues());
	}

	/**
	 * 设置预编译SQL参数。
	 * 
	 * @param cn
	 * @param st
	 * @param paramValues
	 * @return
	 * @throws SQLException
	 */
	public List<Object> setParamValues(Connection cn, PreparedStatement st, List<SqlParamValue> paramValues)
			throws SQLException
	{
		List<Object> setValues = new ArrayList<>(paramValues.size());

		for (int i = 1; i <= paramValues.size(); i++)
		{
			Object setValue = setParamValue(cn, st, i, paramValues.get(i - 1));
			setValues.add(setValue);
		}

		return setValues;
	}

	/**
	 * 设置预编译SQL参数。
	 * 
	 * @param cn
	 * @param st
	 * @param paramValues
	 * @return
	 * @throws SQLException
	 */
	public Object[] setParamValues(Connection cn, PreparedStatement st, SqlParamValue... paramValues)
			throws SQLException
	{
		Object[] setValues = new Object[paramValues.length];

		for (int i = 1; i <= paramValues.length; i++)
			setValues[i - 1] = setParamValue(cn, st, i, paramValues[i - 1]);

		return setValues;
	}

	/**
	 * 创建用于查询的{@linkplain PreparedStatement}。
	 * 
	 * @param cn
	 * @param sql
	 * @param resultSetType
	 * @return
	 * @throws SQLException
	 */
	public PreparedStatement createQueryPreparedStatement(Connection cn, String sql, int resultSetType)
			throws SQLException
	{
		if (ResultSet.TYPE_FORWARD_ONLY == resultSetType)
		{
			return cn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		}
		else
		{
			PreparedStatement pst = null;

			try
			{
				pst = cn.prepareStatement(sql, resultSetType, ResultSet.CONCUR_READ_ONLY);
			}
			catch (SQLFeatureNotSupportedException e)
			{
				@JDBCCompatiblity("某些驱动程序不支持TYPE_SCROLL_INSENSITIVE，则降级获取")
				PreparedStatement thenPst = cn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_READ_ONLY);
				pst = thenPst;
			}

			return pst;
		}
	}

	/**
	 * 创建用于更新的{@linkplain PreparedStatement}。
	 * 
	 * @param cn
	 * @param sql
	 * @return
	 * @throws SQLException
	 */
	public PreparedStatement createUpdatePreparedStatement(Connection cn, String sql) throws SQLException
	{
		return cn.prepareStatement(sql);
	}

	/**
	 * 创建用于查询的{@linkplain Statement}。
	 * 
	 * @param cn
	 * @param resultSetType
	 * @return
	 * @throws SQLException
	 */
	public Statement createQueryStatement(Connection cn, int resultSetType) throws SQLException
	{
		if (ResultSet.TYPE_FORWARD_ONLY == resultSetType)
		{
			return cn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		}
		else
		{
			Statement st = null;

			try
			{
				st = cn.createStatement(resultSetType, ResultSet.CONCUR_READ_ONLY);
			}
			catch (SQLFeatureNotSupportedException e)
			{
				@JDBCCompatiblity("某些驱动程序不支持TYPE_SCROLL_INSENSITIVE，则降级获取")
				Statement thenSt = cn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				st = thenSt;
			}

			return st;
		}
	}

	/**
	 * 创建用于更新的{@linkplain Statement}。
	 * 
	 * @param cn
	 * @param sql
	 * @return
	 * @throws SQLException
	 */
	public Statement createUpdateStatement(Connection cn) throws SQLException
	{
		return cn.createStatement();
	}

	/**
	 * 设置{@linkplain PreparedStatement}的参数值。
	 * <p>
	 * 此方法实现参考自JDBC4.0规范“Data Type Conversion Tables”章节中的“Java Types Mapper to
	 * JDBC Types”表。
	 * </p>
	 * 
	 * @param cn
	 * @param st
	 * @param paramIndex
	 * @param paramValue
	 * @return
	 * @throws SQLException
	 */
	@JDBCCompatiblity("某些驱动程序不支持PreparedStatement.setObject方法（比如：Hive JDBC），所以这里没有使用")
	public Object setParamValue(Connection cn, PreparedStatement st, int paramIndex, SqlParamValue paramValue)
			throws SQLException
	{
		int sqlType = paramValue.getType();
		Object value = paramValue.getValue();

		if (value == null)
		{
			st.setNull(paramIndex, sqlType);
			return null;
		}

		switch (sqlType)
		{
			case Types.CHAR:
			case Types.VARCHAR:
			case Types.LONGVARCHAR:
			{
				if (value instanceof String)
					st.setString(paramIndex, (String) value);
				else if (value instanceof Reader)
					st.setCharacterStream(paramIndex, (Reader) value);
				else
					value = setParamValueExt(cn, st, paramIndex, paramValue);

				break;
			}

			case Types.NUMERIC:
			case Types.DECIMAL:
			{
				BigDecimal v = null;

				if (value instanceof BigDecimal)
					v = (BigDecimal) value;
				else if (value instanceof BigInteger)
					v = new BigDecimal((BigInteger) value);
				else if (value instanceof Number)
					v = new BigDecimal(((Number) value).doubleValue());

				if (v != null)
				{
					st.setBigDecimal(paramIndex, v);
					value = v;
				}
				else
					value = setParamValueExt(cn, st, paramIndex, paramValue);

				break;
			}

			case Types.BIT:
			case Types.BOOLEAN:
			{
				if (value instanceof Boolean)
					st.setBoolean(paramIndex, (Boolean) value);
				else
					value = setParamValueExt(cn, st, paramIndex, paramValue);

				break;
			}

			case Types.TINYINT:
			case Types.SMALLINT:
			case Types.INTEGER:
			{
				Integer v = null;

				if (value instanceof Integer)
					v = (Integer) value;
				else if (value instanceof Number)
					v = ((Number) value).intValue();

				if (v != null)
				{
					st.setInt(paramIndex, v);
					value = v;
				}
				else
					value = setParamValueExt(cn, st, paramIndex, paramValue);

				break;
			}

			case Types.BIGINT:
			{
				Long v = null;

				if (value instanceof Long)
					v = (Long) value;
				else if (value instanceof Number)
					v = ((Number) value).longValue();

				if (v != null)
				{
					st.setLong(paramIndex, v);
					value = v;
				}
				else
					value = setParamValueExt(cn, st, paramIndex, paramValue);

				break;
			}

			case Types.REAL:
			{
				Float v = null;

				if (value instanceof Float)
					v = (Float) value;
				else if (value instanceof Number)
					v = ((Number) value).floatValue();

				if (v != null)
				{
					st.setFloat(paramIndex, v);
					value = v;
				}
				else
					value = setParamValueExt(cn, st, paramIndex, paramValue);

				break;
			}

			case Types.FLOAT:
			case Types.DOUBLE:
			{
				Double v = null;

				if (value instanceof Double)
					v = (Double) value;
				else if (value instanceof Number)
					v = ((Number) value).doubleValue();

				if (v != null)
				{
					st.setDouble(paramIndex, v);
					value = v;
				}
				else
					value = setParamValueExt(cn, st, paramIndex, paramValue);

				break;
			}

			case Types.BINARY:
			case Types.VARBINARY:
			case Types.LONGVARBINARY:
			{
				if (value instanceof byte[])
					st.setBytes(paramIndex, (byte[]) value);
				else if (value instanceof InputStream)
					st.setBinaryStream(paramIndex, (InputStream) value);
				else if (value instanceof File)
				{
					InputStream v = getInputStreamForSql((File) value);
					st.setBinaryStream(paramIndex, v);
					value = v;
				}
				else
					value = setParamValueExt(cn, st, paramIndex, paramValue);

				break;
			}

			case Types.DATE:
			{
				java.sql.Date v = null;

				if (value instanceof java.sql.Date)
					v = (java.sql.Date) value;
				else if (value instanceof java.util.Date)
					v = new java.sql.Date(((java.util.Date) value).getTime());

				if (v != null)
				{
					st.setDate(paramIndex, v);
					value = v;
				}
				else
					value = setParamValueExt(cn, st, paramIndex, paramValue);

				break;
			}

			case Types.TIME:
			{
				java.sql.Time v = null;

				if (value instanceof java.sql.Time)
					v = (java.sql.Time) value;
				else if (value instanceof java.util.Date)
					v = new java.sql.Time(((java.util.Date) value).getTime());

				if (v != null)
				{
					st.setTime(paramIndex, v);
					value = v;
				}
				else
					value = setParamValueExt(cn, st, paramIndex, paramValue);

				break;
			}

			case Types.TIMESTAMP:
			{
				java.sql.Timestamp v = null;

				if (value instanceof java.sql.Timestamp)
					v = (java.sql.Timestamp) value;
				else if (value instanceof java.util.Date)
					v = new java.sql.Timestamp(((java.util.Date) value).getTime());

				if (v != null)
				{
					st.setTimestamp(paramIndex, v);
					value = v;
				}
				else
					value = setParamValueExt(cn, st, paramIndex, paramValue);

				break;
			}

			case Types.CLOB:
			{
				if (value instanceof Clob)
					st.setClob(paramIndex, (Clob) value);
				else if (value instanceof Reader)
					st.setClob(paramIndex, (Reader) value);
				else
					value = setParamValueExt(cn, st, paramIndex, paramValue);

				break;
			}

			case Types.BLOB:
			{
				if (value instanceof Blob)
					st.setBlob(paramIndex, (Blob) value);
				else if (value instanceof InputStream)
					st.setBlob(paramIndex, (InputStream) value);
				else if (value instanceof File)
				{
					InputStream v = getInputStreamForSql((File) value);
					st.setBlob(paramIndex, v);
					value = v;
				}
				else
					value = setParamValueExt(cn, st, paramIndex, paramValue);

				break;
			}

			case Types.NCHAR:
			case Types.NVARCHAR:
			case Types.LONGNVARCHAR:
			{
				if (value instanceof String)
					st.setNString(paramIndex, (String) value);
				else if (value instanceof Reader)
					st.setNCharacterStream(paramIndex, (Reader) value);
				else
					value = setParamValueExt(cn, st, paramIndex, paramValue);

				break;
			}

			case Types.NCLOB:
			{
				if (value instanceof NClob)
					st.setNClob(paramIndex, (NClob) value);
				else if (value instanceof Reader)
					st.setNClob(paramIndex, (Reader) value);
				else
					value = setParamValueExt(cn, st, paramIndex, paramValue);

				break;
			}

			case Types.SQLXML:
			{
				if (value instanceof SQLXML)
					st.setSQLXML(paramIndex, (SQLXML) value);
				else
					value = setParamValueExt(cn, st, paramIndex, paramValue);

				break;
			}

			default:
				value = setParamValueExt(cn, st, paramIndex, paramValue);
		}

		return value;
	}

	/**
	 * 扩展设置{@linkplain SqlParamValue}。
	 * 
	 * @param cn
	 * @param st
	 * @param paramIndex
	 * @param paramValue
	 * @return
	 * @throws SQLException
	 */
	protected Object setParamValueExt(Connection cn, PreparedStatement st, int paramIndex, SqlParamValue paramValue)
			throws SQLException
	{
		throw new UnsupportedOperationException("Set JDBC [" + paramValue.getType() + "] type value is not supported");
	}

	protected InputStream getInputStreamForSql(File file) throws SQLException
	{
		try
		{
			return IOUtil.getInputStream(file);
		}
		catch (FileNotFoundException e)
		{
			throw new SQLException("File [" + file.getAbsolutePath() + "] not found");
		}
	}

	/**
	 * 获取列值。
	 * <p>
	 * 此方法实现参考自JDBC4.0规范“Data Type Conversion Tables”章节中的“Type Conversions
	 * Supported by ResultSet getter Methods”表，并且使用其中的最佳方法。
	 * </p>
	 * 
	 * @param cn
	 * @param rs
	 * @param column
	 * @return
	 * @throws SQLException
	 */
	public Object getColumnValue(Connection cn, ResultSet rs, String columnName, int sqlType) throws SQLException
	{
		Object value = null;

		switch (sqlType)
		{
			case Types.TINYINT:
			{
				value = rs.getByte(columnName);
				break;
			}

			case Types.SMALLINT:
			{
				value = rs.getShort(columnName);
				break;
			}

			case Types.INTEGER:
			{
				value = rs.getInt(columnName);
				break;
			}

			case Types.BIGINT:
			{
				value = rs.getLong(columnName);
				break;
			}

			case Types.REAL:
			{
				value = rs.getFloat(columnName);
				break;
			}

			case Types.FLOAT:
			case Types.DOUBLE:
			{
				value = rs.getDouble(columnName);
				break;
			}

			case Types.DECIMAL:
			case Types.NUMERIC:
			{
				value = rs.getBigDecimal(columnName);
				break;
			}

			case Types.BIT:
			{
				value = rs.getBoolean(columnName);
				break;
			}

			case Types.BOOLEAN:
			{
				value = rs.getBoolean(columnName);
				break;
			}

			case Types.CHAR:
			case Types.VARCHAR:
			{
				value = rs.getString(columnName);
				break;
			}

			case Types.LONGVARCHAR:
			{
				value = rs.getCharacterStream(columnName);
				break;
			}

			case Types.BINARY:
			case Types.VARBINARY:
			{
				value = rs.getBytes(columnName);
				break;
			}

			case Types.LONGVARBINARY:
			{
				value = rs.getBinaryStream(columnName);
				break;
			}

			case Types.DATE:
			{
				value = rs.getDate(columnName);
				break;
			}

			case Types.TIME:
			{
				value = rs.getTime(columnName);
				break;
			}

			case Types.TIMESTAMP:
			{
				value = rs.getTimestamp(columnName);
				break;
			}

			case Types.CLOB:
			{
				value = rs.getClob(columnName);
				break;
			}

			case Types.BLOB:
			{
				value = rs.getBlob(columnName);
				break;
			}

			case Types.NCHAR:
			case Types.NVARCHAR:
			{
				value = rs.getNString(columnName);
				break;
			}

			case Types.LONGNVARCHAR:
			{
				value = rs.getNCharacterStream(columnName);
				break;
			}

			case Types.NCLOB:
			{
				value = rs.getNClob(columnName);
				break;
			}

			case Types.SQLXML:
			{
				value = rs.getSQLXML(columnName);
				break;
			}

			default:
				value = getColumnValueExt(cn, rs, columnName, sqlType);
				break;
		}

		if (rs.wasNull())
			value = null;

		return value;
	}

	/**
	 * 扩展获取列值。
	 * 
	 * @param cn
	 * @param rs
	 * @param columnName
	 * @param sqlType
	 * @return
	 * @throws SQLException
	 */
	protected Object getColumnValueExt(Connection cn, ResultSet rs, String columnName, int sqlType) throws SQLException
	{
		throw new UnsupportedOperationException("Get JDBC [" + sqlType + "] type value is not supported");
	}

	/**
	 * 将一个未移动过游标的{@linkplain ResultSet}游标前移至指定行之前。
	 * 
	 * @param rs
	 * @param rowIndex
	 *            行号，以{@code 1}开始
	 * @throws SQLException
	 */
	public void forwardBefore(ResultSet rs, int rowIndex) throws SQLException
	{
		// 第一行不做任何操作，避免不必要的调用可能导致底层不支持而报错
		if (rowIndex == 1)
			return;

		try
		{
			rs.absolute(rowIndex - 1);
		}
		catch (SQLException e)
		{
			@JDBCCompatiblity("避免驱动程序或者ResultSet不支持absolute而抛出异常")
			int i = 1;
			for (; i < rowIndex; i++)
			{
				if (!rs.next())
					break;
			}
		}
	}
}
