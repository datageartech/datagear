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

package org.datagear.analysis.form;

import java.util.List;
import java.util.Locale;

import org.datagear.analysis.ChartDefinition;

/**
 * 对象型图表插件属性。
 * <p>
 * 一个对象型图表插件属性包含多个{@linkplain FormProperty}，描述{@linkplain ChartDefinition#getAttrValues()}中的一个对象型属性值的UI交互操作元信息。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ObjectFormProperty extends AbstractFormProperty implements FormPropertiesGroup
{
	private static final long serialVersionUID = 1L;
	
	public static final String PROPERTY_PROPERTIES = FormPropertiesGroup.PROPERTY_PROPERTIES;
	public static final String PROPERTY_GROUPS = FormPropertiesGroup.PROPERTY_GROUPS;

	/** 子级{@linkplain FormProperty}列表 */
	private List<FormProperty> properties = null;

	/** 上述{@linkplain #properties}的分组信息 */
	private List<FormPropertyGroup> groups = null;

	public ObjectFormProperty()
	{
		super();
		super.setType(PropertyType.OBJECT);
	}

	public ObjectFormProperty(String name)
	{
		super(name, PropertyType.OBJECT);
	}

	public ObjectFormProperty(String name, List<FormProperty> properties)
	{
		super(name, PropertyType.OBJECT);
		this.properties = properties;
	}

	/**
	 * 始终返回{@linkplain PropertyType#OBJECT}。
	 */
	@Override
	public String getType()
	{
		return super.getType();
	}

	@Override
	public void setType(String type)
	{
		super.setType(PropertyType.OBJECT);
	}

	/**
	 * 获取子级{@linkplain FormProperty}列表，为空或{@code null}表示没有。
	 * <p>
	 * 子级{@linkplain FormProperty#getName()}不应重复。
	 * </p>
	 * 
	 * @return
	 */
	@Override
	public List<FormProperty> getProperties()
	{
		return properties;
	}

	public void setProperties(List<FormProperty> properties)
	{
		this.properties = properties;
	}

	@Override
	public List<FormPropertyGroup> getGroups()
	{
		return groups;
	}

	public void setGroups(List<FormPropertyGroup> groups)
	{
		this.groups = groups;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [name=" + getName() + ", required=" + isRequired() + ", array="
				+ isArray() + ", children=" + properties + "]";
	}

	@Override
	public ObjectFormProperty toLocale(Locale locale)
	{
		ObjectFormProperty target = new ObjectFormProperty();
		copyToLocale(target, locale);
		target.setProperties(FormProperty.toLocale(this.properties, locale));
		target.setGroups(FormPropertyGroup.toLocale(this.groups, locale));

		return target;
	}
}
