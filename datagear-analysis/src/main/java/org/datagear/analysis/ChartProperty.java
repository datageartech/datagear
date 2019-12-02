/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.analysis;

import java.util.Collections;
import java.util.List;

import org.datagear.analysis.constraint.Constraint;

/**
 * 图表可设置属性。
 * 
 * @author datagear@163.com
 *
 */
public class ChartProperty
{
	private String name;

	private PropertyType type;

	private String label;

	private Object defaultValue;

	@SuppressWarnings("unchecked")
	private List<Constraint> constraints = Collections.EMPTY_LIST;

	public ChartProperty()
	{
	}

	public ChartProperty(String name, PropertyType type)
	{
		super();
		this.name = name;
		this.type = type;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public PropertyType getType()
	{
		return type;
	}

	public void setType(PropertyType type)
	{
		this.type = type;
	}

	public String getLabel()
	{
		return label;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}

	public Object getDefaultValue()
	{
		return defaultValue;
	}

	public void setDefaultValue(Object defaultValue)
	{
		this.defaultValue = defaultValue;
	}

	public List<Constraint> getConstraints()
	{
		return constraints;
	}

	public void setConstraints(List<Constraint> constraints)
	{
		this.constraints = constraints;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [name=" + name + ", type=" + type + "]";
	}
}
