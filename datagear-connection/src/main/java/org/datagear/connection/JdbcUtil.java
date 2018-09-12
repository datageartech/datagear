/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.connection;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * JDBC工具支持类。
 * 
 * @author datagear@163.com
 *
 */
public class JdbcUtil
{
	public JdbcUtil()
	{
	}

	/**
	 * 获取指定类型的列值。
	 * 
	 * @param rs
	 * @param row
	 * @param columnIndex
	 * @param targetType
	 * @return
	 * @throws SQLException
	 */
	public static Object getColumnValue(ResultSet rs, long row, int columnIndex, Class<?> targetType)
			throws SQLException
	{
		if (String.class.equals(targetType))
		{
			return rs.getString(columnIndex);
		}
		else if (int.class.equals(targetType))
		{
			return rs.getInt(columnIndex);
		}
		else if (Integer.class.equals(targetType))
		{
			Integer v = rs.getInt(columnIndex);

			return (rs.wasNull() ? null : v);
		}
		else if (boolean.class.equals(targetType))
		{
			return rs.getBoolean(columnIndex);
		}
		else if (Boolean.class.equals(targetType))
		{
			Boolean v = rs.getBoolean(columnIndex);

			return (rs.wasNull() ? null : v);
		}
		else if (long.class.equals(targetType))
		{
			return rs.getLong(columnIndex);
		}
		else if (Long.class.equals(targetType))
		{
			long v = rs.getLong(columnIndex);

			return (rs.wasNull() ? null : v);
		}
		else if (short.class.equals(targetType))
		{
			return rs.getShort(columnIndex);
		}
		else if (Short.class.equals(targetType))
		{
			short v = rs.getShort(columnIndex);

			return (rs.wasNull() ? null : v);
		}
		else if (float.class.equals(targetType))
		{
			return rs.getFloat(columnIndex);
		}
		else if (Float.class.equals(targetType))
		{
			float v = rs.getFloat(columnIndex);

			return (rs.wasNull() ? null : v);
		}
		else if (double.class.equals(targetType))
		{
			return rs.getDouble(columnIndex);
		}
		else if (Double.class.equals(targetType))
		{
			double v = rs.getDouble(columnIndex);

			return (rs.wasNull() ? null : v);
		}
		else if (java.util.Date.class.equals(targetType))
		{
			return rs.getDate(columnIndex);
		}
		else if (BigDecimal.class.equals(targetType))
		{
			return rs.getBigDecimal(columnIndex);
		}
		else if (BigInteger.class.equals(targetType))
		{
			BigDecimal bd = rs.getBigDecimal(columnIndex);

			return (bd == null ? null : bd.toBigInteger());
		}
		else if (byte.class.equals(targetType))
		{
			return rs.getByte(columnIndex);
		}
		else if (Byte.class.equals(targetType))
		{
			byte v = rs.getByte(columnIndex);

			return (rs.wasNull() ? null : v);
		}
		else if (byte[].class.equals(targetType))
		{
			return rs.getBytes(columnIndex);
		}
		else if (char.class.equals(targetType))
		{
			String v = rs.getString(columnIndex);

			return (v == null ? (char) 0 : v.charAt(0));
		}
		else if (Character.class.equals(targetType))
		{
			String v = rs.getString(columnIndex);

			return (v == null ? null : v.charAt(0));
		}
		else if (java.sql.Date.class.equals(targetType))
		{
			return rs.getDate(columnIndex);
		}
		else if (java.sql.Timestamp.class.equals(targetType))
		{
			return rs.getTimestamp(columnIndex);
		}
		else if (java.sql.Time.class.equals(targetType))
		{
			return rs.getTime(columnIndex);
		}
		else if (Enum.class.isAssignableFrom(targetType))
		{
			String enumName = rs.getString(columnIndex);

			if (rs.wasNull() || enumName == null || enumName.isEmpty())
				return null;
			else
			{
				@SuppressWarnings({ "unchecked", "rawtypes" })
				Enum em = Enum.valueOf((Class<Enum>) targetType, enumName);

				return em;
			}
		}
		else if (Object.class.equals(targetType))
		{
			return rs.getObject(columnIndex);
		}
		else
			throw new UnsupportedOperationException("Getting [" + ResultSet.class.getName() + "] column value of type ["
					+ targetType.getName() + "] is not supported");
	}

	/**
	 * 关闭{@linkplain Connection}。
	 * 
	 * @param cn
	 */
	public static void closeConnection(Connection cn)
	{
		if (cn == null)
			return;

		try
		{
			cn.close();
		}
		catch (Exception t)
		{
		}
	}

	/**
	 * 关闭{@linkplain Statement}。
	 * 
	 * @param st
	 */
	public static void closeStatement(Statement st)
	{
		if (st == null)
			return;

		try
		{
			st.close();
		}
		catch (Exception t)
		{
		}
	}

	/**
	 * 关闭{@linkplain ResultSet}。
	 * 
	 * @param rs
	 */
	public static void closeResultSet(ResultSet rs)
	{
		if (rs == null)
			return;

		try
		{
			rs.close();
		}
		catch (Exception t)
		{
		}
	}
}
