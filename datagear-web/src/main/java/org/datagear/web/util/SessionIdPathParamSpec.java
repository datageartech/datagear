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

import javax.servlet.http.HttpServletRequest;

/**
 * 路径参数会话ID规范类。
 * <p>
 * 当客户端不支持cookie时，应使用URL路径参数传递会话ID，格式示例：<br>
 * <code>/aa/bb;jsessionid=【会话ID】?p0=v0</code>
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class SessionIdPathParamSpec
{
	/**
	 * Servlet规范中的路径参数会话ID名
	 */
	public static final String DEFAULT_SESSION_ID_PARAM_NAME = "jsessionid";

	private String sessionIdParamName = DEFAULT_SESSION_ID_PARAM_NAME;

	public SessionIdPathParamSpec()
	{
		super();
	}

	public String getSessionIdParamName()
	{
		return sessionIdParamName;
	}

	public void setSessionIdParamName(String sessionIdParamName)
	{
		this.sessionIdParamName = sessionIdParamName;
	}

	/**
	 * 为指定URL添加会话ID参数。
	 * <p>
	 * 当要保持会话而客户端不支持cookie时，应使用此方法为URL添加会话ID参数。
	 * </p>
	 * 
	 * @param url
	 * @param sessionId
	 * @return
	 */
	public String addSessionId(String url, String sessionId)
	{
		int qidx = url.indexOf('?');

		if (qidx < 0)
		{
			return url + ";" + this.sessionIdParamName + "=" + sessionId;
		}
		else
		{
			String sub0 = url.substring(0, qidx);
			String sub1 = url.substring(qidx, url.length());

			return sub0 + ";" + this.sessionIdParamName + "=" + sessionId + sub1;
		}
	}

	/**
	 * 为指定URL添加会话ID参数。
	 * 
	 * @param url
	 * @param session
	 * @return
	 */
	public String addSessionId(String url, HttpServletRequest request)
	{
		String sessionId = getSessionId(request);
		return addSessionId(url, sessionId);
	}

	/**
	 * 获取请求的会话ID。
	 * 
	 * @param request
	 * @return
	 */
	public String getSessionId(HttpServletRequest request)
	{
		return request.getSession().getId();
	}
}
