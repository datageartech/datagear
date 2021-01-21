/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
