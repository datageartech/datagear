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

import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

/**
 * 简单访问频次控制器。
 * 
 * @author datagear@163.com
 *
 */
public class SimpleAccessLatch extends AbstractAccessLatch
{
	/** 限定秒数：小于0 不限；0 不允许访问；大于0 限定过去秒数内 */
	private final int seconds;

	/** 限定次数：小于0 不限；0 不允许访问；大于0 限定次数 */
	private final int frequency;

	private final LoadingCache<String, AccessLog> _cache;

	/**
	 * 创建
	 * 
	 * @param seconds
	 *            限定在过去的秒数，{@code <0 }表示不限
	 * @param frequency
	 *            限定的访问次数，{@code <0 }表示不限
	 */
	public SimpleAccessLatch(int seconds, int frequency)
	{
		super();
		this.seconds = seconds;
		this.frequency = frequency;
		this._cache = Caffeine.newBuilder()
				.expireAfterAccess((this.seconds < 0 ? 60 : this.seconds * 2), TimeUnit.SECONDS)
				.build(new CacheLoader<String, AccessLog>()
				{
					@Override
					public AccessLog load(String key) throws Exception
					{
						return new AccessLog();
					}
				});
	}

	/**
	 * 获取限定的过去秒数。
	 * 
	 * @return {@code <0 }表示不限
	 */
	public int getSeconds()
	{
		return seconds;
	}

	/**
	 * 获取限定访问次数。
	 * 
	 * @return {@code <0 }表示不限
	 */
	public int getFrequency()
	{
		return frequency;
	}

	@Override
	public int remain(String identity, String resource, long time)
	{
		if (this.seconds < 0 || this.frequency < 0)
			return -1;

		AccessLog accessLog = getAccessLogNonNull(toAccessKey(identity, resource));

		synchronized (accessLog)
		{
			time = time - this.seconds * 1000;
			int count = accessLog.countAfterOrEq(time);
			int remain = (this.frequency - count);

			return (remain < 0 ? 0 : remain);
		}
	}

	@Override
	public boolean access(String identity, String resource, long time)
	{
		if (this.seconds < 0 || this.frequency < 0)
			return true;

		long valveTime = time - this.seconds * 1000;

		AccessLog accessLog = getAccessLogNonNull(toAccessKey(identity, resource));

		synchronized (accessLog)
		{
			int count = accessLog.countAfterOrEq(valveTime);

			if (count >= this.frequency)
				return false;

			accessLog.log(time);
			accessLog.deleteBefore(valveTime);

			return true;
		}
	}

	@Override
	public void clear(String identity, String resource)
	{
		this._cache.invalidate(toAccessKey(identity, resource));
	}

	protected String toAccessKey(String identity, String resource)
	{
		return identity + "_" + resource;
	}

	protected AccessLog getAccessLogNonNull(String accessKey)
	{
		AccessLog al = this._cache.get(accessKey);
		return al;
	}
}
