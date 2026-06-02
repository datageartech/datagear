/*
 * Copyright 2018-present datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
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
public interface ChartPlugin extends Identifiable, Labeled, AdditionsAware
{
	String PROPERTY_ID = "id";
	String PROPERTY_NAME_LABEL = Labeled.PROPERTY_NAME_LABEL;
	String PROPERTY_DESC_LABEL = Labeled.PROPERTY_DESC_LABEL;
	String PROPERTY_RESOURCES = "resources";
	String PROPERTY_CONFIG_FORM = "configForm";
	String PROPERTY_DATA_SIGN_SPEC = "dataSignSpec";
	String PROPERTY_DATA_SET_RANGE = "dataSetRange";
	String PROPERTY_VERSION = "version";
	String PROPERTY_ORDER = "order";
	String PROPERTY_CATEGORY_JOINS = "categoryJoins";
	String PROPERTY_AUTHOR = "author";
	String PROPERTY_CONTACT = "contact";
	String PROPERTY_ISSUE_DATE = "issueDate";
	String PROPERTY_ADDITIONS = AdditionsAware.PROPERTY_ADDITIONS;

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
	 * 获取{@linkplain ChartPluginConfigForm}，{@code null}表示没有。
	 * 
	 * @return
	 */
	ChartPluginConfigForm getConfigForm();

	/**
	 * 获取{@linkplain DataSignSpec}。
	 * <p>
	 * 返回{@code null}表示没有。
	 * </p>
	 * 
	 * @return
	 */
	DataSignSpec getDataSignSpec();

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
	 * @return {@code null}表示无类别
	 */
	List<CategoryJoin> getCategoryJoins();

	/**
	 * 获取作者。
	 * <p>
	 * 返回{@code null}或空字符串表示没有。
	 * </p>
	 * 
	 * @return
	 */
	String getAuthor();

	/**
	 * 获取作者联系方式。
	 * <p>
	 * 返回{@code null}或空字符串表示没有。
	 * </p>
	 * 
	 * @return
	 */
	String getContact();

	/**
	 * 获取发布日期。
	 * <p>
	 * 返回{@code null}或空字符串表示没有。
	 * </p>
	 * 
	 * @return
	 */
	String getIssueDate();
}
