/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.meta;

/**
 * 主键。
 * 
 * @author datagear@163.com
 *
 */
public class PrimaryKey extends AbstractKey
{
	private static final long serialVersionUID = 1L;

	public PrimaryKey()
	{
		super();
	}

	public PrimaryKey(String[] columnNames)
	{
		super(columnNames);
	}
}
