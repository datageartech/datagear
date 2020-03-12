/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.meta;

/**
 * 唯一键。
 * 
 * @author datagear@163.com
 *
 */
public class UniqueKey extends AbstractKey
{
	private static final long serialVersionUID = 1L;

	public UniqueKey()
	{
		super();
	}

	public UniqueKey(String[] columnNames)
	{
		super(columnNames);
	}
}
