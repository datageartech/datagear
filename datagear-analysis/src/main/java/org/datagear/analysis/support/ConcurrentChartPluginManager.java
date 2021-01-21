/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
	public void register(ChartPlugin chartPlugin)
	{
		WriteLock writeLock = this.lock.writeLock();

		try
		{
			writeLock.lock();

			registerChartPlugin(chartPlugin);
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
