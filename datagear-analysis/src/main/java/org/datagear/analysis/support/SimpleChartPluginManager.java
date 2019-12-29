/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import java.util.List;

import org.datagear.analysis.ChartPlugin;
import org.datagear.analysis.ChartPluginManager;
import org.datagear.analysis.RenderContext;

/**
 * 简单{@linkplain ChartPluginManager}。
 * <p>
 * 此类不是线程安全的。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class SimpleChartPluginManager extends AbstractChartPluginManager
{
	public SimpleChartPluginManager()
	{
		super();
	}

	@Override
	public void register(ChartPlugin<?> chartPlugin)
	{
		registerChartPlugin(chartPlugin);
	}

	@Override
	public ChartPlugin<?>[] remove(String... ids)
	{
		return removeChartPlugins(ids);
	}

	@Override
	public <T extends RenderContext> ChartPlugin<T> get(String id)
	{
		return getChartPlugin(id);
	}

	@Override
	public <T extends RenderContext> List<ChartPlugin<T>> getAll(Class<? extends T> renderContextType)
	{
		return findChartPlugins(renderContextType);
	}

	@Override
	public List<ChartPlugin<?>> getAll()
	{
		return getAllChartPlugins();
	}
}
