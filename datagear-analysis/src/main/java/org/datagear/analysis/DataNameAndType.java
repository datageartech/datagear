/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis;

/**
 * 数据名称、类型封装类。
 * 
 * @author datagear@163.com
 *
 */
public class DataNameAndType
{
	/** 名称 */
	private String name;

	/** 类型 */
	private DataType type;

	public DataNameAndType()
	{
		super();
	}

	public DataNameAndType(String name, DataType type)
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

	public DataType getType()
	{
		return type;
	}

	public void setType(DataType type)
	{
		this.type = type;
	}
}
