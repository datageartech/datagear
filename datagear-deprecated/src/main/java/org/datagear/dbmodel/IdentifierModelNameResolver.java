/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.dbmodel;

/**
 * 标识符{@linkplain ModelNameResolver}。
 * 
 * @author datagear@163.com
 *
 */
public class IdentifierModelNameResolver extends NameResolverSupport implements ModelNameResolver
{
	public IdentifierModelNameResolver()
	{
		super();
	}

	@Override
	public String resolve(String tableName)
	{
		return resolveForIndentifier(tableName);
	}
}
