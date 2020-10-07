/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.meta;

import java.io.Serializable;
import java.util.Arrays;

import org.datagear.util.StringUtil;

/**
 * 表键。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractKey implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** 列名 */
	private String[] columnNames;

	/** 键名 */
	private String keyName;

	public AbstractKey()
	{
		super();
	}

	public AbstractKey(String[] columnNames)
	{
		super();
		this.columnNames = columnNames;
	}

	public String[] getColumnNames()
	{
		return columnNames;
	}

	public void setColumnNames(String[] columnNames)
	{
		this.columnNames = columnNames;
	}

	public boolean hasKeyName()
	{
		return (this.keyName != null && !this.keyName.isEmpty());
	}

	public String getKeyName()
	{
		return keyName;
	}

	public void setKeyName(String keyName)
	{
		this.keyName = keyName;
	}

	/**
	 * 是否包含指定列名。
	 * 
	 * @param columnName
	 * @return
	 */
	public boolean containsColumnName(String columnName)
	{
		if (this.columnNames == null)
			return false;

		for (String cn : this.columnNames)
		{
			if (StringUtil.isEquals(cn, columnName))
				return true;
		}

		return false;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [columnNames=" + Arrays.toString(columnNames) + ", keyName=" + keyName
				+ "]";
	}
}
