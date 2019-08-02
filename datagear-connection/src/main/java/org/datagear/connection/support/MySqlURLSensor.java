/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.connection.support;

import org.datagear.connection.PrefixURLSensor;
import org.datagear.connection.URLSensor;

/**
 * MySQL {@linkplain URLSensor}。
 * 
 * @author datagear@163.com
 *
 */
public class MySqlURLSensor extends PrefixURLSensor
{
	public static final String JDBC_PREFIX = "jdbc:mysql";

	public MySqlURLSensor()
	{
		super(JDBC_PREFIX);
	}

	@Override
	public void setPrefix(String prefix)
	{
		throw new UnsupportedOperationException();
	}
}
