/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.util;

/**
 * SQL类型。
 * 
 * @author datagear@163.com
 *
 */
public class SqlType
{
	/** JDBC类型 */
	private int type;

	/** SQL类型名称 */
	private String typeName;

	public SqlType()
	{
		super();
	}

	public SqlType(int type)
	{
		super();
		this.type = type;
	}

	public SqlType(int type, String typeName)
	{
		super();
		this.type = type;
		this.typeName = typeName;
	}

	public int getType()
	{
		return type;
	}

	public void setType(int type)
	{
		this.type = type;
	}

	public boolean hasTypeName()
	{
		return (this.typeName != null && !this.typeName.isEmpty());
	}

	public String getTypeName()
	{
		return typeName;
	}

	public void setTypeName(String typeName)
	{
		this.typeName = typeName;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [type=" + type + ", typeName=" + typeName + "]";
	}
}
