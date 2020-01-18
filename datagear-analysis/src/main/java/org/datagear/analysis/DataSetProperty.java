/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */

package org.datagear.analysis;

import java.io.Serializable;

/**
 * 数据集属性元信息。
 * 
 * @author datagear@163.com
 *
 */
public class DataSetProperty extends DataNameAndType implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** 展示标签 */
	private String label;

	public DataSetProperty()
	{
		super();
	}

	public DataSetProperty(String name, DataType type)
	{
		super(name, type);
	}

	public boolean hasLabel()
	{
		return (this.label != null && !this.label.isEmpty());
	}

	public String getLabel()
	{
		return label;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [name=" + getName() + ", type=" + getType() + ", label=" + label + "]";
	}
}
