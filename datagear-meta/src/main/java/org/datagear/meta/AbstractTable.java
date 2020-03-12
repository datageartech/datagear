/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.meta;

import java.io.Serializable;

/**
 * 抽象表元信息。
 * 
 * @author datagear@163.com
 *
 */
public class AbstractTable implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** 名称 */
	private String name;

	/** 类型 */
	private TableType type;

	/** 描述 */
	private String comment;

	public AbstractTable()
	{
		super();
	}

	public AbstractTable(String name, TableType type)
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

	public TableType getType()
	{
		return type;
	}

	public void setType(TableType type)
	{
		this.type = type;
	}

	public String getComment()
	{
		return comment;
	}

	public void setComment(String comment)
	{
		this.comment = comment;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [name=" + name + ", type=" + type + ", comment=" + comment + "]";
	}
}
