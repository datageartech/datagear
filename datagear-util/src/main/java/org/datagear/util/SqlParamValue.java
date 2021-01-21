/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.util;

/**
 * SQL参数值。
 * 
 * @author datagear@163.com
 *
 */
public class SqlParamValue
{
	/** 参数值 */
	private Object value;

	/** 类型 */
	private int type;

	public SqlParamValue()
	{
		super();
	}

	public SqlParamValue(Object value, int type)
	{
		super();
		this.value = value;
		this.type = type;
	}

	public boolean hasValue()
	{
		return (this.value != null);
	}

	public Object getValue()
	{
		return value;
	}

	public void setValue(Object value)
	{
		this.value = value;
	}

	public int getType()
	{
		return type;
	}

	public void setType(int type)
	{
		this.type = type;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [value=" + value + ", type=" + type + "]";
	}
}
