/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.security;

import org.springframework.security.core.AuthenticationException;

/**
 * 用户名登录锁定异常。
 * 
 * @author datagear@163.com
 *
 */
public class UsernameLoginLatchedException extends AuthenticationException
{
	private static final long serialVersionUID = 1L;

	public UsernameLoginLatchedException()
	{
		super("User name login latched");
	}

	public UsernameLoginLatchedException(String msg)
	{
		super(msg);
	}

	public UsernameLoginLatchedException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}
