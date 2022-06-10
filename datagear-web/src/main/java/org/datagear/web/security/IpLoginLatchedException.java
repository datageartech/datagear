/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.security;

import org.springframework.security.core.AuthenticationException;

/**
 * IP登录锁定异常。
 * 
 * @author datagear@163.com
 *
 */
public class IpLoginLatchedException extends AuthenticationException
{
	private static final long serialVersionUID = 1L;

	public IpLoginLatchedException()
	{
		super("IP login latched");
	}

	public IpLoginLatchedException(String msg)
	{
		super(msg);
	}

	public IpLoginLatchedException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}
