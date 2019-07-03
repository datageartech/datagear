/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

import java.util.Set;

/**
 * 简单{@linkplain BatchDataExchange}。
 * 
 * @author datagear@163.com
 *
 */
public class SimpleBatchDataExchange extends BatchDataExchange
{
	private Set<SubDataExchange> subDataExchanges;

	public SimpleBatchDataExchange()
	{
		super();
	}

	public SimpleBatchDataExchange(ConnectionFactory connectionFactory, Set<SubDataExchange> subDataExchanges)
	{
		super(connectionFactory);
		this.subDataExchanges = subDataExchanges;
	}

	@Override
	public Set<SubDataExchange> getSubDataExchanges()
	{
		return subDataExchanges;
	}

	public void setSubDataExchanges(Set<SubDataExchange> subDataExchanges)
	{
		this.subDataExchanges = subDataExchanges;
	}
}
