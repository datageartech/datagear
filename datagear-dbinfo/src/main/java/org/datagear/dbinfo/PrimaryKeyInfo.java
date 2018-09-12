/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.dbinfo;

/**
 * 主键列信息。
 * 
 * @author datagear@163.com
 *
 */
public class PrimaryKeyInfo extends ResultSetSpecBean
{
	private static final long serialVersionUID = 1L;

	/** 主键列名称 */
	private String columnName;

	public PrimaryKeyInfo()
	{
		super();
	}

	public PrimaryKeyInfo(String columnName)
	{
		super();
		this.columnName = columnName;
	}

	public String getColumnName()
	{
		return columnName;
	}

	public void setColumnName(String columnName)
	{
		this.columnName = columnName;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [columnName=" + columnName + "]";
	}
}
