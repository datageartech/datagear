/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.analysis;

import java.util.List;

/**
 * 图表插件管理器。
 * 
 * @author datagear@163.com
 *
 */
public interface ChartPluginManager
{
	<T extends ChartContext> ChartPlugin<T> get(String name);

	<T extends ChartContext> List<ChartPlugin<T>> find(Class<T> chartContextType);

	List<ChartPlugin<?>> getAll();

	void register(ChartPlugin<?> chartPlugin);
}
