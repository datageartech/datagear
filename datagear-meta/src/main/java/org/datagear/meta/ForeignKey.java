/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.meta;

import java.util.Arrays;

/**
 * 外键。
 * 
 * @author datagear@163.com
 *
 */
public class ForeignKey extends AbstractKey
{
	private static final long serialVersionUID = 1L;

	/** 主表名 */
	private String primaryTableName;

	/** 主表键列名 */
	private String[] primaryColumnNames;

	public ForeignKey()
	{
		super();
	}

	public ForeignKey(String[] columnNames, String primaryTableName, String[] primaryColumnNames)
	{
		super(columnNames);
		this.primaryTableName = primaryTableName;
		this.primaryColumnNames = primaryColumnNames;
	}

	public String getPrimaryTableName()
	{
		return primaryTableName;
	}

	public void setPrimaryTableName(String primaryTableName)
	{
		this.primaryTableName = primaryTableName;
	}

	public String[] getPrimaryColumnNames()
	{
		return primaryColumnNames;
	}

	public void setPrimaryColumnNames(String[] primaryColumnNames)
	{
		this.primaryColumnNames = primaryColumnNames;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [columnNames=" + Arrays.toString(getColumnNames())
				+ ", primaryTableName=" + primaryTableName + ", primaryColumnNames="
				+ Arrays.toString(primaryColumnNames) + "]";
	}
}
