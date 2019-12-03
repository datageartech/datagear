/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.analysis;

import java.util.List;

import org.datagear.analysis.constraint.Constraint;
import org.datagear.util.i18n.Label;

/**
 * 图表属性。
 * <p>
 * 此类表示可由用户在界面输入设置的图表属性。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ChartProperty
{
	/** 名称 */
	private String name;

	/** 类型 */
	private PropertyType type;

	/** 名称标签 */
	private Label nameLabel;

	/** 描述标签 */
	private Label descLabel;

	/** 默认值 */
	private Object defaultValue;

	/** 约束 */
	private List<Constraint> constraints;

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

	public boolean hasNameLabel()
	{
		return (this.nameLabel != null);
	}

	public Label getNameLabel()
	{
		return nameLabel;
	}

	public void setNameLabel(Label nameLabel)
	{
		this.nameLabel = nameLabel;
	}

	public boolean hasDescLabel()
	{
		return (this.descLabel != null);
	}

	public Label getDescLabel()
	{
		return descLabel;
	}

	public void setDescLabel(Label descLabel)
	{
		this.descLabel = descLabel;
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
