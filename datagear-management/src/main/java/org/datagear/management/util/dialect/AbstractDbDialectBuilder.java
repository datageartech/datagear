/*
 * Copyright 2018-2024 datagear.tech
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
 * 抽象数据库方言构建器。
 * <p>
 * 此类依次根据给定的{@linkplain #getDialectName()}、数据库连接，构建数据库方言。
 * </p>
 * 
 * @author datagear@163.com
 *
 * @param <T>
 *            数据库方言类
 */
public abstract class AbstractDbDialectBuilder<T>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDbDialectBuilder.class);

	public static final String DIALECT_NAME_DERBY = "derby";

	public static final String DIALECT_NAME_MYSQL = "mysql";

	public static final String DIALECT_NAME_ORACLE = "oracle";

	public static final String DIALECT_NAME_POSTGRESQL = "postgresql";

	public static final String DIALECT_NAME_SQLSERVER = "sqlserver";

	/** 方言名 */
	private String dialectName = null;

	public AbstractDbDialectBuilder()
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
	 * 设置方言名，可以是{@code DIALECT_NAME_*}常量标识，或者是方言类全名。
	 * 
	 * @param dialectName
	 *            允许为{@code null}，此时将根当前数据库自动判断
	 */
	public void setDialectName(String dialectName)
	{
		this.dialectName = dialectName;
	}

	/**
	 * 构建方言。
	 * 
	 * @param dataSource
	 * @return
	 * @throws DbDialectBuilderException
	 */
	public T build(DataSource dataSource) throws DbDialectBuilderException
	{
		Connection cn = null;

		try
		{
			cn = dataSource.getConnection();
			return build(cn);
		}
		catch (SQLException e)
		{
			throw new DbDialectBuilderException(e);
		}
		finally
		{
			JdbcUtil.closeConnection(cn);
		}
	}

	/**
	 * 构建方言。
	 * <p>
	 * 注意：此方法不会关闭{@code cn}。
	 * </p>
	 * 
	 * @param cn
	 * @return
	 */
	public T build(Connection cn) throws DbDialectBuilderException
	{
		T dialect = null;

		try
		{
			if (!StringUtil.isEmpty(this.dialectName))
			{
				dialect = buildByDialectName(cn, this.dialectName);
			}

			if (dialect == null)
			{
				String url = getUrl(cn);
				dialect = buildByUrl(cn, url);
			}

			if (dialect == null)
				dialect = buildForNull(cn);
		}
		catch (DbDialectBuilderException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			throw new DbDialectBuilderException(e);
		}

		return dialect;
	}

	/**
	 * 没有构建任何方言时的最终构建方法。
	 * 
	 * @param cn
	 * @return
	 * @throws DbDialectBuilderException
	 * @throws Exception
	 */
	protected T buildForNull(Connection cn) throws DbDialectBuilderException, Exception
	{
		throw new DbDialectBuilderException(
				"Can not build " + getDialectClassDisplayName() + " for current environment");
	}

	protected String getDialectClassDisplayName()
	{
		return "dialect";
	}

	protected String getUrl(Connection cn) throws Exception
	{
		return JdbcUtil.getURLIfSupports(cn);
	}

	/**
	 * 根据方言名构建。
	 * 
	 * @param cn
	 * @param dialectName
	 *            允许为{@code null}
	 * @return 返回{@code null}表示不支持
	 * @throws Exception
	 */
	protected T buildByDialectName(Connection cn, String dialectName) throws Exception
	{
		T dialect = null;

		if (StringUtil.isEmpty(dialectName))
		{
		}
		else if (DIALECT_NAME_DERBY.equalsIgnoreCase(dialectName))
		{
			dialect = buildDerbyDialect(cn);
		}
		else if (DIALECT_NAME_MYSQL.equalsIgnoreCase(dialectName))
		{
			dialect = buildMysqlDialect(cn);
		}
		else if (DIALECT_NAME_ORACLE.equalsIgnoreCase(dialectName))
		{
			dialect = buildOracleDialect(cn);
		}
		else if (DIALECT_NAME_POSTGRESQL.equalsIgnoreCase(dialectName))
		{
			dialect = buildPostgresqlDialect(cn);
		}
		else if (DIALECT_NAME_SQLSERVER.equalsIgnoreCase(dialectName))
		{
			dialect = buildSqlserverDialect(cn);
		}
		else
		{
			dialect = buildByUnknownDialectName(cn, dialectName);
		}

		return dialect;
	}

	/**
	 * 根据未知方言名构建。
	 * 
	 * @param cn
	 * @param dialectName
	 * @return 返回{@code null}表示不支持
	 * @throws Exception
	 */
	protected T buildByUnknownDialectName(Connection cn, String dialectName) throws Exception
	{
		// 可能是类名
		if (dialectName.indexOf('.') > 0)
		{
			return buildByDialectClassName(cn, dialectName);
		}
		else
		{
			return null;
		}
	}

	/**
	 * 根据方言类名构建。
	 * 
	 * @param cn
	 * @param dialectClassName
	 * @return 返回{@code null}表示不支持
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	protected T buildByDialectClassName(Connection cn, String dialectClassName) throws Exception
	{
		T dialect = null;
		
		try
		{
			Class<?> dialectClass = Class.forName(dialectClassName);
			dialect = (T) dialectClass.newInstance();
		}
		catch(Throwable t)
		{
			if (LOGGER.isWarnEnabled())
				LOGGER.warn(
						"Can not build " + getDialectClassDisplayName() + " by class [" + dialectClassName
						+ "]", t);
		}

		return dialect;
	}

	/**
	 * 根据连接URL构建。
	 * 
	 * @param cn
	 * @param url
	 *            允许为{@code null}
	 * @return 返回{@code null}表示不支持
	 * @throws Exception
	 */
	protected T buildByUrl(Connection cn, String url) throws Exception
	{
		T dialect = null;

		if (StringUtil.isEmpty(url))
		{
		}
		else if (DerbyURLSensor.INSTANCE.supports(url))
		{
			dialect = buildDerbyDialect(cn);
		}
		else if (MySqlURLSensor.INSTANCE.supports(url))
		{
			dialect = buildMysqlDialect(cn);
		}
		else if (OracleURLSensor.INSTANCE.supports(url))
		{
			dialect = buildOracleDialect(cn);
		}
		else if (PostgresqlURLSensor.INSTANCE.supports(url))
		{
			dialect = buildPostgresqlDialect(cn);
		}
		else if (SqlServerURLSensor.INSTANCE.supports(url))
		{
			dialect = buildSqlserverDialect(cn);
		}
		else
		{
			dialect = buildByUnknownUrl(cn, url);
		}

		return dialect;
	}

	/**
	 * 根据未知连接URL构建。
	 * 
	 * @param cn
	 * @param url
	 * @return 返回{@code null}表示不支持
	 * @throws Exception
	 */
	protected T buildByUnknownUrl(Connection cn, String url) throws Exception
	{
		return null;
	}

	protected T buildDerbyDialect(Connection cn) throws Exception
	{
		return null;
	}

	protected T buildMysqlDialect(Connection cn) throws Exception
	{
		return null;
	}

	protected T buildOracleDialect(Connection cn) throws Exception
	{
		return null;
	}

	protected T buildPostgresqlDialect(Connection cn) throws Exception
	{
		return null;
	}

	protected T buildSqlserverDialect(Connection cn) throws Exception
	{
		return null;
	}
}
