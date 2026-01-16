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
import java.util.Locale;

/**
 * 对象型图表插件属性。
 * <p>
 * 一个对象型图表插件属性包含多个{@linkplain ChartPluginAttribute}，描述{@linkplain ChartDefinition#getAttrValues()}中的一个对象型属性值的UI交互操作元信息。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ChartPluginObjectAttribute extends AbstractChartPluginAttribute
{
	private static final long serialVersionUID = 1L;
	
	public static final String PROPERTY_CHILDREN = "children";

	private List<ChartPluginAttribute> children = null;

	public ChartPluginObjectAttribute()
	{
		super();
		super.setType(DataType.OBJECT);
	}

	public ChartPluginObjectAttribute(String name)
	{
		super(name, DataType.OBJECT);
	}

	public ChartPluginObjectAttribute(String name, List<ChartPluginAttribute> children)
	{
		super(name, DataType.OBJECT);
		this.children = children;
	}

	/**
	 * 始终返回{@linkplain DataType#OBJECT}。
	 */
	@Override
	public String getType()
	{
		return super.getType();
	}

	@Override
	public void setType(String type)
	{
		super.setType(DataType.OBJECT);
	}

	/**
	 * 获取子级{@linkplain ChartPluginAttribute}列表，为空或{@code null}表示没有。
	 * <p>
	 * 子级{@linkplain ChartPluginAttribute#getName()}不应重复。
	 * </p>
	 * 
	 * @return
	 */
	public List<ChartPluginAttribute> getChildren()
	{
		return children;
	}

	public void setChildren(List<ChartPluginAttribute> children)
	{
		this.children = children;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [name=" + getName() + ", required=" + isRequired() + ", array="
				+ isArray() + ", children=" + children + "]";
	}

	@Override
	public ChartPluginObjectAttribute toLocale(Locale locale)
	{
		ChartPluginObjectAttribute target = new ChartPluginObjectAttribute();
		copyToLocale(target, locale);
		target.setChildren(ChartPluginAttribute.toLocale(this.children, locale));

		return target;
	}
}
