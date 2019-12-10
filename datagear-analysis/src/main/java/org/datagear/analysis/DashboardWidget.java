/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */
package org.datagear.analysis;

import java.util.List;

/**
 * 看板部件。
 * <p>
 * 它可在{@linkplain RenderContext}中渲染自己所描述的{@linkplain Dashboard}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public abstract class DashboardWidget<T extends RenderContext> extends AbstractIdentifiable
{
	private List<ChartWidget<T>> chartWidgets;

	public DashboardWidget()
	{
		super();
	}

	@SuppressWarnings("unchecked")
	public DashboardWidget(String id, List<? extends ChartWidget<T>> chartWidgets)
	{
		super(id);
		this.chartWidgets = (List<ChartWidget<T>>) chartWidgets;
	}

	public List<ChartWidget<T>> getChartWidgets()
	{
		return chartWidgets;
	}

	@SuppressWarnings("unchecked")
	public void setChartWidgets(List<? extends ChartWidget<T>> chartWidgets)
	{
		this.chartWidgets = (List<ChartWidget<T>>) chartWidgets;
	}

	/**
	 * 渲染{@linkplain Dashboard}。
	 * 
	 * @param renderContext
	 * @return
	 * @throws RenderException
	 */
	public abstract Dashboard render(T renderContext) throws RenderException;
}
