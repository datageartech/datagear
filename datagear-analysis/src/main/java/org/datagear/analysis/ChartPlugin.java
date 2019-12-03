/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis;

import java.util.List;

import org.datagear.util.i18n.Label;

/**
 * 图表插件。
 * 
 * @author datagear@163.com
 *
 */
public interface ChartPlugin
{
	/**
	 * 获取名称。
	 * 
	 * @return
	 */
	String getName();

	/**
	 * 获取名称标签。
	 * <p>
	 * 返回{@code null}表示无名称标签。
	 * </p>
	 * 
	 * @return
	 */
	Label getNameLabel();

	/**
	 * 获取描述标签。
	 * <p>
	 * 返回{@code null}表示无描述标签。
	 * </p>
	 * 
	 * @return
	 */
	Label getDescLabel();

	/**
	 * 获取使用指南标签。
	 * <p>
	 * 返回{@code null}表示无使用标签。
	 * </p>
	 * 
	 * @return
	 */
	Label getManualLabel();

	/**
	 * 获取图标。
	 * <p>
	 * 返回{@code null}表示无图标。
	 * </p>
	 * 
	 * @param theme
	 * @return
	 */
	Icon getIcon(String theme);

	/**
	 * 获取{@linkplain ChartProperty}列表。
	 * <p>
	 * 返回{@code null}或者空列表表示没有。
	 * </p>
	 * 
	 * @return
	 */
	List<ChartProperty> getChartProperties();

	/**
	 * 是否支持在指定类型{@linkplain ChartRenderContext}中绘制图表。
	 * 
	 * @param chartContextType
	 * @return
	 */
	boolean supports(Class<? extends ChartRenderContext> chartRenderContextType);

	/**
	 * 绘制图表。
	 * 
	 * @param chartRenderContext
	 * @param chartPropertyValues
	 * @param dataSetFactories
	 * @return
	 */
	Chart renderChart(ChartRenderContext chartRenderContext, ChartPropertyValues chartPropertyValues,
			DataSetFactory... dataSetFactories);
}
