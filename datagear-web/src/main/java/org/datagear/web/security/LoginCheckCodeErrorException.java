/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.security;

import org.springframework.security.core.AuthenticationException;

/**
 * 登录验证码错误异常。
 * 
 * @author datagear@163.com
 *
 */
public class LoginCheckCodeErrorException extends AuthenticationException
{
	private static final long serialVersionUID = 1L;

	public LoginCheckCodeErrorException()
	{
		super("Check code error");
	}

	public LoginCheckCodeErrorException(String msg)
	{
		super(msg);
	}

	public LoginCheckCodeErrorException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}
