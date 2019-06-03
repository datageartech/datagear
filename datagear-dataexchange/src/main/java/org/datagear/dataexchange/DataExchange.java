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

	public DataExchange()
	{
		super();
	}

	public DataExchange(ConnectionFactory connectionFactory)
	{
		super();
		this.connectionFactory = connectionFactory;
	}

	public ConnectionFactory getConnectionFactory()
	{
		return connectionFactory;
	}

	public void setConnectionFactory(ConnectionFactory connectionFactory)
	{
		this.connectionFactory = connectionFactory;
	}
}
