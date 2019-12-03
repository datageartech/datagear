/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis;

/**
 * 数据列元信息。
 * 
 * @author datagear@163.com
 *
 */
public class ColumnMeta
{
	/** 名称 */
	private String name;

	/** 数据类型 */
	private DataType dataType;

	/** 展示标签 */
	private String label;

	public ColumnMeta()
	{
	}

	public ColumnMeta(String name, DataType dataType)
	{
		super();
		this.name = name;
		this.dataType = dataType;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public DataType getDataType()
	{
		return dataType;
	}

	public void setDataType(DataType dataType)
	{
		this.dataType = dataType;
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
		return getClass().getSimpleName() + " [name=" + name + ", dataType=" + dataType + "]";
	}
}
