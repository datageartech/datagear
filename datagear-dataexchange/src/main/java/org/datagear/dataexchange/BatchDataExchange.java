/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

import java.util.Set;

import org.datagear.util.resource.ConnectionFactory;

/**
 * 批量数据交换。
 * <p>
 * 在调用{@linkplain DataExchangeService#exchange(DataExchange)}后，可通过{@linkplain #getResult()}获取结果。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public abstract class BatchDataExchange extends DataExchange
{
	private BatchDataExchangeListener listener;

	private BatchDataExchangeResult result;

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

	public BatchDataExchangeResult getResult()
	{
		return result;
	}

	public void setResult(BatchDataExchangeResult result)
	{
		this.result = result;
	}

	/**
	 * 获取子数据交换集合。
	 * 
	 * @return
	 * @throws DataExchangeException
	 */
	public abstract Set<SubDataExchange> getSubDataExchanges() throws DataExchangeException;
}
