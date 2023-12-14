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

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.datagear.util.StringUtil;

/**
 * 会话ID参数处理类。
 * <p>
 * 当客户端不支持cookie时，应使用URL参数传递会话ID，格式示例：<br>
 * <code>/aa/bb;jsessionid=【会话ID】?p0=v0</code>
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class SessionIdParamResolver
{
	/**
	 * Servlet规范中的路径参数会话ID名
	 */
	public static final String DEFAULT_SESSION_ID_PARAM_NAME = "jsessionid";

	private String sessionIdParamName = DEFAULT_SESSION_ID_PARAM_NAME;

	public SessionIdParamResolver()
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
	 * 
	 * @param url
	 * @param session
	 * @return
	 */
	public String addSessionId(String url, HttpServletRequest request)
	{
		String sessionId = getAddableSessionId(request);
		return addSessionId(url, sessionId);
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
		int aidx = url.indexOf('#');
		int splitIdx = -1;

		if (qidx >= 0)
		{
			splitIdx = qidx;
		}
		else if (aidx >= 0)
		{
			splitIdx = aidx;
		}

		if (splitIdx >= 0)
		{
			String sub0 = url.substring(0, splitIdx);
			String sub1 = url.substring(splitIdx, url.length());

			return sub0 + ";" + this.sessionIdParamName + "=" + sessionId + sub1;
		}
		else
		{
			return url + ";" + this.sessionIdParamName + "=" + sessionId;
		}
	}

	/**
	 * 获取可添加至URL参数的当前会话ID，用于{@linkplain #addSessionId(String, String)}、{@linkplain #addSessionId(String, HttpServletRequest)}。
	 * 
	 * @param request
	 * @return
	 */
	public String getAddableSessionId(HttpServletRequest request)
	{
		return request.getSession().getId();
	}

	/**
	 * 解析客户端请求的会话ID。
	 * 
	 * @param request
	 * @return
	 */
	public List<String> resolveSessionIds(HttpServletRequest request)
	{
		return resolveSessionIds(request.getRequestURI());
	}

	/**
	 * 解析客户端请求的会话ID。
	 * 
	 * @param url
	 * @return
	 */
	public List<String> resolveSessionIds(String url)
	{
		List<String> re = new ArrayList<>(3);

		int startIdx = url.indexOf(';');

		if (startIdx < 0)
			return re;

		startIdx = startIdx + 1;

		if (startIdx >= url.length())
			return re;

		int endIdx = url.indexOf('?', startIdx);

		if (endIdx < 0)
			endIdx = url.indexOf('#', startIdx);

		if (endIdx < 0)
			endIdx = url.length();
		
		if(endIdx <= startIdx)
			return re;
		
		String pathParamStr = url.substring(startIdx, endIdx);
		String[] pathParamStrs = StringUtil.split(pathParamStr, ";", false);

		if (pathParamStrs == null || pathParamStrs.length == 0)
			return re;

		for (String pp : pathParamStrs)
		{
			int eqIdx = pp.indexOf('=');
			if (eqIdx >= 0 && (eqIdx + 1) < pp.length())
			{
				String name = pp.substring(0, eqIdx);
				String value = pp.substring(eqIdx + 1);

				if (this.sessionIdParamName.equals(name) && !StringUtil.isEmpty(value))
				{
					re.add(value);
				}
			}
		}

		return re;
	}
}
