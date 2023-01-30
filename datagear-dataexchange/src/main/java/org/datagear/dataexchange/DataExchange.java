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
