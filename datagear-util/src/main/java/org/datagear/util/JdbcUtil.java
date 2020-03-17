/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.util;

import java.math.BigDecimal;
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
	 * 静默提交。
	 * 
	 * @param cn
	 */
	public static void commitSilently(Connection cn)
	{
		try
		{
			commitIfSupports(cn);
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
	 * 静默回滚。
	 * 
	 * @param cn
	 */
	public static void rollbackSilently(Connection cn)
	{
		try
		{
			rollbackIfSupports(cn);
		}
		catch (Throwable t)
		{
		}
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
}
