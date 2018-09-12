/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.dbinfo;

import java.sql.Connection;

import org.datagear.connection.ConnectionSensor;

/**
 * 抽象数据库连接敏感的{@linkplain DevotedDatabaseInfoResolver}。
 * <p>
 * 此类可以作为特定数据库{@linkplain DevotedDatabaseInfoResolver}实现类的父类。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractConnectionDevotedDatabaseInfoResolver extends AbstractDevotedDatabaseInfoResolver
{
	private ConnectionSensor connectionSensor;

	public AbstractConnectionDevotedDatabaseInfoResolver()
	{
		super();
	}

	public AbstractConnectionDevotedDatabaseInfoResolver(ConnectionSensor connectionSensor)
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
