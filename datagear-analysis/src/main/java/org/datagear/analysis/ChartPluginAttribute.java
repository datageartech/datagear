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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.datagear.util.i18n.Labeled;

/**
 * 图表插件属性。
 * <p>
 * 一个插件属性描述由插件绘制图表的{@linkplain ChartDefinition#getAttrValues()}中的一个属性值的UI交互操作元信息，
 * 包括：{@linkplain ChartPluginObjectAttribute}、{@linkplain ChartPluginInputAttribute}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public interface ChartPluginAttribute extends Labeled, NameTypeAware, AdditionsAware, Serializable
{
	String PROPERTY_NAME = "name";
	String PROPERTY_TYPE = "type";
	String PROPERTY_NAME_LABEL = Labeled.PROPERTY_NAME_LABEL;
	String PROPERTY_DESC_LABEL = Labeled.PROPERTY_DESC_LABEL;
	String PROPERTY_REQUIRED = "required";
	String PROPERTY_ARRAY = "array";
	String PROPERTY_ADDITIONS = AdditionsAware.PROPERTY_ADDITIONS;

	/**
	 * 获取名称，不应为空。
	 */
	@Override
	String getName();

	/**
	 * 获取类型，具体类型参考{@linkplain DataType}，不应为{@code null}。
	 * <p>
	 * 当{@linkplain #isArray()}为{@code true}时表示的是数组元素的类型。
	 * </p>
	 */
	@Override
	String getType();

	/**
	 * 是否必填。
	 * 
	 * @return
	 */
	boolean isRequired();

	/**
	 * 是否数组。
	 * 
	 * @return
	 */
	boolean isArray();

	/**
	 * 复制并转换为指定{@linkplain Locale}下的{@linkplain ChartPluginAttribute}。
	 * 
	 * @param locale
	 * @return
	 */
	ChartPluginAttribute toLocale(Locale locale);

	/**
	 * 复制并转换为指定{@linkplain Locale}下的{@linkplain ChartPluginAttribute}列表。
	 * 
	 * @param attributes
	 * @param locale
	 * @return
	 */
	static List<ChartPluginAttribute> toLocale(Collection<? extends ChartPluginAttribute> attributes, Locale locale)
	{
		if (attributes == null)
			return null;

		if (attributes.isEmpty())
			return Collections.emptyList();

		List<ChartPluginAttribute> re = new ArrayList<>(attributes.size());

		for (ChartPluginAttribute attribute : attributes)
		{
			re.add(attribute == null ? null : attribute.toLocale(locale));
		}

		return re;
	}

	/**
	 * {@linkplain ChartPluginAttribute#getType()}枚举。
	 * 
	 * @author datagear@163.com
	 *
	 */
	static class DataType
	{
		/** 字符串 */
		public static final String STRING = "STRING";

		/** 布尔值 */
		public static final String BOOLEAN = "BOOLEAN";

		/** 整数 */
		public static final String INTEGER = "INTEGER";

		/** 数值 */
		public static final String NUMBER = "NUMBER";

		/** 对象 */
		public static final String OBJECT = "OBJECT";
	}
}
