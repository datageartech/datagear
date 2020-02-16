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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.ChartPlugin;
import org.datagear.analysis.ChartPluginManager;
import org.datagear.analysis.RenderContext;
import org.datagear.util.StringUtil;
import org.datagear.util.version.Version;
import org.springframework.core.GenericTypeResolver;

/**
 * 抽象{@linkplain ChartPluginManager}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractChartPluginManager implements ChartPluginManager
{
	private Map<String, ChartPlugin<?>> chartPluginMap = new HashMap<String, ChartPlugin<?>>();

	private Map<String, Class<?>> renderContextTypeMap = new HashMap<String, Class<?>>();

	public AbstractChartPluginManager()
	{
		super();
	}

	protected Map<String, ? extends ChartPlugin<?>> getChartPluginMap()
	{
		return chartPluginMap;
	}

	@SuppressWarnings("unchecked")
	protected void setChartPluginMap(Map<String, ? extends ChartPlugin<?>> chartPluginMap)
	{
		this.chartPluginMap = (Map<String, ChartPlugin<?>>) chartPluginMap;
	}

	protected Map<String, Class<?>> getRenderContextTypeMap()
	{
		return renderContextTypeMap;
	}

	protected void setRenderContextTypeMap(Map<String, Class<?>> renderContextTypeMap)
	{
		this.renderContextTypeMap = renderContextTypeMap;
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
	protected boolean registerChartPlugin(ChartPlugin<?> chartPlugin)
	{
		checkLegalChartPlugin(chartPlugin);

		boolean put = true;

		ChartPlugin<?> prev = this.chartPluginMap.get(chartPlugin.getId());
		if (prev != null)
			put = canReplaceForSameId(chartPlugin, prev);

		if (put)
		{
			this.chartPluginMap.put(chartPlugin.getId(), chartPlugin);
			this.renderContextTypeMap.put(chartPlugin.getId(),
					resolveChartPluginRenderContextType(chartPlugin.getClass()));
		}

		return put;
	}

	/**
	 * 移除{@linkplain ChartPlugin}。
	 * 
	 * @param id
	 */
	protected ChartPlugin<?>[] removeChartPlugins(String[] ids)
	{
		ChartPlugin<?>[] removed = new ChartPlugin<?>[ids.length];

		for (int i = 0; i < ids.length; i++)
			removed[i] = removeChartPlugin(ids[i]);

		return removed;
	}

	/**
	 * 移除{@linkplain ChartPlugin}。
	 * 
	 * @param id
	 */
	protected ChartPlugin<?> removeChartPlugin(String id)
	{
		this.renderContextTypeMap.remove(id);
		return this.chartPluginMap.remove(id);
	}

	/**
	 * 移除所有{@linkplain ChartPlugin}。
	 */
	protected void removeAllChartPlugins()
	{
		this.chartPluginMap.clear();
		this.renderContextTypeMap.clear();
	}

	/**
	 * 获取指定ID的{@linkplain ChartPlugin}。
	 * 
	 * @param id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected <T extends RenderContext> ChartPlugin<T> getChartPlugin(String id)
	{
		return (ChartPlugin<T>) this.chartPluginMap.get(id);
	}

	/**
	 * 查找支持指定类型{@linkplain RenderContext}的所有{@linkplain ChartPlugin}。
	 * <p>
	 * 返回列表已使用{@linkplain #sortChartPlugins(List)}排序。
	 * </p>
	 * 
	 * @param renderContextType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected <T extends RenderContext> List<ChartPlugin<T>> findChartPlugins(Class<? extends T> renderContextType)
	{
		List<ChartPlugin<T>> reChartPlugins = new ArrayList<ChartPlugin<T>>();

		for (Map.Entry<String, Class<?>> entry : this.renderContextTypeMap.entrySet())
		{
			if (entry.getValue().isAssignableFrom(renderContextType))
				reChartPlugins.add((ChartPlugin<T>) this.chartPluginMap.get(entry.getKey()));
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
	protected List<ChartPlugin<?>> getAllChartPlugins()
	{
		List<ChartPlugin<?>> reChartPlugins = new ArrayList<ChartPlugin<?>>();

		reChartPlugins.addAll(this.chartPluginMap.values());

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

	/**
	 * {@code my}是否可替换{@code old}。
	 * 
	 * @param my
	 * @param old 允许为{@code null}
	 * @return
	 */
	protected boolean canReplaceForSameId(ChartPlugin<?> my, ChartPlugin<?> old)
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

		// 没定义版本号的总是替换，便于自定义插件的替换调试
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
	protected void checkLegalChartPlugin(ChartPlugin<?> chartPlugin) throws IllegalArgumentException
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
	protected boolean isLegalChartPlugin(ChartPlugin<?> chartPlugin)
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
