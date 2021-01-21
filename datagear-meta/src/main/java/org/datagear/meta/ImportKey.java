/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.meta;

import java.util.Arrays;

/**
 * 导入外键。
 * 
 * @author datagear@163.com
 *
 */
public class ImportKey extends AbstractKey
{
	private static final long serialVersionUID = 1L;

	/** 主表名 */
	private String primaryTableName;

	/** 主表键列名 */
	private String[] primaryColumnNames;

	public ImportKey()
	{
		super();
	}

	public ImportKey(String[] columnNames, String primaryTableName, String[] primaryColumnNames)
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
