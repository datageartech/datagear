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
	public void register(ChartPlugin chartPlugin)
	{
		registerChartPlugin(chartPlugin);
	}

	@Override
	public ChartPlugin[] remove(String... ids)
	{
		return removeChartPlugins(ids);
	}

	@Override
	public ChartPlugin get(String id)
	{
		return getChartPlugin(id);
	}

	@Override
	public <T extends ChartPlugin> List<T> getAll(Class<? super T> chartPluginType)
	{
		return findChartPlugins(chartPluginType);
	}

	@Override
	public List<ChartPlugin> getAll()
	{
		return getAllChartPlugins();
	}
}
