/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */

package org.datagear.analysis;

import java.util.List;
import java.util.Map;

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
	 * 获取所有风格图标。
	 * <p>
	 * 返回{@code null}或空表示无图标。
	 * </p>
	 * 
	 * @return
	 */
	Map<RenderStyle, Icon> getIcons();

	/**
	 * 获取指定风格的图标，没有找到则返回{@code null}。
	 * 
	 * @param renderStyle
	 * @return
	 */
	Icon getIcon(RenderStyle renderStyle);

	/**
	 * 获取{@linkplain ChartProperty}列表。
	 * <p>
	 * 返回{@code null}表示没有。
	 * </p>
	 * 
	 * @return
	 */
	List<ChartProperty> getChartProperties();

	/**
	 * 获取指定名称的{@linkplain ChartProperty}，没有找到则返回{@code null}。
	 * 
	 * @param name
	 * @return
	 */
	ChartProperty getChartProperty(String name);

	/**
	 * 获取{@linkplain DataSign}列表。
	 * <p>
	 * 返回{@code null}表示没有。
	 * </p>
	 * 
	 * @return
	 */
	List<DataSign> getDataSigns();

	/**
	 * 获取指定名称的{@linkplain DataSign}，没有找到则返回{@code null}。
	 * 
	 * @param name
	 * @return
	 */
	DataSign getDataSign(String name);

	/**
	 * 渲染{@linkplain Chart}。
	 * 
	 * @param renderContext
	 * @param chartPropertyValues
	 * @param chartDataSets
	 * @return
	 * @throws RenderException
	 */
	Chart renderChart(T renderContext, Map<String, ?> chartPropertyValues, ChartDataSet... chartDataSets)
			throws RenderException;

	/**
	 * 获取版本号。
	 * <p>
	 * 版本号格式应为：<code>[主版本号].[次版本号].[修订版本号]</code>。
	 * </p>
	 * <p>
	 * 返回{@code null}或空字符串表示无版本号标识。
	 * </p>
	 * 
	 * @return
	 */
	String getVersion();

	/**
	 * 获取排序值。
	 * <p>
	 * {@linkplain ChartPluginManager#getAll()}、和{@linkplain ChartPluginManager#getAll(Class)}使用此值进行排序，越小越靠前。
	 * </p>
	 * 
	 * @return
	 */
	int getOrder();
}
