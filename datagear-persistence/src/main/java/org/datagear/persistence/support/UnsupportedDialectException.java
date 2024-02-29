/*
 * Copyright 2018-2024 datagear.tech
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
