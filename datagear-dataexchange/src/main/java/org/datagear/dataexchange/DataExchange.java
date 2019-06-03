/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

/**
 * 数据交换。
 * 
 * @author datagear@163.com
 *
 */
public abstract class DataExchange
{
	/** 数据交换数据源 */
	private ConnectionFactory connectionFactory;

	/** 出错时是否终止 */
	private boolean abortOnError;

	public DataExchange()
	{
		super();
	}

	public DataExchange(ConnectionFactory connectionFactory, boolean abortOnError)
	{
		super();
		this.connectionFactory = connectionFactory;
		this.abortOnError = abortOnError;
	}

	public ConnectionFactory getConnectionFactory()
	{
		return connectionFactory;
	}

	public void setConnectionFactory(ConnectionFactory connectionFactory)
	{
		this.connectionFactory = connectionFactory;
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
