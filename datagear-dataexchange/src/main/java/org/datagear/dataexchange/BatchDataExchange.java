/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * 批量数据交换。
 * <p>
 * 在调用{@linkplain DataExchangeService#exchange(DataExchange)}后，可通过{@linkplain #getResults()}获取执行结果。
 * </p>
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public abstract class BatchDataExchange<T extends DataExchange> extends DataExchange
{
	private List<Future<T>> results;

	public BatchDataExchange()
	{
		super();
	}

	public BatchDataExchange(ConnectionFactory connectionFactory)
	{
		super(connectionFactory);
	}

	public List<Future<T>> getResults()
	{
		return results;
	}

	public void setResults(List<Future<T>> results)
	{
		this.results = results;
	}

	/**
	 * 阻塞获取执行结果。
	 * 
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public List<T> getForResult() throws InterruptedException, ExecutionException
	{
		if (this.results == null)
			throw new IllegalStateException();

		List<T> subResults = new ArrayList<T>(this.results.size());

		for (Future<T> future : this.results)
		{
			T result = future.get();

			subResults.add(result);
		}

		return subResults;
	}

	/**
	 * 拆分导入。
	 * 
	 * @return
	 * @throws DataExchangeException
	 */
	public abstract List<T> split() throws DataExchangeException;
}
