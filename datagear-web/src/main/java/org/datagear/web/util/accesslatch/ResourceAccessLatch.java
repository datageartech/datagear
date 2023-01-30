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

/**
 * 固定资源{@linkplain AccessLatch}。
 * 
 * @author datagear@163.com
 *
 */
public class ResourceAccessLatch
{
	private String resourceId;

	private AccessLatch accessLatch;

	public ResourceAccessLatch()
	{
		super();
	}

	public ResourceAccessLatch(String resourceId, AccessLatch accessLatch)
	{
		super();
		this.resourceId = resourceId;
		this.accessLatch = accessLatch;
	}

	public String getResourceId()
	{
		return resourceId;
	}

	public void setResourceId(String resourceId)
	{
		this.resourceId = resourceId;
	}

	public AccessLatch getAccessLatch()
	{
		return accessLatch;
	}

	public void setAccessLatch(AccessLatch accessLatch)
	{
		this.accessLatch = accessLatch;
	}

	/**
	 * 剩余可访问次数。
	 * 
	 * @param identity
	 *            身份标识，比如：IP、用户ID
	 * @return {@code <0 }表示不限
	 */
	public int remain(String identity)
	{
		return this.accessLatch.remain(identity, getResourceId());
	}

	/**
	 * 尝试一次访问。
	 * 
	 * @param identity
	 *            身份标识，比如：IP、用户ID
	 * @return {@code false}，失败，因为已超限；{@code true} 成功
	 */
	public boolean access(String identity)
	{
		return this.accessLatch.access(identity, getResourceId());
	}

	/**
	 * 清除访问记录。
	 * 
	 * @param identity
	 */
	public void clear(String identity)
	{
		this.accessLatch.clear(identity, getResourceId());
	}
}
