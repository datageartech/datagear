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
 * 抽象{@linkplain AccessLatch}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractAccessLatch implements AccessLatch
{
	/** 限定秒数：小于0 不限；0 不允许访问；大于0 限定过去秒数内 */
	private int seconds;

	/** 限定次数：小于0 不限；0 不允许访问；大于0 限定次数 */
	private int frequency;

	public AbstractAccessLatch()
	{
		super();
	}

	public AbstractAccessLatch(int seconds, int frequency)
	{
		super();
		this.seconds = seconds;
		this.frequency = frequency;
	}

	public int getSeconds()
	{
		return seconds;
	}

	public void setSeconds(int seconds)
	{
		this.seconds = seconds;
	}

	public int getFrequency()
	{
		return frequency;
	}

	public void setFrequency(int frequency)
	{
		this.frequency = frequency;
	}

	@Override
	public int remain(String identity, String resource)
	{
		return remain(identity, resource, System.currentTimeMillis());
	}

	@Override
	public int remain(String identity, String resource, long time)
	{
		if (this.seconds < 0 || this.frequency < 0)
			return -1;

		AccessLog accessLog = getAccessLog(toAccessKey(identity, resource), false);

		if (accessLog == null)
			return this.frequency;

		synchronized (accessLog)
		{
			time = time - this.seconds * 1000;
			int count = accessLog.countAfterOrEq(time);
			int remain = (this.frequency - count);

			return (remain < 0 ? 0 : remain);
		}
	}

	@Override
	public boolean access(String identity, String resource)
	{
		return access(identity, resource, System.currentTimeMillis());
	}

	@Override
	public boolean access(String identity, String resource, long time)
	{
		if (this.seconds < 0 || this.frequency < 0)
			return true;

		boolean re;

		long valveTime = time - this.seconds * 1000;
		String accessKey = toAccessKey(identity, resource);
		AccessLog accessLog = getAccessLog(accessKey, true);

		synchronized (accessLog)
		{
			int count = accessLog.countAfterOrEq(valveTime);

			if (count >= this.frequency)
			{
				re = false;
			}
			else
			{
				accessLog.log(time);
				accessLog.deleteBefore(valveTime);
				updateAccessLog(accessKey, accessLog);
				re = true;
			}
		}

		return re;
	}

	@Override
	public void clear(String identity, String resource)
	{
		clearAccessLog(toAccessKey(identity, resource));
	}

	protected String toAccessKey(String identity, String resource)
	{
		return identity + "_" + resource;
	}

	/**
	 * 获取{@linkplain AccessLog}。
	 * 
	 * @param accessKey
	 * @param nonNull
	 *            是否不应返回{@code null}
	 * @return
	 */
	protected abstract AccessLog getAccessLog(String accessKey, boolean nonNull);

	/**
	 * 更新{@linkplain AccessLog}。
	 * 
	 * @param accessKey
	 * @param accessLog
	 */
	protected abstract void updateAccessLog(String accessKey, AccessLog accessLog);

	/**
	 * 删除{@linkplain AccessLog}。
	 * 
	 * @param accessKey
	 * @param accessLog
	 */
	protected abstract void clearAccessLog(String accessKey);
}
