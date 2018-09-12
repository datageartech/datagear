/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.persistence.support.dialect;

import java.sql.Connection;

import org.datagear.connection.PrefixURLSensor;
import org.datagear.connection.URLSensor;
import org.datagear.persistence.Dialect;
import org.datagear.persistence.DialectBuilder;
import org.datagear.persistence.support.AbstractURLSensedDialectBuilder;

/**
 * Mysql的{@linkplain DialectBuilder}。
 * 
 * @author datagear@163.com
 *
 */
public class MysqlDialectBuilder extends AbstractURLSensedDialectBuilder
{
	protected static final String JDBC_PREFIX = "jdbc:mysql";

	public MysqlDialectBuilder()
	{
		super(new PrefixURLSensor(JDBC_PREFIX));
	}

	@Override
	public void setUrlSensor(URLSensor urlSensor)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Dialect build(Connection cn)
	{
		return new MysqlDialect(getIdentifierQuote(cn));
	}
}
