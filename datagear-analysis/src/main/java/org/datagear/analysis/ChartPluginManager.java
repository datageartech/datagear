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
	void register(ChartPlugin<?> chartPlugin);

	/**
	 * 移除指定ID的{@linkplain ChartPlugin}。
	 * 
	 * @param id
	 * @return 被移除的{@linkplain ChartPlugin}或者{@code null}。
	 */
	ChartPlugin<?> remove(String id);

	/**
	 * 获取指定ID的{@linkplain ChartPlugin}。
	 * 
	 * @param id
	 * @return
	 */
	<T extends RenderContext> ChartPlugin<T> get(String id);

	/**
	 * 获取支持指定类型{@linkplain RenderContext}的所有{@linkplain ChartPlugin}。
	 * 
	 * @param renderContextType
	 * @return
	 */
	<T extends RenderContext> List<ChartPlugin<? super T>> getAll(Class<T> renderContextType);

	/**
	 * 获取所有{@linkplain ChartPlugin}。
	 * 
	 * @return
	 */
	List<ChartPlugin<?>> getAll();
}
