/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.connection;

/**
 * 建立数据库连接出现异常。
 * 
 * @author datagear@163.com
 *
 */
public class EstablishConnectionException extends ConnectionSourceException
{
	private static final long serialVersionUID = 1L;

	private ConnectionOption connectionOption;

	public EstablishConnectionException(ConnectionOption connectionOption, Throwable cause)
	{
		super(cause);

		this.connectionOption = connectionOption;
	}

	public ConnectionOption getConnectionOption()
	{
		return connectionOption;
	}
}
