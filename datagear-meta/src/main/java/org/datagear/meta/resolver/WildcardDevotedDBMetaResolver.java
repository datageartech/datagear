/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.meta.resolver;

import java.sql.Connection;

/**
 * 通配{@linkplain DevotedDBMetaResolver}。
 * 
 * @author datagear@163.com
 *
 */
public class WildcardDevotedDBMetaResolver extends AbstractDevotedDBMetaResolver
{
	public WildcardDevotedDBMetaResolver()
	{
		super();
	}

	@Override
	public boolean supports(Connection cn)
	{
		return true;
	}
}
