/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

import java.util.Set;

/**
 * 批量数据交换。
 * <p>
 * 在调用{@linkplain DataExchangeService#exchange(DataExchange)}后，可通过{@linkplain #getResults()}获取执行结果。
 * </p>
 * <p>
 * 如果{@linkplain #getResults()}某个位置的元素为{@code null}，表明对应子数据交换任务提交失败。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public abstract class BatchDataExchange extends DataExchange
{
	/** 是否同步等待完成 */
	private boolean waitForFinish = false;

	private BatchDataExchangeListener listener;

	public BatchDataExchange()
	{
		super();
	}

	public BatchDataExchange(ConnectionFactory connectionFactory)
	{
		super(connectionFactory);
	}

	public boolean isWaitForFinish()
	{
		return waitForFinish;
	}

	public void setWaitForFinish(boolean waitForFinish)
	{
		this.waitForFinish = waitForFinish;
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

	/**
	 * 获取子数据交换集合。
	 * 
	 * @return
	 * @throws DataExchangeException
	 */
	public abstract Set<SubDataExchange> getSubDataExchanges() throws DataExchangeException;
}
