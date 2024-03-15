/*
 * Copyright 2018-present datagear.tech
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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * 简单访问频次控制器。
 * 
 * @author datagear@163.com
 *
 */
public class SimpleAccessLatch extends AbstractAccessLatch
{
	private final Cache<String, AccessLog> _cache;

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
		super(seconds, frequency);
		this._cache = Caffeine.newBuilder()
				.expireAfterAccess((seconds < 0 ? 60 : seconds * 2), TimeUnit.SECONDS)
				.build();
	}

	@Override
	public void setSeconds(int seconds)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setFrequency(int frequency)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	protected AccessLog getAccessLog(String accessKey, boolean nonNull)
	{
		if (nonNull)
		{
			return this._cache.get(accessKey, (key) ->
			{
				return new AccessLog();
			});
		}
		else
		{
			return this._cache.getIfPresent(accessKey);
		}
	}

	@Override
	protected void updateAccessLog(String accessKey, AccessLog accessLog)
	{
		this._cache.put(accessKey, accessLog);
	}

	@Override
	protected void clearAccessLog(String accessKey)
	{
		this._cache.invalidate(accessKey);
	}
}
