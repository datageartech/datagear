/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

import java.sql.Connection;

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
			this.resource.close();
	}
}
