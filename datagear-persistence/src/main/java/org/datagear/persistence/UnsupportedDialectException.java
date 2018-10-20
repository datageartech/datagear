/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence;

import org.datagear.connection.ConnectionOption;

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
