/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.util.accesslatch;

/**
 * 用户名登录{@linkplain AccessLatch}。
 * 
 * @author datagear@163.com
 *
 */
public class UsernameLoginLatch extends ResourceAccessLatch
{
	public static final String RESOURCE = UsernameLoginLatch.class.getName();

	public UsernameLoginLatch()
	{
		super();
	}

	public UsernameLoginLatch(AccessLatch accessLatch)
	{
		super(RESOURCE, accessLatch);
	}

	@Override
	public void setResourceId(String resourceId)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public int remain(String username)
	{
		return super.remain(username);
	}

	@Override
	public boolean access(String username)
	{
		return super.access(username);
	}

	@Override
	public void clear(String username)
	{
		super.clear(username);
	}
}
