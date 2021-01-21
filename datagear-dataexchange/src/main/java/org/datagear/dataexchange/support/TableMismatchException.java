/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.dataexchange.support;

import org.datagear.dataexchange.DataExchangeException;

/**
 * 表不匹配异常。
 * <p>
 * 导入数据时，在给定表中没有找到任何匹配列。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class TableMismatchException extends DataExchangeException
{
	private static final long serialVersionUID = 1L;

	private String table;

	public TableMismatchException(String table)
	{
		super();
		this.table = table;
	}

	public TableMismatchException(String table, String message)
	{
		super(message);
		this.table = table;
	}

	public TableMismatchException(String table, Throwable cause)
	{
		super(cause);
		this.table = table;
	}

	public TableMismatchException(String table, String message, Throwable cause)
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
