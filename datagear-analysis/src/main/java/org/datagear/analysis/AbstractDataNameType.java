/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis;

/**
 * 抽象{@linkplain DataNameType}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractDataNameType implements DataNameType
{
	/** 名称 */
	private String name;

	/** 类型 */
	private DataType type;

	public AbstractDataNameType()
	{
		super();
	}

	public AbstractDataNameType(String name, DataType type)
	{
		super();
		this.name = name;
		this.type = type;
	}

	@Override
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public DataType getType()
	{
		return type;
	}

	public void setType(DataType type)
	{
		this.type = type;
	}
}
