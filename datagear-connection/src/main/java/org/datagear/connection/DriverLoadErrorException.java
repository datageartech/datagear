/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.connection;

/**
 * 加载驱动程序类出错异常。
 * 
 * @author datagear@163.com
 *
 */
public class DriverLoadErrorException extends PathDriverFactoryException
{
	private static final long serialVersionUID = 1L;

	public DriverLoadErrorException(Error cause)
	{
		super(cause);
	}
}
