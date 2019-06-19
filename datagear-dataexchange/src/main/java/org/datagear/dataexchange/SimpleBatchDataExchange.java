/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

import java.util.List;

/**
 * 简单{@linkplain BatchDataExchange}。
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public class SimpleBatchDataExchange<T extends DataExchange> extends BatchDataExchange<T>
{
	private List<T> subDataExchanges;

	public SimpleBatchDataExchange()
	{
		super();
	}

	public SimpleBatchDataExchange(ConnectionFactory connectionFactory, List<T> subDataExchanges)
	{
		super(connectionFactory);
		this.subDataExchanges = subDataExchanges;
	}

	public List<T> getSubDataExchanges()
	{
		return subDataExchanges;
	}

	public void setSubDataExchanges(List<T> subDataExchanges)
	{
		this.subDataExchanges = subDataExchanges;
	}

	@Override
	public List<T> split() throws DataExchangeException
	{
		return this.subDataExchanges;
	}
}
