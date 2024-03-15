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
	public boolean register(ChartPlugin chartPlugin)
	{
		return registerChartPlugin(chartPlugin);
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
