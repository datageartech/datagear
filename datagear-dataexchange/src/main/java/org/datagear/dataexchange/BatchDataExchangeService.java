/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 批量数据交换服务。
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public class BatchDataExchangeService<T extends BatchDataExchange> extends AbstractDevotedDataExchangeService<T>
{
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

		DefaultBatchDataExchangeContext context = null;

		try
		{
			Set<SubDataExchange> subDataExchanges = getSubDataExchanges(dataExchange);

			checkCycleDependency(subDataExchanges);

			context = buildDefaultBatchDataExchangeContext(dataExchange, subDataExchanges);
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
			if (listener != null && (context == null || context.getSubmitSuccessCount() == 0))
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

	protected DefaultBatchDataExchangeContext buildDefaultBatchDataExchangeContext(T dataExchange,
			Set<SubDataExchange> subDataExchanges)
	{
		DefaultBatchDataExchangeContext context = new DefaultBatchDataExchangeContext(subDataExchanges,
				this.subDataExchangeService, this.executorService);
		context.setListener(dataExchange.getListener());

		return context;
	}

	protected Set<SubDataExchange> getSubDataExchanges(T dataExchange) throws DataExchangeException
	{
		return dataExchange.getSubDataExchanges();
	}

	/**
	 * 检查循环依赖。
	 * 
	 * @param subDataExchanges
	 * @throws CycleDependencyException
	 */
	protected void checkCycleDependency(Set<SubDataExchange> subDataExchanges) throws CycleDependencyException
	{
		int maxDepth = subDataExchanges.size();

		for (SubDataExchange subDataExchange : subDataExchanges)
		{
			if (isCycleDependency(subDataExchange, maxDepth))
				throw new CycleDependencyException(subDataExchange);
		}
	}

	protected boolean isCycleDependency(SubDataExchange subDataExchange, int maxDepth)
	{
		Set<SubDataExchange> dependencies = subDataExchange.getDependencies();

		return isCycleDependency(subDataExchange, dependencies, maxDepth);
	}

	protected boolean isCycleDependency(SubDataExchange subDataExchange, Set<SubDataExchange> dependencies,
			int maxDepth)
	{
		if (maxDepth <= 0 || dependencies == null || dependencies.isEmpty())
			return false;

		for (SubDataExchange dependency : dependencies)
		{
			if (subDataExchange == dependency)
				return true;

			if (isCycleDependency(subDataExchange, dependency.getDependencies(), maxDepth - 1))
				return true;
		}

		return false;
	}
}
