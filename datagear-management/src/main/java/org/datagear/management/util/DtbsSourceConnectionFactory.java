/*
 * Copyright 2018-present datagear.tech
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

package org.datagear.management.util;

import java.io.Serializable;
import java.sql.Connection;

import org.datagear.connection.ConnectionSource;
import org.datagear.management.domain.DtbsSource;
import org.datagear.util.JdbcUtil;
import org.datagear.util.resource.ConnectionFactory;

/**
 * 封装{@linkplain DtbsSource}的{@linkplain ConnectionFactory}。
 * 
 * @author datagear@163.com
 *
 */
public class DtbsSourceConnectionFactory extends DtbsSourceConnectionSupport implements ConnectionFactory, Serializable
{
	private static final long serialVersionUID = 1L;

	private transient ConnectionSource connectionSource;

	private DtbsSource dtbsSource;

	public DtbsSourceConnectionFactory()
	{
		super();
	}

	public DtbsSourceConnectionFactory(ConnectionSource connectionSource, DtbsSource dtbsSource)
	{
		super();
		this.connectionSource = connectionSource;
		this.dtbsSource = dtbsSource;
	}

	public ConnectionSource getConnectionSource()
	{
		return connectionSource;
	}

	public void setConnectionSource(ConnectionSource connectionSource)
	{
		this.connectionSource = connectionSource;
	}

	public DtbsSource getDtbsSource()
	{
		return dtbsSource;
	}

	public void setDtbsSource(DtbsSource dtbsSource)
	{
		this.dtbsSource = dtbsSource;
	}

	@Override
	public Connection get() throws Exception
	{
		return super.getDtbsSourceConnection(this.connectionSource, this.dtbsSource);
	}

	@Override
	public void release(Connection resource) throws Exception
	{
		JdbcUtil.closeConnection(resource);
	}
}
