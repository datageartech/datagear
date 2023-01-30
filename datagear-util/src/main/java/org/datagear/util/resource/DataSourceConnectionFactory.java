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

package org.datagear.util.resource;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.datagear.util.JdbcUtil;

/**
 * 数据源{@linkplain ConnectionSource}。
 * 
 * @author datagear@163.com
 *
 */
public class DataSourceConnectionFactory implements ConnectionFactory
{
	private DataSource dataSource;

	public DataSourceConnectionFactory()
	{
		super();
	}

	public DataSourceConnectionFactory(DataSource dataSource)
	{
		super();
		this.dataSource = dataSource;
	}

	public DataSource getDataSource()
	{
		return dataSource;
	}

	public void setDataSource(DataSource dataSource)
	{
		this.dataSource = dataSource;
	}

	@Override
	public Connection get() throws SQLException
	{
		return this.dataSource.getConnection();
	}

	@Override
	public void release(Connection cn) throws SQLException
	{
		JdbcUtil.closeConnection(cn);
	}
}
