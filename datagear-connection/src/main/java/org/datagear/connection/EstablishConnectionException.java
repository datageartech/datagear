/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
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
