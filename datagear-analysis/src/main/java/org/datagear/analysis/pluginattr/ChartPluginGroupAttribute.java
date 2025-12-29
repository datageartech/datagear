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

package org.datagear.analysis.pluginattr;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.datagear.util.i18n.LabelUtil;

/**
 * 分组图表插件属性。
 * <p>
 * 一个分组描述插件属性值根对象中的一个对象型属性值，包含多个{@linkplain ChartPluginInputAttribute}，每一个则描述这个对象的一个基本属性值。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ChartPluginGroupAttribute extends AbstractChartPluginAttribute
{
	private static final long serialVersionUID = 1L;
	
	public static final String PROPERTY_ATTRIBUTES = "attributes";

	private List<ChartPluginInputAttribute> attributes = Collections.emptyList();

	public ChartPluginGroupAttribute()
	{
		super();
	}

	public ChartPluginGroupAttribute(String name)
	{
		super(name);
	}

	public ChartPluginGroupAttribute(String name, List<ChartPluginInputAttribute> attributes)
	{
		super();
		this.attributes = attributes;
	}

	public List<ChartPluginInputAttribute> getAttributes()
	{
		return attributes;
	}

	public void setAttributes(List<ChartPluginInputAttribute> attributes)
	{
		this.attributes = attributes;
	}

	@Override
	public String toString()
	{
		return "ChartPluginGroupAttribute [name=" + getName() + ", required=" + isRequired() + ", array=" + isArray()
				+ ", attributes=" + attributes + "]";
	}

	/**
	 * 复制为指定{@linkplain Locale}的对象。
	 * 
	 * @param locale
	 * @return
	 */
	public ChartPluginGroupAttribute clone(Locale locale)
	{
		ChartPluginGroupAttribute target = new ChartPluginGroupAttribute(getName());
		target.setRequired(isRequired());
		target.setArray(isArray());
		target.setAdditions(getAdditions());
		LabelUtil.concrete(this, target, locale);
		target.setAttributes(ChartPluginInputAttribute.clone(this.attributes, locale));

		return target;
	}
}
