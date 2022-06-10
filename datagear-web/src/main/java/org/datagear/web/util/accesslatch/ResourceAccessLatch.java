/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
