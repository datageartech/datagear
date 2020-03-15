/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence;

import org.datagear.meta.Column;

/**
 * 列值。
 * 
 * @author datagear@163.com
 *
 */
public class ColumnValue
{
	/** 列对象 */
	private Column column;

	/** 列值 */
	private Object value;

	public ColumnValue()
	{
		super();
	}

	public ColumnValue(Column column, Object value)
	{
		super();
		this.column = column;
		this.value = value;
	}

	public Column getColumn()
	{
		return column;
	}

	public void setColumn(Column column)
	{
		this.column = column;
	}

	public boolean hasValue()
	{
		return (value != null);
	}

	public Object getValue()
	{
		return value;
	}

	public void setValue(Object value)
	{
		this.value = value;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [column=" + column + ", value=" + value + "]";
	}
}
