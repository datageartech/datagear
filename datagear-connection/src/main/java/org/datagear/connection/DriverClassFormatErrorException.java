/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.connection;

/**
 * 驱动程序类格式错误异常。
 * 
 * @author datagear@163.com
 *
 */
public class DriverClassFormatErrorException extends PathDriverFactoryException
{
	private static final long serialVersionUID = 1L;

	public DriverClassFormatErrorException(ClassFormatError cause)
	{
		super(cause);
	}
}
