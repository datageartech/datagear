/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.meta.resolver;

/**
 * 表未找到异常。
 * 
 * @author datagear@163.com
 *
 */
public class TableNotFoundException extends DBMetaResolverException
{
	private static final long serialVersionUID = 1L;

	private String tableName;

	public TableNotFoundException()
	{
		super();
	}

	public TableNotFoundException(String tableName)
	{
		super("Table [" + tableName + "] not found");
		this.tableName = tableName;
	}

	public TableNotFoundException(String tableName, Throwable cause)
	{
		this(tableName);
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
