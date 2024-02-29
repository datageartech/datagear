/*
 * Copyright 2018-2024 datagear.tech
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

/**
 * 访问频次控制器。
 * <p>
 * 限定在指定时间段内允许访问资源的次数。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public interface AccessLatch
{
	/**
	 * 获取当前时间点的剩余可访问次数。
	 * 
	 * @param identity
	 *            身份标识，比如：IP、用户ID
	 * @param resource
	 *            资源标识
	 * @return {@code <0 }表示不限；
	 */
	int remain(String identity, String resource);

	/**
	 * 获取指定时间点的剩余可访问次数。
	 * 
	 * @param identity
	 *            身份标识，比如：IP、用户ID
	 * @param resource
	 *            资源标识
	 * @param time
	 *            时间点
	 * @return {@code <0 }表示不限；
	 */
	int remain(String identity, String resource, long time);

	/**
	 * 在当前时间尝试一次访问。
	 * 
	 * @param identity
	 *            身份标识，比如：IP、用户ID
	 * @param resource
	 *            资源标识
	 * @return {@code false}，失败，因为已超限；{@code true} 成功
	 */
	boolean access(String identity, String resource);

	/**
	 * 在指定时间点尝试一次访问。
	 * 
	 * @param identity
	 *            身份标识，比如：IP、用户ID
	 * @param resource
	 *            资源标识
	 * @param time
	 *            时间点
	 * @return {@code false}，失败，因为已超限；{@code true} 成功
	 */
	boolean access(String identity, String resource, long time);

	/**
	 * 清除访问记录。
	 * 
	 * @param identity
	 * @return
	 */
	void clear(String identity, String resource);

	/**
	 * 是否已无剩余访问次数。
	 * 
	 * @param remain
	 * @return
	 */
	static boolean isLatched(int remain)
	{
		return (remain == 0);
	}

	/**
	 * 是否剩余无限访问次数。
	 * 
	 * @param remain
	 * @return
	 */
	static boolean isNonLatch(int remain)
	{
		return (remain < 0);
	}

	/**
	 * 访问日志。
	 * 
	 * @author datagear@163.com
	 *
	 */
	class AccessLog implements Serializable
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
