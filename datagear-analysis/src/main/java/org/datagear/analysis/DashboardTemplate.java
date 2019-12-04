/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
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
public interface DashboardTemplate<T extends RenderContext> extends Identifiable
{
	/**
	 * 获取看板的{@linkplain ChartTemplate}列表。
	 * 
	 * @return
	 */
	List<ChartTemplate<T>> getChartTemplates();

	/**
	 * 渲染{@linkplain Dashboard}。
	 * 
	 * @param renderContext
	 * @return
	 * @throws RenderException
	 */
	Dashboard render(T renderContext) throws RenderException;
}
