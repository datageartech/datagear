/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.persistence.support;

import org.datagear.persistence.PersistenceException;

/**
 * 表中没有定义任何列异常。
 * 
 * @author datagear@163.com
 *
 */
public class NoColumnDefinedException extends PersistenceException
{
	private static final long serialVersionUID = 1L;

	private String tableName;

	public NoColumnDefinedException(String tableName)
	{
		super();
		this.tableName = tableName;
	}

	public NoColumnDefinedException(String tableName, String message)
	{
		super(message);
		this.tableName = tableName;
	}

	public NoColumnDefinedException(String tableName, Throwable cause)
	{
		super(cause);
		this.tableName = tableName;
	}

	public NoColumnDefinedException(String tableName, String message, Throwable cause)
	{
		super(message, cause);
		this.tableName = tableName;
	}

	public String getTableName()
	{
		return tableName;
	}

	protected void setTableName(String tableName)
	{
		this.tableName = tableName;
	}
}
