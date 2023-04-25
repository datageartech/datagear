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

import java.util.List;

/**
 * 消息通道。
 * <p>
 * 支持向指定名称的通道发送消息、读取消息。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public interface MessageChannel
{
	/**
	 * 推入消息。
	 * 
	 * @param channel
	 * @param messages
	 */
	public void push(String channel, Object... messages);

	/**
	 * 拉取消息。
	 * 
	 * @param <T>
	 * @param channel
	 * @return 消息对象，返回{@code null}表示无消息
	 */
	public <T> T pull(String channel);

	/**
	 * 拉取最多指定数量的消息。
	 * 
	 * @param <T>
	 * @param channel
	 * @param count
	 * @return 消息对象列表，返回空列表表示无消息
	 */
	public <T> List<T> pull(String channel, int count);
}
