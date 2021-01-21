/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
