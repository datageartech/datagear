/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.connection;

/**
 * 不支持获取连接操作异常。
 * <p>
 * {@linkplain DefaultConnectionSource#getConnection(ConnectionOption)}在无法获取合适的连接时将抛出此异常。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class UnsupportedGetConnectionException extends ConnectionSourceException
{
	private static final long serialVersionUID = 1L;

	private ConnectionOption connectionOption;

	public UnsupportedGetConnectionException(ConnectionOption connectionOption)
	{
		super("Get connection for [" + connectionOption + "] is not supported");

		this.connectionOption = connectionOption;
	}

	public ConnectionOption getConnectionOption()
	{
		return connectionOption;
	}
}
