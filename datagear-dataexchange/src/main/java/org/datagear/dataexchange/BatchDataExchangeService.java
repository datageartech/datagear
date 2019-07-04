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

			context.submitNexts();

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
		BatchDataExchangeContextImpl context = new BatchDataExchangeContextImpl(subDataExchanges.size(),
				subDataExchanges, this.subDataExchangeService, this.executorService);
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

		private final Object _subCountLock = new Object();
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

		public int incrementSubSubmitSuccessCount()
		{
			return this._subSubmitSuccessCount.incrementAndGet();
		}

		public void addSubSubmitFailCount(int count)
		{
			boolean finish = false;

			synchronized (this._subCountLock)
			{
				this._subSubmitFailCount += count;
				finish = ((this._subSubmitFailCount + this._subFinishCount) == this.subTotal);
			}

			if (finish)
			{
				this._finishCountDownLatch.countDown();

				if (this.listener != null)
					this.listener.onFinish();
			}
		}

		public void addSubFinishCount(int count)
		{
			boolean finish = false;

			synchronized (this._subCountLock)
			{
				this._subFinishCount += count;
				finish = ((this._subSubmitFailCount + this._subFinishCount) == this.subTotal);
			}

			if (finish)
			{
				this._finishCountDownLatch.countDown();

				if (this.listener != null)
					this.listener.onFinish();
			}
		}

		/**
		 * 提交下一批具备执行条件的子数据交换。
		 * <p>
		 * 具备执行条件：无任何依赖，或者，所有依赖都已执行完成
		 * </p>
		 * 
		 * @return
		 */
		public Set<SubDataExchange> submitNexts()
		{
			Set<SubDataExchange> submits = new HashSet<SubDataExchange>();

			synchronized (this._subDataExchangesLock)
			{
				for (SubDataExchange next : this.subDataExchanges)
				{
					boolean canSubmit = false;

					if (!next.hasDependent())
						canSubmit = true;
					else
					{
						canSubmit = true;

						Set<SubDataExchange> dependents = next.getDependents();
						for (SubDataExchange dependent : dependents)
						{
							if (this.subDataExchanges.contains(dependent))
							{
								canSubmit = false;
								break;
							}
						}
					}

					if (canSubmit)
						submits.add(next);
				}

				for (SubDataExchange submit : submits)
					this.subDataExchanges.remove(submit);
			}

			for (SubDataExchange submit : submits)
			{
				SubDataExchangeFutureTask task = buildSubDataExchangeFutureTask(submit);
				submit(task);
			}

			return submits;
		}

		/**
		 * 取消未提交的所有后置的子数据交换。
		 * 
		 * @param subDataExchange
		 * @return
		 */
		public Set<SubDataExchange> cancelNotSubmitDescendants(SubDataExchange subDataExchange)
		{
			Set<SubDataExchange> removeds = new HashSet<SubDataExchange>();

			synchronized (this._subDataExchangesLock)
			{
				removeDescendants(subDataExchange, removeds);
			}

			addSubFinishCount(removeds.size());

			if (this.listener != null)
			{
				for (SubDataExchange removed : removeds)
					this.listener.onCancel(removed);
			}

			return removeds;
		}

		@Override
		public boolean[] cancel(String... subDataExchangeIds)
		{
			boolean[] canceled = new boolean[subDataExchangeIds.length];

			Set<SubDataExchange> removeds = new HashSet<SubDataExchange>();

			synchronized (this._subDataExchangesLock)
			{
				for (int i = 0; i < subDataExchangeIds.length; i++)
				{
					SubDataExchange subDataExchange = removeSubDataExchange(subDataExchangeIds[i]);

					if (subDataExchange == null)
					{
						canceled[i] = false;
						continue;
					}

					canceled[i] = true;
					removeds.add(subDataExchange);
					removeDescendants(subDataExchange, removeds);
				}
			}

			addSubFinishCount(removeds.size());

			for (int i = 0; i < subDataExchangeIds.length; i++)
			{
				if (canceled[i])
					continue;

				for (SubDataExchange removed : removeds)
				{
					if (removed.getId().equals(subDataExchangeIds[i]))
						canceled[i] = true;
				}
			}

			if (this.listener != null)
			{
				for (SubDataExchange removed : removeds)
					this.listener.onCancel(removed);
			}

			return canceled;
		}

		/**
		 * 移除指定ID且未提交的{@linkplain SubDataExchange}。
		 * <p>
		 * 如果未找到，将返回{@code null}。
		 * </p>
		 * 
		 * @param subDataExchangeId
		 * @return
		 */
		protected SubDataExchange removeSubDataExchange(String subDataExchangeId)
		{
			Iterator<SubDataExchange> iterator = this.subDataExchanges.iterator();

			while (iterator.hasNext())
			{
				SubDataExchange next = iterator.next();

				if (next.getId().equals(subDataExchangeId))
				{
					iterator.remove();
					return next;
				}
			}

			return null;
		}

		/**
		 * 递归移除未提交的所有后置子数据交换。
		 * 
		 * @param subDataExchange
		 * @param removeds
		 */
		protected void removeDescendants(SubDataExchange subDataExchange, Set<SubDataExchange> removeds)
		{
			Set<SubDataExchange> myRemoveds = new HashSet<SubDataExchange>();

			Iterator<SubDataExchange> iterator = this.subDataExchanges.iterator();

			while (iterator.hasNext())
			{
				SubDataExchange next = iterator.next();

				if (!next.hasDependent())
					continue;

				if (next.getDependents().contains(subDataExchange))
				{
					myRemoveds.add(next);
					iterator.remove();
				}
			}

			removeds.addAll(myRemoveds);

			for (SubDataExchange myRemoved : myRemoveds)
				removeDescendants(myRemoved, removeds);
		}

		/**
		 * 提交一个子数据交换任务。
		 * 
		 * @param task
		 * @return true 提交成功；false 提交失败
		 */
		protected boolean submit(SubDataExchangeFutureTask task)
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
				addSubSubmitFailCount(1);

				LOGGER.error("submit sub exchange task error", t);
			}

			if (this.listener != null)
			{
				if (submitFailThrowable == null)
					listener.onSubmitSuccess(task.getSubDataExchange());
				else
					listener.onSubmitFail(task.getSubDataExchange(), submitFailThrowable);
			}

			return (submitFailThrowable == null);
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

			int subFinishCount = 1;

			if (isCanceled)
			{
				if (this.listener != null)
					this.listener.onCancel(this.subDataExchange);

				this.batchDataExchangeContext.cancelNotSubmitDescendants(this.subDataExchange);
			}

			this.batchDataExchangeContext.addSubFinishCount(subFinishCount);

			if (!isCanceled)
				this.batchDataExchangeContext.submitNexts();
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
