/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

import java.sql.Connection;

/**
 * 数据交换。
 * 
 * @author datagear@163.com
 *
 */
public abstract class DataExchange
{
	/** 数据交换连接 */
	private Connection connection;

	/** 出错时是否终止 */
	private boolean abortOnError;

	public DataExchange()
	{
		super();
	}

	public DataExchange(Connection connection, boolean abortOnError)
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
