/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.support.dialect;

import java.sql.Connection;

import org.datagear.connection.PrefixURLSensor;
import org.datagear.connection.URLSensor;
import org.datagear.persistence.Dialect;
import org.datagear.persistence.DialectBuilder;
import org.datagear.persistence.support.AbstractURLSensedDialectBuilder;

/**
 * SqlServer的{@linkplain DialectBuilder}。
 * 
 * @author datagear@163.com
 *
 */
public class SqlServerDialectBuilder extends AbstractURLSensedDialectBuilder
{
	protected static final String JDBC_PREFIX = "jdbc:sqlserver";

	public SqlServerDialectBuilder()
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
		return new SqlServerDialect(getIdentifierQuote(cn));
	}
}
