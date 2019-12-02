/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis;

import java.util.Collections;
import java.util.List;

/**
 * 图表可设置属性集。
 * 
 * @author datagear@163.com
 *
 */
public class ChartProperties
{
	@SuppressWarnings("unchecked")
	private List<ChartProperty> properties = Collections.EMPTY_LIST;

	public ChartProperties()
	{
		super();
	}

	public ChartProperties(List<ChartProperty> properties)
	{
		super();
		this.properties = properties;
	}

	public List<ChartProperty> getProperties()
	{
		return properties;
	}

	public void setProperties(List<ChartProperty> properties)
	{
		this.properties = properties;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [properties=" + properties + "]";
	}
}
