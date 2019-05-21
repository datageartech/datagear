/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

import java.sql.Connection;

/**
 * 导出端。
 * 
 * @author datagear@163.com
 *
 */
public abstract class Export
{
	/** 导出源连接 */
	private Connection connection;

	public Export()
	{
		super();
	}

	public Export(Connection connection)
	{
		super();
		this.connection = connection;
	}

	public Connection getConnection()
	{
		return connection;
	}

	public void setConnection(Connection connection)
	{
		this.connection = connection;
	}
}
