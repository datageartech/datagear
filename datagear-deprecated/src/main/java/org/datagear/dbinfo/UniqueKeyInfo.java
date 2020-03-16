/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.dbinfo;

/**
 * 唯一键列信息。
 * 
 * @author datagear@163.com
 *
 */
public class UniqueKeyInfo extends ResultSetSpecBean
{
	private static final long serialVersionUID = 1L;

	/** 唯一键名称 */
	private String keyName;

	/** 唯一键列名称 */
	private String columnName;

	public UniqueKeyInfo()
	{
		super();
	}

	public UniqueKeyInfo(String keyName, String columnName)
	{
		super();
		this.keyName = keyName;
		this.columnName = columnName;
	}

	public String getKeyName()
	{
		return keyName;
	}

	public void setKeyName(String keyName)
	{
		this.keyName = keyName;
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
		return getClass().getSimpleName() + " [keyName=" + keyName + ", columnName=" + columnName + "]";
	}
}
