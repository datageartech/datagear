/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.security;

/**
 * 加密异常。
 * 
 * @author datagear@163.com
 *
 */
public class EncryptorException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public EncryptorException()
	{
		super();
	}

	public EncryptorException(String message)
	{
		super(message);
	}

	public EncryptorException(Throwable cause)
	{
		super(cause);
	}

	public EncryptorException(String message, Throwable cause)
	{
		super(message, cause);
	}

}
