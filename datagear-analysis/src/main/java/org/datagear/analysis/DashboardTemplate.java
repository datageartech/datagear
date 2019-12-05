/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */
package org.datagear.analysis;

import java.util.List;

/**
 * 看板模板。
 * <p>
 * 它可在{@linkplain RenderContext}中绘制自己所描述的{@linkplain Dashboard}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public abstract class DashboardTemplate<T extends RenderContext> extends AbstractIdentifiable
{
	private List<ChartTemplate<T>> chartTemplates;

	public DashboardTemplate()
	{
		super();
	}

	public DashboardTemplate(String id, List<ChartTemplate<T>> chartTemplates)
	{
		super(id);
		this.chartTemplates = chartTemplates;
	}

	public List<ChartTemplate<T>> getChartTemplates()
	{
		return chartTemplates;
	}

	public void setChartTemplates(List<ChartTemplate<T>> chartTemplates)
	{
		this.chartTemplates = chartTemplates;
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
