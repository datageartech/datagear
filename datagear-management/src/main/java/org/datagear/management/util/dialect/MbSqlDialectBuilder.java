/*
 * Copyright 2018-2023 datagear.tech
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
public class MbSqlDialectBuilder extends AbstractDbDialectBuilder<MbSqlDialect>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(MbSqlDialectBuilder.class);

	public static final String DIALECT_NAME_DEFAULT = "default";

	/**
	 * 默认标识符引用符。
	 */
	public static final String DEFAULT_IDENTIFIER_QUOTE = " ";

	public MbSqlDialectBuilder()
	{
		super();
	}

	@Override
	public MbSqlDialect build(Connection cn) throws DbDialectBuilderException
	{
		MbSqlDialect mbd = super.build(cn);

		if (LOGGER.isInfoEnabled())
			LOGGER.info("Build " + mbd + " for current environment");

		return mbd;
	}

	@Override
	protected MbSqlDialect buildByUnknownDialectName(Connection cn, String dialectName) throws Exception
	{
		if (DIALECT_NAME_DEFAULT.equals(dialectName))
		{
			return buildDefaultDialect(cn);
		}
		else
			return super.buildByUnknownDialectName(cn, dialectName);
	}

	@Override
	protected MbSqlDialect buildByDialectClassName(Connection cn, String dialectClassName) throws Exception
	{
		MbSqlDialect dialect = super.buildByDialectClassName(cn, dialectClassName);

		if (dialect != null && StringUtil.isEmpty(dialect.getIdentifierQuote()))
			dialect.setIdentifierQuote(getIdentifierQuote(cn));

		return dialect;
	}

	@Override
	protected DerbyMbSqlDialect buildDerbyDialect(Connection cn) throws SQLException
	{
		return new DerbyMbSqlDialect(getIdentifierQuote(cn));
	}

	@Override
	protected MysqlMbSqlDialect buildMysqlDialect(Connection cn) throws SQLException
	{
		return new MysqlMbSqlDialect(getIdentifierQuote(cn));
	}

	@Override
	protected OracleMbSqlDialect buildOracleDialect(Connection cn) throws SQLException
	{
		return new OracleMbSqlDialect(getIdentifierQuote(cn));
	}

	@Override
	protected PostgresqlMbSqlDialect buildPostgresqlDialect(Connection cn) throws SQLException
	{
		return new PostgresqlMbSqlDialect(getIdentifierQuote(cn));
	}

	@Override
	protected SqlserverMbSqlDialect buildSqlserverDialect(Connection cn) throws SQLException
	{
		return new SqlserverMbSqlDialect(getIdentifierQuote(cn));
	}

	@Override
	protected String getDialectClassDisplayName()
	{
		return MbSqlDialect.class.getSimpleName();
	}

	protected DefaultMbSqlDialect buildDefaultDialect(Connection cn) throws SQLException
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
