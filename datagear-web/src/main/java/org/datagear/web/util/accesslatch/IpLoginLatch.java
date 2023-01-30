/*
 * Copyright 2018-2023 datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
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
