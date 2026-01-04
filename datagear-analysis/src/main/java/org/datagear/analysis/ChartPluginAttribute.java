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
 * 一个插件属性描述插件属性值根对象的一个属性值，
 * 目前仅包括{@linkplain ChartPluginGroupAttribute}、{@linkplain ChartPluginInputAttribute}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public interface ChartPluginAttribute extends Labeled, NameAware, AdditionsAware, Serializable
{
	String PROPERTY_NAME = "name";
	String PROPERTY_NAME_LABEL = Labeled.PROPERTY_NAME_LABEL;
	String PROPERTY_DESC_LABEL = Labeled.PROPERTY_DESC_LABEL;
	String PROPERTY_REQUIRED = "required";
	String PROPERTY_ARRAY = "array";
	String PROPERTY_ADDITIONS = AdditionsAware.PROPERTY_ADDITIONS;

	@Override
	String getName();

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
	static List<ChartPluginAttribute> toLocale(Collection<ChartPluginAttribute> attributes, Locale locale)
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
}
