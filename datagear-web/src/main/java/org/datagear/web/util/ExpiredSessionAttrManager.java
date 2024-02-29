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

package org.datagear.web.util;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 带有过期特性的会话属性管理器。
 * 
 * @author datagear@163.com
 *
 */
public class ExpiredSessionAttrManager
{
	protected static final Logger LOGGER = LoggerFactory.getLogger(ExpiredSessionAttrManager.class);

	public ExpiredSessionAttrManager()
	{
		super();
	}

	/**
	 * 更新会话中{@linkplain ExpiredSessionAttr}的访问时间。
	 * 
	 * @param request
	 * @param name
	 */
	public void updateAccessTime(HttpServletRequest request, String name)
	{
		long time = System.currentTimeMillis();
		updateAccessTime(request, name, time);
	}

	/**
	 * 更新会话中{@linkplain ExpiredSessionAttr}的访问时间。
	 * 
	 * @param request
	 * @param name
	 */
	public void updateAccessTime(HttpServletRequest request, String name, long time)
	{
		HttpSession session = request.getSession();
		ExpiredSessionAttr attr = (ExpiredSessionAttr) session.getAttribute(name);

		if (attr == null)
			return;

		updateAccessTime(request, name, attr, time);
	}

	/**
	 * 更新会话中{@linkplain ExpiredSessionAttr}的访问时间。
	 * 
	 * @param request
	 * @param name
	 * @param attr
	 */
	public void updateAccessTime(HttpServletRequest request, String name, ExpiredSessionAttr attr)
	{
		long time = System.currentTimeMillis();
		updateAccessTime(request, name, attr, time);
	}

	/**
	 * 更新会话中{@linkplain ExpiredSessionAttr}的访问时间。
	 * 
	 * @param request
	 * @param name
	 * @param attr
	 * @param time
	 */
	public void updateAccessTime(HttpServletRequest request, String name, ExpiredSessionAttr attr, long time)
	{
		attr.setLastAccessTime(time);

		// 这里再次存入会话，为可能扩展的分布式会话提供支持
		setAttr(request, name, attr);
	}

	/**
	 * 设置会话中{@linkplain ExpiredSessionAttr}。
	 * 
	 * @param request
	 * @param name
	 * @param attr
	 */
	public void setAttr(HttpServletRequest request, String name, ExpiredSessionAttr attr)
	{
		request.getSession().setAttribute(name, attr);
	}

	/**
	 * 获取会话中{@linkplain ExpiredSessionAttr}。
	 * 
	 * @param <T>
	 * @param request
	 * @param name
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends ExpiredSessionAttr> T getAttr(HttpServletRequest request, String name)
	{
		return (T)request.getSession().getAttribute(name);
	}

	/**
	 * 移除会话中过期{@linkplain ExpiredSessionAttr}对象。
	 * 
	 * @param request
	 * @param timeout
	 *            过期毫秒数
	 * @param type
	 */
	public void removeExpired(HttpServletRequest request, long timeout, Class<? extends ExpiredSessionAttr> type)
	{
		HttpSession session = request.getSession();
		Enumeration<String> names = session.getAttributeNames();
		Set<String> removes = new HashSet<String>();
		long time = System.currentTimeMillis();
		int total = 0;

		while (names.hasMoreElements())
		{
			String name = names.nextElement();
			Object attr = session.getAttribute(name);

			if (attr != null && type.isAssignableFrom(attr.getClass()))
			{
				total++;

				ExpiredSessionAttr expAttr = (ExpiredSessionAttr) attr;
				if ((time - expAttr.getLastAccessTime()) > timeout)
					removes.add(name);
			}
		}

		int removesTotal = removes.size();
		int removeCount = 0;

		for (String remove : removes)
		{
			session.removeAttribute(remove);
			removeCount++;

			if (LOGGER.isDebugEnabled())
				LOGGER.debug("Removed expired session attribute '{}' of type [{}]： {}/{}, total {}", remove, type,
						removeCount, removesTotal, total);
		}
	}

	/**
	 * 有过期特性的会话属性。
	 */
	public interface ExpiredSessionAttr extends Serializable
	{
		/**
		 * 获取上次访问时间。
		 * 
		 * @return
		 */
		long getLastAccessTime();

		/**
		 * 设置上次访问时间。
		 */
		void setLastAccessTime(long time);
	}
}
