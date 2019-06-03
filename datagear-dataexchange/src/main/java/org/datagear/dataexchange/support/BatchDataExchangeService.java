/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.datagear.dataexchange.DataExchange;
import org.datagear.dataexchange.DataExchangeException;
import org.datagear.dataexchange.DataExchangeResult;
import org.datagear.dataexchange.DataExchangeService;

/**
 * 批量数据交换服务。
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public abstract class BatchDataExchangeService<G extends DataExchange, T extends BatchDataExchange<G>>
		extends AbstractDevotedDataExchangeService<T>
{
	private DataExchangeService<? super G> delegateDataExchangeService;

	private ExecutorService executorService;

	public BatchDataExchangeService()
	{
		super();
	}

	public BatchDataExchangeService(DataExchangeService<? super G> delegateDataExchangeService,
			ExecutorService executorService)
	{
		super();
		this.delegateDataExchangeService = delegateDataExchangeService;
		this.executorService = executorService;
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
	 * 处理数据交换{@linkplain Future}。
	 * 
	 * @param dataExchange
	 * @param subDataExchanges
	 * @param subDataExchangeFutures
	 */
	protected abstract void handleExchangeFutures(T dataExchange, List<G> subDataExchanges, List<Future<G>> subDataExchangeFutures);

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

			DataExchangeCallable<G> dataExchangeCallable = new DataExchangeCallable<G>(this.delegateDataExchangeService,
					subDataExchange);

			Future<G> future = executorService.submit(dataExchangeCallable);

			futures.add(future);
		}

		handleExchangeFutures(dataExchange, subDataExchanges, futures);
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
	 * 获取最大耗时。
	 * 
	 * @param dataImportResults
	 * @return
	 */
	protected long getMaxDuration(List<DataExchangeResult> dataExchangeResults)
	{
		if (dataExchangeResults == null)
			return 0;

		long duration = 0;

		for (int i = 0, len = dataExchangeResults.size(); i < len; i++)
		{
			long myDuration = dataExchangeResults.get(i).getDuration();

			if (duration < myDuration)
				duration = myDuration;
		}

		return duration;
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
