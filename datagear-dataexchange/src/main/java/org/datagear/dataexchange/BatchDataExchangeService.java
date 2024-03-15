/*
 * Copyright 2018-present datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
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
	protected void exchange(T dataExchange, DataExchangeContext context) throws Throwable
	{
		Set<SubDataExchange> subDataExchanges = getSubDataExchanges(dataExchange);

		checkCircularDependency(subDataExchanges);

		BatchDataExchangeResult result = createBatchDataExchangeResult(dataExchange, subDataExchanges);

		dataExchange.setResult(result);

		result.submit();
	}

	@Override
	protected void onFinish(T dataExchange, DataExchangeContext context)
	{
		BatchDataExchangeListener listener = dataExchange.getListener();

		if (listener == null)
			return;

		BatchDataExchangeResult result = dataExchange.getResult();

		// 没有任何子任务提交成功，那么需要在这里调用onFinish
		if (result == null || result.getSubmitSuccessCount() == 0)
			listener.onFinish();
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

	protected BatchDataExchangeResult createBatchDataExchangeResult(T dataExchange,
			Set<SubDataExchange> subDataExchanges)
	{
		DefaultBatchDataExchangeResult result = new DefaultBatchDataExchangeResult(subDataExchanges,
				this.subDataExchangeService, this.executorService);
		result.setListener(dataExchange.getListener());

		return result;
	}

	protected Set<SubDataExchange> getSubDataExchanges(T dataExchange) throws DataExchangeException
	{
		return dataExchange.getSubDataExchanges();
	}

	/**
	 * 检查循环依赖。
	 * 
	 * @param subDataExchanges
	 * @throws CircularDependencyException
	 */
	protected void checkCircularDependency(Set<SubDataExchange> subDataExchanges) throws CircularDependencyException
	{
		int maxDepth = subDataExchanges.size();

		for (SubDataExchange subDataExchange : subDataExchanges)
		{
			if (isCircularDependency(subDataExchange, maxDepth))
				throw new CircularDependencyException(subDataExchange);
		}
	}

	protected boolean isCircularDependency(SubDataExchange subDataExchange, int maxDepth)
	{
		Set<SubDataExchange> dependencies = subDataExchange.getDependencies();

		return isCircularDependency(subDataExchange, dependencies, maxDepth);
	}

	protected boolean isCircularDependency(SubDataExchange subDataExchange, Set<SubDataExchange> dependencies,
			int maxDepth)
	{
		if (maxDepth <= 0 || dependencies == null || dependencies.isEmpty())
			return false;

		for (SubDataExchange dependency : dependencies)
		{
			if (subDataExchange == dependency)
				return true;

			if (isCircularDependency(subDataExchange, dependency.getDependencies(), maxDepth - 1))
				return true;
		}

		return false;
	}
}
