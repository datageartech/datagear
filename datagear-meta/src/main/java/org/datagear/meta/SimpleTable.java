/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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

	public SimpleTable(String name, String type)
	{
		super(name, type);
	}
}
