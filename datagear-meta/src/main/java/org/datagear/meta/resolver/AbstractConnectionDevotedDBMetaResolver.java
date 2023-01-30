/*
 * Copyright 2018-2023 datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
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
