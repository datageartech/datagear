/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.datagear.analysis.ChartPlugin;
import org.datagear.analysis.ChartPluginManager;
import org.datagear.analysis.RenderContext;

/**
 * 并发{@linkplain ChartPluginManager}。
 * <p>
 * 此类是线程安全的。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ConcurrentChartPluginManager extends AbstractChartPluginManager implements ChartPluginManager
{
	private List<ChartPlugin<?>> chartPlugins = new ArrayList<ChartPlugin<?>>();

	private List<Class<? extends RenderContext>> _supportRenderContextTypes = null;

	private ReentrantReadWriteLock _lock = new ReentrantReadWriteLock();

	public ConcurrentChartPluginManager()
	{
		super();
	}

	public ConcurrentChartPluginManager(List<ChartPlugin<?>> chartPlugins)
	{
		super();
		this.chartPlugins = chartPlugins;
		this._supportRenderContextTypes = resolveChartPluginRenderContextTypes(this.chartPlugins);
	}

	@Override
	public void register(ChartPlugin<?> chartPlugin)
	{
		WriteLock writeLock = this._lock.writeLock();

		try
		{
			writeLock.lock();

			addOrReplace(this.chartPlugins, chartPlugin);

			this._supportRenderContextTypes = resolveChartPluginRenderContextTypes(this.chartPlugins);
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public ChartPlugin<?> remove(String id)
	{
		WriteLock writeLock = this._lock.writeLock();

		try
		{
			ChartPlugin<?> chartPlugin = removeById(this.chartPlugins, id);

			this._supportRenderContextTypes = resolveChartPluginRenderContextTypes(this.chartPlugins);

			return chartPlugin;
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public <T extends RenderContext> ChartPlugin<T> get(String id)
	{
		ReadLock readLock = this._lock.readLock();

		try
		{
			readLock.lock();

			return getById(this.chartPlugins, id);
		}
		finally
		{
			readLock.unlock();
		}
	}

	@Override
	public <T extends RenderContext> List<ChartPlugin<? super T>> getAll(Class<T> renderContextType)
	{
		ReadLock readLock = this._lock.readLock();

		try
		{
			readLock.lock();

			return getAllByRenderContextType(this.chartPlugins, this._supportRenderContextTypes, renderContextType);
		}
		finally
		{
			readLock.unlock();
		}
	}

	@Override
	public List<ChartPlugin<?>> getAll()
	{
		ReadLock readLock = this._lock.readLock();

		try
		{
			readLock.lock();

			return new ArrayList<ChartPlugin<?>>(this.chartPlugins);
		}
		finally
		{
			readLock.unlock();
		}
	}
}
