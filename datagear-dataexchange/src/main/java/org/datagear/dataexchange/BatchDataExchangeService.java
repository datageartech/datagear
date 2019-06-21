/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;

import org.datagear.dataexchange.support.AbstractDevotedDataExchangeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final Logger LOGGER = LoggerFactory.getLogger(BatchDataExchangeService.class);

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
		BatchDataExchangeListener<G> listener = dataExchange.getListener();

		if (listener != null)
			listener.onStart();

		List<Future<G>> results = new ArrayList<Future<G>>();
		dataExchange.setResults(results);

		try
		{
			List<G> subDataExchanges = getSubDataExchanges(dataExchange);
			exchange(this.executorService, dataExchange, subDataExchanges, results);

			if (listener != null)
				listener.onSuccess();
		}
		catch (Throwable t)
		{
			DataExchangeException e = wrapToDataExchangeException(t);

			if (listener != null)
				listener.onException(e);
			else
				throw e;
		}
		finally
		{
			// 没有任何子任务提交成功，那么需要在这里调用onFinish
			if (listener != null && isNoneSubmitSuccess(results))
				listener.onFinish();
		}
	}

	/**
	 * 关闭。
	 */
	public void shutdown()
	{
		this.executorService.shutdown();
	}

	/**
	 * 是否已关闭。
	 * 
	 * @return
	 */
	public boolean isShutdown()
	{
		return this.executorService.isShutdown();
	}

	/**
	 * 是否没有任一提交成功。
	 * 
	 * @param results
	 * @return
	 */
	protected boolean isNoneSubmitSuccess(List<Future<G>> results)
	{
		if (results == null)
			return true;

		boolean noneSubmitSuccess = true;

		int size = results.size();

		for (int i = 0; i < size; i++)
		{
			if (results.get(i) != null)
			{
				noneSubmitSuccess = false;
				break;
			}
		}

		return noneSubmitSuccess;
	}

	/**
	 * 使用{@linkplain ExecutorService}进行数据交换。
	 * 
	 * @param executorService
	 * @param dataExchange
	 * @param subDataExchanges
	 * @param results
	 */
	protected void exchange(ExecutorService executorService, T dataExchange, List<G> subDataExchanges,
			List<Future<G>> results)
	{
		BatchDataExchangeListener<G> listener = dataExchange.getListener();

		int size = subDataExchanges.size();

		AtomicInteger taskSubmitSuccessCount = new AtomicInteger(0);
		CountDownLatch submitFinishLatch = new CountDownLatch(1);
		AtomicInteger taskFinishCount = new AtomicInteger(0);

		try
		{
			for (int i = 0; i < size; i++)
			{
				G subDataExchange = subDataExchanges.get(i);

				SubDataExchangeFutureTask<G> futureTask = null;
				Throwable submitFailThrowable = null;

				try
				{
					futureTask = buildSubDataExchangeFutureTask(dataExchange, subDataExchange, i,
							taskSubmitSuccessCount, submitFinishLatch, taskFinishCount);
					executorService.submit(futureTask);

					taskSubmitSuccessCount.incrementAndGet();
				}
				catch (Throwable t)
				{
					futureTask = null;
					submitFailThrowable = t;

					LOGGER.error("submit exchange task error", t);
				}

				results.add(futureTask);

				if (listener != null)
				{
					if (submitFailThrowable == null)
						listener.onSubmitSuccess(subDataExchange, i);
					else
						listener.onSubmitFail(subDataExchange, i, submitFailThrowable);
				}
			}
		}
		finally
		{
			submitFinishLatch.countDown();
		}
	}

	/**
	 * 构建{@linkplain SubDataExchangeFutureTask}。
	 * 
	 * @param dataExchange
	 * @param subDataExchange
	 * @param subDataExchangeIndex
	 * @param taskSubmitSuccessCount
	 * @param submitFinishLatch
	 * @param taskFinishCount
	 * @return
	 */
	protected SubDataExchangeFutureTask<G> buildSubDataExchangeFutureTask(T dataExchange, G subDataExchange,
			int subDataExchangeIndex, AtomicInteger taskSubmitSuccessCount, CountDownLatch submitFinishLatch,
			AtomicInteger taskFinishCount)
	{
		SubDataExchangeFutureTask<G> futureTask = new SubDataExchangeFutureTask<G>(this.delegateDataExchangeService,
				subDataExchange, subDataExchangeIndex, taskSubmitSuccessCount, submitFinishLatch, taskFinishCount);

		BatchDataExchangeListener<G> listener = dataExchange.getListener();
		futureTask.setListener(listener);

		return futureTask;
	}

	protected List<G> getSubDataExchanges(T dataExchange)
	{
		return dataExchange.getSubDataExchanges();
	}

	/**
	 * 子数据交换{@linkplain FutureTask}。
	 * 
	 * @author datagear@163.com
	 *
	 * @param <T>
	 */
	protected static class SubDataExchangeFutureTask<T extends DataExchange> extends FutureTask<T>
	{
		private T subDataExchange;

		private int subDataExchangeIndex;

		private BatchDataExchangeListener<T> listener;

		private AtomicInteger taskSubmitSuccessCount;

		private CountDownLatch submitFinishLatch;

		private AtomicInteger taskFinishCount;

		public SubDataExchangeFutureTask(DataExchangeService<? super T> dataExchangeService, T dataExchange,
				int dataExchangeIndex, AtomicInteger taskSubmitSuccessCount, CountDownLatch submitFinishLatch,
				AtomicInteger taskFinishCount)
		{
			super(new SubDataExchangeCallable<T>(dataExchangeService, dataExchange));
			this.subDataExchange = dataExchange;
			this.subDataExchangeIndex = dataExchangeIndex;
			this.taskSubmitSuccessCount = taskSubmitSuccessCount;
			this.submitFinishLatch = submitFinishLatch;
			this.taskFinishCount = taskFinishCount;
		}

		public T getSubDataExchange()
		{
			return subDataExchange;
		}

		public void setSubDataExchange(T subDataExchange)
		{
			this.subDataExchange = subDataExchange;
		}

		public int getSubDataExchangeIndex()
		{
			return subDataExchangeIndex;
		}

		public void setSubDataExchangeIndex(int subDataExchangeIndex)
		{
			this.subDataExchangeIndex = subDataExchangeIndex;
		}

		public BatchDataExchangeListener<T> getListener()
		{
			return listener;
		}

		public void setListener(BatchDataExchangeListener<T> listener)
		{
			this.listener = listener;
		}

		public AtomicInteger getTaskSubmitSuccessCount()
		{
			return taskSubmitSuccessCount;
		}

		public void setTaskSubmitSuccessCount(AtomicInteger taskSubmitSuccessCount)
		{
			this.taskSubmitSuccessCount = taskSubmitSuccessCount;
		}

		public CountDownLatch getSubmitFinishLatch()
		{
			return submitFinishLatch;
		}

		public void setSubmitFinishLatch(CountDownLatch submitFinishLatch)
		{
			this.submitFinishLatch = submitFinishLatch;
		}

		public AtomicInteger getTaskFinishCount()
		{
			return taskFinishCount;
		}

		public void setTaskFinishCount(AtomicInteger taskFinishCount)
		{
			this.taskFinishCount = taskFinishCount;
		}

		@Override
		protected void done()
		{
			int myFinishCount = this.taskFinishCount.incrementAndGet();

			if (this.listener != null)
			{
				if (isCancelled())
					this.listener.onCancel(this.subDataExchange, this.subDataExchangeIndex);

				try
				{
					this.submitFinishLatch.await();
				}
				catch (InterruptedException e)
				{
				}

				if (myFinishCount == this.taskSubmitSuccessCount.get())
					this.listener.onFinish();
			}
		}
	}

	/**
	 * 子数据交换{@linkplain Callback}。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static class SubDataExchangeCallable<T extends DataExchange> implements Callable<T>
	{
		private DataExchangeService<? super T> dataExchangeService;

		private T dataExchange;

		public SubDataExchangeCallable()
		{
			super();
		}

		public SubDataExchangeCallable(DataExchangeService<? super T> dataExchangeService, T dataExchange)
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
