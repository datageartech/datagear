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

package org.datagear.analysis.support;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.datagear.analysis.ChartPlugin;
import org.datagear.analysis.ChartPluginManager;
import org.datagear.util.LastModifiedService;

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

	private LastModifiedService lastModifiedService = null;

	private String lastModifiedName = null;

	public ConcurrentChartPluginManager()
	{
		super();
		setLastModifiedName(getClass().getName());
	}

	public LastModifiedService getLastModifiedService()
	{
		return lastModifiedService;
	}

	public void setLastModifiedService(LastModifiedService lastModifiedService)
	{
		this.lastModifiedService = lastModifiedService;
	}

	public String getLastModifiedName()
	{
		return lastModifiedName;
	}

	public void setLastModifiedName(String lastModifiedName)
	{
		this.lastModifiedName = lastModifiedName;
	}

	@Override
	public boolean isRegisterable(ChartPlugin chartPlugin)
	{
		ReadLock readLock = this.lock.readLock();

		try
		{
			readLock.lock();

			return super.isRegisterable(chartPlugin);
		}
		finally
		{
			readLock.unlock();
		}
	}

	@Override
	public boolean register(ChartPlugin chartPlugin)
	{
		WriteLock writeLock = this.lock.writeLock();

		try
		{
			writeLock.lock();

			boolean re = registerChartPlugin(chartPlugin, false);

			if (re)
				updateLastModified();

			return re;
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

			ChartPlugin[] re = removeChartPlugins(ids);
			updateLastModified();

			return re;
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
		return getAll(chartPluginType, null);
	}

	@Override
	public <T extends ChartPlugin> List<T> getAll(Class<? super T> chartPluginType, Comparator<T> comparator)
	{
		ReadLock readLock = this.lock.readLock();

		try
		{
			readLock.lock();

			return findChartPlugins(chartPluginType, comparator);
		}
		finally
		{
			readLock.unlock();
		}
	}

	@Override
	public List<ChartPlugin> getAll()
	{
		return getAll((Comparator<ChartPlugin>) null);
	}

	@Override
	public List<ChartPlugin> getAll(Comparator<ChartPlugin> comparator)
	{
		ReadLock readLock = this.lock.readLock();

		try
		{
			readLock.lock();

			return getAllChartPlugins(comparator);
		}
		finally
		{
			readLock.unlock();
		}
	}

	@Override
	public long getLastModified()
	{
		if (this.lastModifiedService == null || this.lastModifiedName == null)
			return LastModifiedService.LAST_MODIFIED_UNSET;

		return this.lastModifiedService.getLastModified(this.lastModifiedName);
	}

	protected void setLastModified(long lastModified)
	{
		if (this.lastModifiedService == null || this.lastModifiedName == null)
			return;

		this.lastModifiedService.setLastModified(this.lastModifiedName, lastModified);
	}

	protected void updateLastModified()
	{
		setLastModified(System.currentTimeMillis());
	}
}
