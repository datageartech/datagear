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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

/**
 * 访问频次控制器。
 * <p>
 * 限定在过去秒数内（{@linkplain #getSeconds()}）允许访问资源的次数（{@linkplain #getFrequency()}）。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class AccessLatch
{
	/** 限定秒数：小于0 不限；0 不允许访问；大于0 限定过去秒数内 */
	private final int seconds;

	/** 限定次数：小于0 不限；0 不允许访问；大于0 限定次数 */
	private final int frequency;

	private final LoadingCache<AccessKey, AccessLog> _cache;

	/**
	 * 创建
	 * 
	 * @param seconds
	 *            限定在过去的秒数，{@code <0 }表示不限
	 * @param frequency
	 *            限定的访问次数，{@code <0 }表示不限
	 */
	public AccessLatch(int seconds, int frequency)
	{
		super();
		this.seconds = seconds;
		this.frequency = frequency;
		this._cache = Caffeine.newBuilder()
				.expireAfterAccess((this.seconds < 0 ? 60 : this.seconds * 2), TimeUnit.SECONDS)
				.build(new CacheLoader<AccessKey, AccessLog>()
				{
					@Override
					public AccessLog load(AccessKey key) throws Exception
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

	/**
	 * 剩余可访问次数。
	 * 
	 * @param identity
	 *            身份标识，比如：IP、用户ID
	 * @param resource
	 *            资源标识
	 * @return {@code <0 }表示不限；
	 */
	public int remain(String identity, String resource)
	{
		if (this.seconds < 0 || this.frequency < 0)
			return -1;

		AccessLog accessLog = getAccessLogNonNull(createAccessKey(identity, resource));

		synchronized (accessLog)
		{
			long time = System.currentTimeMillis() - this.seconds * 1000;
			int count = accessLog.countAfterOrEq(time);
			int remain = (this.frequency - count);

			return (remain < 0 ? 0 : remain);
		}
	}

	/**
	 * 尝试一次访问。
	 * 
	 * @param identity
	 *            身份标识，比如：IP、用户ID
	 * @param resource
	 *            资源标识
	 * @return {@code false}，失败，因为已超限；{@code true} 成功
	 */
	public boolean access(String identity, String resource)
	{
		if (this.seconds < 0 || this.frequency < 0)
			return true;

		long time = System.currentTimeMillis();
		long valveTime = time - this.seconds * 1000;

		AccessLog accessLog = getAccessLogNonNull(createAccessKey(identity, resource));

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

	/**
	 * 清除访问记录。
	 * 
	 * @param identity
	 * @return
	 */
	public void clear(String identity, String resource)
	{
		this._cache.invalidate(createAccessKey(identity, resource));
	}

	protected AccessKey createAccessKey(String identity, String resource)
	{
		return new AccessKey(identity, resource);
	}

	protected AccessLog getAccessLogNonNull(AccessKey accessKey)
	{
		AccessLog al = this._cache.get(accessKey);
		return al;
	}

	/**
	 * 是否已无剩余访问次数。
	 * 
	 * @param remain
	 * @return
	 */
	public static boolean isLatched(int remain)
	{
		return (remain == 0);
	}

	/**
	 * 是否剩余无限访问次数。
	 * 
	 * @param remain
	 * @return
	 */
	public static boolean isNonLatch(int remain)
	{
		return (remain < 0);
	}

	protected static class AccessKey implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private final String identity;
		private final String resource;

		public AccessKey(String identity, String resource)
		{
			super();
			this.identity = identity;
			this.resource = resource;
		}

		public String getIdentity()
		{
			return identity;
		}

		public String getResource()
		{
			return resource;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((identity == null) ? 0 : identity.hashCode());
			result = prime * result + ((resource == null) ? 0 : resource.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			AccessKey other = (AccessKey) obj;
			if (identity == null)
			{
				if (other.identity != null)
					return false;
			}
			else if (!identity.equals(other.identity))
				return false;
			if (resource == null)
			{
				if (other.resource != null)
					return false;
			}
			else if (!resource.equals(other.resource))
				return false;
			return true;
		}

		@Override
		public String toString()
		{
			return getClass().getSimpleName() + " [identity=" + identity + ", resource=" + resource + "]";
		}
	}

	protected static class AccessLog implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private List<Long> times = new ArrayList<Long>();

		public AccessLog()
		{
			super();
		}

		public void log(long time)
		{
			this.times.add(time);
		}

		public int countAfterOrEq(long time)
		{
			int count = 0;

			for (Long myTime : this.times)
			{
				if (myTime >= time)
					count++;
			}

			return count;
		}

		public void deleteBefore(long time)
		{
			List<Long> timesNew = new ArrayList<Long>(this.times.size());

			for (Long myTime : this.times)
			{
				if (myTime >= time)
					timesNew.add(myTime);
			}

			this.times = timesNew;
		}
	}
}
