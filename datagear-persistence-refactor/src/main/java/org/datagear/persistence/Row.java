/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence;

import java.util.HashMap;
import java.util.Map;

/**
 * 行对象。
 * 
 * @author datagear@163.com
 *
 */
public class Row extends HashMap<String, Object>
{
	private static final long serialVersionUID = 1L;

	public Row()
	{
		super();
	}

	public Row(Map<? extends String, ? extends Object> m)
	{
		super(m);
	}
}
