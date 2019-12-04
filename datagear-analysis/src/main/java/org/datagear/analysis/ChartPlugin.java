/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */

package org.datagear.analysis;

import org.datagear.util.i18n.Label;

/**
 * 图表插件。
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public interface ChartPlugin<T extends RenderContext> extends Identifiable
{
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
	 * 获取{@linkplain ChartProperties}。
	 * <p>
	 * 返回{@code null}表示没有。
	 * </p>
	 * 
	 * @return
	 */
	ChartProperties getChartProperties();

	/**
	 * 渲染{@linkplain Chart}。
	 * 
	 * @param renderContext
	 * @param chartConfig
	 * @return
	 * @throws RenderException
	 */
	Chart renderChart(T renderContext, ChartConfig chartConfig) throws RenderException;
}
