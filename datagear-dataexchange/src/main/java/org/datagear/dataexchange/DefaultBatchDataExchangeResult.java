/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 默认{@linkplain BatchDataExchangeResult}实现类。
 * 
 * @author datagear@163.com
 *
 */
public class DefaultBatchDataExchangeResult implements BatchDataExchangeResult
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultBatchDataExchangeResult.class);

	private final int subTotal;
	private final DataExchangeService<?> subDataExchangeService;
	private final ExecutorService executorService;
	private BatchDataExchangeListener listener;

	private Set<SubDataExchange> _unsubmits = new HashSet<SubDataExchange>();
	private Set<SubDataExchange> _submitFails = new HashSet<SubDataExchange>();
	private Set<SubDataExchange> _cancelleds = new HashSet<SubDataExchange>();
	private Set<SubDataExchange> _finishes = new HashSet<SubDataExchange>();

	private Set<SubDataExchangeFutureTask> _submitSuccesses = new HashSet<SubDataExchangeFutureTask>();

	private final AtomicBoolean _finishFlag = new AtomicBoolean(false);
	private final CountDownLatch _finishCountDownLatch = new CountDownLatch(1);

	private final Object _subLock = new Object();

	public DefaultBatchDataExchangeResult(Set<SubDataExchange> subDataExchanges,
			DataExchangeService<?> subDataExchangeService, ExecutorService executorService)
	{
		super();
		this.subTotal = subDataExchanges.size();
		this._unsubmits.addAll(subDataExchanges);
		this.subDataExchangeService = subDataExchangeService;
		this.executorService = executorService;
	}

	public int getSubTotal()
	{
		return subTotal;
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

	/**
	 * 批量处理是否已完成。
	 * 
	 * @return
	 */
	@Override
	public boolean isFinish()
	{
		synchronized (this._subLock)
		{
			return (this._submitFails.size() + this._cancelleds.size() + this._finishes.size()) >= this.subTotal;
		}
	}

	@Override
	public Set<SubDataExchange> getSubmitFails()
	{
		synchronized (this._subLock)
		{
			return new HashSet<SubDataExchange>(this._submitFails);
		}
	}

	@Override
	public Set<SubDataExchange> getCancelleds()
	{
		synchronized (this._subLock)
		{
			return new HashSet<SubDataExchange>(this._cancelleds);
		}
	}

	@Override
	public Set<SubDataExchange> getFinishes()
	{
		synchronized (this._subLock)
		{
			return new HashSet<SubDataExchange>(this._finishes);
		}
	}

	/**
	 * 获取提交成功数。
	 * 
	 * @return
	 */
	public int getSubmitSuccessCount()
	{
		synchronized (this._subLock)
		{
			return this._submitSuccesses.size();
		}
	}

	@Override
	public void cancel(String subDataExchangeId)
	{
		SubDataExchangeFutureTask task = null;

		synchronized (this._subLock)
		{
			task = findSubmitSuccess(subDataExchangeId);
		}

		if (task != null)
		{
			task.cancel(false);
		}
		else
		{
			Set<SubDataExchange> cancelleds = new HashSet<SubDataExchange>();

			synchronized (this._subLock)
			{
				SubDataExchange subDataExchange = removeSubDataExchange(this._unsubmits, subDataExchangeId);

				if (subDataExchange != null)
				{
					cancelleds.add(subDataExchange);
					removeDescendants(this._unsubmits, subDataExchange, cancelleds);

					this._cancelleds.addAll(cancelleds);
				}
			}

			if (this.listener != null)
			{
				for (SubDataExchange cancelled : cancelleds)
					this.listener.onCancel(cancelled);
			}

			postProcessIfFinish();
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

		synchronized (this._subLock)
		{
			for (SubDataExchange unsubmit : this._unsubmits)
			{
				boolean canSubmit = false;

				if (!unsubmit.hasDependency())
					canSubmit = true;
				else
				{
					canSubmit = true;

					Set<SubDataExchange> dependencies = unsubmit.getDependencies();
					for (SubDataExchange dependency : dependencies)
					{
						if (!this._finishes.contains(dependency))
						{
							canSubmit = false;
							break;
						}
					}
				}

				if (canSubmit)
					submits.add(unsubmit);
			}

			for (SubDataExchange submit : submits)
				this._unsubmits.remove(submit);
		}

		submitAll(submits);

		return submits;
	}

	/**
	 * 子数据交换任务取消后续处理。
	 * 
	 * @param subDataExchange
	 */
	protected void forCancel(SubDataExchange subDataExchange)
	{
		Set<SubDataExchange> cancelleds = new HashSet<SubDataExchange>();

		synchronized (this._subLock)
		{
			cancelleds.add(subDataExchange);
			removeDescendants(this._unsubmits, subDataExchange, cancelleds);

			this._cancelleds.addAll(cancelleds);
		}

		if (this.listener != null)
		{
			for (SubDataExchange cancelled : cancelleds)
				this.listener.onCancel(cancelled);
		}

		postProcessIfFinish();
	}

	/**
	 * 子数据交换任务完成后续处理。
	 * 
	 * @param subDataExchange
	 */
	protected void forFinish(SubDataExchange subDataExchange)
	{
		synchronized (this._subLock)
		{
			this._finishes.add(subDataExchange);
		}

		postProcessIfFinish();

		submitNexts();
	}

	/**
	 * 批处理完成后置处理。
	 */
	protected void postProcessIfFinish()
	{
		if (isFinish() && this._finishFlag.compareAndSet(false, true))
		{
			this._finishCountDownLatch.countDown();

			if (this.listener != null)
				this.listener.onFinish();
		}
	}

	protected void submitAll(Set<SubDataExchange> subDataExchanges)
	{
		Set<SubDataExchange> submitSuccesses = new HashSet<SubDataExchange>();
		Set<SubDataExchange> submitFails = new HashSet<SubDataExchange>();

		synchronized (this._subLock)
		{
			for (SubDataExchange subDataExchange : subDataExchanges)
			{
				SubDataExchangeFutureTask task = buildSubDataExchangeFutureTask(subDataExchange);
				boolean success = submit(task);

				if (success)
				{
					submitSuccesses.add(subDataExchange);
					this._submitSuccesses.add(task);
				}
				else
				{
					submitFails.add(subDataExchange);
					removeDescendants(this._unsubmits, subDataExchange, submitFails);
				}
			}

			this._submitFails.addAll(submitFails);
		}

		if (this.listener != null)
		{
			for (SubDataExchange sub : submitSuccesses)
				this.listener.onSubmitSuccess(sub);

			for (SubDataExchange sub : submitFails)
				this.listener.onSubmitFail(sub);
		}
	}

	/**
	 * 提交一个子数据交换任务。
	 * 
	 * @param task
	 * @return true 提交成功；false 提交失败
	 */
	protected boolean submit(SubDataExchangeFutureTask task)
	{
		try
		{
			this.executorService.submit(task);
			return true;
		}
		catch (Throwable t)
		{
			LOGGER.error("submit sub exchange task error", t);
			return false;
		}
	}

	protected SubDataExchangeFutureTask buildSubDataExchangeFutureTask(SubDataExchange subDataExchange)
	{
		SubDataExchangeFutureTask subDataExchangeFutureTask = new SubDataExchangeFutureTask(subDataExchange);
		return subDataExchangeFutureTask;
	}

	/**
	 * 查找已提交的{@linkplain SubDataExchangeFutureTask}。
	 * <p>
	 * 如果未找到，将返回{@code null}。
	 * </p>
	 * 
	 * @param subDataExchangeId
	 * @return
	 */
	protected SubDataExchangeFutureTask findSubmitSuccess(String subDataExchangeId)
	{
		for (SubDataExchangeFutureTask subDataExchangeFutureTask : this._submitSuccesses)
		{
			if (subDataExchangeFutureTask.getSubDataExchange().getId().equals(subDataExchangeId))
				return subDataExchangeFutureTask;
		}

		return null;
	}

	/**
	 * 查找指定ID的{@linkplain SubDataExchange}。
	 * <p>
	 * 如果未找到，将返回{@code null}。
	 * </p>
	 * 
	 * @param subDataExchanges
	 * @param subDataExchangeId
	 * @return
	 */
	protected SubDataExchange findSubDataExchange(Set<SubDataExchange> subDataExchanges, String subDataExchangeId)
	{
		for (SubDataExchange subDataExchange : subDataExchanges)
		{
			if (subDataExchange.getId().equals(subDataExchangeId))
				return subDataExchange;
		}

		return null;
	}

	/**
	 * 移除指定ID且未提交的{@linkplain SubDataExchange}。
	 * <p>
	 * 如果未找到，将返回{@code null}。
	 * </p>
	 * 
	 * @param subDataExchanges
	 * @param subDataExchangeId
	 * @return
	 */
	protected SubDataExchange removeSubDataExchange(Set<SubDataExchange> subDataExchanges, String subDataExchangeId)
	{
		Iterator<SubDataExchange> iterator = subDataExchanges.iterator();

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
	 * 移除所有后置子数据交换。
	 * 
	 * @param subDataExchanges
	 * @param subDataExchange
	 * @param removeds
	 */
	protected void removeDescendants(Set<SubDataExchange> subDataExchanges, SubDataExchange subDataExchange,
			Set<SubDataExchange> removeds)
	{
		Set<SubDataExchange> myRemoveds = new HashSet<SubDataExchange>();

		Iterator<SubDataExchange> iterator = subDataExchanges.iterator();

		while (iterator.hasNext())
		{
			SubDataExchange next = iterator.next();

			if (!next.hasDependency())
				continue;

			if (next.getDependencies().contains(subDataExchange))
			{
				myRemoveds.add(next);
				iterator.remove();
			}
		}

		removeds.addAll(myRemoveds);

		for (SubDataExchange myRemoved : myRemoveds)
			removeDescendants(subDataExchanges, myRemoved, removeds);
	}

	/**
	 * 子数据交换{@linkplain FutureTask}。
	 * 
	 * @author datagear@163.com
	 *
	 * @param <T>
	 */
	protected class SubDataExchangeFutureTask extends FutureTask<SubDataExchange>
	{
		private final SubDataExchange subDataExchange;

		private AtomicBoolean _run = new AtomicBoolean(false);

		public SubDataExchangeFutureTask(SubDataExchange subDataExchange)
		{
			super(new SubDataExchangeRunnable(subDataExchange), subDataExchange);
			this.subDataExchange = subDataExchange;
		}

		public SubDataExchange getSubDataExchange()
		{
			return subDataExchange;
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning)
		{
			// 执行中的不允许取消
			return super.cancel(false);
		}

		@Override
		public void run()
		{
			_run.set(true);
			super.run();
		}

		@Override
		protected void done()
		{
			boolean run = this._run.get();

			// XXX 执行中的任务，调用cancel后，isCancelled()仍会是true！！
			boolean isCanceled = (isCancelled() && !run);

			if (isCanceled)
				DefaultBatchDataExchangeResult.this.forCancel(this.subDataExchange);
		}
	}

	/**
	 * 子数据交换{@linkplain Callback}。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected class SubDataExchangeRunnable implements Runnable
	{
		private final SubDataExchange subDataExchange;

		public SubDataExchangeRunnable(SubDataExchange subDataExchange)
		{
			super();
			this.subDataExchange = subDataExchange;
		}

		public SubDataExchange getSubDataExchange()
		{
			return subDataExchange;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void run()
		{
			try
			{
				((DataExchangeService<DataExchange>) DefaultBatchDataExchangeResult.this.subDataExchangeService)
						.exchange(this.subDataExchange.getDataExchange());
			}
			finally
			{
				DefaultBatchDataExchangeResult.this.forFinish(this.subDataExchange);
			}
		}
	}
}
