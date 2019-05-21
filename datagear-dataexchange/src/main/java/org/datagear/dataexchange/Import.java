/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

import java.sql.Connection;

/**
 * 导入源。
 * 
 * @author datagear@163.com
 *
 */
public abstract class Import
{
	/** 导入目标连接 */
	private Connection connection;

	/** 导入出错时是否终止 */
	private boolean abortOnError;

	public Import()
	{
		super();
	}

	public Import(Connection connection, boolean abortOnError)
	{
		super();
		this.connection = connection;
		this.abortOnError = abortOnError;
	}

	public Connection getConnection()
	{
		return connection;
	}

	public void setConnection(Connection connection)
	{
		this.connection = connection;
	}

	public boolean isAbortOnError()
	{
		return abortOnError;
	}

	public void setAbortOnError(boolean abortOnError)
	{
		this.abortOnError = abortOnError;
	}
}
