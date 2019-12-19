/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.datagear.analysis.ChartPlugin;
import org.datagear.analysis.ChartPluginManager;
import org.datagear.analysis.RenderContext;
import org.springframework.core.GenericTypeResolver;

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
	 * <p>
	 * 返回列表已使用{@linkplain #sortChartPlugins(List)}排序。
	 * </p>
	 * 
	 * @param <T>
	 * @param chartPlugins
	 * @param supportTypes
	 * @param renderContextType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected <T extends RenderContext> List<ChartPlugin<T>> getAllByRenderContextType(
			List<? extends ChartPlugin<?>> chartPlugins, List<Class<? extends RenderContext>> supportTypes,
			Class<? extends T> renderContextType)
	{
		List<ChartPlugin<T>> reChartPlugins = new ArrayList<ChartPlugin<T>>();

		for (int i = 0, len = chartPlugins.size(); i < len; i++)
		{
			Class<? extends RenderContext> supportType = supportTypes.get(i);

			if (supportType.isAssignableFrom(renderContextType))
				reChartPlugins.add((ChartPlugin<T>) chartPlugins.get(i));
		}

		sortChartPlugins(reChartPlugins);

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
			Class<? extends RenderContext> renderContextType = resolveChartPluginRenderContextType(
					chartPlugin.getClass());

			renderContextTypes.add(renderContextType);
		}

		return renderContextTypes;
	}

	/**
	 * 解析指定{@linkplain ChartPlugin}类所支持的{@linkplain RenderContext}类型。
	 * 
	 * @param chartPluginType
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Class<? extends RenderContext> resolveChartPluginRenderContextType(
			Class<? extends ChartPlugin> chartPluginType)
	{
		Class<?> renderContextType = GenericTypeResolver.resolveTypeArgument(chartPluginType, ChartPlugin.class);

		if (renderContextType == null)
			renderContextType = RenderContext.class;

		return (Class<? extends RenderContext>) renderContextType;
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

	protected void sortChartPlugins(List<? extends ChartPlugin<?>> chartPlugins)
	{
		sort(chartPlugins);
	}

	/**
	 * 按照{@linkplain ChartPlugin#getOrder()}进行排序，越小越靠前。
	 * 
	 * @param chartPlugins
	 */
	public static void sort(List<? extends ChartPlugin<?>> chartPlugins)
	{
		Collections.sort(chartPlugins, new Comparator<ChartPlugin<?>>()
		{
			@Override
			public int compare(ChartPlugin<?> o1, ChartPlugin<?> o2)
			{
				return Integer.valueOf(o1.getOrder()).compareTo(o2.getOrder());
			}
		});
	}
}
