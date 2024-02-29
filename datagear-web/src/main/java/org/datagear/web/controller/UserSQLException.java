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

package org.datagear.web.controller;

import java.sql.SQLException;

/**
 * 用户输入的SQL语句执行出错。
 * 
 * @author datagear@163.com
 *
 */
public class UserSQLException extends ControllerException
{
	private static final long serialVersionUID = 1L;

	public UserSQLException()
	{
		super();
	}

	public UserSQLException(SQLException cause)
	{
		super(cause);
	}

	public UserSQLException(String message, SQLException cause)
	{
		super(message, cause);
	}

	@Override
	public SQLException getCause()
	{
		return (SQLException) super.getCause();
	}
}
