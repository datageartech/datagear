/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

/**
 * 
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
