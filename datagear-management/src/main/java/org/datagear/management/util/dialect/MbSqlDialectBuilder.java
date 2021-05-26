/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.util.dialect;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.datagear.connection.support.DerbyURLSensor;
import org.datagear.connection.support.MySqlURLSensor;
import org.datagear.connection.support.OracleURLSensor;
import org.datagear.connection.support.PostgresqlURLSensor;
import org.datagear.connection.support.SqlServerURLSensor;
import org.datagear.util.JdbcUtil;
import org.datagear.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@linkplain MbSqlDialect}构建器。
 * <p>
 * 此类依次根据给定的{@linkplain #getDialectName()}、数据库连接，构建对应的{@linkplain MbSqlDialect}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class MbSqlDialectBuilder
{
	private static final Logger LOGGER = LoggerFactory.getLogger(MbSqlDialectBuilder.class);

	/**
	 * 默认标识符引用符。
	 */
	public static final String DEFAULT_IDENTIFIER_QUOTE = " ";

	public static final String DIALECT_NAME_DERBY = "derby";

	public static final String DIALECT_NAME_MYSQL = "mysql";

	public static final String DIALECT_NAME_ORACLE = "oracle";

	public static final String DIALECT_NAME_POSTGRESQL = "postgresql";

	public static final String DIALECT_NAME_SQLSERVER = "sqlserver";

	public static final String DIALECT_NAME_DEFAULT = "default";

	/** 方言名 */
	private String dialectName = null;

	public MbSqlDialectBuilder()
	{
	}

	/**
	 * 获取方言名。
	 * 
	 * @return 可能为{@code null}
	 */
	public String getDialectName()
	{
		return dialectName;
	}

	/**
	 * 设置方言名，可以是{@code DIALECT_NAME_*}常量标识，或者是{@linkplain MbSqlDialect}的实现类全名。
	 * 
	 * @param dialectName
	 *            允许为{@code null}，此时将根当前数据库自动判断
	 */
	public void setDialectName(String dialectName)
	{
		this.dialectName = dialectName;
	}

	/**
	 * 构建{@linkplain MbSqlDialect}。
	 * 
	 * @param dataSource
	 * @return
	 * @throws SQLException
	 */
	public MbSqlDialect build(DataSource dataSource) throws SQLException
	{
		Connection cn = null;

		try
		{
			cn = dataSource.getConnection();
			return build(cn);
		}
		finally
		{
			JdbcUtil.closeConnection(cn);
		}
	}

	/**
	 * 构建{@linkplain MbSqlDialect}。
	 * <p>
	 * 注意：此方法不会关闭{@code cn}。
	 * </p>
	 * 
	 * @param cn
	 * @return
	 */
	public MbSqlDialect build(Connection cn) throws SQLException
	{
		MbSqlDialect dialect = null;

		if (!StringUtil.isEmpty(this.dialectName))
		{
			dialect = buildByDialectName(cn, this.dialectName);
		}

		if (dialect == null)
		{
			String url = JdbcUtil.getURLIfSupports(cn);
			dialect = buildByUrl(cn, url);
		}

		if (dialect == null)
			dialect = buildDefaultMbSqlDialect(cn);

		if (LOGGER.isInfoEnabled())
			LOGGER.info("Build " + dialect.toString() + " for current environment");

		return dialect;
	}

	/**
	 * 
	 * @param cn
	 * @param dialectName
	 *            允许为{@code null}
	 * @return 返回{@code null}表示不支持
	 * @throws SQLException
	 */
	protected MbSqlDialect buildByDialectName(Connection cn, String dialectName) throws SQLException
	{
		MbSqlDialect dialect = null;

		if (StringUtil.isEmpty(dialectName))
		{
		}
		else if (DIALECT_NAME_DERBY.equalsIgnoreCase(dialectName))
		{
			dialect = buildDerbyMbSqlDialect(cn);
		}
		else if (DIALECT_NAME_MYSQL.equalsIgnoreCase(dialectName))
		{
			dialect = buildMysqlMbSqlDialect(cn);
		}
		else if (DIALECT_NAME_ORACLE.equalsIgnoreCase(dialectName))
		{
			dialect = buildOracleMbSqlDialect(cn);
		}
		else if (DIALECT_NAME_POSTGRESQL.equalsIgnoreCase(dialectName))
		{
			dialect = buildPostgresqlMbSqlDialect(cn);
		}
		else if (DIALECT_NAME_SQLSERVER.equalsIgnoreCase(dialectName))
		{
			dialect = buildSqlserverMbSqlDialect(cn);
		}
		else if (DIALECT_NAME_DEFAULT.equalsIgnoreCase(dialectName))
		{
			dialect = buildDefaultMbSqlDialect(cn);
		}
		else
		{
			dialect = buildByDialectClassName(cn, dialectName);
		}

		return dialect;
	}

	protected MbSqlDialect buildByDialectClassName(Connection cn, String dialectClassName) throws SQLException
	{
		try
		{
			Class<?> dialectClass = Class.forName(dialectClassName);
			@SuppressWarnings("deprecation")
			MbSqlDialect dialect = (MbSqlDialect) dialectClass.newInstance();

			if (StringUtil.isEmpty(dialect.getIdentifierQuote()))
				dialect.setIdentifierQuote(getIdentifierQuote(cn));

			return dialect;
		}
		catch (Exception e)
		{
			throw new MbSqlDialectException(e);
		}
	}

	/**
	 * 
	 * @param cn
	 * @param url
	 *            允许为{@code null}
	 * @return 返回{@code null}表示不支持
	 * @throws SQLException
	 */
	protected MbSqlDialect buildByUrl(Connection cn, String url) throws SQLException
	{
		MbSqlDialect dialect = null;

		if (StringUtil.isEmpty(url))
		{
		}
		else if (DerbyURLSensor.INSTANCE.supports(url))
		{
			dialect = buildDerbyMbSqlDialect(cn);
		}
		else if (MySqlURLSensor.INSTANCE.supports(url))
		{
			dialect = buildMysqlMbSqlDialect(cn);
		}
		else if (OracleURLSensor.INSTANCE.supports(url))
		{
			dialect = buildOracleMbSqlDialect(cn);
		}
		else if (PostgresqlURLSensor.INSTANCE.supports(url))
		{
			dialect = buildPostgresqlMbSqlDialect(cn);
		}
		else if (SqlServerURLSensor.INSTANCE.supports(url))
		{
			dialect = buildSqlserverMbSqlDialect(cn);
		}

		return dialect;
	}

	protected DerbyMbSqlDialect buildDerbyMbSqlDialect(Connection cn) throws SQLException
	{
		return new DerbyMbSqlDialect(getIdentifierQuote(cn));
	}

	protected MysqlMbSqlDialect buildMysqlMbSqlDialect(Connection cn) throws SQLException
	{
		return new MysqlMbSqlDialect(getIdentifierQuote(cn));
	}

	protected OracleMbSqlDialect buildOracleMbSqlDialect(Connection cn) throws SQLException
	{
		return new OracleMbSqlDialect(getIdentifierQuote(cn));
	}

	protected PostgresqlMbSqlDialect buildPostgresqlMbSqlDialect(Connection cn) throws SQLException
	{
		return new PostgresqlMbSqlDialect(getIdentifierQuote(cn));
	}

	protected SqlserverMbSqlDialect buildSqlserverMbSqlDialect(Connection cn) throws SQLException
	{
		return new SqlserverMbSqlDialect(getIdentifierQuote(cn));
	}

	protected DefaultMbSqlDialect buildDefaultMbSqlDialect(Connection cn) throws SQLException
	{
		return new DefaultMbSqlDialect(getIdentifierQuote(cn));
	}

	/**
	 * 获取数据库标识符引用符。
	 * <p>
	 * 如果出现异常，将返回{@linkplain #DEFAULT_IDENTIFIER_QUOTE}。
	 * </p>
	 * 
	 * @param cn
	 * @return
	 */
	protected String getIdentifierQuote(Connection cn)
	{
		String identifierQuote = null;

		try
		{
			identifierQuote = cn.getMetaData().getIdentifierQuoteString();
		}
		catch (SQLException e)
		{
			identifierQuote = DEFAULT_IDENTIFIER_QUOTE;

			LOGGER.error("Default identifier quote \"" + identifierQuote + "\" is used for error", e);
		}

		return identifierQuote;
	}
}
