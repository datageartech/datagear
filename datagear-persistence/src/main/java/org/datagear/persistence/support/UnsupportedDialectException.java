/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.persistence.support;

import org.datagear.connection.ConnectionOption;
import org.datagear.persistence.Dialect;
import org.datagear.persistence.DialectException;

/**
 * 不支持的{@linkplain Dialect}异常。
 * 
 * @author datagear@163.com
 *
 */
public class UnsupportedDialectException extends DialectException
{
	private static final long serialVersionUID = 1L;

	private ConnectionOption connectionOption;

	public UnsupportedDialectException(ConnectionOption connectionOption)
	{
		super("No Dialect found for [" + connectionOption + "]");
		this.connectionOption = connectionOption;
	}

	public ConnectionOption getConnectionOption()
	{
		return connectionOption;
	}
}
