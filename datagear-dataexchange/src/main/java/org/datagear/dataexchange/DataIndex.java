/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

import java.io.Serializable;

/**
 * 数据索引。
 * 
 * @author datagear@163.com
 *
 */
public abstract class DataIndex implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Override
	public abstract String toString();
}
