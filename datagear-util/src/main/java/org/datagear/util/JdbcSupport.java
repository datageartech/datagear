/*
 * Copyright 2018-present datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package org.datagear.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JDBC操作支持类。
 * 
 * @author datagear@163.com
 *
 */
public class JdbcSupport
{
	private static final Logger LOGGER = LoggerFactory.getLogger(JdbcSupport.class);

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
		LOGGER.debug("execute {}, resultSetType={}", sql, resultSetType);

		Statement st = null;
		ResultSet rs = null;
		@SuppressWarnings("unchecked")
		List<Object> setParams = Collections.EMPTY_LIST;

		try
		{
			@JDBCCompatiblity("如果没有参数，则不必采用预编译方式，避免某些驱动对预编译功能支持有问题")
			boolean hasParamValue = sql.hasParamValue();

			if (hasParamValue)
			{
				PreparedStatement pst = createQueryPreparedStatement(cn, sql.getSqlValue(), resultSetType);
				st = pst;
				setParams = setParamValues(cn, pst, sql);
				rs = pst.executeQuery();
			}
			else
			{
				Statement stt = createQueryStatement(cn, resultSetType);
				st = stt;
				rs = stt.executeQuery(sql.getSqlValue());
			}

			return new QueryResultSet(st, rs, setParams);
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

			// pst此时可能为null
			Integer actualResultSetType = (st != null ? st.getResultSetType() : null);

			if (actualResultSetType != null && actualResultSetType == ResultSet.TYPE_FORWARD_ONLY)
			{
				IOUtil.closeIf(setParams);
				JdbcUtil.closeResultSet(rs);
				JdbcUtil.closeStatement(st);

				throw e;
			}
			else
			{
				JdbcUtil.closeResultSet(rs);
				JdbcUtil.closeStatement(st);

				LOGGER.debug("query is downgraded to [ResultSet.TYPE_FORWARD_ONLY] for exception :", e);

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
		LOGGER.debug("execute {}", sql);

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
	 * 执行更新。
	 * <p>
	 * 注意：如果底层数据库驱动程序不支持返回自动生成数据，那么{@linkplain AutoGeneratedResult#hasGeneratedResult()}为{@code false}。
	 * </p>
	 * 
	 * @param cn
	 * @param sql
	 * @param autoGeneratedNames
	 *            要返回的自动生成列名
	 * @param autoGeneratedTypes
	 *            要返回的自动生成列类型
	 * @return
	 */
	public AutoGeneratedResult executeUpdate(Connection cn, Sql sql, String[] autoGeneratedNames,
			int[] autoGeneratedTypes) throws SQLException
	{
		LOGGER.debug("execute {}, autoGeneratedNames={}", sql, autoGeneratedNames);

		PreparedStatement pst = null;
		List<Object> setParams = null;

		try
		{
			pst = createUpdatePreparedStatement(cn, sql.getSqlValue(), autoGeneratedNames);
			setParams = setParamValues(cn, pst, sql);

			int updateCount = pst.executeUpdate();
			List<Map<String, Object>> generatedResult = new ArrayList<>(3);

			try
			{
				@JDBCCompatiblity("避免某些驱动程序不支持返回自动生成键，或者生成键结果集与参数中期望的列不匹配，所以这里只要出现异常，就忽略生成键数据")

				ResultSet genKeys = pst.getGeneratedKeys();
				ResultSetMetaData rsm = genKeys.getMetaData();
				int genKeyCount = rsm.getColumnCount();

				if (genKeyCount == autoGeneratedNames.length)
				{
					while (genKeys.next())
					{
						Map<String, Object> row = new HashMap<>();

						for (int i = 0; i < autoGeneratedNames.length; i++)
						{
							String name = autoGeneratedNames[i];

							@JDBCCompatiblity("驱动程序返回结果集的列名不一定就是参数执行的名称，比如Mysql返回：GENERATED_KEY")
							String keyColName = getColumnName(rsm, i + 1);

							Object value = getColumnValue(cn, genKeys, keyColName, autoGeneratedTypes[i]);
							row.put(name, value);
						}

						generatedResult.add(row);
					}
				}
			}
			catch (Throwable t)
			{
				LOGGER.debug("no auto generated data will return for exception :", t);
			}

			return new AutoGeneratedResult(updateCount, generatedResult);
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
				LOGGER.debug(
						"query PreparedStatement is downgraded to [ResultSet.TYPE_FORWARD_ONLY] for exception :" + e);

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
	 * 创建用于更新的{@linkplain PreparedStatement}。
	 * 
	 * @param cn
	 * @param sql
	 * @param returnedColumnNames
	 *            期望{@linkplain PreparedStatement#getGeneratedKeys()}返回数据的列名，通常用于获取自动生成列
	 * @return
	 * @throws SQLException
	 */
	public PreparedStatement createUpdatePreparedStatement(Connection cn, String sql, String... returnedColumnNames)
			throws SQLException
	{
		PreparedStatement pst = null;

		try
		{
			pst = cn.prepareStatement(sql, returnedColumnNames);
		}
		catch (SQLException e)
		{
			JdbcUtil.closeStatement(pst);

			LOGGER.debug("update PreparedStatement is downgraded to non generated keys for exception :" + e);

			@JDBCCompatiblity("如果驱动程序不支持返回自动生成数据，那么降级执行")
			PreparedStatement myPst = createUpdatePreparedStatement(cn, sql);
			pst = myPst;
		}

		return pst;
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
				LOGGER.debug("query Statement is downgraded to [ResultSet.TYPE_FORWARD_ONLY] for exception :" + e);

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
	@JDBCCompatiblity("某些驱动程序不支持PreparedStatement.setObject()方法（比如：Hive JDBC），所以这里没有使用")
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
				if (value instanceof BigDecimal)
					st.setBigDecimal(paramIndex, (BigDecimal) value);
				else
					value = setParamValueForNumber(cn, st, paramIndex, paramValue);

				break;
			}

			case Types.BIT:
			case Types.BOOLEAN:
			{
				if (value instanceof Boolean)
					st.setBoolean(paramIndex, (Boolean) value);
				else if(value instanceof String)
				{
					String sv = (String)value;
					st.setBoolean(paramIndex, toJdbcBoolean(sv));
				}
				else if(value instanceof Number)
				{
					Number nv = (Number)value;
					st.setBoolean(paramIndex, toJdbcBoolean(nv));
				}
				else
					value = setParamValueExt(cn, st, paramIndex, paramValue);

				break;
			}

			case Types.TINYINT:
			{
				if (value instanceof Byte)
					st.setByte(paramIndex, (Byte) value);
				else
					value = setParamValueForNumber(cn, st, paramIndex, paramValue);

				break;
			}
			
			case Types.SMALLINT:
			{
				if (value instanceof Short)
					st.setShort(paramIndex, (Short) value);
				else
					value = setParamValueForNumber(cn, st, paramIndex, paramValue);

				break;
			}
			
			case Types.INTEGER:
			{
				if (value instanceof Integer)
					st.setInt(paramIndex, (Integer) value);
				else
					value = setParamValueForNumber(cn, st, paramIndex, paramValue);

				break;
			}

			case Types.BIGINT:
			{
				if (value instanceof Long)
					st.setLong(paramIndex, (Long) value);
				else
					value = setParamValueForNumber(cn, st, paramIndex, paramValue);

				break;
			}

			case Types.REAL:
			case Types.FLOAT:
			{
				if (value instanceof Float)
					st.setFloat(paramIndex, (Float) value);
				else
					value = setParamValueForNumber(cn, st, paramIndex, paramValue);

				break;
			}

			case Types.DOUBLE:
			{
				if (value instanceof Double)
					st.setDouble(paramIndex, (Double) value);
				else
					value = setParamValueForNumber(cn, st, paramIndex, paramValue);

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
				else if (value instanceof Number)
					value = new java.sql.Date(((Number) value).longValue());

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
			case Types.TIME_WITH_TIMEZONE:
			{
				java.sql.Time v = null;

				if (value instanceof java.sql.Time)
					v = (java.sql.Time) value;
				else if (value instanceof java.util.Date)
					v = new java.sql.Time(((java.util.Date) value).getTime());
				else if (value instanceof Number)
					value = new java.sql.Time(((Number) value).longValue());

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
			case Types.TIMESTAMP_WITH_TIMEZONE:
			{
				java.sql.Timestamp v = null;

				if (value instanceof java.sql.Timestamp)
					v = (java.sql.Timestamp) value;
				else if (value instanceof java.util.Date)
					v = new java.sql.Timestamp(((java.util.Date) value).getTime());
				else if (value instanceof Number)
					value = new java.sql.Timestamp(((Number) value).longValue());

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
				else if (value instanceof String)
				{
					Clob clob = cn.createClob();
					clob.setString(1, (String) value);

					st.setClob(paramIndex, clob);
					value = clob;
				}
				else if (value instanceof Reader)
					st.setClob(paramIndex, (Reader) value);
				else if (value instanceof InputStream)
				{
					Clob clob = cn.createClob();
					OutputStream out = clob.setAsciiStream(1);
					write((InputStream) value, out);

					st.setClob(paramIndex, clob);
					value = clob;
				}
				else if (value instanceof File)
				{
					Clob clob = cn.createClob();
					OutputStream out = clob.setAsciiStream(1);
					write((File) value, out);

					st.setClob(paramIndex, clob);
					value = clob;
				}
				else
					value = setParamValueExt(cn, st, paramIndex, paramValue);

				break;
			}

			case Types.BLOB:
			{
				if (value instanceof Blob)
					st.setBlob(paramIndex, (Blob) value);
				else if (value instanceof byte[])
				{
					Blob blob = cn.createBlob();
					blob.setBytes(1, (byte[]) value);

					st.setBlob(paramIndex, blob);
					value = blob;
				}
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
				else if (value instanceof String)
				{
					NClob nClob = cn.createNClob();
					nClob.setString(1, (String) value);

					st.setNClob(paramIndex, nClob);
					value = nClob;
				}
				else if (value instanceof Reader)
					st.setNClob(paramIndex, (Reader) value);
				else if (value instanceof InputStream)
				{
					NClob nClob = cn.createNClob();
					OutputStream out = nClob.setAsciiStream(1);
					write((InputStream) value, out);

					st.setNClob(paramIndex, nClob);
					value = nClob;
				}
				else if (value instanceof File)
				{
					NClob nClob = cn.createNClob();
					OutputStream out = nClob.setAsciiStream(1);
					write((File) value, out);

					st.setNClob(paramIndex, nClob);
					value = nClob;
				}
				else
					value = setParamValueExt(cn, st, paramIndex, paramValue);

				break;
			}

			case Types.SQLXML:
			{
				if (value instanceof SQLXML)
					st.setSQLXML(paramIndex, (SQLXML) value);
				else if (value instanceof String)
				{
					SQLXML sqlxml = cn.createSQLXML();
					sqlxml.setString((String) value);

					st.setSQLXML(paramIndex, sqlxml);
					value = sqlxml;
				}
				else if (value instanceof Reader)
				{
					SQLXML sqlxml = cn.createSQLXML();
					Writer out = sqlxml.setCharacterStream();
					write((Reader) value, out);

					st.setSQLXML(paramIndex, sqlxml);
					value = sqlxml;
				}
				else if (value instanceof InputStream)
				{
					SQLXML sqlxml = cn.createSQLXML();
					OutputStream out = sqlxml.setBinaryStream();
					write((InputStream) value, out);

					st.setSQLXML(paramIndex, sqlxml);
					value = sqlxml;
				}
				else if (value instanceof File)
				{
					SQLXML sqlxml = cn.createSQLXML();
					OutputStream out = sqlxml.setBinaryStream();
					write((File) value, out);

					st.setSQLXML(paramIndex, sqlxml);
					value = sqlxml;
				}
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
	 * 根据数值类型设置{@linkplain SqlParamValue}。
	 * 
	 * @param cn
	 * @param st
	 * @param paramIndex
	 * @param paramValue
	 * @return
	 * @throws SQLException
	 */
	protected Object setParamValueForNumber(Connection cn, PreparedStatement st, int paramIndex, SqlParamValue paramValue)
			throws SQLException
	{
		Object value = paramValue.getValue();
		
		if(value instanceof Byte)
			st.setByte(paramIndex, ((Byte)value).byteValue());
		else if(value instanceof Short)
			st.setShort(paramIndex, ((Short)value).shortValue());
		else if(value instanceof Integer)
			st.setInt(paramIndex, ((Integer)value).intValue());
		else if(value instanceof Long)
			st.setLong(paramIndex, ((Long)value).longValue());
		else if(value instanceof Float)
			st.setFloat(paramIndex, ((Float)value).floatValue());
		else if(value instanceof Double)
			st.setDouble(paramIndex, ((Double)value).doubleValue());
		else if(value instanceof BigInteger)
			st.setBigDecimal(paramIndex, new BigDecimal((BigInteger)value));
		else if(value instanceof BigDecimal)
			st.setBigDecimal(paramIndex, (BigDecimal)value);
		else if(value instanceof AtomicInteger)
			st.setInt(paramIndex, ((AtomicInteger)value).intValue());
		else if(value instanceof AtomicLong)
			st.setLong(paramIndex, ((AtomicLong)value).longValue());
		else
			value = setParamValueExt(cn, st, paramIndex, paramValue);
			
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
		throw new UnsupportedOperationException("Set JDBC type [" + paramValue.getType() + "] value is not supported");
	}

	/**
	 * 字符串转换为JDBC的布尔值。
	 * 
	 * @param v
	 * @return
	 */
	protected boolean toJdbcBoolean(String v)
	{
		return StringUtil.toBoolean(v);
	}

	/**
	 * 数值转换为JDBC的布尔值。
	 * 
	 * @param v
	 * @return
	 */
	protected boolean toJdbcBoolean(Number v)
	{
		return StringUtil.toBoolean(v);
	}

	protected void write(File file, OutputStream out) throws SQLException
	{
		try
		{
			IOUtil.write(file, out);
		}
		catch (IOException e)
		{
			throw new SQLException(e);
		}
		finally
		{
			IOUtil.close(out);
		}
	}

	protected void write(InputStream in, OutputStream out) throws SQLException
	{
		try
		{
			IOUtil.write(in, out);
		}
		catch (IOException e)
		{
			throw new SQLException(e);
		}
		finally
		{
			IOUtil.close(in);
			IOUtil.close(out);
		}
	}

	protected void write(Reader in, Writer out) throws SQLException
	{
		try
		{
			IOUtil.write(in, out);
		}
		catch (IOException e)
		{
			throw new SQLException(e);
		}
		finally
		{
			IOUtil.close(in);
			IOUtil.close(out);
		}
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
	 * 此方法实现参考自JDBC4.0规范“Data Type Conversion Tables”章节中的“TABLE B-6 Type
	 * Conversions Supported by ResultSet getter Methods”表，并且尽量使用其中的推荐方法。
	 * </p>
	 * 
	 * @param cn
	 * @param rs
	 * @param column
	 * @return 当{@linkplain ResultSet#wasNull()}时将返回{@code null}
	 * @throws SQLException
	 */
	@JDBCCompatiblity("某些驱动程序可能不支持ResultSet.getObject()方法，所以这里没有使用")
	public Object getColumnValue(Connection cn, ResultSet rs, String columnName, int sqlType) throws SQLException
	{
		Object value = null;

		switch (sqlType)
		{
			case Types.ARRAY:
			{
				value = rs.getArray(columnName);
				value = checkGotWasNull(cn, rs, columnName, value);
				break;
			}

			case Types.BIGINT:
			{
				value = getColumnValueForBIGINT(cn, rs, columnName);
				break;
			}

			case Types.BINARY:
			{
				value = rs.getBytes(columnName);
				value = checkGotWasNull(cn, rs, columnName, value);
				break;
			}

			case Types.BIT:
			{
				value = rs.getBoolean(columnName);
				value = checkGotWasNull(cn, rs, columnName, value);
				break;
			}

			case Types.BLOB:
			{
				value = rs.getBlob(columnName);
				value = checkGotWasNull(cn, rs, columnName, value);
				break;
			}

			case Types.BOOLEAN:
			{
				value = rs.getBoolean(columnName);
				value = checkGotWasNull(cn, rs, columnName, value);
				break;
			}

			case Types.CHAR:
			{
				value = rs.getString(columnName);
				value = checkGotWasNull(cn, rs, columnName, value);
				break;
			}

			case Types.CLOB:
			{
				value = rs.getClob(columnName);
				value = checkGotWasNull(cn, rs, columnName, value);
				break;
			}

			case Types.DATALINK:
			{
				value = rs.getObject(columnName);
				value = checkGotWasNull(cn, rs, columnName, value);
				break;
			}

			case Types.DATE:
			{
				value = rs.getDate(columnName);
				value = checkGotWasNull(cn, rs, columnName, value);
				break;
			}

			case Types.DECIMAL:
			{
				value = rs.getBigDecimal(columnName);
				value = checkGotWasNull(cn, rs, columnName, value);
				break;
			}

			case Types.DISTINCT:
			{
				value = rs.getObject(columnName);
				value = checkGotWasNull(cn, rs, columnName, value);
				break;
			}

			case Types.DOUBLE:
			{
				value = getColumnValueForDOUBLE(cn, rs, columnName);
				break;
			}

			case Types.FLOAT:
			{
				value = getColumnValueForFLOAT(cn, rs, columnName);
				break;
			}

			case Types.INTEGER:
			{
				value = getColumnValueForINTEGER(cn, rs, columnName);
				break;
			}

			case Types.JAVA_OBJECT:
			{
				value = rs.getObject(columnName);
				value = checkGotWasNull(cn, rs, columnName, value);
				break;
			}

			case Types.LONGNVARCHAR:
			{
				value = rs.getNCharacterStream(columnName);
				value = checkGotWasNull(cn, rs, columnName, value);
				break;
			}

			case Types.LONGVARBINARY:
			{
				value = rs.getBinaryStream(columnName);
				value = checkGotWasNull(cn, rs, columnName, value);
				break;
			}

			case Types.LONGVARCHAR:
			{
				value = rs.getCharacterStream(columnName);
				value = checkGotWasNull(cn, rs, columnName, value);
				break;
			}

			case Types.NCHAR:
			{
				value = rs.getNString(columnName);
				value = checkGotWasNull(cn, rs, columnName, value);
				break;
			}

			case Types.NCLOB:
			{
				value = rs.getNClob(columnName);
				value = checkGotWasNull(cn, rs, columnName, value);
				break;
			}

			case Types.NUMERIC:
			{
				value = rs.getBigDecimal(columnName);
				value = checkGotWasNull(cn, rs, columnName, value);
				break;
			}

			case Types.NVARCHAR:
			{
				value = rs.getNString(columnName);
				value = checkGotWasNull(cn, rs, columnName, value);
				break;
			}

			case Types.OTHER:
			{
				value = rs.getObject(columnName);
				value = checkGotWasNull(cn, rs, columnName, value);
				break;
			}

			case Types.REAL:
			{
				value = getColumnValueForFLOAT(cn, rs, columnName);
				break;
			}

			case Types.REF:
			{
				value = rs.getRef(columnName);
				value = checkGotWasNull(cn, rs, columnName, value);
				break;
			}

			case Types.REF_CURSOR:
			{
				value = rs.getObject(columnName);
				value = checkGotWasNull(cn, rs, columnName, value);
				break;
			}

			case Types.ROWID:
			{
				value = rs.getRowId(columnName);
				value = checkGotWasNull(cn, rs, columnName, value);
				break;
			}

			case Types.SMALLINT:
			{
				value = getColumnValueForSMALLINT(cn, rs, columnName);
				break;
			}

			case Types.SQLXML:
			{
				value = rs.getSQLXML(columnName);
				value = checkGotWasNull(cn, rs, columnName, value);
				break;
			}

			case Types.STRUCT:
			{
				value = rs.getObject(columnName);
				value = checkGotWasNull(cn, rs, columnName, value);
				break;
			}

			case Types.TIME:
			case Types.TIME_WITH_TIMEZONE:
			{
				value = rs.getTime(columnName);
				value = checkGotWasNull(cn, rs, columnName, value);
				break;
			}

			case Types.TIMESTAMP:
			case Types.TIMESTAMP_WITH_TIMEZONE:
			{
				value = rs.getTimestamp(columnName);
				value = checkGotWasNull(cn, rs, columnName, value);
				break;
			}

			case Types.TINYINT:
			{
				value = getColumnValueForTINYINT(cn, rs, columnName);
				break;
			}

			case Types.VARBINARY:
			{
				value = rs.getBytes(columnName);
				value = checkGotWasNull(cn, rs, columnName, value);
				break;
			}

			case Types.VARCHAR:
			{
				value = rs.getString(columnName);
				value = checkGotWasNull(cn, rs, columnName, value);
				break;
			}

			default:
			{
				value = getColumnValueExt(cn, rs, columnName, sqlType);
				break;
			}
		}

		return value;
	}

	/**
	 * 获取列值。
	 * <p>
	 * 对于{@linkplain Types#ARRAY}、{@linkplain Types#BLOB}、{@linkplain Types#CLOB}等复杂类型，
	 * 将获取{@linkplain Object}、{@code byte[]}、{@linkplain String}等实际数据。
	 * </p>
	 * <p>
	 * 此方法实现参考自JDBC4.0规范“Data Type Conversion Tables”章节中的“TABLE B-6 Type
	 * Conversions Supported by ResultSet getter Methods”表，并且尽量使用其中的推荐方法。
	 * </p>
	 * 
	 * @param cn
	 * @param rs
	 * @param column
	 * @return 当{@linkplain ResultSet#wasNull()}时将返回{@code null}
	 * @throws SQLException
	 */
	@JDBCCompatiblity("某些驱动程序可能不支持ResultSet.getObject()方法，所以这里没有使用")
	public Object getColumnValueExtract(Connection cn, ResultSet rs, String columnName, int sqlType) throws SQLException
	{
		Object value = null;

		switch (sqlType)
		{
			case Types.ARRAY:
			{
				value = getColumnValueForARRAY(cn, rs, columnName);
				break;
			}

			case Types.BIGINT:
			{
				value = getColumnValueForBIGINT(cn, rs, columnName);
				break;
			}

			case Types.BINARY:
			{
				value = rs.getBytes(columnName);
				value = checkGotWasNull(cn, rs, columnName, value);
				break;
			}

			case Types.BIT:
			{
				value = rs.getBoolean(columnName);
				value = checkGotWasNull(cn, rs, columnName, value);
				break;
			}

			case Types.BLOB:
			{
				value = getColumnValueForBLOB(cn, rs, columnName);
				break;
			}

			case Types.BOOLEAN:
			{
				value = rs.getBoolean(columnName);
				value = checkGotWasNull(cn, rs, columnName, value);
				break;
			}

			case Types.CHAR:
			{
				value = rs.getString(columnName);
				value = checkGotWasNull(cn, rs, columnName, value);
				break;
			}

			case Types.CLOB:
			{
				value = getColumnValueForCLOB(cn, rs, columnName);

				break;
			}

			case Types.DATALINK:
			{
				value = rs.getObject(columnName);
				value = checkGotWasNull(cn, rs, columnName, value);
				break;
			}

			case Types.DATE:
			{
				value = rs.getDate(columnName);
				value = checkGotWasNull(cn, rs, columnName, value);
				break;
			}

			case Types.DECIMAL:
			{
				value = rs.getBigDecimal(columnName);
				value = checkGotWasNull(cn, rs, columnName, value);
				break;
			}

			case Types.DISTINCT:
			{
				value = rs.getObject(columnName);
				value = checkGotWasNull(cn, rs, columnName, value);
				break;
			}

			case Types.DOUBLE:
			{
				value = getColumnValueForDOUBLE(cn, rs, columnName);
				value = checkGotWasNull(cn, rs, columnName, value);
				break;
			}

			case Types.FLOAT:
			{
				value = getColumnValueForFLOAT(cn, rs, columnName);
				break;
			}

			case Types.INTEGER:
			{
				value = getColumnValueForINTEGER(cn, rs, columnName);
				break;
			}

			case Types.JAVA_OBJECT:
			{
				value = rs.getObject(columnName);
				value = checkGotWasNull(cn, rs, columnName, value);
				break;
			}

			case Types.LONGNVARCHAR:
			{
				value = getColumnValueForLONGNVARCHAR(cn, rs, columnName);
				break;
			}

			case Types.LONGVARBINARY:
			{
				value = getColumnValueForLONGVARBINARY(cn, rs, columnName);
				break;
			}

			case Types.LONGVARCHAR:
			{
				value = getColumnValueForLONGVARCHAR(cn, rs, columnName);
				break;
			}

			case Types.NCHAR:
			{
				value = rs.getNString(columnName);
				value = checkGotWasNull(cn, rs, columnName, value);
				break;
			}

			case Types.NCLOB:
			{
				value = getColumnValueForNCLOB(cn, rs, columnName);
				break;
			}

			case Types.NUMERIC:
			{
				value = rs.getBigDecimal(columnName);
				value = checkGotWasNull(cn, rs, columnName, value);
				break;
			}

			case Types.NVARCHAR:
			{
				value = rs.getNString(columnName);
				value = checkGotWasNull(cn, rs, columnName, value);
				break;
			}

			case Types.OTHER:
			{
				value = rs.getObject(columnName);
				value = checkGotWasNull(cn, rs, columnName, value);
				break;
			}

			case Types.REAL:
			{
				value = getColumnValueForFLOAT(cn, rs, columnName);
				break;
			}

			case Types.REF:
			{
				value = getColumnValueForREF(cn, rs, columnName);
				break;
			}

			case Types.REF_CURSOR:
			{
				value = rs.getObject(columnName);
				value = checkGotWasNull(cn, rs, columnName, value);
				break;
			}

			case Types.ROWID:
			{
				value = getColumnValueForROWID(cn, rs, columnName);
				break;
			}

			case Types.SMALLINT:
			{
				value = getColumnValueForSMALLINT(cn, rs, columnName);
				break;
			}

			case Types.SQLXML:
			{
				value = getColumnValueForSQLXML(cn, rs, columnName);
				break;
			}

			case Types.STRUCT:
			{
				value = rs.getObject(columnName);
				value = checkGotWasNull(cn, rs, columnName, value);
				break;
			}

			case Types.TIME:
			case Types.TIME_WITH_TIMEZONE:
			{
				value = rs.getTime(columnName);
				value = checkGotWasNull(cn, rs, columnName, value);
				break;
			}

			case Types.TIMESTAMP:
			case Types.TIMESTAMP_WITH_TIMEZONE:
			{
				value = rs.getTimestamp(columnName);
				value = checkGotWasNull(cn, rs, columnName, value);
				break;
			}

			case Types.TINYINT:
			{
				value = getColumnValueForTINYINT(cn, rs, columnName);
				break;
			}

			case Types.VARBINARY:
			{
				value = rs.getBytes(columnName);
				value = checkGotWasNull(cn, rs, columnName, value);
				break;
			}

			case Types.VARCHAR:
			{
				value = rs.getString(columnName);
				value = checkGotWasNull(cn, rs, columnName, value);
				break;
			}

			default:
			{
				value = getColumnValueExtractExt(cn, rs, columnName, sqlType);
				break;
			}
		}

		return value;
	}

	/**
	 * 获取{@linkplain Types#ARRAY}的值。
	 * 
	 * @param cn
	 * @param rs
	 * @param columnName
	 * @return
	 * @throws SQLException
	 */
	protected Object getColumnValueForARRAY(Connection cn, ResultSet rs, String columnName) throws SQLException
	{
		Array array = rs.getArray(columnName);
		Object value = (array == null ? null : array.getArray());
		return checkGotWasNull(cn, rs, columnName, value);
	}

	/**
	 * 获取{@linkplain Types#BLOB}的值。
	 * 
	 * @param cn
	 * @param rs
	 * @param columnName
	 * @return
	 * @throws SQLException
	 */
	protected Object getColumnValueForBLOB(Connection cn, ResultSet rs, String columnName) throws SQLException
	{
		byte[] value = null;

		Blob blob = rs.getBlob(columnName);

		if (!rs.wasNull())
		{
			InputStream in = blob.getBinaryStream();
			value = readBytes(in);
		}

		return value;
	}

	/**
	 * 获取{@linkplain Types#CLOB}的值。
	 * 
	 * @param cn
	 * @param rs
	 * @param columnName
	 * @return
	 * @throws SQLException
	 */
	protected Object getColumnValueForCLOB(Connection cn, ResultSet rs, String columnName) throws SQLException
	{
		String value = null;

		Clob clob = rs.getClob(columnName);

		if (!rs.wasNull())
		{
			Reader in = clob.getCharacterStream();
			value = readString(in);
		}

		return value;
	}

	/**
	 * 获取{@linkplain Types#LONGNVARCHAR}的值。
	 * 
	 * @param cn
	 * @param rs
	 * @param columnName
	 * @return
	 * @throws SQLException
	 */
	protected Object getColumnValueForLONGNVARCHAR(Connection cn, ResultSet rs, String columnName) throws SQLException
	{
		String value = null;

		Reader in = rs.getNCharacterStream(columnName);

		if (!rs.wasNull())
			value = readString(in);
		else
			IOUtil.close(in);

		return value;
	}

	/**
	 * 获取{@linkplain Types#LONGVARBINARY}的值。
	 * 
	 * @param cn
	 * @param rs
	 * @param columnName
	 * @return
	 * @throws SQLException
	 */
	protected Object getColumnValueForLONGVARBINARY(Connection cn, ResultSet rs, String columnName) throws SQLException
	{
		byte[] value = null;

		InputStream in = rs.getBinaryStream(columnName);

		if (!rs.wasNull())
			value = readBytes(in);
		else
			IOUtil.close(in);

		return value;
	}

	/**
	 * 获取{@linkplain Types#LONGVARCHAR}的值。
	 * 
	 * @param cn
	 * @param rs
	 * @param columnName
	 * @return
	 * @throws SQLException
	 */
	protected Object getColumnValueForLONGVARCHAR(Connection cn, ResultSet rs, String columnName) throws SQLException
	{
		String value = null;

		Reader in = rs.getCharacterStream(columnName);

		if (!rs.wasNull())
			value = readString(in);
		else
			IOUtil.close(in);

		return value;
	}

	/**
	 * 获取{@linkplain Types#NCLOB}的值。
	 * 
	 * @param cn
	 * @param rs
	 * @param columnName
	 * @return
	 * @throws SQLException
	 */
	protected Object getColumnValueForNCLOB(Connection cn, ResultSet rs, String columnName) throws SQLException
	{
		String value = null;

		NClob clob = rs.getNClob(columnName);

		if (!rs.wasNull())
		{
			Reader in = clob.getCharacterStream();
			value = readString(in);
		}

		return value;
	}

	/**
	 * 获取{@linkplain Types#REF}的值。
	 * 
	 * @param cn
	 * @param rs
	 * @param columnName
	 * @return
	 * @throws SQLException
	 */
	protected Object getColumnValueForREF(Connection cn, ResultSet rs, String columnName) throws SQLException
	{
		Ref ref = rs.getRef(columnName);
		Object value = (ref == null ? null : ref.getObject());
		return checkGotWasNull(cn, rs, columnName, value);
	}

	/**
	 * 获取{@linkplain Types#ROWID}的值。
	 * 
	 * @param cn
	 * @param rs
	 * @param columnName
	 * @return
	 * @throws SQLException
	 */
	protected Object getColumnValueForROWID(Connection cn, ResultSet rs, String columnName) throws SQLException
	{
		RowId rowId = rs.getRowId(columnName);
		byte[] value = (rowId == null ? null : rowId.getBytes());
		return checkGotWasNull(cn, rs, columnName, value);
	}

	/**
	 * 获取{@linkplain Types#SQLXML}的值。
	 * 
	 * @param cn
	 * @param rs
	 * @param columnName
	 * @return
	 * @throws SQLException
	 */
	protected Object getColumnValueForSQLXML(Connection cn, ResultSet rs, String columnName) throws SQLException
	{
		String value = null;

		SQLXML sqlxml = rs.getSQLXML(columnName);

		if (!rs.wasNull())
		{
			Reader in = sqlxml.getCharacterStream();
			value = readString(in);
		}

		return value;
	}

	/**
	 * 获取{@linkplain Types#BIGINT}的值。
	 * 
	 * @param cn
	 * @param rs
	 * @param columnName
	 * @return
	 * @throws SQLException
	 */
	protected Object getColumnValueForBIGINT(Connection cn, ResultSet rs, String columnName)
			throws SQLException
	{
		Object value = null;

		try
		{
			// 优先使用JDBC规范中的推荐方法
			value = rs.getLong(columnName);
			value = checkGotWasNull(cn, rs, columnName, value);
		}
		catch (SQLException e)
		{
			@JDBCCompatiblity("某些数据库允许无符号整数类型，上述ResultSet.getLong()取值可能因为超出long类型值范围而报错，这里升级类型再次尝试")
			BigDecimal bigValue = rs.getBigDecimal(columnName);
			value = (bigValue == null ? null : bigValue.toBigInteger());
			value = checkGotWasNull(cn, rs, columnName, value);
		}

		return value;
	}

	/**
	 * 获取{@linkplain Types#DOUBLE}的值。
	 * 
	 * @param cn
	 * @param rs
	 * @param columnName
	 * @return
	 * @throws SQLException
	 */
	protected Object getColumnValueForDOUBLE(Connection cn, ResultSet rs, String columnName)
			throws SQLException
	{
		Object value = null;

		try
		{
			// 优先使用JDBC规范中的推荐方法
			value = rs.getDouble(columnName);
			value = checkGotWasNull(cn, rs, columnName, value);
		}
		catch (SQLException e)
		{
			@JDBCCompatiblity("数据库中的值可能因为超出double类型值范围而报错，这里升级类型再次尝试")
			BigDecimal bigValue = rs.getBigDecimal(columnName);
			value = bigValue;
			value = checkGotWasNull(cn, rs, columnName, value);
		}

		return value;
	}

	/**
	 * 获取{@linkplain Types#FLOAT}的值。
	 * 
	 * @param cn
	 * @param rs
	 * @param columnName
	 * @return
	 * @throws SQLException
	 */
	protected Object getColumnValueForFLOAT(Connection cn, ResultSet rs, String columnName)
			throws SQLException
	{
		Object value = null;

		try
		{
			// 优先使用JDBC规范中的推荐方法
			value = rs.getFloat(columnName);
			value = checkGotWasNull(cn, rs, columnName, value);
		}
		catch (SQLException e)
		{
			@JDBCCompatiblity("数据库中的值可能因为超出float类型值范围而报错，这里升级类型再次尝试")
			Object bigValue = getColumnValueForDOUBLE(cn, rs, columnName);
			value = bigValue;
		}

		return value;
	}

	/**
	 * 获取{@linkplain Types#INTEGER}的值。
	 * 
	 * @param cn
	 * @param rs
	 * @param columnName
	 * @return
	 * @throws SQLException
	 */
	protected Object getColumnValueForINTEGER(Connection cn, ResultSet rs, String columnName)
			throws SQLException
	{
		Object value = null;

		try
		{
			// 优先使用JDBC规范中的推荐方法
			value = rs.getInt(columnName);
			value = checkGotWasNull(cn, rs, columnName, value);
		}
		catch (SQLException e)
		{
			@JDBCCompatiblity("某些数据库允许无符号整数类型，上述ResultSet.getInt()取值可能因为超出int类型值范围而报错，这里升级类型再次尝试")
			Object bigValue = getColumnValueForBIGINT(cn, rs, columnName);
			value = bigValue;
		}

		return value;
	}

	/**
	 * 获取{@linkplain Types#SMALLINT}的值。
	 * 
	 * @param cn
	 * @param rs
	 * @param columnName
	 * @return
	 * @throws SQLException
	 */
	protected Object getColumnValueForSMALLINT(Connection cn, ResultSet rs, String columnName)
			throws SQLException
	{
		Object value = null;

		try
		{
			// 优先使用JDBC规范中的推荐方法
			value = rs.getShort(columnName);
			value = checkGotWasNull(cn, rs, columnName, value);
		}
		catch (SQLException e)
		{
			@JDBCCompatiblity("某些数据库允许无符号整数类型，上述ResultSet.getShort()取值可能因为超出short类型值范围而报错，这里升级类型再次尝试")
			Object bigValue = getColumnValueForINTEGER(cn, rs, columnName);
			value = bigValue;
		}

		return value;
	}

	/**
	 * 获取{@linkplain Types#TINYINT}的值。
	 * 
	 * @param cn
	 * @param rs
	 * @param columnName
	 * @return
	 * @throws SQLException
	 */
	protected Object getColumnValueForTINYINT(Connection cn, ResultSet rs, String columnName)
			throws SQLException
	{
		Object value = null;

		try
		{
			// 优先使用JDBC规范中的推荐方法
			value = rs.getByte(columnName);
			value = checkGotWasNull(cn, rs, columnName, value);
		}
		catch (SQLException e)
		{
			@JDBCCompatiblity("某些数据库允许无符号整数类型，上述ResultSet.getByte()取值可能因为超出byte类型值范围而报错，这里升级类型再次尝试")
			Object bigValue = getColumnValueForSMALLINT(cn, rs, columnName);
			value = bigValue;
		}

		return value;
	}

	/**
	 * 扩展{@linkplain #getColumnValue(Connection, ResultSet, String, int)}方法获取列值。
	 * <p>
	 * 注意：实现方法应在{@linkplain ResultSet#wasNull()}时返回{@code null}。
	 * </p>
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
		throw new UnsupportedOperationException("Get JDBC [" + sqlType + "] type value unsupported");
	}

	/**
	 * 扩展{@linkplain #getColumnValueExtractExt(Connection, ResultSet, String, int)}方法获取列值。
	 * <p>
	 * 注意：实现方法应在{@linkplain ResultSet#wasNull()}时返回{@code null}。
	 * </p>
	 * 
	 * @param cn
	 * @param rs
	 * @param columnName
	 * @param sqlType
	 * @return
	 * @throws SQLException
	 */
	protected Object getColumnValueExtractExt(Connection cn, ResultSet rs, String columnName, int sqlType)
			throws SQLException
	{
		throw new UnsupportedOperationException("Get JDBC [" + sqlType + "] type value unsupported");
	}

	/**
	 * 校验获取列是否{@code null}。
	 * 
	 * @param cn
	 * @param rs
	 * @param columnName
	 * @param gotValue
	 * @return
	 * @throws SQLException
	 */
	protected Object checkGotWasNull(Connection cn, ResultSet rs, String columnName, Object gotValue)
			throws SQLException
	{
		return (rs.wasNull() ? null : gotValue);
	}

	protected byte[] readBytes(InputStream in) throws SQLException
	{
		try
		{
			return IOUtil.readBytes(in, false);
		}
		catch (IOException e)
		{
			throw new SQLException(e);
		}
		finally
		{
			IOUtil.close(in);
		}
	}

	protected String readString(Reader in) throws SQLException
	{
		try
		{
			return IOUtil.readString(in, false);
		}
		catch (IOException e)
		{
			throw new SQLException(e);
		}
		finally
		{
			IOUtil.close(in);
		}
	}

	/**
	 * 获取{@linkplain SqlParamValue}。
	 * 
	 * @param obj
	 *            允许{@code null}
	 * @return
	 */
	public SqlParamValue toSqlParamValue(Object obj)
	{
		int jdbcType = getJdbcType(obj);
		return new SqlParamValue(obj, jdbcType);
	}

	/**
	 * 将对象列表转换为{@linkplain SqlParamValue}列表。
	 * 
	 * @param objs
	 * @return
	 */
	public List<SqlParamValue> toSqlParamValues(List<?> objs)
	{
		List<SqlParamValue> spvs = new ArrayList<>(objs.size());

		for (Object obj : objs)
		{
			SqlParamValue spv = toSqlParamValue(obj);
			spvs.add(spv);
		}

		return spvs;
	}

	/**
	 * 将对象数组转换为{@linkplain SqlParamValue}列表。
	 * 
	 * @param objs
	 * @return
	 */
	public List<SqlParamValue> toSqlParamValues(Object[] objs)
	{
		List<SqlParamValue> spvs = new ArrayList<>(objs.length);

		for (Object obj : objs)
		{
			SqlParamValue spv = toSqlParamValue(obj);
			spvs.add(spv);
		}

		return spvs;
	}

	/**
	 * 获取指定对象的JDBC类型。
	 * 
	 * @param obj
	 *            允许{@code null}
	 * @return
	 */
	public int getJdbcType(Object obj)
	{
		if (obj == null)
			return Types.NULL;
		else
			return getJdbcType(obj.getClass());
	}

	/**
	 * 获取指定类的JDBC类型。
	 * 
	 * @param clazz
	 * @return
	 */
	public int getJdbcType(Class<?> clazz)
	{
		if (String.class.equals(clazz))
		{
			return Types.VARCHAR;
		}
		else if (boolean.class.equals(clazz) || Boolean.class.equals(clazz))
		{
			return Types.BOOLEAN;
		}
		else if (int.class.equals(clazz) || Integer.class.equals(clazz))
		{
			return Types.INTEGER;
		}
		else if (long.class.equals(clazz) || Long.class.equals(clazz))
		{
			return Types.BIGINT;
		}
		else if (float.class.equals(clazz) || Float.class.equals(clazz))
		{
			return Types.FLOAT;
		}
		else if (double.class.equals(clazz) || Double.class.equals(clazz))
		{
			return Types.DOUBLE;
		}
		else if (byte.class.equals(clazz) || Byte.class.equals(clazz))
		{
			return Types.TINYINT;
		}
		else if (short.class.equals(clazz) || Short.class.equals(clazz))
		{
			return Types.SMALLINT;
		}
		else if (char.class.equals(clazz) || Character.class.equals(clazz))
		{
			return Types.CHAR;
		}
		else if (BigDecimal.class.isAssignableFrom(clazz) || BigInteger.class.isAssignableFrom(clazz))
		{
			return Types.NUMERIC;
		}
		else if (java.sql.Date.class.isAssignableFrom(clazz))
		{
			return Types.DATE;
		}
		else if (java.sql.Time.class.isAssignableFrom(clazz))
		{
			return Types.TIME;
		}
		else if (java.sql.Timestamp.class.isAssignableFrom(clazz))
		{
			return Types.TIMESTAMP;
		}
		else if (java.util.Date.class.isAssignableFrom(clazz))
		{
			return Types.DATE;
		}
		else if (byte[].class.equals(clazz) || Byte[].class.equals(clazz))
		{
			return Types.BINARY;
		}
		else if (java.sql.Clob.class.isAssignableFrom(clazz))
		{
			return Types.CLOB;
		}
		else if (java.sql.Blob.class.isAssignableFrom(clazz))
		{
			return Types.BLOB;
		}
		else if (java.sql.Array.class.isAssignableFrom(clazz))
		{
			return Types.ARRAY;
		}
		else if (java.sql.Struct.class.isAssignableFrom(clazz))
		{
			return Types.STRUCT;
		}
		else if (java.sql.Ref.class.isAssignableFrom(clazz))
		{
			return Types.REF;
		}
		else if (java.net.URL.class.isAssignableFrom(clazz))
		{
			return Types.REF;
		}
		else if (Class.class.equals(clazz))
		{
			return Types.JAVA_OBJECT;
		}
		else
			return getJdbcTypeExt(clazz);
	}

	protected int getJdbcTypeExt(Class<?> clazz)
	{
		throw new UnsupportedOperationException("Get jdbc type for [" + clazz + "] unsupported");
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
			LOGGER.debug("ResultSet [absolute()] is downgraded to [next()] for exception :", e);

			@JDBCCompatiblity("避免驱动程序或者ResultSet不支持absolute而抛出异常")
			int i = 1;
			for (; i < rowIndex; i++)
			{
				if (!rs.next())
					break;
			}
		}
	}

	/**
	 * 获取列名。
	 * 
	 * @param metaData
	 * @param column
	 *            以{@code 1}开头
	 * @return
	 * @throws SQLException
	 */
	public String getColumnName(ResultSetMetaData metaData, int column) throws SQLException
	{
		String columnName = metaData.getColumnLabel(column);
		if (StringUtil.isEmpty(columnName))
			columnName = metaData.getColumnName(column);

		return columnName;
	}

	/**
	 * 获取列名数组。
	 * 
	 * @param metaData
	 * @return
	 * @throws SQLException
	 */
	public String[] getColumnNames(ResultSetMetaData metaData) throws SQLException
	{
		int size = metaData.getColumnCount();

		String[] names = new String[size];

		for (int i = 0; i < names.length; i++)
			names[i] = getColumnName(metaData, i + 1);

		return names;
	}

	/**
	 * 获取列类型。
	 * 
	 * @param metaData
	 * @param column
	 *            以{@code 1}开头
	 * @return
	 * @throws SQLException
	 */
	public SqlType getColumnSqlType(ResultSetMetaData metaData, int column) throws SQLException
	{
		int type = metaData.getColumnType(column);
		String typeName = metaData.getColumnTypeName(column);

		return new SqlType(type, typeName);
	}

	/**
	 * 获取列类型数组。
	 * 
	 * @param metaData
	 * @return
	 * @throws SQLException
	 */
	public SqlType[] getColumnSqlTypes(ResultSetMetaData metaData) throws SQLException
	{
		int size = metaData.getColumnCount();

		SqlType[] sqlTypes = new SqlType[size];

		for (int i = 0; i < sqlTypes.length; i++)
			sqlTypes[i] = getColumnSqlType(metaData, i + 1);

		return sqlTypes;
	}

	/**
	 * SQL插入操作的自动生成结果。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class AutoGeneratedResult
	{
		/** 更新记录数 */
		private int updateCount;

		/** 自动生成结果 */
		private List<Map<String, Object>> generatedResult;

		public AutoGeneratedResult()
		{
			super();
		}

		public AutoGeneratedResult(int updateCount, List<Map<String, Object>> generatedResult)
		{
			super();
			this.updateCount = updateCount;
			this.generatedResult = generatedResult;
		}

		public int getUpdateCount()
		{
			return updateCount;
		}

		public void setUpdateCount(int updateCount)
		{
			this.updateCount = updateCount;
		}

		public boolean hasGeneratedResult()
		{
			return (this.generatedResult != null && !this.generatedResult.isEmpty());
		}

		public List<Map<String, Object>> getGeneratedResult()
		{
			return generatedResult;
		}

		public void setGeneratedResult(List<Map<String, Object>> generatedResult)
		{
			this.generatedResult = generatedResult;
		}
	}
}
