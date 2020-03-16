/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.dbinfo;

/**
 * 表不存在异常。
 * 
 * @author datagear@163.com
 *
 */
public class TableNotExistsException extends DatabaseInfoResolverException
{
	private static final long serialVersionUID = 1L;

	private String tableName;

	public TableNotExistsException()
	{
		super();
	}

	public TableNotExistsException(String tableName, String message)
	{
		super(message);
		this.tableName = tableName;
	}

	public TableNotExistsException(String tableName, Throwable cause)
	{
		super(cause);
		this.tableName = tableName;
	}

	public TableNotExistsException(String tableName, String message, Throwable cause)
	{
		super(message, cause);
		this.tableName = tableName;
	}

	public String getTableName()
	{
		return tableName;
	}

	public void setTableName(String tableName)
	{
		this.tableName = tableName;
	}
}
