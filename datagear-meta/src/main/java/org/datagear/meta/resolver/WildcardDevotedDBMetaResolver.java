/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
