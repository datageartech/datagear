/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

/**
 * JDBC工具支持类。
 * 
 * @author datagear@163.com
 *
 */
public class JdbcUtil
{
	private JdbcUtil()
	{
		throw new UnsupportedOperationException();
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
	 * 获取指定对象的JDBC类型。
	 * 
	 * @param obj
	 * @return
	 */
	public static int getJdbcType(Object obj)
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
	public static int getJdbcType(Class<?> clazz)
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
		else if (java.math.BigDecimal.class.isAssignableFrom(clazz))
		{
			return Types.NUMERIC;
		}
		else if (java.math.BigInteger.class.isAssignableFrom(clazz))
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
			throw new UnsupportedOperationException("Unsupported Java type [" + clazz + "] for getting JDBC type");
	}

	/**
	 * 关闭{@linkplain Connection}。
	 * <p>
	 * 此方法不会抛出任何{@linkplain Throwable}。
	 * </p>
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
		catch (Throwable t)
		{
		}
	}

	/**
	 * 关闭{@linkplain Statement}。
	 * <p>
	 * 此方法不会抛出任何{@linkplain Throwable}。
	 * </p>
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
		catch (Throwable t)
		{
		}
	}

	/**
	 * 关闭{@linkplain ResultSet}。
	 * <p>
	 * 此方法不会抛出任何{@linkplain Throwable}。
	 * </p>
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
		catch (Throwable t)
		{
		}
	}

	/**
	 * 提交。
	 * 
	 * @param cn
	 * @return {@code true} 提交成功；{@code false} 不支持
	 * @throws SQLException
	 */
	public static boolean commitIfSupports(Connection cn) throws SQLException
	{
		DatabaseMetaData metaData = cn.getMetaData();

		if (!metaData.supportsTransactions())
			return false;

		cn.commit();

		return true;
	}

	/**
	 * 回滚。
	 * 
	 * @param cn
	 * @return {@code true} 回滚成功；{@code false} 不支持
	 * @throws SQLException
	 */
	public static boolean rollbackIfSupports(Connection cn) throws SQLException
	{
		DatabaseMetaData metaData = cn.getMetaData();

		if (!metaData.supportsTransactions())
			return false;

		cn.rollback();

		return true;
	}

	/**
	 * 设置为只读。
	 * 
	 * @param cn
	 * @param readonly
	 * @return {@code true} 设置成功；{@code false} 不支持
	 */
	public static boolean setReadonlyIfSupports(Connection cn, boolean readonly)
	{
		try
		{
			cn.setReadOnly(readonly);

			return true;
		}
		// 某些驱动程序可能不支持此方法而抛出异常，比如Hive jdbc
		catch (SQLException e)
		{
			return false;
		}
	}

	/**
	 * 设置是否自动提交。
	 * 
	 * @param cn
	 * @param autoCommit
	 * @return {@code true} 设置成功；{@code false} 不支持
	 */
	public static boolean setAutoCommitIfSupports(Connection cn, boolean autoCommit)
	{
		try
		{
			cn.setAutoCommit(autoCommit);

			return true;
		}
		// 避免有驱动程序不支持此方法而抛出异常
		catch (SQLException e)
		{
			return false;
		}
	}

	/**
	 * 获取连接URL。
	 * 
	 * @param cn
	 * @return 连接URL；{@code null} 不支持时
	 * @throws SQLException
	 */
	public static String getURLIfSupports(Connection cn) throws SQLException
	{
		DatabaseMetaData metaData = cn.getMetaData();

		try
		{
			return metaData.getURL();
		}
		// 避免有驱动程序不支持此方法而抛出异常
		catch (Throwable t)
		{
			return null;
		}
	}
}
