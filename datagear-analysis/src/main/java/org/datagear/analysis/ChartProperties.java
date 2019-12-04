/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */

package org.datagear.analysis;

import java.util.List;

/**
 * 图表属性集。
 * 
 * @author datagear@163.com
 *
 */
public class ChartProperties
{
	private List<ChartProperty> properties;

	public ChartProperties()
	{
		super();
	}

	public ChartProperties(List<ChartProperty> properties)
	{
		super();
		this.properties = properties;
	}

	public boolean hasProperty()
	{
		return (this.properties != null && !this.properties.isEmpty());
	}

	public List<ChartProperty> getProperties()
	{
		return properties;
	}

	public void setProperties(List<ChartProperty> properties)
	{
		this.properties = properties;
	}

	/**
	 * 获取指定名称的{@linkplain ChartProperty}，未找到则返回{@code null}。
	 * 
	 * @param name
	 * @return
	 */
	public ChartProperty getByName(String name)
	{
		if (this.properties == null)
			return null;

		for (ChartProperty property : this.properties)
		{
			if (property.getName().equals(name))
				return property;
		}

		return null;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [properties=" + properties + "]";
	}
}
