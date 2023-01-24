/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis;

/**
 * 看板部件。
 * <p>
 * 它可在{@linkplain RenderContext}中渲染自己所描述的{@linkplain Dashboard}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public interface DashboardWidget extends Identifiable
{
	/**
	 * 渲染{@linkplain Dashboard}。
	 * <p>
	 * 每次渲染的{@linkplain Dashboard#getId()}都应全局唯一，{@linkplain Dashboard#getCharts()}中每个{@linkplain Chart#getId()}应局部唯一。
	 * </p>
	 * 
	 * @param renderContext
	 * @return
	 * @throws RenderException
	 */
	Dashboard render(RenderContext renderContext) throws RenderException;
}
