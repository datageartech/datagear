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

package org.datagear.web.util;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

/**
 * 消息通道。
 * <p>
 * 此类是线程安全的。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class MessageChannel
{
	private LoadingCache<String, LinkedBlockingQueue<Object>> _cache;

	public MessageChannel()
	{
		this(60 * 60);
	}

	public MessageChannel(int channelExpireSeconds)
	{
		super();

		// 消息通道只允许超时，不允许被其他情况移除
		this._cache = Caffeine.newBuilder().maximumSize(Integer.MAX_VALUE)
				.expireAfterAccess(channelExpireSeconds, TimeUnit.SECONDS)
				.build(new CacheLoader<String, LinkedBlockingQueue<Object>>()
				{
					@Override
					public LinkedBlockingQueue<Object> load(String key) throws Exception
					{
						return new LinkedBlockingQueue<Object>();
					}
				});
	}

	/**
	 * 推入消息。
	 * 
	 * @param channel
	 * @param messages
	 */
	public void push(String channel, Object... messages)
	{
		LinkedBlockingQueue<Object> queue = getChannelQueueNonNull(channel);

		for (int i = 0; i < messages.length; i++)
			queue.add(messages[i]);
	}

	/**
	 * 拉取消息。
	 * 
	 * @param <T>
	 * @param channel
	 * @return 消息对象，返回{@code null}表示无消息
	 */
	@SuppressWarnings("unchecked")
	public <T> T pull(String channel)
	{
		LinkedBlockingQueue<Object> queue = getChannelQueueNonNull(channel);

		return (T) queue.poll();
	}

	/**
	 * 拉取最多指定数量的消息。
	 * 
	 * @param <T>
	 * @param channel
	 * @param count
	 * @return 消息对象列表，返回空列表表示无消息
	 */
	public <T> List<T> pull(String channel, int count)
	{
		LinkedBlockingQueue<Object> queue = getChannelQueueNonNull(channel);

		List<T> list = new LinkedList<T>();

		for (int i = 0; i < count; i++)
		{
			@SuppressWarnings("unchecked")
			T msg = (T) queue.poll();

			if (msg != null)
				list.add(msg);
		}

		return list;
	}

	protected LinkedBlockingQueue<Object> getChannelQueueNonNull(String channel)
	{
		try
		{
			return this._cache.get(channel);
		}
		catch (Throwable e)
		{
			throw new MessageChannelException(e);
		}
	}
}
