/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.dbmodel;

import java.sql.Connection;

import org.datagear.connection.ConnectionSensor;
import org.datagear.dbinfo.DatabaseInfoResolver;

/**
 * 抽象数据库连接敏感的{@linkplain DevotedDatabaseModelResolver}。
 * <p>
 * 此类可以作为特定数据库{@linkplain DevotedDatabaseModelResolver}实现类的父类。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractConnectionDevotedDatabaseModelResolver extends AbstractDevotedDatabaseModelResolver
{
	private ConnectionSensor connectionSensor;

	public AbstractConnectionDevotedDatabaseModelResolver()
	{
		super();
	}

	public AbstractConnectionDevotedDatabaseModelResolver(ConnectionSensor connectionSensor,
			DatabaseInfoResolver databaseInfoResolver, PrimitiveModelResolver primitiveModelResolver)
	{
		super(databaseInfoResolver, primitiveModelResolver);
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
