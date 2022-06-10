/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.util.accesslatch;

import javax.servlet.http.HttpServletRequest;

import org.datagear.web.util.WebUtils;

/**
 * IP登录{@linkplain AccessLatch}。
 * 
 * @author datagear@163.com
 *
 */
public class IpLoginLatch extends ResourceAccessLatch
{
	public static final String RESOURCE = IpLoginLatch.class.getName();

	public IpLoginLatch()
	{
		super();
	}

	public IpLoginLatch(AccessLatch accessLatch)
	{
		super(RESOURCE, accessLatch);
	}

	@Override
	public void setResourceId(String resourceId)
	{
		throw new UnsupportedOperationException();
	}

	public int remain(HttpServletRequest requset)
	{
		return remain(getClientAddress(requset));
	}

	public boolean access(HttpServletRequest request)
	{
		return access(getClientAddress(request));
	}

	public void clear(HttpServletRequest request)
	{
		clear(getClientAddress(request));
	}

	public String getClientAddress(HttpServletRequest request)
	{
		return WebUtils.getClientAddress(request);
	}
}
