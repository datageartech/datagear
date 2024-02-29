/*
 * Copyright 2018-2024 datagear.tech
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

package org.datagear.analysis.support;

import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.datagear.analysis.ChartPlugin;
import org.datagear.analysis.ChartPluginManager;

/**
 * 并发{@linkplain ChartPluginManager}。
 * <p>
 * 此类是线程安全的。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ConcurrentChartPluginManager extends AbstractChartPluginManager
{
	protected ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	public ConcurrentChartPluginManager()
	{
		super();
	}

	@Override
	public boolean register(ChartPlugin chartPlugin)
	{
		WriteLock writeLock = this.lock.writeLock();

		try
		{
			writeLock.lock();
			return registerChartPlugin(chartPlugin);
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public ChartPlugin[] remove(String... ids)
	{
		WriteLock writeLock = this.lock.writeLock();

		try
		{
			writeLock.lock();

			return removeChartPlugins(ids);
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public ChartPlugin get(String id)
	{
		ReadLock readLock = this.lock.readLock();

		try
		{
			readLock.lock();

			return getChartPlugin(id);
		}
		finally
		{
			readLock.unlock();
		}
	}

	@Override
	public <T extends ChartPlugin> List<T> getAll(Class<? super T> chartPluginType)
	{
		ReadLock readLock = this.lock.readLock();

		try
		{
			readLock.lock();

			return findChartPlugins(chartPluginType);
		}
		finally
		{
			readLock.unlock();
		}
	}

	@Override
	public List<ChartPlugin> getAll()
	{
		ReadLock readLock = this.lock.readLock();

		try
		{
			readLock.lock();

			return getAllChartPlugins();
		}
		finally
		{
			readLock.unlock();
		}
	}
}
