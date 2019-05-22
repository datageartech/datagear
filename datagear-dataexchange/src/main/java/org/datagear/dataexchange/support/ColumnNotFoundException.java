/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import org.datagear.dataexchange.DataImportException;

/**
 * 在表中没有找到指定列异常。
 * 
 * @author datagear@163.com
 *
 */
public class ColumnNotFoundException extends DataImportException
{
	private static final long serialVersionUID = 1L;

	private String table;

	private String columnName;

	public ColumnNotFoundException(String table, String columnName)
	{
		super("Column [" + columnName + "] not found in table [" + table + "]");

		this.table = table;
		this.columnName = columnName;
	}

	public String getTable()
	{
		return table;
	}

	protected void setTable(String table)
	{
		this.table = table;
	}

	public String getColumnName()
	{
		return columnName;
	}

	protected void setColumnName(String columnName)
	{
		this.columnName = columnName;
	}
}
