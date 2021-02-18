/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.connection.support;

import org.datagear.connection.PrefixURLSensor;
import org.datagear.connection.URLSensor;

/**
 * Derby {@linkplain URLSensor}。
 * 
 * @author datagear@163.com
 *
 */
public class DerbyURLSensor extends PrefixURLSensor
{
	public static final String JDBC_PREFIX = "jdbc:derby";

	public static final DerbyURLSensor INSTANCE = new DerbyURLSensor();

	public DerbyURLSensor()
	{
		super(JDBC_PREFIX);
	}

	@Override
	public void setPrefix(String prefix)
	{
		throw new UnsupportedOperationException();
	}
}
