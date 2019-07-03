/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
public class BatchDataExchangeService<T extends BatchDataExchange> extends AbstractDevotedDataExchangeService<T>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(BatchDataExchangeService.class);

	private DataExchangeService<?> subDataExchangeService;

	private ExecutorService executorService = Executors.newCachedThreadPool();

	public BatchDataExchangeService()
	{
		super();
	}

	public BatchDataExchangeService(DataExchangeService<?> subDataExchangeService)
	{
		super();
		this.subDataExchangeService = subDataExchangeService;
	}

	public DataExchangeService<?> getSubDataExchangeService()
	{
		return subDataExchangeService;
	}

	public void setSubDataExchangeService(DataExchangeService<?> subDataExchangeService)
	{
		this.subDataExchangeService = subDataExchangeService;
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
		BatchDataExchangeListener listener = dataExchange.getListener();

		if (listener != null)
			listener.onStart();

		BatchDataExchangeContextImpl context = null;

		try
		{
			Set<SubDataExchange> subDataExchanges = getSubDataExchanges(dataExchange);
			context = buildBatchDataExchangeContextImpl(dataExchange, subDataExchanges);
			dataExchange.setContext(context);

			context.submitIndependents();

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
			if (listener != null && context.getSubSubmitSuccessCount() == 0)
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

	protected BatchDataExchangeContextImpl buildBatchDataExchangeContextImpl(T dataExchange,
			Set<SubDataExchange> subDataExchanges)
	{
		BatchDataExchangeContextImpl context = new BatchDataExchangeContextImpl(subDataExchanges.size(), subDataExchanges,
				this.subDataExchangeService, this.executorService);
		context.setListener(dataExchange.getListener());

		return context;
	}

	protected Set<SubDataExchange> getSubDataExchanges(T dataExchange) throws DataExchangeException
	{
		return dataExchange.getSubDataExchanges();
	}

	/**
	 * 批处理数据交换上下文。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static class BatchDataExchangeContextImpl implements BatchDataExchangeContext
	{
		private final int subTotal;
		private final Set<SubDataExchange> subDataExchanges = new HashSet<SubDataExchange>();
		private final DataExchangeService<?> subDataExchangeService;
		private final ExecutorService executorService;

		private BatchDataExchangeListener listener;

		private final AtomicInteger _subSubmitSuccessCount = new AtomicInteger(0);

		private int _subSubmitFailCount = 0;
		private int _subFinishCount = 0;

		private final Object _countLock = new Object();
		private final Object _subDataExchangesLock = new Object();

		private final CountDownLatch _finishCountDownLatch = new CountDownLatch(1);

		public BatchDataExchangeContextImpl(int subTotal, Set<SubDataExchange> subDataExchanges,
				DataExchangeService<?> subDataExchangeService, ExecutorService executorService)
		{
			super();
			this.subTotal = subTotal;
			this.subDataExchanges.addAll(subDataExchanges);
			this.subDataExchangeService = subDataExchangeService;
			this.executorService = executorService;
		}

		public int getSubTotal()
		{
			return subTotal;
		}

		public Set<SubDataExchange> getSubDataExchanges()
		{
			return subDataExchanges;
		}

		public DataExchangeService<?> getSubDataExchangeService()
		{
			return subDataExchangeService;
		}

		public ExecutorService getExecutorService()
		{
			return executorService;
		}

		public BatchDataExchangeListener getListener()
		{
			return listener;
		}

		public void setListener(BatchDataExchangeListener listener)
		{
			this.listener = listener;
		}

		@Override
		public void waitForFinish() throws InterruptedException
		{
			this._finishCountDownLatch.await();
		}

		public int getSubSubmitSuccessCount()
		{
			return this._subSubmitSuccessCount.intValue();
		}

		public void incrementSubSubmitSuccessCount()
		{
			this._subSubmitSuccessCount.incrementAndGet();
		}

		public void incrementSubSubmitFailCount()
		{
			boolean finish = false;

			synchronized (this._countLock)
			{
				this._subSubmitFailCount++;

				if ((this._subSubmitFailCount + this._subFinishCount) == this.subTotal)
					finish = true;
			}

			if (finish)
				onBatchFinish();
		}

		public void incrementSubFinishCount()
		{
			boolean finish = false;

			synchronized (this._countLock)
			{
				this._subFinishCount++;

				if ((this._subSubmitFailCount + this._subFinishCount) == this.subTotal)
					finish = true;
			}

			if (finish)
				onBatchFinish();
		}

		/**
		 * 提交没有依赖的子数据交换。
		 */
		public void submitIndependents()
		{
			Set<SubDataExchange> independents = getIndependents();

			for (SubDataExchange subDataExchange : independents)
			{
				SubDataExchangeFutureTask task = buildSubDataExchangeFutureTask(subDataExchange);
				submit(task);
			}
		}

		/**
		 * 提交所有后置子数据交换。
		 * 
		 * @param subDataExchange
		 */
		public void submitSubsequents(SubDataExchange subDataExchange)
		{
			Set<SubDataExchange> subsequents = getSubsequents(subDataExchange);

			for (SubDataExchange subsequent : subsequents)
			{
				SubDataExchangeFutureTask task = buildSubDataExchangeFutureTask(subsequent);
				submit(task);
			}
		}

		/**
		 * 获取没有依赖的子数据交换Set。
		 * 
		 * @return
		 */
		public Set<SubDataExchange> getIndependents()
		{
			Set<SubDataExchange> independents = new HashSet<SubDataExchange>();

			synchronized (this._subDataExchangesLock)
			{
				Iterator<SubDataExchange> iterator = this.subDataExchanges.iterator();

				while (iterator.hasNext())
				{
					SubDataExchange next = iterator.next();

					if (!next.hasDependent())
					{
						independents.add(next);
						iterator.remove();
					}
				}
			}

			return independents;
		}

		/**
		 * 获取后置的子数据交换Set。
		 * 
		 * @param subDataExchange
		 * @return
		 */
		public Set<SubDataExchange> getSubsequents(SubDataExchange subDataExchange)
		{
			Set<SubDataExchange> subsequents = new HashSet<SubDataExchange>();

			synchronized (this._subDataExchangesLock)
			{
				Iterator<SubDataExchange> iterator = this.subDataExchanges.iterator();

				while (iterator.hasNext())
				{
					SubDataExchange next = iterator.next();

					if (!next.hasDependent())
						continue;

					if (next.getDependents().contains(subDataExchange))
					{
						subsequents.add(next);
						iterator.remove();
					}
				}
			}

			return subsequents;
		}

		@Override
		public boolean[] cancel(String... subDataExchangeIds)
		{
			// TODO
			return null;
		}

		protected void onBatchFinish()
		{
			this._finishCountDownLatch.countDown();

			if (this.listener != null)
				this.listener.onFinish();
		}

		protected void submit(SubDataExchangeFutureTask task)
		{
			Throwable submitFailThrowable = null;
			try
			{
				executorService.submit(task);
				incrementSubSubmitSuccessCount();
			}
			catch (Throwable t)
			{
				submitFailThrowable = t;
				incrementSubSubmitFailCount();

				LOGGER.error("submit exchange task error", t);
			}

			if (this.listener != null)
			{
				if (submitFailThrowable == null)
					listener.onSubmitSuccess(task.getSubDataExchange());
				else
					listener.onSubmitFail(task.getSubDataExchange(), submitFailThrowable);
			}
		}

		protected SubDataExchangeFutureTask buildSubDataExchangeFutureTask(SubDataExchange subDataExchange)
		{
			return new SubDataExchangeFutureTask(this, this.subDataExchangeService, subDataExchange);
		}
	}

	/**
	 * 子数据交换{@linkplain FutureTask}。
	 * 
	 * @author datagear@163.com
	 *
	 * @param <T>
	 */
	protected static class SubDataExchangeFutureTask extends FutureTask<SubDataExchange>
	{
		private BatchDataExchangeContextImpl batchDataExchangeContext;

		private SubDataExchange subDataExchange;

		private BatchDataExchangeListener listener;

		public SubDataExchangeFutureTask(BatchDataExchangeContextImpl batchDataExchangeContext,
				DataExchangeService<?> subDataExchangeService, SubDataExchange subDataExchange)
		{
			super(new SubDataExchangeRunnable(subDataExchangeService, subDataExchange.getDataExchange()),
					subDataExchange);
			this.batchDataExchangeContext = batchDataExchangeContext;
			this.subDataExchange = subDataExchange;
		}

		public BatchDataExchangeContextImpl getBatchDataExchangeContext()
		{
			return batchDataExchangeContext;
		}

		public SubDataExchange getSubDataExchange()
		{
			return subDataExchange;
		}

		public BatchDataExchangeListener getListener()
		{
			return listener;
		}

		public void setListener(BatchDataExchangeListener listener)
		{
			this.listener = listener;
		}

		@Override
		protected void done()
		{
			boolean isCanceled = isCancelled();

			if (isCanceled && this.listener != null)
			{
				this.listener.onCancel(this.subDataExchange);

				Set<SubDataExchange> subsequents = this.batchDataExchangeContext.getSubsequents(this.subDataExchange);

				if (subsequents != null)
				{
					for (SubDataExchange subsequent : subsequents)
						this.listener.onCancel(subsequent);
				}
			}

			this.batchDataExchangeContext.incrementSubFinishCount();

			if (!isCanceled)
				this.batchDataExchangeContext.submitSubsequents(this.subDataExchange);
		}
	}

	/**
	 * 子数据交换{@linkplain Callback}。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static class SubDataExchangeRunnable implements Runnable
	{
		private DataExchangeService<?> dataExchangeService;

		private DataExchange dataExchange;

		public SubDataExchangeRunnable()
		{
			super();
		}

		public SubDataExchangeRunnable(DataExchangeService<?> dataExchangeService, DataExchange dataExchange)
		{
			super();
			this.dataExchangeService = dataExchangeService;
			this.dataExchange = dataExchange;
		}

		public DataExchangeService<?> getDataExchangeService()
		{
			return dataExchangeService;
		}

		public void setDataExchangeService(DataExchangeService<?> dataExchangeService)
		{
			this.dataExchangeService = dataExchangeService;
		}

		public DataExchange getDataExchange()
		{
			return dataExchange;
		}

		public void setDataExchange(DataExchange dataExchange)
		{
			this.dataExchange = dataExchange;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void run()
		{
			((DataExchangeService<DataExchange>) this.dataExchangeService).exchange(this.dataExchange);
		}
	}
}
