/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */

package org.datagear.analysis;

import java.util.Map;

/**
 * 值映射表。
 * 
 * @author datagear@163.com
 *
 */
public class ValueMap
{
	private Map<String, ?> values;

	public ValueMap()
	{
	}

	public ValueMap(Map<String, ?> values)
	{
		super();
		this.values = values;
	}

	public boolean hasValue()
	{
		return (this.values != null && !this.values.isEmpty());
	}

	public Map<String, ?> getValues()
	{
		return values;
	}

	public void setValues(Map<String, ?> values)
	{
		this.values = values;
	}

	@SuppressWarnings("unchecked")
	public <T> T getValue(String name)
	{
		return (T) (this.values == null ? null : this.values.get(name));
	}
}
