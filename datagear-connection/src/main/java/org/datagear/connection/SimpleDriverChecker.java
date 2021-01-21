/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.connection;

import java.sql.Driver;

/**
 * 简单{@linkplain DriverChecker}实现。
 * <p>
 * 它仅使用{@linkplain Driver#acceptsURL(String)}校验。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class SimpleDriverChecker extends AbstractDriverChecker
{
	public SimpleDriverChecker()
	{
		super();
	}

	@Override
	protected boolean checkConnection(Driver driver, ConnectionOption connectionOption) throws Throwable
	{
		return true;
	}

}
