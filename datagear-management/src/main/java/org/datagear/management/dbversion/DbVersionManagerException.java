/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.dbversion;

/**
 * 版本管理异常。
 * 
 * @author datagear@163.com
 *
 */
public class DbVersionManagerException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public DbVersionManagerException()
	{
		super();
	}

	public DbVersionManagerException(String message)
	{
		super(message);
	}

	public DbVersionManagerException(Throwable cause)
	{
		super(cause);
	}

	public DbVersionManagerException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
