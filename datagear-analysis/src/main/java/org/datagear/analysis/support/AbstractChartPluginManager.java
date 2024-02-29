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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.ChartPlugin;
import org.datagear.analysis.ChartPluginManager;
import org.datagear.util.StringUtil;
import org.datagear.util.version.Version;

/**
 * 抽象{@linkplain ChartPluginManager}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractChartPluginManager implements ChartPluginManager
{
	private Map<String, ChartPlugin> chartPluginMap = new HashMap<>();

	public AbstractChartPluginManager()
	{
		super();
	}

	protected Map<String, ChartPlugin> getChartPluginMap()
	{
		return chartPluginMap;
	}

	protected void setChartPluginMap(Map<String, ChartPlugin> chartPluginMap)
	{
		this.chartPluginMap = chartPluginMap;
	}

	@Override
	public boolean isRegisterable(ChartPlugin chartPlugin)
	{
		if (!isLegalChartPlugin(chartPlugin))
			return false;

		ChartPlugin old = this.chartPluginMap.get(chartPlugin.getId());
		boolean canPut = canReplaceForSameId(chartPlugin, old);

		return canPut;
	}

	/**
	 * 注册一个{@linkplain ChartPlugin}。
	 * <p>
	 * 如果已存在一个更高版本的，则注册失败，返回{@code false}。
	 * </p>
	 * 
	 * @param chartPlugin
	 * @return
	 */
	protected boolean registerChartPlugin(ChartPlugin chartPlugin)
	{
		checkLegalChartPlugin(chartPlugin);

		ChartPlugin old = this.chartPluginMap.get(chartPlugin.getId());
		boolean canPut = canReplaceForSameId(chartPlugin, old);

		if (canPut)
			this.chartPluginMap.put(chartPlugin.getId(), chartPlugin);

		return canPut;
	}

	/**
	 * 移除{@linkplain ChartPlugin}。
	 * 
	 * @param id
	 */
	protected ChartPlugin[] removeChartPlugins(String[] ids)
	{
		ChartPlugin[] removed = new ChartPlugin[ids.length];

		for (int i = 0; i < ids.length; i++)
			removed[i] = removeChartPlugin(ids[i]);

		return removed;
	}

	/**
	 * 移除{@linkplain ChartPlugin}。
	 * 
	 * @param id
	 */
	protected ChartPlugin removeChartPlugin(String id)
	{
		return this.chartPluginMap.remove(id);
	}

	/**
	 * 移除所有{@linkplain ChartPlugin}。
	 */
	protected void removeAllChartPlugins()
	{
		this.chartPluginMap.clear();
	}

	/**
	 * 获取指定ID的{@linkplain ChartPlugin}。
	 * 
	 * @param id
	 * @return
	 */
	protected ChartPlugin getChartPlugin(String id)
	{
		return this.chartPluginMap.get(id);
	}

	/**
	 * 查找指定类型的所有{@linkplain ChartPlugin}。
	 * <p>
	 * 返回列表已使用{@linkplain #sortChartPlugins(List)}排序。
	 * </p>
	 * 
	 * @param chartPluginType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected <T extends ChartPlugin> List<T> findChartPlugins(Class<? super T> chartPluginType)
	{
		List<T> reChartPlugins = new ArrayList<>();

		for (Map.Entry<String, ChartPlugin> entry : this.chartPluginMap.entrySet())
		{
			ChartPlugin plugin = entry.getValue();

			if (chartPluginType.isAssignableFrom(plugin.getClass()))
				reChartPlugins.add((T) plugin);
		}

		sortChartPlugins(reChartPlugins);

		return reChartPlugins;
	}

	/**
	 * 获取所有{@linkplain ChartPlugin}。
	 * <p>
	 * 返回列表已使用{@linkplain #sortChartPlugins(List)}排序。
	 * </p>
	 * 
	 * @return
	 */
	protected List<ChartPlugin> getAllChartPlugins()
	{
		List<ChartPlugin> reChartPlugins = new ArrayList<>();

		reChartPlugins.addAll(this.chartPluginMap.values());

		sortChartPlugins(reChartPlugins);

		return reChartPlugins;
	}

	protected void sortChartPlugins(List<? extends ChartPlugin> chartPlugins)
	{
		sort(chartPlugins);
	}

	/**
	 * 按照{@linkplain ChartPlugin#getOrder()}进行排序，越小越靠前。
	 * 
	 * @param chartPlugins
	 */
	public static void sort(List<? extends ChartPlugin> chartPlugins)
	{
		Collections.sort(chartPlugins, new Comparator<ChartPlugin>()
		{
			@Override
			public int compare(ChartPlugin o1, ChartPlugin o2)
			{
				return Integer.valueOf(o1.getOrder()).compareTo(o2.getOrder());
			}
		});
	}

	/**
	 * {@code my}是否可替换{@code old}。
	 * 
	 * @param my
	 * @param old
	 *            允许为{@code null}
	 * @return
	 */
	protected boolean canReplaceForSameId(ChartPlugin my, ChartPlugin old)
	{
		if (old == null)
			return true;

		boolean replace = false;

		Version myVersion = null;
		Version oldVersion = null;

		if (!StringUtil.isEmpty(my.getVersion()))
		{
			try
			{
				myVersion = Version.valueOf(my.getVersion());
			}
			catch (Exception e)
			{
			}
		}

		if (!StringUtil.isEmpty(old.getVersion()))
		{
			try
			{
				oldVersion = Version.valueOf(old.getVersion());
			}
			catch (Exception e)
			{
			}
		}

		replace = canReplaceForSameId(my, myVersion, old, oldVersion);

		return replace;
	}

	/**
	 * {@code my}是否可替换{@code old}。
	 * 
	 * @param my
	 * @param myVersion
	 * @param old
	 * @param oldVersion
	 * @return
	 */
	protected boolean canReplaceForSameId(ChartPlugin my, Version myVersion, ChartPlugin old, Version oldVersion)
	{
		boolean replace = false;

		// 没定义版本号的总是替换，便于调试和添加覆盖自定义插件
		if (StringUtil.isEmpty(oldVersion) && StringUtil.isEmpty(myVersion))
			replace = true;
		else if (StringUtil.isEmpty(oldVersion))
			replace = true;
		else if (StringUtil.isEmpty(myVersion))
			replace = false;
		else
			replace = myVersion.isHigherThan(oldVersion);

		return replace;
	}

	/**
	 * 检查{@linkplain ChartPlugin}是否合法，如果不合法，将抛出{@linkplain IllegalArgumentException}。
	 * 
	 * @param chartPlugin
	 * @throws IllegalArgumentException
	 */
	protected void checkLegalChartPlugin(ChartPlugin chartPlugin) throws IllegalArgumentException
	{
		if (!isLegalChartPlugin(chartPlugin))
			throw new IllegalArgumentException("[" + chartPlugin + "] is illegal");
	}

	/**
	 * 是否合法的{@linkplain ChartPlugin}。
	 * 
	 * @param chartPlugin
	 * @return
	 */
	protected boolean isLegalChartPlugin(ChartPlugin chartPlugin)
	{
		if (chartPlugin == null)
			return false;

		if (StringUtil.isEmpty(chartPlugin.getId()))
			return false;

		if (chartPlugin.getNameLabel() == null)
			return false;

		return true;
	}
}
