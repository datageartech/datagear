/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
