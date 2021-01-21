/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.meta.resolver;

import java.sql.Connection;

import org.datagear.connection.ConnectionSensor;

/**
 * 抽象数据库连接敏感的{@linkplain DevotedDBMetaResolver}。
 * <p>
 * 此类可以作为特定数据库{@linkplain DevotedDBMetaResolver}实现类的父类。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractConnectionDevotedDBMetaResolver extends AbstractDevotedDBMetaResolver
{
	private ConnectionSensor connectionSensor;

	public AbstractConnectionDevotedDBMetaResolver()
	{
		super();
	}

	public AbstractConnectionDevotedDBMetaResolver(ConnectionSensor connectionSensor)
	{
		super();
		this.connectionSensor = connectionSensor;
	}

	public ConnectionSensor getConnectionSensor()
	{
		return connectionSensor;
	}

	public void setConnectionSensor(ConnectionSensor connectionSensor)
	{
		this.connectionSensor = connectionSensor;
	}

	@Override
	public boolean supports(Connection cn)
	{
		return this.connectionSensor.supports(cn);
	}
}
