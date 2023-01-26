/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis;

import java.util.List;

import org.datagear.util.i18n.Label;
import org.datagear.util.i18n.Labeled;

/**
 * 图表插件。
 * 
 * @author datagear@163.com
 *
 */
public interface ChartPlugin extends Identifiable, Labeled
{
	String PROPERTY_ID = "id";
	String PROPERTY_NAME_LABEL = Labeled.PROPERTY_NAME_LABEL;
	String PROPERTY_DESC_LABEL = Labeled.PROPERTY_DESC_LABEL;
	String PROPERTY_RESOURCES = "resources";
	String PROPERTY_ATTRIBUTES = "attributes";
	String PROPERTY_DATA_SIGNS = "dataSigns";
	String PROPERTY_DATA_SET_RANGE = "dataSetRange";
	String PROPERTY_VERSION = "version";
	String PROPERTY_ORDER = "order";
	String PROPERTY_CATEGORIES = "categories";
	String PROPERTY_CATEGORY_ORDERS = "categoryOrders";

	/** 默认图标主题名 */
	String DEFAULT_ICON_THEME_NAME = "default";

	/**
	 * 获取名称标签。
	 * <p>
	 * 此方法不应返回{@code null}。
	 * </p>
	 * 
	 * @return
	 */
	@Override
	Label getNameLabel();

	/**
	 * 获取此插件的所有资源。
	 * <p>
	 * 返回{@code null}或空表示没有任何资源。
	 * </p>
	 * 
	 * @return
	 */
	List<ChartPluginResource> getResources();
	
	/**
	 * 获取此插件指定名称的资源。
	 * 
	 * @param name
	 * @return 返回{@code null}表示没有
	 */
	ChartPluginResource getResource(String name);
	
	/**
	 * 获取最匹配指定主题名称的图标资源名，用于通过{@linkplain #getResource(String)}获取图标资源。
	 * 
	 * @param themeName
	 * @return 返回{@code null}或空表示没有图标
	 */
	String getIconResourceName(String themeName);

	/**
	 * 获取{@linkplain ChartPluginAttribute}列表。
	 * <p>
	 * 返回{@code null}表示没有。
	 * </p>
	 * 
	 * @return
	 */
	List<ChartPluginAttribute> getAttributes();

	/**
	 * 获取指定名称的{@linkplain ChartPluginAttribute}，没有找到则返回{@code null}。
	 * 
	 * @param name
	 * @return
	 */
	ChartPluginAttribute getAttribute(String name);

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
	 * 获取{@linkplain ChartPluginDataSetRange}。
	 * 
	 * @return {@code null}表示无约束
	 */
	ChartPluginDataSetRange getDataSetRange();

	/**
	 * 渲染{@linkplain Chart}。
	 * 
	 * @param chartDefinition
	 * @param renderContext
	 * @return
	 * @throws RenderException
	 */
	Chart renderChart(ChartDefinition chartDefinition, RenderContext renderContext) throws RenderException;

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

	/**
	 * 获取所属类别。
	 * 
	 * @return 返回{@code null}表示无类别
	 */
	List<Category> getCategories();

	/**
	 * 获取上述所属类别中的排序值。
	 * <p>
	 * 如果没有{@linkplain #getCategories()}中对应索引的排序值，则取{@linkplain #getOrder()}。
	 * </p>
	 * <p>
	 * 返回{@code null}或空列表表示无排序。
	 * </p>
	 * 
	 * @return
	 */
	List<Integer> getCategoryOrders();
}
