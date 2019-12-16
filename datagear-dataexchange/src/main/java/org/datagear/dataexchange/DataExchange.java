/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

import org.datagear.util.resource.ConnectionFactory;

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

	/**
	 * 是否有{@linkplain DataExchangeListener}。
	 * 
	 * @return
	 */
	public boolean hasListener()
	{
		return (getListener() != null);
	}

	/**
	 * 获取{@linkplain DataExchangeListener}。
	 * <p>
	 * 返回{@code null}表示未设置。
	 * </p>
	 * <p>
	 * 此方法默认返回{@code null}，子类可以在需要时重写。
	 * </p>
	 * 
	 * @return
	 */
	public DataExchangeListener getListener()
	{
		return null;
	}
}
