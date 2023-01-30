/*
 * Copyright 2018-2023 datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
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
