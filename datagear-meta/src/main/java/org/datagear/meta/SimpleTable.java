/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.meta;

/**
 * 简单表元信息。
 * 
 * @author datagear@163.com
 *
 */
public class SimpleTable extends AbstractTable
{
	private static final long serialVersionUID = 1L;

	public SimpleTable()
	{
		super();
	}

	public SimpleTable(String name, TableType type)
	{
		super(name, type);
	}
}
