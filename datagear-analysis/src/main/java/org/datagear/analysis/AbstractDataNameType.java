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
	private String type;

	public AbstractDataNameType()
	{
		super();
	}

	public AbstractDataNameType(String name, String type)
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
	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}
}
