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
 * 分组图表插件属性。
 * <p>
 * 一个分组描述插件属性值根对象中的一个对象型属性值（当{@linkplain #getName()}为{@code null}或者{@code ""}时则是根对象），
 * 包含多个{@linkplain ChartPluginInputAttribute}，每一个则描述这个对象的一个基本属性值。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ChartPluginGroupAttribute extends AbstractChartPluginAttribute
{
	private static final long serialVersionUID = 1L;
	
	public static final String PROPERTY_CHILDREN = "children";

	private List<ChartPluginInputAttribute> children = null;

	public ChartPluginGroupAttribute()
	{
		super();
	}

	public ChartPluginGroupAttribute(String name)
	{
		super(name);
	}

	public ChartPluginGroupAttribute(String name, List<ChartPluginInputAttribute> children)
	{
		super();
		this.children = children;
	}

	/**
	 * 获取名称，可能为{@code null}，具体参考{@linkplain #setName(String)}。
	 */
	@Override
	public String getName()
	{
		return super.getName();
	}

	/**
	 * 设置名称，当为{@code null}或者{@code ""}时表明{@linkplain #getChildren()}描述的是插件属性值根对象的基本属性值，
	 * 另参考{@linkplain #isArray()}。
	 */
	@Override
	public void setName(String name)
	{
		super.setName(name);
	}

	/**
	 * 当{@linkplain #getName()}为{@code null}或者{@code ""}时，无论此值是否为{@code true}，都应强制作为{@code false}处理，
	 * 因为此时分组包含的属性值无法存储为对象数组，
	 */
	@Override
	public boolean isArray()
	{
		return super.isArray();
	}

	/**
	 * 获取子级{@linkplain ChartPluginInputAttribute}列表，为空或{@code null}表示没有。
	 * 
	 * @return
	 */
	public List<ChartPluginInputAttribute> getChildren()
	{
		return children;
	}

	public void setChildren(List<ChartPluginInputAttribute> children)
	{
		this.children = children;
	}

	@Override
	public String toString()
	{
		return "ChartPluginGroupAttribute [name=" + getName() + ", required=" + isRequired() + ", array=" + isArray()
				+ ", children=" + children + "]";
	}

	@Override
	public ChartPluginGroupAttribute toLocale(Locale locale)
	{
		ChartPluginGroupAttribute target = new ChartPluginGroupAttribute();
		copyToLocale(target, locale);
		target.setChildren(ChartPluginInputAttribute.toLocale(this.children, locale));

		return target;
	}
}
