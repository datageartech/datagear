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
			if (listener != null && context.getSubmitSuccessCount() == 0)
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
}
