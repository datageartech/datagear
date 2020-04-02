/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.persistence.support.dialect;

import java.sql.Connection;

import org.datagear.connection.URLSensor;
import org.datagear.connection.support.PostgresqlURLSensor;
import org.datagear.persistence.Dialect;
import org.datagear.persistence.DialectBuilder;
import org.datagear.persistence.support.AbstractURLSensedDialectBuilder;

/**
 * PostgreSQL的{@linkplain DialectBuilder}。
 * 
 * @author datagear@163.com
 *
 */
public class PostgresqlDialectBuilder extends AbstractURLSensedDialectBuilder
{
	public PostgresqlDialectBuilder()
	{
		super(new PostgresqlURLSensor());
	}

	@Override
	public void setUrlSensor(URLSensor urlSensor)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Dialect build(Connection cn)
	{
		return new PostgresqlDialect(getIdentifierQuote(cn));
	}
}
