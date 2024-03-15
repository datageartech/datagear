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

package org.datagear.util.resource;

import java.sql.Connection;

import org.datagear.util.JdbcUtil;

/**
 * 简单{@linkplain ConnectionFactory}。
 * 
 * @author datagear@163.com
 *
 */
public class SimpleConnectionFactory implements ConnectionFactory
{
	private Connection resource;

	private boolean closeOnRelease = true;

	public SimpleConnectionFactory()
	{
		super();
	}

	public SimpleConnectionFactory(Connection resource, boolean closeOnRelease)
	{
		super();
		this.resource = resource;
		this.closeOnRelease = closeOnRelease;
	}

	public Connection getResource() throws Exception
	{
		return resource;
	}

	public void setResource(Connection resource)
	{
		this.resource = resource;
	}

	public boolean isCloseOnRelease()
	{
		return closeOnRelease;
	}

	public void setCloseOnRelease(boolean closeOnRelease)
	{
		this.closeOnRelease = closeOnRelease;
	}

	@Override
	public Connection get() throws Exception
	{
		return this.resource;
	}

	@Override
	public void release(Connection resource) throws Exception
	{
		if (this.resource != resource)
			throw new IllegalStateException();

		if (this.closeOnRelease && this.resource != null)
			JdbcUtil.closeConnection(this.resource);
	}
}
