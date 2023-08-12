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
	 * 发送消息至指定通道末尾。
	 * 
	 * @param name
	 *            消息通道名称
	 * @param messages
	 */
	public void push(String name, Object... messages);

	/**
	 * 从指定通道开头读取一条消息并删除它。
	 * 
	 * @param <T>
	 * @param name
	 *            消息通道名称
	 * @return 消息对象，返回{@code null}表示无消息
	 */
	public <T> T poll(String name);

	/**
	 * 从指定通道开头读取最多指定数量的消息并删除它们。
	 * 
	 * @param <T>
	 * @param name
	 *            消息通道名称
	 * @param count
	 *            最多数量，{@code -1}表示所有消息
	 * @return 消息对象列表，返回空列表表示无消息
	 */
	public <T> List<T> poll(String name, int count);
}
