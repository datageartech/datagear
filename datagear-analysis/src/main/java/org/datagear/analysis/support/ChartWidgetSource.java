/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support;

/**
 * {@linkplain ChartWidget}源。
 * 
 * @author datagear@163.com
 *
 */
public interface ChartWidgetSource
{
	/**
	 * 获取指定ID的{@linkplain ChartWidget}，没有则返回{@code null}。
	 * 
	 * @param id
	 * @return
	 * @throws Throwable
	 */
	ChartWidget getChartWidget(String id) throws Throwable;
}
