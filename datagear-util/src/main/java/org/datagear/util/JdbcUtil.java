/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.util;

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
	/** JDBC标准URL前缀 */
	public static final String JDBC_URL_PREFIX = "jdbc:";

	private JdbcUtil()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * 是否是二进制SQL类型。
	 * 
	 * @param sqlType
	 * @return
	 */
	public static boolean isBinaryType(int sqlType)
	{
		switch (sqlType)
		{
			case Types.BINARY:
			case Types.VARBINARY:
			case Types.LONGVARBINARY:
			case Types.BLOB:

				return true;

			default:

				return false;
		}
	}

	/**
	 * 是否是文本SQL类型。
	 * 
	 * @param sqlType
	 * @return
	 */
	public static boolean isTextType(int sqlType)
	{
		switch (sqlType)
		{
			case Types.CHAR:
			case Types.VARCHAR:
			case Types.LONGVARCHAR:
			case Types.CLOB:
			case Types.NCHAR:
			case Types.NVARCHAR:
			case Types.LONGNVARCHAR:
			case Types.NCLOB:
			case Types.SQLXML:

				return true;

			default:

				return false;
		}
	}

	/**
	 * 是否数值类型。
	 * 
	 * @param sqlType
	 * @return
	 */
	public static boolean isNumberType(int sqlType)
	{
		switch (sqlType)
		{
			case Types.TINYINT:
			case Types.SMALLINT:
			case Types.INTEGER:
			case Types.BIGINT:
			case Types.REAL:
			case Types.FLOAT:
			case Types.DOUBLE:
			case Types.DECIMAL:
			case Types.NUMERIC:
				return true;
			default:
				return false;
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
	 * @param defaultValue
	 *            出现异常时的默认值
	 * @return
	 */
	public static boolean isReadonlyIfSupports(Connection cn, boolean defaultValue)
	{
		try
		{
			return cn.isReadOnly();
		}
		catch(Throwable e)
		{
			return defaultValue;
		}
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
		catch(Throwable e)
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
		catch(Throwable e)
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
		catch(Throwable t)
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
		catch(Throwable t)
		{
			return null;
		}
	}

	/**
	 * 设置{@linkplain Statement#setFetchSize(int)}。
	 * 
	 * @param st
	 * @param fetchSize
	 * @return
	 */
	@JDBCCompatiblity("避免有驱动程序不支持此方法而抛出异常")
	public static boolean setFetchSizeIfSupports(Statement st, int fetchSize)
	{
		try
		{
			st.setFetchSize(fetchSize);
			return true;
		}
		catch(Throwable e)
		{
			return false;
		}
	}

	/**
	 * 获取{@linkplain DatabaseMetaData#getDatabaseProductName()}。
	 * 
	 * @param cn
	 * @return 返回{@code null}表示不支持
	 */
	@JDBCCompatiblity("避免有驱动程序不支持此方法而抛出异常")
	public static String getDatabaseProductNameIfSupports(Connection cn)
	{
		try
		{
			DatabaseMetaData metaData = cn.getMetaData();
			return metaData.getDatabaseProductName();
		}
		catch(Throwable e)
		{
			return null;
		}
	}

	/**
	 * 获取{@linkplain DatabaseMetaData#getDatabaseProductVersion()}。
	 * 
	 * @param cn
	 * @return 返回{@code null}表示不支持
	 */
	@JDBCCompatiblity("避免有驱动程序不支持此方法而抛出异常")
	public static String getDatabaseProductVersionIfSupports(Connection cn)
	{
		try
		{
			DatabaseMetaData metaData = cn.getMetaData();
			return metaData.getDatabaseProductVersion();
		}
		catch(Throwable e)
		{
			return null;
		}
	}

	/**
	 * 获取{@linkplain DatabaseMetaData#getDatabaseMajorVersion()}。
	 * 
	 * @param cn
	 * @return 返回{@code null}表示不支持
	 */
	@JDBCCompatiblity("避免有驱动程序不支持此方法而抛出异常")
	public static Integer getDatabaseMajorVersionIfSupports(Connection cn)
	{
		try
		{
			DatabaseMetaData metaData = cn.getMetaData();
			return metaData.getDatabaseMajorVersion();
		}
		catch(Throwable e)
		{
			return null;
		}
	}

	/**
	 * 获取{@linkplain DatabaseMetaData#getDatabaseMinorVersion()}。
	 * 
	 * @param cn
	 * @return 返回{@code null}表示不支持
	 */
	@JDBCCompatiblity("避免有驱动程序不支持此方法而抛出异常")
	public static Integer getDatabaseMinorVersionIfSupports(Connection cn)
	{
		try
		{
			DatabaseMetaData metaData = cn.getMetaData();
			return metaData.getDatabaseMinorVersion();
		}
		catch(Throwable e)
		{
			return null;
		}
	}

	/**
	 * 获取标识符引用符。
	 * 
	 * @param cn
	 * @return
	 */
	public static String getIdentifierQuote(Connection cn)
	{
		String iq;

		try
		{
			iq = cn.getMetaData().getIdentifierQuoteString();
		}
		catch(Throwable e)
		{
			// 默认应为JDBC规范的空格
			iq = " ";
		}

		return iq;
	}

	/**
	 * 给SQL标识符添加引号。
	 * 
	 * @param str
	 * @param quote
	 * @return
	 */
	public static String quote(String str, String quote)
	{
		// 字符串内的引号需进行转义
		if (str.indexOf(quote) > -1)
			str = str.replace(quote, quote + quote);

		return quote + str + quote;
	}

	/**
	 * 给SQL标识符添加引号。
	 * 
	 * @param str
	 * @param cn
	 * @return
	 */
	public static String quote(String str, Connection cn)
	{
		String quote = getIdentifierQuote(cn);
		return quote(str, quote);
	}
}
