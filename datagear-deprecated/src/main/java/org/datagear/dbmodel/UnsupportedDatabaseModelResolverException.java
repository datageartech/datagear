/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.dbmodel;

import org.datagear.connection.ConnectionOption;
import org.datagear.model.Model;

/**
 * 不支持数据库模型解析异常。。
 * <p>
 * {@linkplain GenericDatabaseModelResolver}在未找到支持
 * {@linkplain DevotedDatabaseModelResolver}时抛出此异常。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class UnsupportedDatabaseModelResolverException extends DatabaseModelResolverException
{
	private static final long serialVersionUID = 1L;

	private ConnectionOption connectionOption;

	public UnsupportedDatabaseModelResolverException(ConnectionOption connectionOption)
	{
		super("Resolving database " + Model.class.getSimpleName() + " for [" + connectionOption + "] is not supported");

		this.connectionOption = connectionOption;
	}

	public ConnectionOption getConnectionOption()
	{
		return connectionOption;
	}
}
