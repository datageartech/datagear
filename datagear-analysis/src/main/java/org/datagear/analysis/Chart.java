/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */

package org.datagear.analysis;

/**
 * 图表。
 * 
 * @author datagear@163.com
 *
 */
public interface Chart extends Identifiable
{
	/**
	 * 获取渲染此图表的{@linkplain ChartPlugin}。
	 * 
	 * @return
	 */
	ChartPlugin<?> getPlugin();

	/**
	 * 获取{@linkplain RenderContext}。
	 * 
	 * @return
	 */
	RenderContext getRenderContext();

	/**
	 * 获取{@linkplain ChartPropertyValues}。
	 * 
	 * @return
	 */
	ChartPropertyValues getPropertyValues();

	/**
	 * 获取{@linkplain DataSetFactory}。
	 * 
	 * @return
	 */
	DataSetFactory[] getDataSetFactories();
}
