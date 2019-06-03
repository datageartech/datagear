/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 简单{@linkplain ConnectionFactory}。
 * 
 * @author datagear@163.com
 *
 */
public class SimpleConnectionFactory implements ConnectionFactory
{
	private Connection connection;

	private boolean closeOnReclaim;

	public SimpleConnectionFactory()
	{
		super();
	}

	public SimpleConnectionFactory(Connection connection, boolean closeOnReclaim)
	{
		super();
		this.connection = connection;
		this.closeOnReclaim = closeOnReclaim;
	}

	public void setConnection(Connection connection)
	{
		this.connection = connection;
	}

	@Override
	public Connection getConnection() throws SQLException
	{
		return this.connection;
	}

	public boolean isCloseOnReclaim()
	{
		return closeOnReclaim;
	}

	public void setCloseOnReclaim(boolean closeOnReclaim)
	{
		this.closeOnReclaim = closeOnReclaim;
	}

	@Override
	public void reclaimConnection(Connection cn) throws SQLException
	{
		if (this.connection != cn)
			throw new IllegalStateException();

		if (this.closeOnReclaim && this.connection != null)
			cn.close();
	}
}
