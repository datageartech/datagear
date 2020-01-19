/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
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
