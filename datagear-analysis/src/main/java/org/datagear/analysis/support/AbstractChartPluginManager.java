/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import java.util.ArrayList;
import java.util.List;

import org.datagear.analysis.ChartPlugin;
import org.datagear.analysis.ChartPluginManager;
import org.datagear.analysis.RenderContext;

/**
 * 抽象{@linkplain ChartPluginManager}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractChartPluginManager implements ChartPluginManager
{
	public AbstractChartPluginManager()
	{
		super();
	}

	/**
	 * 添加或者替换{@linkplain ChartPlugin}。
	 * 
	 * @param chartPlugins
	 * @param chartPlugin
	 * @return 添加时返回{@code null}，替换时返回旧对象
	 */
	@SuppressWarnings("unchecked")
	protected ChartPlugin<?> addOrReplace(List<? extends ChartPlugin<?>> chartPlugins, ChartPlugin<?> chartPlugin)
	{
		int oldIndex = getIndexById(chartPlugins, chartPlugin.getId());

		if (oldIndex < 0)
		{
			((List<ChartPlugin<?>>) chartPlugin).add(chartPlugin);

			return null;
		}
		else
		{
			ChartPlugin<?> old = chartPlugins.get(oldIndex);
			((List<ChartPlugin<?>>) chartPlugin).set(oldIndex, chartPlugin);

			return old;
		}
	}

	/**
	 * 移除{@linkplain ChartPlugin}。
	 * 
	 * @param chartPlugins
	 * @param id
	 * @return
	 */
	protected ChartPlugin<?> removeById(List<? extends ChartPlugin<?>> chartPlugins, String id)
	{
		int index = getIndexById(chartPlugins, id);

		if (index < 0)
			return null;

		return chartPlugins.remove(index);
	}

	/**
	 * 获取{@linkplain ChartPlugin}，没有则返回{@code null}。
	 * 
	 * @param chartPlugins
	 * @param id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected <T extends RenderContext> ChartPlugin<T> getById(List<? extends ChartPlugin<?>> chartPlugins, String id)
	{
		int index = getIndexById(chartPlugins, id);

		if (index < 0)
			return null;

		return (ChartPlugin<T>) chartPlugins.get(index);
	}

	/**
	 * 获取支持指定类型{@linkplain RenderContext}的所有{@linkplain ChartPlugin}。
	 * 
	 * @param <T>
	 * @param chartPlugins
	 * @param supportTypes
	 * @param renderContextType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected <T extends RenderContext> List<ChartPlugin<? super T>> getAllByRenderContextType(
			List<? extends ChartPlugin<?>> chartPlugins, List<Class<? extends RenderContext>> supportTypes,
			Class<T> renderContextType)
	{
		List<ChartPlugin<? super T>> reChartPlugins = new ArrayList<ChartPlugin<? super T>>();

		for (int i = 0, len = chartPlugins.size(); i < len; i++)
		{
			Class<? extends RenderContext> supportType = supportTypes.get(i);

			if (supportType.isAssignableFrom(renderContextType))
				reChartPlugins.add((ChartPlugin<? super T>) chartPlugins.get(i));
		}

		return reChartPlugins;
	}

	/**
	 * 解析{@linkplain ChartPlugin}可支持的{@linkplain RenderContext}类型列表。
	 * 
	 * @param chartPlugins
	 * @return
	 */
	protected List<Class<? extends RenderContext>> resolveChartPluginRenderContextTypes(
			List<? extends ChartPlugin<?>> chartPlugins)
	{
		List<Class<? extends RenderContext>> renderContextTypes = new ArrayList<Class<? extends RenderContext>>(
				chartPlugins.size());

		for (ChartPlugin<?> chartPlugin : chartPlugins)
		{
			// TODO 解析参数类型
			renderContextTypes.add(RenderContext.class);
		}

		return renderContextTypes;
	}

	/**
	 * 查找指定ID的{@linkplain ChartPlugin}位置，没有则返回{@code -1}。
	 * 
	 * @param chartPlugins
	 * @param id
	 * @return
	 */
	protected int getIndexById(List<? extends ChartPlugin<?>> chartPlugins, String id)
	{
		if (chartPlugins == null)
			return -1;

		for (int i = 0, len = chartPlugins.size(); i < len; i++)
		{
			ChartPlugin<?> chartPlugin = chartPlugins.get(i);

			if (chartPlugin.getId().equals(id))
				return i;
		}

		return -1;
	}
}
