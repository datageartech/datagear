/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis;

import java.util.List;

/**
 * 看板。
 * 
 * @author datagear@163.com
 *
 */
public interface Dashboard extends Identifiable
{
	/**
	 * 获取{@linkplain DashboardWidget}。
	 * 
	 * @return
	 */
	DashboardWidget<?> getWidget();

	/**
	 * 获取{@linkplain RenderContext}。
	 * 
	 * @return
	 */
	RenderContext getRenderContext();

	/**
	 * 获取{@linkplain Chart}列表。
	 * 
	 * @return
	 */
	List<? extends Chart> getCharts();

	/**
	 * 获取指定ID的{@linkplain Chart}。
	 * 
	 * @param id
	 * @return
	 */
	Chart getChart(String id);
}
