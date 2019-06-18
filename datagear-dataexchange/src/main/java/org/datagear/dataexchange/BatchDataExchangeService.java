/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.datagear.dataexchange.support.AbstractDevotedDataExchangeService;

/**
 * 批量数据交换服务。
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public class BatchDataExchangeService<G extends DataExchange, T extends BatchDataExchange<G>>
		extends AbstractDevotedDataExchangeService<T>
{
	private DataExchangeService<? super G> delegateDataExchangeService;

	private ExecutorService executorService = Executors.newCachedThreadPool();

	public BatchDataExchangeService()
	{
		super();
	}

	public BatchDataExchangeService(DataExchangeService<? super G> delegateDataExchangeService)
	{
		super();
		this.delegateDataExchangeService = delegateDataExchangeService;
	}

	public DataExchangeService<? super G> getDelegateDataExchangeService()
	{
		return delegateDataExchangeService;
	}

	public void setDelegateDataExchangeService(DataExchangeService<? super G> delegateDataExchangeService)
	{
		this.delegateDataExchangeService = delegateDataExchangeService;
	}

	public ExecutorService getExecutorService()
	{
		return executorService;
	}

	public void setExecutorService(ExecutorService executorService)
	{
		this.executorService = executorService;
	}

	@Override
	public void exchange(T dataExchange) throws DataExchangeException
	{
		List<G> subDataExchanges = getSubDataExchange(dataExchange);

		exchange(this.executorService, dataExchange, subDataExchanges);
	}

	/**
	 * 使用线程池进行数据交换。
	 * 
	 * @param executorService
	 * @param dataExchange
	 * @param subDataExchanges
	 */
	protected void exchange(ExecutorService executorService, T dataExchange, List<G> subDataExchanges)
	{
		int size = subDataExchanges.size();

		List<Future<G>> futures = new ArrayList<Future<G>>(size);

		for (int i = 0; i < size; i++)
		{
			G subDataExchange = subDataExchanges.get(i);

			DataExchangeCallable<G> dataExchangeCallable = buildDataExchangeCallable(dataExchange, subDataExchange);

			Future<G> future = executorService.submit(dataExchangeCallable);

			futures.add(future);
		}

		dataExchange.setResults(futures);
	}

	/**
	 * 构建{@linkplain DataExchangeCallable}。
	 * 
	 * @param dataExchange
	 * @param subDataExchange
	 * @return
	 */
	protected DataExchangeCallable<G> buildDataExchangeCallable(T dataExchange, G subDataExchange)
	{
		return new DataExchangeCallable<G>(this.delegateDataExchangeService, subDataExchange);
	}

	/**
	 * 拆分。
	 * 
	 * @param dataExchange
	 * @return
	 */
	protected List<G> getSubDataExchange(T dataExchange)
	{
		return dataExchange.split();
	}

	/**
	 * 数据交换{@linkplain Callback}。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static class DataExchangeCallable<T extends DataExchange> implements Callable<T>
	{
		private DataExchangeService<? super T> dataExchangeService;

		private T dataExchange;

		public DataExchangeCallable()
		{
			super();
		}

		public DataExchangeCallable(DataExchangeService<? super T> dataExchangeService, T dataExchange)
		{
			super();
			this.dataExchangeService = dataExchangeService;
			this.dataExchange = dataExchange;
		}

		public DataExchangeService<? super T> getDataExchangeService()
		{
			return dataExchangeService;
		}

		public void setDataExchangeService(DataExchangeService<? super T> dataExchangeService)
		{
			this.dataExchangeService = dataExchangeService;
		}

		public T getDataExchange()
		{
			return dataExchange;
		}

		public void setDataExchange(T dataExchange)
		{
			this.dataExchange = dataExchange;
		}

		@Override
		public T call() throws Exception
		{
			this.dataExchangeService.exchange(this.dataExchange);

			return this.dataExchange;
		}
	}
}
