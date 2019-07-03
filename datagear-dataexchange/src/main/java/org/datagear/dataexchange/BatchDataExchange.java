/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

import java.util.Set;

/**
 * 批量数据交换。
 * <p>
 * 在调用{@linkplain DataExchangeService#exchange(DataExchange)}后，可通过{@linkplain #getContext()}获取执行上下文。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public abstract class BatchDataExchange extends DataExchange
{
	private BatchDataExchangeListener listener;
	
	private BatchDataExchangeContext context;

	public BatchDataExchange()
	{
		super();
	}

	public BatchDataExchange(ConnectionFactory connectionFactory)
	{
		super(connectionFactory);
	}

	@Override
	public BatchDataExchangeListener getListener()
	{
		return listener;
	}

	public void setListener(BatchDataExchangeListener listener)
	{
		this.listener = listener;
	}

	public BatchDataExchangeContext getContext() {
		return context;
	}

	public void setContext(BatchDataExchangeContext context) {
		this.context = context;
	}

	/**
	 * 获取子数据交换集合。
	 * 
	 * @return
	 * @throws DataExchangeException
	 */
	public abstract Set<SubDataExchange> getSubDataExchanges() throws DataExchangeException;
}
