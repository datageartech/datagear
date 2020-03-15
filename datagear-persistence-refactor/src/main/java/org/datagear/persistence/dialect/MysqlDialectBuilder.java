/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.dialect;

import java.sql.Connection;

import org.datagear.connection.URLSensor;
import org.datagear.connection.support.MySqlURLSensor;
import org.datagear.persistence.AbstractURLSensedDialectBuilder;
import org.datagear.persistence.Dialect;
import org.datagear.persistence.DialectBuilder;

/**
 * Mysql的{@linkplain DialectBuilder}。
 * 
 * @author datagear@163.com
 *
 */
public class MysqlDialectBuilder extends AbstractURLSensedDialectBuilder
{
	public MysqlDialectBuilder()
	{
		super(new MySqlURLSensor());
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
