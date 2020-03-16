/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.dbinfo;

import org.datagear.connection.ConnectionOption;

/**
 * 不支持数据库信息解析异常。
 * <p>
 * {@linkplain GenericDatabaseInfoResolver}在未找到支持
 * {@linkplain DevotedDatabaseInfoResolver}时抛出此异常。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class UnsupportedDatabaseInfoResolverException extends DatabaseInfoResolverException
{
	private static final long serialVersionUID = 1L;

	private ConnectionOption connectionOption;

	public UnsupportedDatabaseInfoResolverException(ConnectionOption connectionOption)
	{
		super("Resolving database info for [" + connectionOption + "] is not supported");
		this.connectionOption = connectionOption;
	}

	public ConnectionOption getConnectionOption()
	{
		return connectionOption;
	}
}
