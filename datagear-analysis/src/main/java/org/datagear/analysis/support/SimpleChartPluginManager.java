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
 * 简单{@linkplain ChartPluginManager}。
 * <p>
 * 此类不是线程安全的。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class SimpleChartPluginManager extends AbstractChartPluginManager implements ChartPluginManager
{
	private List<ChartPlugin<?>> chartPlugins = new ArrayList<ChartPlugin<?>>();

	private List<Class<? extends RenderContext>> _supportRenderContextTypes = null;

	public SimpleChartPluginManager()
	{
		super();
	}

	public SimpleChartPluginManager(List<ChartPlugin<?>> chartPlugins)
	{
		super();
		this.chartPlugins = chartPlugins;
	}

	public List<ChartPlugin<?>> getChartPlugins()
	{
		return chartPlugins;
	}

	public void setChartPlugins(List<ChartPlugin<?>> chartPlugins)
	{
		this.chartPlugins = chartPlugins;
	}

	@Override
	public void register(ChartPlugin<?> chartPlugin)
	{
		addOrReplace(this.chartPlugins, chartPlugin);

		this._supportRenderContextTypes = null;
	}

	@Override
	public ChartPlugin<?> remove(String id)
	{
		ChartPlugin<?> chartPlugin = removeById(this.chartPlugins, id);

		this._supportRenderContextTypes = null;

		return chartPlugin;
	}

	@Override
	public <T extends RenderContext> ChartPlugin<T> get(String id)
	{
		return getById(this.chartPlugins, id);
	}

	@Override
	public <T extends RenderContext> List<ChartPlugin<T>> getAll(Class<? extends T> renderContextType)
	{
		if (this._supportRenderContextTypes == null)
			this._supportRenderContextTypes = resolveChartPluginRenderContextTypes(this.chartPlugins);

		return getAllByRenderContextType(this.chartPlugins, this._supportRenderContextTypes, renderContextType);
	}

	@Override
	public List<ChartPlugin<?>> getAll()
	{
		List<ChartPlugin<?>> re = new ArrayList<ChartPlugin<?>>(this.chartPlugins);
		sortChartPlugins(re);

		return re;
	}
}
