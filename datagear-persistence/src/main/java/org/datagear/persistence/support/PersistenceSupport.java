/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.support;

import java.io.Closeable;
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
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.datagear.meta.Column;
import org.datagear.meta.Table;
import org.datagear.persistence.Dialect;
import org.datagear.persistence.PersistenceException;
import org.datagear.persistence.Row;
import org.datagear.persistence.RowMapper;
import org.datagear.persistence.RowMapperException;
import org.datagear.persistence.Sql;
import org.datagear.persistence.SqlParamValue;
import org.datagear.util.IOUtil;
import org.datagear.util.JDBCCompatiblity;
import org.datagear.util.JdbcUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 持久操作支持类。
 * 
 * @author datagear@163.com
 *
 */
public class PersistenceSupport
{
	private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceSupport.class);

	/**
	 * 转换为引号名字。
	 * 
	 * @param dialect
	 * @param name
	 * @return
	 */
	public String quote(Dialect dialect, String name)
	{
		return dialect.quote(name);
	}

	/**
	 * 执行列表结果查询。
	 * 
	 * @param cn
	 * @param table
	 * @param sql
	 * @return
	 * @throws PersistenceException
	 */
	public List<Row> executeListQuery(Connection cn, Table table, Sql sql) throws PersistenceException
	{
		return executeListQuery(cn, table, sql, 1, -1, null);
	}

	/**
	 * 执行列表结果查询。
	 * 
	 * @param cn
	 * @param table
	 * @param sql
	 * @param mapper
	 *            允许为{@code null}
	 * @return
	 * @throws PersistenceException
	 */
	public List<Row> executeListQuery(Connection cn, Table table, Sql sql, RowMapper mapper) throws PersistenceException
	{
		return executeListQuery(cn, table, sql, 1, -1, mapper);
	}

	/**
	 * 执行列表结果查询。
	 * 
	 * @param cn
	 * @param query
	 * @param startRow
	 *            起始行号，以{@code 1}开头
	 * @param count
	 *            读取行数，如果{@code <0}，表示读取全部
	 * @param mapper
	 *            允许为{@code null}
	 * @return
	 */
	public List<Row> executeListQuery(Connection cn, Table table, Sql sql, int startRow, int count, RowMapper mapper)
			throws PersistenceException
	{
		if (startRow < 1)
			startRow = 1;

		List<Row> resultList = new ArrayList<>();

		QueryResultSet qrs = null;

		try
		{
			qrs = executeQuery(cn, sql);

			ResultSet rs = qrs.getResultSet();

			if (count >= 0 && startRow > 1)
				JdbcUtil.moveToBeforeRow(rs, startRow);

			int endRow = (count >= 0 ? startRow + count : -1);

			int rowIndex = startRow;
			while (rs.next())
			{
				if (endRow >= 0 && rowIndex >= endRow)
					break;

				Row row = mapToRow(cn, table, rs, rowIndex, mapper);

				resultList.add(row);

				rowIndex++;
			}

			return resultList;
		}
		catch (SQLException e)
		{
			throw new PersistenceException(e);
		}
		finally
		{
			QueryResultSet.close(qrs);
		}
	}

	/**
	 * 将结果集行映射为{@linkplain Row}对象。
	 * 
	 * @param cn
	 * @param table
	 * @param rs
	 * @param rowIndex
	 *            行号，以{@code 1}开头
	 * @return
	 * @throws RowMapperException
	 */
	public Row mapToRow(Connection cn, Table table, ResultSet rs, int rowIndex) throws RowMapperException
	{
		return mapToRow(cn, table, rs, rowIndex, null);
	}

	/**
	 * 将结果集行映射为{@linkplain Row}对象。
	 * 
	 * @param cn
	 * @param table
	 * @param rs
	 * @param rowIndex
	 *            行号，以{@code 1}开头
	 * @param mapper
	 *            允许为{@code null}
	 * @return
	 * @throws RowMapperException
	 */
	public Row mapToRow(Connection cn, Table table, ResultSet rs, int rowIndex, RowMapper mapper)
			throws RowMapperException
	{
		if (mapper != null)
			return mapper.map(cn, table, rs, rowIndex);
		else
		{
			Row row = new Row();

			try
			{
				Column[] columns = table.getColumns();
				for (int i = 0; i < columns.length; i++)
				{
					Object value = getColumnValue(cn, rs, columns[i]);
					row.put(columns[i].getName(), value);
				}
			}
			catch (SQLException e)
			{
				throw new RowMapperException(e);
			}

			return row;
		}
	}

	/**
	 * 执行数目查询。
	 * 
	 * @param cn
	 * @param query
	 * @return
	 */
	public long executeCountQuery(Connection cn, Sql query)
	{
		QueryResultSet qrs = null;

		try
		{
			long count = 0;

			qrs = executeQuery(cn, query);

			ResultSet rs = qrs.getResultSet();

			if (rs.next())
				count = rs.getInt(1);

			return count;
		}
		catch (SQLException e)
		{
			throw new PersistenceException(e);
		}
		finally
		{
			QueryResultSet.close(qrs);
		}
	}

	public QueryResultSet executeQuery(Connection cn, Sql sql) throws PersistenceException
	{
		PreparedStatement pst = null;
		ResultSet rs = null;
		List<Object> setParams = null;
		try
		{
			pst = createPreparedStatement(cn, sql);
			setParams = setParamValues(cn, pst, sql);
			rs = pst.executeQuery();

			return new QueryResultSet(pst, rs, setParams);
		}
		catch (SQLException e)
		{
			closeIfClosable(setParams);
			JdbcUtil.closeResultSet(rs);
			JdbcUtil.closeStatement(pst);

			throw new PersistenceException(e);
		}
	}

	public int executeUpdate(Connection cn, Sql sql) throws PersistenceException
	{
		PreparedStatement pst = null;
		List<Object> setParams = null;

		try
		{
			pst = createPreparedStatement(cn, sql);
			setParams = setParamValues(cn, pst, sql);

			return pst.executeUpdate();
		}
		catch (SQLException e)
		{
			throw new PersistenceException(e);
		}
		finally
		{
			closeIfClosable(setParams);
			JdbcUtil.closeStatement(pst);
		}
	}

	public List<Object> setParamValues(Connection cn, PreparedStatement st, Sql sql) throws SQLException
	{
		return setParamValues(cn, st, sql.getParamValues());
	}

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

	public Object[] setParamValues(Connection cn, PreparedStatement st, SqlParamValue... paramValues)
			throws SQLException
	{
		Object[] setValues = new Object[paramValues.length];

		for (int i = 1; i <= paramValues.length; i++)
			setValues[i - 1] = setParamValue(cn, st, i, paramValues[i - 1]);

		return setValues;
	}

	public PreparedStatement createPreparedStatement(Connection cn, Sql sql) throws PersistenceException
	{
		PreparedStatement pst = null;

		try
		{
			pst = cn.prepareStatement(sql.getSqlValue());
		}
		catch (SQLException e)
		{
			JdbcUtil.closeStatement(pst);

			throw new PersistenceException(e);
		}

		return pst;
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
	public Object getColumnValue(Connection cn, ResultSet rs, Column column) throws SQLException
	{
		String columnName = column.getName();
		int sqlType = column.getType();

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
				value = getColumnValueExt(cn, rs, column, columnName);
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
	 * @param column
	 * @param columnName
	 * @return
	 * @throws SQLException
	 */
	protected Object getColumnValueExt(Connection cn, ResultSet rs, Column column, String columnName)
			throws SQLException
	{
		throw new UnsupportedOperationException("Get JDBC [" + column.getType() + "] type value is not supported");
	}

	/**
	 * 静默回滚。
	 * 
	 * @param cn
	 */
	public void rollbackSilently(Connection cn)
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
	public void commitSilently(Connection cn)
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

	public void closeIfClosable(List<?> list)
	{
		if (list == null)
			return;

		for (Object o : list)
			closeIfClosable(o);
	}

	public void closeIfClosable(Object... objs)
	{
		for (Object o : objs)
			closeIfClosable(o);
	}

	public boolean closeIfClosable(Object o)
	{
		if (o instanceof Closeable)
		{
			IOUtil.close((Closeable) o);
			return true;
		}

		return false;
	}
}
