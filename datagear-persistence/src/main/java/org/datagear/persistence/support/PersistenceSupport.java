/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.support;

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
import org.datagear.persistence.Sql;
import org.datagear.persistence.SqlParamValue;
import org.datagear.util.JDBCCompatiblity;
import org.datagear.util.JdbcUtil;

/**
 * 持久操作支持类。
 * 
 * @author datagear@163.com
 *
 */
public abstract class PersistenceSupport
{
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

	public List<Row> executeListQuery(Connection cn, Table table, Sql sql, RowMapper mapper) throws PersistenceException
	{
		return executeListQuery(cn, table, sql, mapper, 1, -1);
	}

	/**
	 * @param cn
	 * @param query
	 * @param mapper
	 * @param startRow 起始行号，以1开头
	 * @param count    读取行数，如果{@code <0}，表示读取全部
	 * @return
	 */
	public List<Row> executeListQuery(Connection cn, Table table, Sql sql, RowMapper mapper, int startRow,
			int count) throws PersistenceException
	{
		if (startRow < 1)
			startRow = 1;

		List<Row> resultList = new ArrayList<Row>();

		QueryResultSet qrs = null;

		try
		{
			qrs = executeQuery(cn, sql);

			ResultSet rs = qrs.getResultSet();

			if (count >= 0 && startRow > 1)
				JdbcUtil.moveToBeforeRow(rs, startRow);

			int endRow = (count >= 0 ? startRow + count : -1);

			int row = startRow;
			while (rs.next())
			{
				if (endRow >= 0 && row >= endRow)
					break;

				Row rowObj = mapper.map(cn, table, rs, row);

				resultList.add(rowObj);

				row++;
			}

			return resultList;
		}
		catch(SQLException e)
		{
			throw new PersistenceException(e);
		}
		finally
		{
			org.datagear.persistence.support.QueryResultSet.close(qrs);
		}
	}

	/**
	 * 查询数目。
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
		catch(SQLException e)
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

		try
		{
			pst = createPreparedStatement(cn, sql);
			setParamValues(cn, pst, sql);
			rs = pst.executeQuery();

			return new QueryResultSet(pst, rs);
		}
		catch(SQLException e)
		{
			JdbcUtil.closeResultSet(rs);
			JdbcUtil.closeStatement(pst);

			throw new PersistenceException(e);
		}
	}

	public int executeUpdate(Connection cn, Sql sql) throws PersistenceException
	{
		PreparedStatement pst = null;

		try
		{
			pst = createPreparedStatement(cn, sql);
			setParamValues(cn, pst, sql);

			return pst.executeUpdate();
		}
		catch(SQLException e)
		{
			JdbcUtil.closeStatement(pst);

			throw new PersistenceException(e);
		}
		finally
		{
			JdbcUtil.closeStatement(pst);
		}
	}

	public void setParamValues(Connection cn, PreparedStatement st, Sql sql) throws SQLException
	{
		List<SqlParamValue> paramValues = sql.getParamValues();

		for (int i = 1; i <= paramValues.size(); i++)
			setParamValue(cn, st, i, paramValues.get(i - 1));
	}

	public void setParamValues(Connection cn, PreparedStatement st, List<SqlParamValue> paramValues) throws SQLException
	{
		for (int i = 1; i <= paramValues.size(); i++)
			setParamValue(cn, st, i, paramValues.get(i - 1));
	}

	public void setParamValues(Connection cn, PreparedStatement st, SqlParamValue... paramValues) throws SQLException
	{
		for (int i = 1; i <= paramValues.length; i++)
			setParamValue(cn, st, i, paramValues[i - 1]);
	}

	public PreparedStatement createPreparedStatement(Connection cn, Sql sql) throws PersistenceException
	{
		PreparedStatement pst = null;

		try
		{
			pst = cn.prepareStatement(sql.getSqlValue());
		}
		catch(SQLException e)
		{
			JdbcUtil.closeStatement(pst);

			throw new PersistenceException(e);
		}

		return pst;
	}

	/**
	 * 设置{@linkplain PreparedStatement}的参数值。
	 * <p>
	 * 此方法实现参考自JDBC4.0规范“Data Type Conversion Tables”章节中的“Java Types Mapper to JDBC
	 * Types”表。
	 * </p>
	 * 
	 * @param cn
	 * @param st
	 * @param paramIndex
	 * @param paramValue
	 * @throws SQLException
	 */
	@JDBCCompatiblity("某些驱动程序不支持PreparedStatement.setObject方法（比如：Hive JDBC），所以这里没有使用")
	public void setParamValue(Connection cn, PreparedStatement st, int paramIndex, SqlParamValue paramValue)
			throws SQLException
	{
		int sqlType = paramValue.getType();
		Object value = paramValue.getValue();

		if (value == null)
		{
			st.setNull(paramIndex, sqlType);
			return;
		}

		switch (sqlType)
		{
			case Types.CHAR:
			case Types.VARCHAR:
			case Types.LONGVARCHAR:
			{
				String v = null;

				if (value instanceof String)
					v = (String) value;
				else
					v = value.toString();

				st.setString(paramIndex, v);

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
				else if (value instanceof String)
					v = new BigDecimal((String) value);

				if (v != null)
					st.setBigDecimal(paramIndex, v);
				else
					setParamValueUnknown(cn, st, paramIndex, paramValue);

				break;
			}

			case Types.BIT:
			case Types.BOOLEAN:
			{
				Boolean v = null;

				if (value instanceof Boolean)
					v = (Boolean) value;
				else if (value instanceof String)
				{
					String str = (String) value;
					v = ("true".equalsIgnoreCase(str) || "1".equals(str) || "on".equalsIgnoreCase(str));
				}
				else if (value instanceof Number)
					v = (((Number) value).intValue() > 0);

				if (v != null)
					st.setBoolean(paramIndex, v);
				else
					setParamValueUnknown(cn, st, paramIndex, paramValue);

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
				else if (value instanceof String)
					v = Integer.parseInt((String) value);

				if (v != null)
					st.setInt(paramIndex, v);
				else
					setParamValueUnknown(cn, st, paramIndex, paramValue);

				break;
			}

			case Types.BIGINT:
			{
				Long v = null;

				if (value instanceof Long)
					v = (Long) value;
				else if (value instanceof Number)
					v = ((Number) value).longValue();
				else if (value instanceof String)
					v = Long.parseLong((String) value);

				if (v != null)
					st.setLong(paramIndex, v);
				else
					setParamValueUnknown(cn, st, paramIndex, paramValue);

				break;
			}

			case Types.REAL:
			{
				Float v = null;

				if (value instanceof Float)
					v = (Float) value;
				else if (value instanceof Number)
					v = ((Number) value).floatValue();
				else if (value instanceof String)
					v = Float.parseFloat((String) value);

				if (v != null)
					st.setFloat(paramIndex, v);
				else
					setParamValueUnknown(cn, st, paramIndex, paramValue);

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
				else if (value instanceof String)
					v = Double.parseDouble((String) value);

				if (v != null)
					st.setDouble(paramIndex, v);
				else
					setParamValueUnknown(cn, st, paramIndex, paramValue);

				break;
			}

			case Types.BINARY:
			case Types.VARBINARY:
			case Types.LONGVARBINARY:
			{
				byte[] v = null;

				if (value instanceof byte[])
					v = (byte[]) value;

				if (v != null)
					st.setBytes(paramIndex, v);
				else
					setParamValueUnknown(cn, st, paramIndex, paramValue);

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
					st.setDate(paramIndex, v);
				else
					setParamValueUnknown(cn, st, paramIndex, paramValue);

				break;
			}

			case Types.TIME:
			{
				java.sql.Time v = null;

				if (value instanceof java.sql.Time)
					v = (java.sql.Time) value;
				else if (value instanceof java.util.Date)
					v = new java.sql.Time(((java.util.Date) value).getTime());
				else if (value instanceof Number)
					v = new java.sql.Time(((Number) value).longValue());

				if (v != null)
					st.setTime(paramIndex, v);
				else
					setParamValueUnknown(cn, st, paramIndex, paramValue);

				break;
			}

			case Types.TIMESTAMP:
			{
				java.sql.Timestamp v = null;

				if (value instanceof java.sql.Timestamp)
					v = (java.sql.Timestamp) value;
				else if (value instanceof java.util.Date)
					v = new java.sql.Timestamp(((java.util.Date) value).getTime());
				else if (value instanceof Number)
					v = new java.sql.Timestamp(((Number) value).longValue());

				if (v != null)
					st.setTimestamp(paramIndex, v);
				else
					setParamValueUnknown(cn, st, paramIndex, paramValue);

				break;
			}

			case Types.CLOB:
			{
				Clob v = null;

				if (value instanceof Clob)
				{
					v = (Clob) value;
				}
				else if (value instanceof String)
				{
					String str = (String) value;

					v = cn.createClob();
					v.setString(1, str);
				}

				if (v != null)
					st.setClob(paramIndex, v);
				else
					setParamValueUnknown(cn, st, paramIndex, paramValue);

				break;
			}

			case Types.BLOB:
			{
				Blob v = null;

				if (value instanceof Blob)
				{
					v = (Blob) value;
				}
				else if (value instanceof byte[])
				{
					byte[] bytes = (byte[]) value;
					v = cn.createBlob();
					v.setBytes(1, bytes);
				}

				if (v != null)
					st.setBlob(paramIndex, v);
				else
					setParamValueUnknown(cn, st, paramIndex, paramValue);

				break;
			}

			case Types.NCHAR:
			case Types.NVARCHAR:
			case Types.LONGNVARCHAR:
			{
				String v = null;

				if (value instanceof String)
					v = (String) value;

				if (v != null)
					st.setNString(paramIndex, v);
				else
					setParamValueUnknown(cn, st, paramIndex, paramValue);

				break;
			}

			case Types.NCLOB:
			{
				NClob v = null;

				if (value instanceof NClob)
				{
					v = (NClob) value;
				}
				else if (value instanceof String)
				{
					String str = (String) value;
					v = cn.createNClob();
					v.setString(1, str);
				}

				if (v != null)
					st.setNClob(paramIndex, v);
				else
					setParamValueUnknown(cn, st, paramIndex, paramValue);

				break;
			}

			case Types.SQLXML:
			{
				SQLXML v = null;

				if (value instanceof SQLXML)
				{
					v = (SQLXML) value;
				}
				else if (value instanceof String)
				{
					String str = (String) value;
					v = cn.createSQLXML();
					v.setString(str);
				}

				if (v != null)
					st.setSQLXML(paramIndex, v);
				else
					setParamValueUnknown(cn, st, paramIndex, paramValue);

				break;
			}

			default:
				setParamValueUnknown(cn, st, paramIndex, paramValue);
		}
	}

	/**
	 * 设置未知类型的{@linkplain SqlParamValue}。
	 * 
	 * @param cn
	 * @param st
	 * @param paramIndex
	 * @param paramValue
	 * @throws SQLException
	 */
	protected void setParamValueUnknown(Connection cn, PreparedStatement st, int paramIndex, SqlParamValue paramValue)
			throws SQLException
	{
		throw new UnsupportedOperationException("Set JDBC [" + paramValue.getType() + "] type value is not supported");
	}

	/**
	 * 获取列值。
	 * <p>
	 * 此方法实现参考自JDBC4.0规范“Data Type Conversion Tables”章节中的“Type Conversions Supported
	 * by ResultSet getter Methods”表，并且使用其中的最佳方法。
	 * </p>
	 * 
	 * @param cn
	 * @param rs
	 * @param column
	 * @return
	 * @throws SQLException
	 */
	public Object getColumnValue(Connection cn, ResultSet rs, Column column)
			throws SQLException
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
				value = getColumnValueUnknown(cn, rs, column, columnName);
				break;
		}

		if (rs.wasNull())
			value = null;

		return value;
	}

	/**
	 * 获取未知类型的列值。
	 * 
	 * @param cn
	 * @param rs
	 * @param column
	 * @param columnName
	 * @return
	 * @throws SQLException
	 */
	protected Object getColumnValueUnknown(Connection cn, ResultSet rs, Column column, String columnName)
			throws SQLException
	{
		throw new UnsupportedOperationException("Get JDBC [" + column.getType() + "] type value is not supported");
	}
}
