/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.analysis;

import java.util.List;

/**
 * {@linkplain ChartPlugin}管理器。
 * 
 * @author datagear@163.com
 *
 */
public interface ChartPluginManager
{
	/**
	 * 注册一个{@linkplain ChartPlugin}。
	 * 
	 * @param chartPlugin
	 */
	void register(ChartPlugin chartPlugin);

	/**
	 * 移除指定名称的{@linkplain ChartPlugin}。
	 * 
	 * @param name
	 * @return 被移除的{@linkplain ChartPlugin}或者{@code null}。
	 */
	ChartPlugin remove(String name);

	/**
	 * 获取指定名称的{@linkplain ChartPlugin}。
	 * 
	 * @param name
	 * @return
	 */
	ChartPlugin get(String name);

	/**
	 * 获取支持指定类型{@linkplain ChartRenderContext}的所有{@linkplain ChartPlugin}。
	 * 
	 * @param chartRenderContextType
	 * @return
	 */
	List<? extends ChartPlugin> getAll(Class<? extends ChartRenderContext> chartRenderContextType);

	/**
	 * 获取所有{@linkplain ChartPlugin}。
	 * 
	 * @return
	 */
	List<? extends ChartPlugin> getAll();
}
