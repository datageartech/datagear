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
public class ChartProperties extends AbstractDelegatedList<ChartProperty>
{
	public ChartProperties()
	{
		super();
	}

	public ChartProperties(List<ChartProperty> chartProperties)
	{
		super(chartProperties);
	}

	/**
	 * 获取指定名称的{@linkplain ChartProperty}，未找到则返回{@code null}。
	 * 
	 * @param name
	 * @return
	 */
	public ChartProperty getByName(String name)
	{
		for (int i = 0, len = this.size(); i < len; i++)
		{
			ChartProperty chartProperty = get(i);

			if (chartProperty.getName().equals(name))
				return chartProperty;
		}

		return null;
	}
}
