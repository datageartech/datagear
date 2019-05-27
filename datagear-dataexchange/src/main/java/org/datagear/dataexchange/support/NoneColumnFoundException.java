/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import org.datagear.dataexchange.DataImportException;

/**
 * 导入数据时，在表中没有找到任何对应列异常。
 * 
 * @author datagear@163.com
 *
 */
public class NoneColumnFoundException extends DataImportException
{
	private static final long serialVersionUID = 1L;

	private String table;

	public NoneColumnFoundException(String table)
	{
		super();
		this.table = table;
	}

	public NoneColumnFoundException(String table, String message)
	{
		super(message);
		this.table = table;
	}

	public NoneColumnFoundException(String table, Throwable cause)
	{
		super(cause);
		this.table = table;
	}

	public NoneColumnFoundException(String table, String message, Throwable cause)
	{
		super(message, cause);
		this.table = table;
	}

	public String getTable()
	{
		return table;
	}

	protected void setTable(String table)
	{
		this.table = table;
	}
}
