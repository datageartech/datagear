/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.dbmodel;

/**
 * 简单{@linkplain ModelNameResolver}。
 * 
 * @author datagear@163.com
 *
 */
public class SimpleModelNameResolver implements ModelNameResolver
{
	public SimpleModelNameResolver()
	{
		super();
	}

	@Override
	public String resolve(String tableName)
	{
		return tableName;
	}
}
