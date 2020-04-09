/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.analysis;

import java.io.Serializable;
import java.util.Set;

import org.datagear.analysis.constraint.Constraint;
import org.datagear.util.i18n.AbstractLabeled;
import org.datagear.util.i18n.Labeled;

/**
 * 图表属性。
 * <p>
 * 此类表示可由用户在界面输入设置的图表属性。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ChartProperty extends AbstractLabeled implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String PROPERTY_NAME = "name";
	public static final String PROPERTY_TYPE = "type";
	public static final String PROPERTY_NAME_LABEL = Labeled.PROPERTY_NAME_LABEL;
	public static final String PROPERTY_DESC_LABEL = Labeled.PROPERTY_DESC_LABEL;
	public static final String PROPERTY_DEFAULT_VALUE = "defaultValue";
	public static final String PROPERTY_CONSTRAINTS = "constraints";

	/** 名称 */
	private String name;

	/** 类型 */
	private PropertyType type;

	/** 默认值 */
	private Object defaultValue;

	/** 约束 */
	private Set<Constraint> constraints;

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

	public boolean hasDefaultValue()
	{
		return (this.defaultValue != null);
	}

	public Object getDefaultValue()
	{
		return defaultValue;
	}

	public void setDefaultValue(Object defaultValue)
	{
		this.defaultValue = defaultValue;
	}

	public boolean hasConstraint()
	{
		return (this.constraints != null && !this.constraints.isEmpty());
	}

	public Set<Constraint> getConstraints()
	{
		return constraints;
	}

	public void setConstraints(Set<Constraint> constraints)
	{
		this.constraints = constraints;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [name=" + name + ", type=" + type + "]";
	}
}
