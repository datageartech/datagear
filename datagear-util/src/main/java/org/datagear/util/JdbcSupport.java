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
	 * <p>
	 * 注意：调用者负责调用{@linkplain QueryResultSet#close()}。。
	 * </p>
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
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("execute {}, resultSetType={}", sql, resultSetType);

		Statement st = null;
		ResultSet rs = null;
		List<Object> setParams = Collections.emptyList();

		try
		{
			@JDBCCompatiblity("如果没有参数，则不必采用预编译方式，避免某些驱动对预编译功能支持有问题")
			boolean hasParamValue = sql.hasParamValue();

			if (hasParamValue)
			{
				PreparedStatement pst = createQueryPreparedStatement(cn, sql.getSqlValue(), resultSetType);
				st = pst;
				setParams = setParamValues(cn, pst, sql.getParamValues());
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
		catch (SQLException e)
		{
			if (isRetryNotNeedSQLException(e))
			{
				IOUtil.closeIf(setParams);
				JdbcUtil.closeResultSet(rs);
				JdbcUtil.closeStatement(st);

				@JDBCCompatiblity("这些异常必定不是驱动程序的ResultSet.TYPE_SCROLL_*支持与否问题，不需要再降级处理")
				SQLException e1 = e;
				throw e1;
			}
			else
			{
				@JDBCCompatiblity("某些不支持ResultSet.TYPE_SCROLL_*的驱动程序不是在创建Statemen时报错，"
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
					// 关闭内部新生成的输入流
					closeSetParamsGenerated(sql, setParams);
					JdbcUtil.closeResultSet(rs);
					JdbcUtil.closeStatement(st);

					if (LOGGER.isDebugEnabled())
					{
						LOGGER.debug(
								"Statement.executeQuery() is downgraded to [ResultSet.TYPE_FORWARD_ONLY] for exception",
								e);
					}

					@JDBCCompatiblity("降级为ResultSet.TYPE_FORWARD_ONLY重新执行")
					QueryResultSet qrs = executeQuery(cn, sql, ResultSet.TYPE_FORWARD_ONLY);
					return qrs;
				}
			}
		}
	}

	protected boolean isRetryNotNeedSQLException(SQLException e)
	{
		if ((e instanceof SQLSyntaxErrorException) || (e instanceof SQLDataException)
				|| (e instanceof SQLTimeoutException) || (e instanceof SQLWarning))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	protected void closeSetParamsGenerated(Sql sql, List<Object> setParams)
	{
		List<SqlParamValue> rawParams = sql.getParamValues();

		if (rawParams == null || setParams == null)
			return;

		for (int i = 0, len = setParams.size(), rawLen = rawParams.size(); i < len; i++)
		{
			Object rawParam = (i >= rawLen ? null : rawParams.get(i).getValue());
			Object setParam = setParams.get(i);

			if (setParam != rawParam)
				IOUtil.closeIf(setParam);
		}
	}

	/**
	 * 执行SQL。
	 * <p>
	 * 注意：调用者负责调用{@linkplain SqlExecuteState#close()}。。
	 * </p>
	 * 
	 * @param cn
	 * @param sql
	 * @return
	 * @throws SQLException
	 */
	public SqlExecuteState execute(Connection cn, Sql sql) throws SQLException
	{
		return execute(cn, sql, Statement.NO_GENERATED_KEYS);
	}

	/**
	 * 执行SQL。
	 * <p>
	 * 注意：调用者负责调用{@linkplain SqlExecuteState#close()}。。
	 * </p>
	 * 
	 * @param cn
	 * @param sql
	 * @param autoGeneratedKeys
	 *            自动生成键标识，参考{@linkplain Statement#NO_GENERATED_KEYS}、{@linkplain Statement#RETURN_GENERATED_KEYS}
	 * @return
	 * @throws SQLException
	 */
	public SqlExecuteState execute(Connection cn, Sql sql, int autoGeneratedKeys) throws SQLException
	{
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("execute {}, autoGeneratedKeys={}", sql, autoGeneratedKeys);

		String sqlValue = sql.getSqlValue();
		Statement st = null;
		boolean resultSet;
		List<Object> setParams = Collections.emptyList();

		try
		{
			@JDBCCompatiblity("如果没有参数，则不必采用预编译方式，避免某些驱动对预编译功能支持有问题")
			boolean hasParamValue = sql.hasParamValue();

			if (hasParamValue)
			{
				@JDBCCompatiblity("如果不要自动生成键，则不使用对应的方法，避免某些驱动不支持（比如Hive的JDBC驱动不支持prepareStatement(String, int)）")
				PreparedStatement pst = (Statement.RETURN_GENERATED_KEYS == autoGeneratedKeys
						? cn.prepareStatement(sqlValue, autoGeneratedKeys)
						: cn.prepareStatement(sqlValue));
				st = pst;
				setParams = setParamValues(cn, pst, sql.getParamValues());
				resultSet = pst.execute();
			}
			else
			{
				st = cn.createStatement();

				@JDBCCompatiblity("如果不要自动生成键，则不使用对应的方法，避免某些驱动不支持（比如Hive的JDBC驱动不支持execute(String, int)）")
				boolean myResultSet = (Statement.RETURN_GENERATED_KEYS == autoGeneratedKeys
						? st.execute(sqlValue, autoGeneratedKeys)
						: st.execute(sqlValue));
				resultSet = myResultSet;
			}

			return new SqlExecuteState(st, resultSet, setParams);
		}
		catch (SQLException e)
		{
			// 注意：此方法不应降级执行，因为可能包含不应重复执行的INSERT/UPDATE语句

			IOUtil.closeIf(setParams);
			JdbcUtil.closeStatement(st);
			
			throw e;
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
		if (LOGGER.isDebugEnabled())
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
		if (LOGGER.isDebugEnabled())
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
							Object value = getColumnValue(cn, genKeys, (i + 1), autoGeneratedTypes[i]);
							row.put(name, value);
						}

						generatedResult.add(row);
					}
				}
			}
			catch (Throwable t)
			{
				if (LOGGER.isDebugEnabled())
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
	 * <p>
	 * 注意：返回列表可能包含输入流，调用者应在执行完SQL语句后检测和关闭它们（比如使用{@linkplain IOUtil#closeIf(java.util.Collection)}）。
	 * </p>
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
	 * <p>
	 * 注意：返回列表可能包含输入流，调用者应在执行完SQL语句后检测和关闭它们（比如使用{@linkplain IOUtil#closeIf(java.util.Collection)}）。
	 * </p>
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
	 * <p>
	 * 注意：返回数组可能包含输入流，调用者应在执行完SQL语句后检测和关闭它们（比如使用{@linkplain IOUtil#closeIf(java.util.Collection)}）。
	 * </p>
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
				if (LOGGER.isDebugEnabled())
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

			if (LOGGER.isDebugEnabled())
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
				if (LOGGER.isDebugEnabled())
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
	 * <p>
	 * 注意：返回值可能是输入流，调用者应在执行完SQL语句后检测和关闭它们（比如使用{@linkplain IOUtil#closeIf(java.util.Collection)}）。
	 * </p>
	 * 
	 * @param cn
	 * @param st
	 * @param paramIndex
	 * @param paramValue
	 * @return 实际设置的值（可能是原始值，也可能是经原始值转换后的新值）
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
					boolean v = toJdbcBoolean((String) value);
					st.setBoolean(paramIndex, v);
					value = v;
				}
				else if(value instanceof Number)
				{
					boolean v = toJdbcBoolean((Number) value);
					st.setBoolean(paramIndex, v);
					value = v;
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
					v = new java.sql.Date(((Number) value).longValue());

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
					v = new java.sql.Time(((Number) value).longValue());

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
					v = new java.sql.Timestamp(((Number) value).longValue());

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
					InputStream in = (InputStream) value;
					OutputStream out = null;
					try
					{
						out = clob.setAsciiStream(1);
						writeWrapException(in, out);
					}
					finally
					{
						IOUtil.close(in);
						IOUtil.close(out);
					}

					st.setClob(paramIndex, clob);
					value = clob;
				}
				else if (value instanceof File)
				{
					Clob clob = cn.createClob();
					OutputStream out = null;
					try
					{
						out = clob.setAsciiStream(1);
						writeWrapException((File) value, out);
					}
					finally
					{
						IOUtil.close(out);
					}

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
					InputStream in = (InputStream) value;
					OutputStream out = null;
					try
					{
						out = nClob.setAsciiStream(1);
						writeWrapException(in, out);
					}
					finally
					{
						IOUtil.close(in);
						IOUtil.close(out);
					}

					st.setNClob(paramIndex, nClob);
					value = nClob;
				}
				else if (value instanceof File)
				{
					NClob nClob = cn.createNClob();
					OutputStream out = null;
					try
					{
						out = nClob.setAsciiStream(1);
						writeWrapException((File) value, out);
					}
					finally
					{
						IOUtil.close(out);
					}

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
					Reader in = (Reader) value;
					Writer out = null;
					try
					{
						out = sqlxml.setCharacterStream();
						writeWrapException(in, out);
					}
					finally
					{
						IOUtil.close(in);
						IOUtil.close(out);
					}

					st.setSQLXML(paramIndex, sqlxml);
					value = sqlxml;
				}
				else if (value instanceof InputStream)
				{
					SQLXML sqlxml = cn.createSQLXML();
					InputStream in = (InputStream) value;
					OutputStream out = null;
					try
					{
						out = sqlxml.setBinaryStream();
						writeWrapException(in, out);
					}
					finally
					{
						IOUtil.close(in);
						IOUtil.close(out);
					}

					st.setSQLXML(paramIndex, sqlxml);
					value = sqlxml;
				}
				else if (value instanceof File)
				{
					SQLXML sqlxml = cn.createSQLXML();
					OutputStream out = null;
					try
					{
						out = sqlxml.setBinaryStream();
						writeWrapException((File) value, out);
					}
					finally
					{
						IOUtil.close(out);
					}

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

	protected void writeWrapException(File file, OutputStream out) throws SQLException
	{
		try
		{
			IOUtil.write(file, out);
		}
		catch (IOException e)
		{
			throw new SQLException(e);
		}
	}

	protected void writeWrapException(InputStream in, OutputStream out) throws SQLException
	{
		try
		{
			IOUtil.write(in, out);
		}
		catch (IOException e)
		{
			throw new SQLException(e);
		}
	}

	protected void writeWrapException(Reader in, Writer out) throws SQLException
	{
		try
		{
			IOUtil.write(in, out);
		}
		catch (IOException e)
		{
			throw new SQLException(e);
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
			throw new SQLException("File [" + file.getName() + "] not found");
		}
	}

	/**
	 * 获取原始列值。
	 * <p>
	 * 此方法不会对{@linkplain Types#ARRAY}、{@linkplain Types#BLOB}、{@linkplain Types#CLOB}等复杂类型进行数据提取和转换。
	 * </p>
	 * <p>
	 * 此方法实现参考自JDBC4.0规范“Data Type Conversion Tables”章节中的“TABLE B-6 Type
	 * Conversions Supported by ResultSet getter Methods”表，并且尽量使用其中的推荐方法。
	 * </p>
	 * 
	 * @param cn
	 * @param rs
	 * @param column
	 *            列号，以{@code 1}开头
	 * @return 当{@linkplain ResultSet#wasNull()}时将返回{@code null}
	 * @throws SQLException
	 */
	@JDBCCompatiblity("某些驱动程序可能不支持ResultSet.getObject()方法，所以这里没有使用。"
			+ "另外，这里也没有使用列名作为参数，因为存在某些不支持列名的场景会报错（比如Oracle执行Statement.RETURN_GENERATED_KEYS的INSERT语句时，"
			+ "Statement.getGeneratedKeys()获取的结果集）")
	public Object getColumnValueRaw(Connection cn, ResultSet rs, int column, int sqlType) throws SQLException
	{
		Object value = null;

		switch (sqlType)
		{
			case Types.ARRAY:
			{
				value = rs.getArray(column);
				value = checkGotWasNull(cn, rs, column, value);
				break;
			}

			case Types.BIGINT:
			{
				value = getColumnValueForBIGINT(cn, rs, column);
				break;
			}

			case Types.BINARY:
			{
				value = rs.getBytes(column);
				value = checkGotWasNull(cn, rs, column, value);
				break;
			}

			case Types.BIT:
			{
				value = rs.getBoolean(column);
				value = checkGotWasNull(cn, rs, column, value);
				break;
			}

			case Types.BLOB:
			{
				value = rs.getBlob(column);
				value = checkGotWasNull(cn, rs, column, value);
				break;
			}

			case Types.BOOLEAN:
			{
				value = rs.getBoolean(column);
				value = checkGotWasNull(cn, rs, column, value);
				break;
			}

			case Types.CHAR:
			{
				value = rs.getString(column);
				value = checkGotWasNull(cn, rs, column, value);
				break;
			}

			case Types.CLOB:
			{
				value = rs.getClob(column);
				value = checkGotWasNull(cn, rs, column, value);
				break;
			}

			case Types.DATALINK:
			{
				value = rs.getObject(column);
				value = checkGotWasNull(cn, rs, column, value);
				break;
			}

			case Types.DATE:
			{
				value = rs.getDate(column);
				value = checkGotWasNull(cn, rs, column, value);
				break;
			}

			case Types.DECIMAL:
			{
				value = rs.getBigDecimal(column);
				value = checkGotWasNull(cn, rs, column, value);
				break;
			}

			case Types.DISTINCT:
			{
				value = rs.getObject(column);
				value = checkGotWasNull(cn, rs, column, value);
				break;
			}

			case Types.DOUBLE:
			{
				value = getColumnValueForDOUBLE(cn, rs, column);
				break;
			}

			case Types.FLOAT:
			{
				value = getColumnValueForFLOAT(cn, rs, column);
				break;
			}

			case Types.INTEGER:
			{
				value = getColumnValueForINTEGER(cn, rs, column);
				break;
			}

			case Types.JAVA_OBJECT:
			{
				value = rs.getObject(column);
				value = checkGotWasNull(cn, rs, column, value);
				break;
			}

			case Types.LONGNVARCHAR:
			{
				value = rs.getNCharacterStream(column);
				value = checkGotWasNull(cn, rs, column, value);
				break;
			}

			case Types.LONGVARBINARY:
			{
				value = rs.getBinaryStream(column);
				value = checkGotWasNull(cn, rs, column, value);
				break;
			}

			case Types.LONGVARCHAR:
			{
				value = rs.getCharacterStream(column);
				value = checkGotWasNull(cn, rs, column, value);
				break;
			}

			case Types.NCHAR:
			{
				value = rs.getNString(column);
				value = checkGotWasNull(cn, rs, column, value);
				break;
			}

			case Types.NCLOB:
			{
				value = rs.getNClob(column);
				value = checkGotWasNull(cn, rs, column, value);
				break;
			}

			case Types.NULL:
			{
				value = null;
				break;
			}

			case Types.NUMERIC:
			{
				value = rs.getBigDecimal(column);
				value = checkGotWasNull(cn, rs, column, value);
				break;
			}

			case Types.NVARCHAR:
			{
				value = rs.getNString(column);
				value = checkGotWasNull(cn, rs, column, value);
				break;
			}

			case Types.OTHER:
			{
				value = rs.getObject(column);
				value = checkGotWasNull(cn, rs, column, value);
				break;
			}

			case Types.REAL:
			{
				value = getColumnValueForFLOAT(cn, rs, column);
				break;
			}

			case Types.REF:
			{
				value = rs.getRef(column);
				value = checkGotWasNull(cn, rs, column, value);
				break;
			}

			case Types.REF_CURSOR:
			{
				value = rs.getObject(column);
				value = checkGotWasNull(cn, rs, column, value);
				break;
			}

			case Types.ROWID:
			{
				value = rs.getRowId(column);
				value = checkGotWasNull(cn, rs, column, value);
				break;
			}

			case Types.SMALLINT:
			{
				value = getColumnValueForSMALLINT(cn, rs, column);
				break;
			}

			case Types.SQLXML:
			{
				value = rs.getSQLXML(column);
				value = checkGotWasNull(cn, rs, column, value);
				break;
			}

			case Types.STRUCT:
			{
				value = rs.getObject(column);
				value = checkGotWasNull(cn, rs, column, value);
				break;
			}

			case Types.TIME:
			case Types.TIME_WITH_TIMEZONE:
			{
				value = rs.getTime(column);
				value = checkGotWasNull(cn, rs, column, value);
				break;
			}

			case Types.TIMESTAMP:
			case Types.TIMESTAMP_WITH_TIMEZONE:
			{
				value = rs.getTimestamp(column);
				value = checkGotWasNull(cn, rs, column, value);
				break;
			}

			case Types.TINYINT:
			{
				value = getColumnValueForTINYINT(cn, rs, column);
				break;
			}

			case Types.VARBINARY:
			{
				value = rs.getBytes(column);
				value = checkGotWasNull(cn, rs, column, value);
				break;
			}

			case Types.VARCHAR:
			{
				value = rs.getString(column);
				value = checkGotWasNull(cn, rs, column, value);
				break;
			}

			default:
			{
				value = getColumnValueRawExt(cn, rs, column, sqlType);
				break;
			}
		}

		return value;
	}

	/**
	 * 扩展{@linkplain #getColumnValueRaw(Connection, ResultSet, String, int)}方法获取列值。
	 * <p>
	 * 注意：实现方法应在{@linkplain ResultSet#wasNull()}时返回{@code null}。
	 * </p>
	 * 
	 * @param cn
	 * @param rs
	 * @param column
	 * @param sqlType
	 * @return
	 * @throws SQLException
	 */
	protected Object getColumnValueRawExt(Connection cn, ResultSet rs, int column, int sqlType) throws SQLException
	{
		throw new UnsupportedOperationException("Get value for JDBC [" + sqlType + "] type unsupported");
	}

	/**
	 * 获取列值。
	 * <p>
	 * 此方法会对{@linkplain Types#ARRAY}、{@linkplain Types#BLOB}、{@linkplain Types#CLOB}等复杂类型进行数据提取和转换。
	 * </p>
	 * <p>
	 * 此方法实现参考自JDBC4.0规范“Data Type Conversion Tables”章节中的“TABLE B-6 Type
	 * Conversions Supported by ResultSet getter Methods”表，并且尽量使用其中的推荐方法。
	 * </p>
	 * 
	 * @param cn
	 * @param rs
	 * @param column
	 *            列号，以{@code 1}开头
	 * @return 当{@linkplain ResultSet#wasNull()}时将返回{@code null}
	 * @throws SQLException
	 */
	@JDBCCompatiblity("某些驱动程序可能不支持ResultSet.getObject()方法，所以这里没有使用。"
			+ "另外，这里也没有使用列名作为参数，因为存在某些不支持列名的场景会报错（比如Oracle执行Statement.RETURN_GENERATED_KEYS的INSERT语句时，"
			+ "Statement.getGeneratedKeys()获取的结果集）")
	public Object getColumnValue(Connection cn, ResultSet rs, int column, int sqlType) throws SQLException
	{
		Object value = null;

		switch (sqlType)
		{
			case Types.ARRAY:
			{
				value = getColumnValueForARRAY(cn, rs, column);
				break;
			}

			case Types.BIGINT:
			{
				value = getColumnValueForBIGINT(cn, rs, column);
				break;
			}

			case Types.BINARY:
			{
				value = rs.getBytes(column);
				value = checkGotWasNull(cn, rs, column, value);
				break;
			}

			case Types.BIT:
			{
				value = rs.getBoolean(column);
				value = checkGotWasNull(cn, rs, column, value);
				break;
			}

			case Types.BLOB:
			{
				value = getColumnValueForBLOB(cn, rs, column);
				break;
			}

			case Types.BOOLEAN:
			{
				value = rs.getBoolean(column);
				value = checkGotWasNull(cn, rs, column, value);
				break;
			}

			case Types.CHAR:
			{
				value = rs.getString(column);
				value = checkGotWasNull(cn, rs, column, value);
				break;
			}

			case Types.CLOB:
			{
				value = getColumnValueForCLOB(cn, rs, column);

				break;
			}

			case Types.DATALINK:
			{
				value = rs.getObject(column);
				value = checkGotWasNull(cn, rs, column, value);
				break;
			}

			case Types.DATE:
			{
				value = rs.getDate(column);
				value = checkGotWasNull(cn, rs, column, value);
				break;
			}

			case Types.DECIMAL:
			{
				value = rs.getBigDecimal(column);
				value = checkGotWasNull(cn, rs, column, value);
				break;
			}

			case Types.DISTINCT:
			{
				value = rs.getObject(column);
				value = checkGotWasNull(cn, rs, column, value);
				break;
			}

			case Types.DOUBLE:
			{
				value = getColumnValueForDOUBLE(cn, rs, column);
				value = checkGotWasNull(cn, rs, column, value);
				break;
			}

			case Types.FLOAT:
			{
				value = getColumnValueForFLOAT(cn, rs, column);
				break;
			}

			case Types.INTEGER:
			{
				value = getColumnValueForINTEGER(cn, rs, column);
				break;
			}

			case Types.JAVA_OBJECT:
			{
				value = rs.getObject(column);
				value = checkGotWasNull(cn, rs, column, value);
				break;
			}

			case Types.LONGNVARCHAR:
			{
				value = getColumnValueForLONGNVARCHAR(cn, rs, column);
				break;
			}

			case Types.LONGVARBINARY:
			{
				value = getColumnValueForLONGVARBINARY(cn, rs, column);
				break;
			}

			case Types.LONGVARCHAR:
			{
				value = getColumnValueForLONGVARCHAR(cn, rs, column);
				break;
			}

			case Types.NCHAR:
			{
				value = rs.getNString(column);
				value = checkGotWasNull(cn, rs, column, value);
				break;
			}

			case Types.NCLOB:
			{
				value = getColumnValueForNCLOB(cn, rs, column);
				break;
			}

			case Types.NULL:
			{
				value = null;
				break;
			}

			case Types.NUMERIC:
			{
				value = rs.getBigDecimal(column);
				value = checkGotWasNull(cn, rs, column, value);
				break;
			}

			case Types.NVARCHAR:
			{
				value = rs.getNString(column);
				value = checkGotWasNull(cn, rs, column, value);
				break;
			}

			case Types.OTHER:
			{
				value = rs.getObject(column);
				value = checkGotWasNull(cn, rs, column, value);
				break;
			}

			case Types.REAL:
			{
				value = getColumnValueForFLOAT(cn, rs, column);
				break;
			}

			case Types.REF:
			{
				value = getColumnValueForREF(cn, rs, column);
				break;
			}

			case Types.REF_CURSOR:
			{
				value = rs.getObject(column);
				value = checkGotWasNull(cn, rs, column, value);
				break;
			}

			case Types.ROWID:
			{
				value = getColumnValueForROWID(cn, rs, column);
				break;
			}

			case Types.SMALLINT:
			{
				value = getColumnValueForSMALLINT(cn, rs, column);
				break;
			}

			case Types.SQLXML:
			{
				value = getColumnValueForSQLXML(cn, rs, column);
				break;
			}

			case Types.STRUCT:
			{
				value = rs.getObject(column);
				value = checkGotWasNull(cn, rs, column, value);
				break;
			}

			case Types.TIME:
			case Types.TIME_WITH_TIMEZONE:
			{
				value = rs.getTime(column);
				value = checkGotWasNull(cn, rs, column, value);
				break;
			}

			case Types.TIMESTAMP:
			case Types.TIMESTAMP_WITH_TIMEZONE:
			{
				value = rs.getTimestamp(column);
				value = checkGotWasNull(cn, rs, column, value);
				break;
			}

			case Types.TINYINT:
			{
				value = getColumnValueForTINYINT(cn, rs, column);
				break;
			}

			case Types.VARBINARY:
			{
				value = rs.getBytes(column);
				value = checkGotWasNull(cn, rs, column, value);
				break;
			}

			case Types.VARCHAR:
			{
				value = rs.getString(column);
				value = checkGotWasNull(cn, rs, column, value);
				break;
			}

			default:
			{
				value = getColumnValueExt(cn, rs, column, sqlType);
				break;
			}
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
	 * @param column
	 * @param sqlType
	 * @return
	 * @throws SQLException
	 */
	protected Object getColumnValueExt(Connection cn, ResultSet rs, int column, int sqlType)
			throws SQLException
	{
		throw new UnsupportedOperationException("Get value for JDBC [" + sqlType + "] type unsupported");
	}

	/**
	 * 获取{@linkplain Types#ARRAY}的值。
	 * 
	 * @param cn
	 * @param rs
	 * @param column
	 * @return
	 * @throws SQLException
	 */
	protected Object getColumnValueForARRAY(Connection cn, ResultSet rs, int column) throws SQLException
	{
		Array array = rs.getArray(column);
		array = checkGotWasNull(cn, rs, column, array);
		return (array == null ? null : array.getArray());
	}

	/**
	 * 获取{@linkplain Types#BLOB}的值。
	 * 
	 * @param cn
	 * @param rs
	 * @param column
	 * @return
	 * @throws SQLException
	 */
	protected Object getColumnValueForBLOB(Connection cn, ResultSet rs, int column) throws SQLException
	{
		byte[] value = null;

		Blob blob = rs.getBlob(column);
		blob = checkGotWasNull(cn, rs, column, blob);

		if (blob != null)
		{
			InputStream in = null;
			try
			{
				in = blob.getBinaryStream();
				value = readBytesWrapException(in);
			}
			finally
			{
				IOUtil.close(in);
			}
		}

		return value;
	}

	/**
	 * 获取{@linkplain Types#CLOB}的值。
	 * 
	 * @param cn
	 * @param rs
	 * @param column
	 * @return
	 * @throws SQLException
	 */
	protected Object getColumnValueForCLOB(Connection cn, ResultSet rs, int column) throws SQLException
	{
		String value = null;

		Clob clob = rs.getClob(column);
		clob = checkGotWasNull(cn, rs, column, clob);

		if (clob != null)
		{
			Reader in = null;
			try
			{
				in = clob.getCharacterStream();
				value = readStringWrapException(in);
			}
			finally
			{
				IOUtil.close(in);
			}
		}

		return value;
	}

	/**
	 * 获取{@linkplain Types#LONGNVARCHAR}的值。
	 * 
	 * @param cn
	 * @param rs
	 * @param column
	 * @return
	 * @throws SQLException
	 */
	protected Object getColumnValueForLONGNVARCHAR(Connection cn, ResultSet rs, int column) throws SQLException
	{
		String value = null;

		Reader in = null;
		try
		{
			in = rs.getNCharacterStream(column);
			in = checkGotWasNull(cn, rs, column, in);
			value = (in == null ? null : readStringWrapException(in));
		}
		finally
		{
			IOUtil.close(in);
		}

		return value;
	}

	/**
	 * 获取{@linkplain Types#LONGVARBINARY}的值。
	 * 
	 * @param cn
	 * @param rs
	 * @param column
	 * @return
	 * @throws SQLException
	 */
	protected Object getColumnValueForLONGVARBINARY(Connection cn, ResultSet rs, int column) throws SQLException
	{
		byte[] value = null;

		InputStream in = null;
		try
		{
			in = rs.getBinaryStream(column);
			in = checkGotWasNull(cn, rs, column, in);
			value = (in == null ? null : readBytesWrapException(in));
		}
		finally
		{
			IOUtil.close(in);
		}

		return value;
	}

	/**
	 * 获取{@linkplain Types#LONGVARCHAR}的值。
	 * 
	 * @param cn
	 * @param rs
	 * @param column
	 * @return
	 * @throws SQLException
	 */
	protected Object getColumnValueForLONGVARCHAR(Connection cn, ResultSet rs, int column) throws SQLException
	{
		String value = null;

		Reader in = null;
		try
		{
			in = rs.getCharacterStream(column);
			in = checkGotWasNull(cn, rs, column, in);
			value = (in == null ? null : readStringWrapException(in));
		}
		finally
		{
			IOUtil.close(in);
		}

		return value;
	}

	/**
	 * 获取{@linkplain Types#NCLOB}的值。
	 * 
	 * @param cn
	 * @param rs
	 * @param column
	 * @return
	 * @throws SQLException
	 */
	protected Object getColumnValueForNCLOB(Connection cn, ResultSet rs, int column) throws SQLException
	{
		String value = null;

		NClob clob = rs.getNClob(column);
		clob = checkGotWasNull(cn, rs, column, clob);

		if (clob != null)
		{
			Reader in = null;
			try
			{
				in = clob.getCharacterStream();
				value = readStringWrapException(in);
			}
			finally
			{
				IOUtil.close(in);
			}
		}

		return value;
	}

	/**
	 * 获取{@linkplain Types#REF}的值。
	 * 
	 * @param cn
	 * @param rs
	 * @param column
	 * @return
	 * @throws SQLException
	 */
	protected Object getColumnValueForREF(Connection cn, ResultSet rs, int column) throws SQLException
	{
		Ref ref = rs.getRef(column);
		ref = checkGotWasNull(cn, rs, column, ref);
		return (ref == null ? null : ref.getObject());
	}

	/**
	 * 获取{@linkplain Types#ROWID}的值。
	 * 
	 * @param cn
	 * @param rs
	 * @param column
	 * @return
	 * @throws SQLException
	 */
	protected Object getColumnValueForROWID(Connection cn, ResultSet rs, int column) throws SQLException
	{
		RowId rowId = rs.getRowId(column);
		rowId = checkGotWasNull(cn, rs, column, rowId);
		return (rowId == null ? null : rowId.toString());
	}

	/**
	 * 获取{@linkplain Types#SQLXML}的值。
	 * 
	 * @param cn
	 * @param rs
	 * @param column
	 * @return
	 * @throws SQLException
	 */
	protected Object getColumnValueForSQLXML(Connection cn, ResultSet rs, int column) throws SQLException
	{
		String value = null;

		SQLXML sqlxml = rs.getSQLXML(column);
		sqlxml = checkGotWasNull(cn, rs, column, sqlxml);

		if (sqlxml != null)
		{
			Reader in = null;
			try
			{
				in = sqlxml.getCharacterStream();
				value = readStringWrapException(in);
			}
			finally
			{
				IOUtil.close(in);
			}
		}

		return value;
	}

	/**
	 * 获取{@linkplain Types#BIGINT}的值。
	 * 
	 * @param cn
	 * @param rs
	 * @param column
	 * @return
	 * @throws SQLException
	 */
	protected Number getColumnValueForBIGINT(Connection cn, ResultSet rs, int column)
			throws SQLException
	{
		Number value = null;

		try
		{
			// 优先使用JDBC规范中的推荐方法
			value = rs.getLong(column);
			value = checkGotWasNull(cn, rs, column, value);
		}
		catch (SQLException e)
		{
			@JDBCCompatiblity("某些数据库允许无符号整数类型，上述ResultSet.getLong()取值可能因为超出long类型值范围而报错，这里升级类型再次尝试")
			BigDecimal bigValue = rs.getBigDecimal(column);
			value = (bigValue == null ? null : bigValue.toBigInteger());
			value = checkGotWasNull(cn, rs, column, value);
		}

		return value;
	}

	/**
	 * 获取{@linkplain Types#DOUBLE}的值。
	 * 
	 * @param cn
	 * @param rs
	 * @param column
	 * @return
	 * @throws SQLException
	 */
	protected Number getColumnValueForDOUBLE(Connection cn, ResultSet rs, int column)
			throws SQLException
	{
		Number value = null;

		try
		{
			// 优先使用JDBC规范中的推荐方法
			value = rs.getDouble(column);
			value = checkGotWasNull(cn, rs, column, value);
		}
		catch (SQLException e)
		{
			@JDBCCompatiblity("数据库中的值可能因为超出double类型值范围而报错，这里升级类型再次尝试")
			BigDecimal bigValue = rs.getBigDecimal(column);
			value = bigValue;
			value = checkGotWasNull(cn, rs, column, value);
		}

		return value;
	}

	/**
	 * 获取{@linkplain Types#FLOAT}的值。
	 * 
	 * @param cn
	 * @param rs
	 * @param column
	 * @return
	 * @throws SQLException
	 */
	protected Number getColumnValueForFLOAT(Connection cn, ResultSet rs, int column)
			throws SQLException
	{
		Number value = null;

		try
		{
			// 优先使用JDBC规范中的推荐方法
			value = rs.getFloat(column);
			value = checkGotWasNull(cn, rs, column, value);
		}
		catch (SQLException e)
		{
			@JDBCCompatiblity("数据库中的值可能因为超出float类型值范围而报错，这里升级类型再次尝试")
			Number bigValue = getColumnValueForDOUBLE(cn, rs, column);
			value = bigValue;
		}

		return value;
	}

	/**
	 * 获取{@linkplain Types#INTEGER}的值。
	 * 
	 * @param cn
	 * @param rs
	 * @param column
	 * @return
	 * @throws SQLException
	 */
	protected Number getColumnValueForINTEGER(Connection cn, ResultSet rs, int column)
			throws SQLException
	{
		Number value = null;

		try
		{
			// 优先使用JDBC规范中的推荐方法
			value = rs.getInt(column);
			value = checkGotWasNull(cn, rs, column, value);
		}
		catch (SQLException e)
		{
			@JDBCCompatiblity("某些数据库允许无符号整数类型，上述ResultSet.getInt()取值可能因为超出int类型值范围而报错，这里升级类型再次尝试")
			Number bigValue = getColumnValueForBIGINT(cn, rs, column);
			value = bigValue;
		}

		return value;
	}

	/**
	 * 获取{@linkplain Types#SMALLINT}的值。
	 * 
	 * @param cn
	 * @param rs
	 * @param column
	 * @return
	 * @throws SQLException
	 */
	protected Number getColumnValueForSMALLINT(Connection cn, ResultSet rs, int column)
			throws SQLException
	{
		Number value = null;

		try
		{
			// 优先使用JDBC规范中的推荐方法
			value = rs.getShort(column);
			value = checkGotWasNull(cn, rs, column, value);
		}
		catch (SQLException e)
		{
			@JDBCCompatiblity("某些数据库允许无符号整数类型，上述ResultSet.getShort()取值可能因为超出short类型值范围而报错，这里升级类型再次尝试")
			Number bigValue = getColumnValueForINTEGER(cn, rs, column);
			value = bigValue;
		}

		return value;
	}

	/**
	 * 获取{@linkplain Types#TINYINT}的值。
	 * 
	 * @param cn
	 * @param rs
	 * @param column
	 * @return
	 * @throws SQLException
	 */
	protected Number getColumnValueForTINYINT(Connection cn, ResultSet rs, int column)
			throws SQLException
	{
		Number value = null;

		try
		{
			// 优先使用JDBC规范中的推荐方法
			value = rs.getByte(column);
			value = checkGotWasNull(cn, rs, column, value);
		}
		catch (SQLException e)
		{
			@JDBCCompatiblity("某些数据库允许无符号整数类型，上述ResultSet.getByte()取值可能因为超出byte类型值范围而报错，这里升级类型再次尝试")
			Number bigValue = getColumnValueForSMALLINT(cn, rs, column);
			value = bigValue;
		}

		return value;
	}

	/**
	 * 校验获取列是否{@code null}。
	 * 
	 * @param cn
	 * @param rs
	 * @param column
	 * @param gotValue
	 * @return
	 * @throws SQLException
	 */
	protected <T> T checkGotWasNull(Connection cn, ResultSet rs, int column, T gotValue) throws SQLException
	{
		return (rs.wasNull() ? null : gotValue);
	}

	protected byte[] readBytesWrapException(InputStream in) throws SQLException
	{
		try
		{
			return IOUtil.readBytes(in, false);
		}
		catch (IOException e)
		{
			throw new SQLException(e);
		}
	}

	protected String readStringWrapException(Reader in) throws SQLException
	{
		try
		{
			return IOUtil.readString(in, false);
		}
		catch (IOException e)
		{
			throw new SQLException(e);
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
			if (LOGGER.isDebugEnabled())
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
	 * 获取列标签。
	 * <p>
	 * 此方法优先取{@linkplain ResultSetMetaData#getColumnLabel(int)}，其次取{@linkplain ResultSetMetaData#getColumnName(int)}。
	 * </p>
	 * 
	 * @param metaData
	 * @param column
	 *            以{@code 1}开头
	 * @return
	 * @throws SQLException
	 */
	public String getColumnLabel(ResultSetMetaData metaData, int column) throws SQLException
	{
		String label = metaData.getColumnLabel(column);

		if (StringUtil.isEmpty(label))
			label = metaData.getColumnName(column);

		return label;
	}

	/**
	 * 获取列标签数组。
	 * <p>
	 * 此方法优先取{@linkplain ResultSetMetaData#getColumnLabel(int)}，其次取{@linkplain ResultSetMetaData#getColumnName(int)}。
	 * </p>
	 * 
	 * @param metaData
	 * @return
	 * @throws SQLException
	 */
	public String[] getColumnLabels(ResultSetMetaData metaData) throws SQLException
	{
		int size = metaData.getColumnCount();

		String[] labels = new String[size];

		for (int i = 0; i < labels.length; i++)
			labels[i] = getColumnLabel(metaData, i + 1);

		return labels;
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
