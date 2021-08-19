/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
