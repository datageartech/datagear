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
 * Oracle的{@linkplain DialectBuilder}。
 * 
 * @author datagear@163.com
 *
 */
public class OracleDialectBuilder extends AbstractURLSensedDialectBuilder
{
	protected static final String JDBC_PREFIX = "jdbc:oracle";

	public OracleDialectBuilder()
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
		return new OracleDialect(getIdentifierQuote(cn));
	}
}
