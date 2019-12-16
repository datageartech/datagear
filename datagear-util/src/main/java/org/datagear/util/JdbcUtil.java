/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;

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
	 * 设置{@linkplain PreparedStatement}的参数值，并在必要时进行数据类型转换。
	 * <p>
	 * 此方法实现参考自JDBC4.0规范“Data Type Conversion Tables”章节中的“Java Types Mapper to
	 * JDBC Types”表。
	 * </p>
	 * 
	 * @param cn
	 * @param st
	 * @param paramIndex
	 * @param sqlType
	 * @param paramValue
	 * @throws SQLException
	 * @throws UnsupportedOperationException
	 */
	@JDBCCompatiblity("某些驱动程序不支持PreparedStatement.setObject方法（比如：Hive JDBC），所以这里没有使用")
	public static void setParamValue(Connection cn, PreparedStatement st, int paramIndex, int sqlType,
			Object paramValue) throws SQLException, UnsupportedOperationException
	{
		if (paramValue == null)
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
				String value = null;

				if (paramValue instanceof String)
					value = (String) paramValue;
				else
					value = paramValue.toString();

				st.setString(paramIndex, value);

				break;
			}

			case Types.NUMERIC:
			case Types.DECIMAL:
			{
				BigDecimal value = null;

				if (paramValue instanceof BigDecimal)
					value = (BigDecimal) paramValue;
				else if (paramValue instanceof BigInteger)
					value = new BigDecimal((BigInteger) paramValue);
				else if (paramValue instanceof Number)
					value = new BigDecimal(((Number) paramValue).doubleValue());
				else if (paramValue instanceof String)
					value = new BigDecimal((String) paramValue);
				else
					throw new UnsupportedOperationException("Set JDBC [" + sqlType + "] type value for type ["
							+ paramValue.getClass().getName() + "] is not supported");

				st.setBigDecimal(paramIndex, value);

				break;
			}

			case Types.BIT:
			case Types.BOOLEAN:
			{
				boolean value = false;

				if (paramValue instanceof Boolean)
					value = (Boolean) paramValue;
				else if (paramValue instanceof String)
				{
					String str = (String) paramValue;
					value = ("true".equalsIgnoreCase(str) || "1".equals(str) || "on".equalsIgnoreCase(str));
				}
				else if (paramValue instanceof Number)
					value = (((Number) paramValue).intValue() > 0);
				else
					throw new UnsupportedOperationException("Set JDBC [" + sqlType + "] type value for type ["
							+ paramValue.getClass().getName() + "] is not supported");

				st.setBoolean(paramIndex, value);

				break;
			}

			case Types.TINYINT:
			case Types.SMALLINT:
			case Types.INTEGER:
			{
				Integer value = null;

				if (paramValue instanceof Integer)
					value = (Integer) paramValue;
				else if (paramValue instanceof Number)
					value = ((Number) paramValue).intValue();
				else if (paramValue instanceof String)
					value = Integer.parseInt((String) paramValue);
				else
					throw new UnsupportedOperationException("Set JDBC [" + sqlType + "] type value for type ["
							+ paramValue.getClass().getName() + "] is not supported");

				st.setInt(paramIndex, value);

				break;
			}

			case Types.BIGINT:
			{
				Long value = null;

				if (paramValue instanceof Long)
					value = (Long) paramValue;
				else if (paramValue instanceof Number)
					value = ((Number) paramValue).longValue();
				else if (paramValue instanceof String)
					value = Long.parseLong((String) paramValue);
				else
					throw new UnsupportedOperationException("Set JDBC [" + sqlType + "] type value for type ["
							+ paramValue.getClass().getName() + "] is not supported");

				st.setLong(paramIndex, value);

				break;
			}

			case Types.REAL:
			{
				Float value = null;

				if (paramValue instanceof Float)
					value = (Float) paramValue;
				else if (paramValue instanceof Number)
					value = ((Number) paramValue).floatValue();
				else if (paramValue instanceof String)
					value = Float.parseFloat((String) paramValue);
				else
					throw new UnsupportedOperationException("Set JDBC [" + sqlType + "] type value for type ["
							+ paramValue.getClass().getName() + "] is not supported");

				st.setFloat(paramIndex, value);

				break;
			}

			case Types.FLOAT:
			case Types.DOUBLE:
			{
				Double value = null;

				if (paramValue instanceof Double)
					value = (Double) paramValue;
				else if (paramValue instanceof Number)
					value = ((Number) paramValue).doubleValue();
				else if (paramValue instanceof String)
					value = Double.parseDouble((String) paramValue);
				else
					throw new UnsupportedOperationException("Set JDBC [" + sqlType + "] type value for type ["
							+ paramValue.getClass().getName() + "] is not supported");

				st.setDouble(paramIndex, value);

				break;
			}

			case Types.BINARY:
			case Types.VARBINARY:
			case Types.LONGVARBINARY:
			{
				byte[] value = null;

				if (paramValue instanceof byte[])
					value = (byte[]) paramValue;
				else
					throw new UnsupportedOperationException("Set JDBC [" + sqlType + "] type value for type ["
							+ paramValue.getClass().getName() + "] is not supported");

				st.setBytes(paramIndex, value);

				break;
			}

			case Types.DATE:
			{
				java.sql.Date value = null;

				if (paramValue instanceof java.sql.Date)
					value = (java.sql.Date) paramValue;
				else if (paramValue instanceof java.util.Date)
					value = new java.sql.Date(((java.util.Date) paramValue).getTime());
				else if (paramValue instanceof Number)
					value = new java.sql.Date(((Number) paramValue).longValue());
				else
					throw new UnsupportedOperationException("Set JDBC [" + sqlType + "] type value for type ["
							+ paramValue.getClass().getName() + "] is not supported");

				st.setDate(paramIndex, value);

				break;
			}

			case Types.TIME:
			{
				java.sql.Time value = null;

				if (paramValue instanceof java.sql.Time)
					value = (java.sql.Time) paramValue;
				else if (paramValue instanceof java.util.Date)
					value = new java.sql.Time(((java.util.Date) paramValue).getTime());
				else if (paramValue instanceof Number)
					value = new java.sql.Time(((Number) paramValue).longValue());
				else
					throw new UnsupportedOperationException("Set JDBC [" + sqlType + "] type value for type ["
							+ paramValue.getClass().getName() + "] is not supported");

				st.setTime(paramIndex, value);

				break;
			}

			case Types.TIMESTAMP:
			{
				java.sql.Timestamp value = null;

				if (paramValue instanceof java.sql.Timestamp)
					value = (java.sql.Timestamp) paramValue;
				else if (paramValue instanceof java.util.Date)
					value = new java.sql.Timestamp(((java.util.Date) paramValue).getTime());
				else if (paramValue instanceof Number)
					value = new java.sql.Timestamp(((Number) paramValue).longValue());
				else
					throw new UnsupportedOperationException("Set JDBC [" + sqlType + "] type value for type ["
							+ paramValue.getClass().getName() + "] is not supported");

				st.setTimestamp(paramIndex, value);

				break;
			}

			case Types.CLOB:
			{
				Clob clob = null;

				if (paramValue instanceof Clob)
				{
					clob = (Clob) paramValue;
				}
				else if (paramValue instanceof String)
				{
					String str = (String) paramValue;

					clob = cn.createClob();
					clob.setString(1, str);
				}
				else
					throw new UnsupportedOperationException("Set JDBC [" + sqlType + "] type value for type ["
							+ paramValue.getClass().getName() + "] is not supported");

				st.setClob(paramIndex, clob);

				break;
			}

			case Types.BLOB:
			{
				Blob blob = null;

				if (paramValue instanceof Blob)
				{
					blob = (Blob) paramValue;
				}
				else if (paramValue instanceof byte[])
				{
					byte[] bytes = (byte[]) paramValue;
					blob = cn.createBlob();
					blob.setBytes(1, bytes);
				}
				else
					throw new UnsupportedOperationException("Set JDBC [" + sqlType + "] type value for type ["
							+ paramValue.getClass().getName() + "] is not supported");

				st.setBlob(paramIndex, blob);

				break;
			}

			case Types.NCHAR:
			case Types.NVARCHAR:
			case Types.LONGNVARCHAR:
			{
				String value = null;

				if (paramValue instanceof String)
					value = (String) paramValue;
				else
					throw new UnsupportedOperationException("Set JDBC [" + sqlType + "] type value for type ["
							+ paramValue.getClass().getName() + "] is not supported");

				st.setNString(paramIndex, value);
				break;
			}

			case Types.NCLOB:
			{
				NClob nclob = null;

				if (paramValue instanceof NClob)
				{
					nclob = (NClob) paramValue;
				}
				else if (paramValue instanceof String)
				{
					String str = (String) paramValue;
					nclob = cn.createNClob();
					nclob.setString(1, str);
				}
				else
					throw new UnsupportedOperationException("Set JDBC [" + sqlType + "] type value for type ["
							+ paramValue.getClass().getName() + "] is not supported");

				st.setNClob(paramIndex, nclob);
				break;
			}

			case Types.SQLXML:
			{
				SQLXML sqlxml = null;

				if (paramValue instanceof SQLXML)
				{
					sqlxml = (SQLXML) paramValue;
				}
				if (paramValue instanceof String)
				{
					String str = (String) paramValue;
					sqlxml = cn.createSQLXML();
					sqlxml.setString(str);
				}
				else
					throw new UnsupportedOperationException("Set JDBC [" + sqlType + "] type value for type ["
							+ paramValue.getClass().getName() + "] is not supported");

				st.setSQLXML(paramIndex, sqlxml);
				break;
			}

			default:

				throw new UnsupportedOperationException("Set JDBC [" + sqlType + "] type value is not supported");
		}
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
			throw new UnsupportedOperationException("Getting [" + ResultSet.class.getName()
					+ "] column value for type [" + targetType.getName() + "] is not supported");
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
	 * 将字符串转换为指定SQL类型的数值。
	 * <p>
	 * 如果{@code str}不合法、或者{@code sqlType}不是数值类型，将返回{@code null}。
	 * </p>
	 * 
	 * @param sqlType
	 * @return
	 */
	public static Number parseToNumber(String str, int sqlType)
	{
		BigDecimal number = null;

		switch (sqlType)
		{
			case Types.NUMERIC:
			case Types.DECIMAL:
			{
				return toBigDecimal(str);
			}
			case Types.TINYINT:
			case Types.SMALLINT:
			case Types.INTEGER:
			{
				number = toBigDecimal(str);
				return (number == null ? null : number.intValue());
			}
			case Types.BIGINT:
			{
				number = toBigDecimal(str);
				return (number == null ? null : number.longValue());
			}
			case Types.REAL:
			case Types.FLOAT:
			{
				number = toBigDecimal(str);
				return (number == null ? null : number.floatValue());
			}
			case Types.DOUBLE:
			{
				number = toBigDecimal(str);
				return (number == null ? null : number.doubleValue());
			}
			default:
				return null;
		}
	}

	/**
	 * 转换为{@code BigDecimal}。
	 * <p>
	 * 如果不合法，将返回{@code null}。
	 * </p>
	 * 
	 * @param str
	 * @return
	 */
	protected static BigDecimal toBigDecimal(String str)
	{
		try
		{
			return new BigDecimal(str);
		}
		catch (Throwable t)
		{
			return null;
		}
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
	 * 是否只读。
	 * 
	 * @param cn
	 * @return
	 */
	public static boolean isReadonly(Connection cn) throws SQLException
	{
		return cn.isReadOnly();
	}

	/**
	 * 设置为只读。
	 * 
	 * @param cn
	 * @param readonly
	 * @return {@code true} 设置成功；{@code false} 不支持
	 */
	@JDBCCompatiblity("某些驱动程序可能不支持此方法而抛出异常，比如Hive jdbc")
	public static boolean setReadonlyIfSupports(Connection cn, boolean readonly)
	{
		try
		{
			cn.setReadOnly(readonly);

			return true;
		}
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
	@JDBCCompatiblity("避免有驱动程序不支持此方法而抛出异常")
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
	 */
	@JDBCCompatiblity("避免有驱动程序不支持此方法而抛出异常")
	public static String getURLIfSupports(Connection cn)
	{
		try
		{
			DatabaseMetaData metaData = cn.getMetaData();
			return metaData.getURL();
		}
		catch (Throwable t)
		{
			return null;
		}
	}

	/**
	 * 获取连接用户名。
	 * 
	 * @param cn
	 * @return 连接用户名；{@code null} 不支持时
	 */
	@JDBCCompatiblity("避免有驱动程序不支持此方法而抛出异常")
	public static String getUserNameIfSupports(Connection cn)
	{
		try
		{
			DatabaseMetaData metaData = cn.getMetaData();
			return metaData.getUserName();
		}
		catch (Throwable t)
		{
			return null;
		}
	}

	/**
	 * 将一个未移动过游标的{@linkplain ResultSet}游标移动至指定行之前。
	 * 
	 * @param rs
	 * @param row
	 * @throws SQLException
	 */
	@JDBCCompatiblity("避免有驱动程序不支持absolute而抛出异常")
	public static void moveToBeforeRow(ResultSet rs, int row) throws SQLException
	{
		// 第一行不做任何操作，避免不必要的调用可能导致底层不支持而报错
		if (row == 1)
			return;

		try
		{
			rs.absolute(row - 1);
		}
		catch (SQLException e)
		{
			for (int i = 1; i < row; i++)
			{
				if (!rs.next())
					break;
			}
		}
	}

	/**
	 * 创建用于执行查询语句的{@linkplain Statement}。
	 * 
	 * @param cn
	 * @return
	 * @throws SQLException
	 */
	@JDBCCompatiblity("避免有驱动程序不支持TYPE_SCROLL_INSENSITIVE而抛出异常")
	public static Statement createQueryStatement(Connection cn) throws SQLException
	{
		try
		{
			return cn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		}
		catch (SQLFeatureNotSupportedException e)
		{
			return cn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		}
	}

	/**
	 * 创建用于执行查询语句的{@linkplain PreparedStatement}。
	 * 
	 * @param cn
	 * @param sql
	 * @return
	 * @throws SQLException
	 */
	@JDBCCompatiblity("避免有驱动程序不支持TYPE_SCROLL_INSENSITIVE而抛出异常")
	public static PreparedStatement createQueryPreparedStatement(Connection cn, String sql) throws SQLException
	{
		try
		{
			return cn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		}
		catch (SQLFeatureNotSupportedException e)
		{
			// 驱动程序不支持TYPE_SCROLL_INSENSITIVE时
			return cn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		}
	}

	/**
	 * 执行查询。
	 * 
	 * @param cn
	 * @param sql
	 * @return
	 * @throws SQLException
	 */
	public static QueryResultSet executeQuery(Connection cn, String sql) throws SQLException
	{
		return executeQuery(cn, sql, null);
	}

	/**
	 * 执行查询。
	 * 
	 * @param cn
	 * @param sql
	 * @param fetchSize
	 * @return
	 * @throws SQLException
	 */
	public static QueryResultSet executeQuery(Connection cn, String sql, Integer fetchSize) throws SQLException
	{
		Statement st = createQueryStatement(cn);
		ResultSet rs = null;

		try
		{
			rs = st.executeQuery(sql);
			return new QueryResultSet(st, rs);
		}
		catch (SQLException e)
		{
			if (ResultSet.TYPE_FORWARD_ONLY == st.getResultSetType())
				throw e;
			else
			{
				JdbcUtil.closeResultSet(rs);
				JdbcUtil.closeStatement(st);
			}
		}

		@JDBCCompatiblity("可能是查询SQL语句不支持ResultSet.TYPE_SCROLL_*（比如SQLServer的聚集列存储索引），"
				+ "因此，这里再降级为ResultSet.TYPE_FORWARD_ONLY再执行查询")
		// TODO 如果是SQL语法错误，则不应再降级执行
		QueryResultSet queryResultSet = null;

		st = cn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

		if (fetchSize != null && fetchSize >= 0)
			st.setFetchSize(fetchSize);

		rs = st.executeQuery(sql);

		queryResultSet = new QueryResultSet(st, rs);

		return queryResultSet;
	}

	/**
	 * 执行查询。
	 * 
	 * @param cn
	 * @param sql
	 * @param paramTypes
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public static QueryResultSet executeQuery(Connection cn, String sql, List<Integer> paramTypes, List<?> params)
			throws SQLException
	{
		return executeQuery(cn, sql, null, paramTypes, params);
	}

	/**
	 * 执行查询。
	 * 
	 * @param cn
	 * @param sql
	 * @param fetchSize
	 * @param paramTypes
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public static QueryResultSet executeQuery(Connection cn, String sql, Integer fetchSize, List<Integer> paramTypes,
			List<?> params) throws SQLException
	{
		PreparedStatement pst = createQueryPreparedStatement(cn, sql);
		ResultSet rs = null;

		for (int i = 0, len = params.size(); i < len; i++)
			setParamValue(cn, pst, i + 1, paramTypes.get(i), params.get(i));

		try
		{
			rs = pst.executeQuery();
			return new QueryResultSet(pst, rs);
		}
		catch (SQLException e)
		{
			if (ResultSet.TYPE_FORWARD_ONLY == pst.getResultSetType())
				throw e;
			else
			{
				JdbcUtil.closeResultSet(rs);
				JdbcUtil.closeStatement(pst);
			}
		}

		@JDBCCompatiblity("可能是查询SQL语句不支持ResultSet.TYPE_SCROLL_*（比如SQLServer的聚集列存储索引），"
				+ "因此，这里再降级为ResultSet.TYPE_FORWARD_ONLY再执行查询")
		// TODO 如果是SQL语法错误，则不应再降级执行
		QueryResultSet queryResultSet = null;

		pst = cn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

		for (int i = 0, len = params.size(); i < len; i++)
			setParamValue(cn, pst, i + 1, paramTypes.get(i), params.get(i));

		if (fetchSize != null && fetchSize >= 0)
			pst.setFetchSize(fetchSize);

		rs = pst.executeQuery();

		queryResultSet = new QueryResultSet(pst, rs);

		return queryResultSet;
	}

	/**
	 * SQL查询结果集。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class QueryResultSet
	{
		private Statement statement;

		private ResultSet resultSet;

		public QueryResultSet()
		{
			super();
		}

		public QueryResultSet(Statement statement, ResultSet resultSet)
		{
			super();
			this.statement = statement;
			this.resultSet = resultSet;
		}

		public Statement getStatement()
		{
			return statement;
		}

		public void setStatement(Statement statement)
		{
			this.statement = statement;
		}

		public ResultSet getResultSet()
		{
			return resultSet;
		}

		public void setResultSet(ResultSet resultSet)
		{
			this.resultSet = resultSet;
		}

		public boolean isPreparedStatement()
		{
			return (this.statement instanceof PreparedStatement);
		}

		public PreparedStatement getPreparedStatement()
		{
			return (PreparedStatement) this.statement;
		}

		/**
		 * 关闭。
		 */
		public void close()
		{
			closeResultSet(this.resultSet);
			closeStatement(this.statement);
		}
	}
}
