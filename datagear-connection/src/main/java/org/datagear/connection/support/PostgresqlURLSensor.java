/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.connection.support;

import org.datagear.connection.PrefixURLSensor;
import org.datagear.connection.URLSensor;

/**
 * PostgreSQL {@linkplain URLSensor}。
 * 
 * @author datagear@163.com
 *
 */
public class PostgresqlURLSensor extends PrefixURLSensor
{
	public static final String JDBC_PREFIX = "jdbc:postgresql";

	public PostgresqlURLSensor()
	{
		super(JDBC_PREFIX);
	}

	@Override
	public void setPrefix(String prefix)
	{
		throw new UnsupportedOperationException();
	}
}
