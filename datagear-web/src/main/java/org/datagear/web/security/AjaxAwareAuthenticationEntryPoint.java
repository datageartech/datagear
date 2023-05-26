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

package org.datagear.web.security;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.web.controller.ErrorController;
import org.datagear.web.util.WebUtils;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

/**
 * Ajax敏感的{@linkplain AuthenticationEntryPoint}。
 * <p>
 * 对于Ajax请求，将重定向或者转发至错误信息页面，而不会执行{@linkplain #getAuthenticationEntryPoint()}的逻辑。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class AjaxAwareAuthenticationEntryPoint implements AuthenticationEntryPoint
{
	private AuthenticationEntryPoint authenticationEntryPoint;

	private boolean useForward = false;

	public AjaxAwareAuthenticationEntryPoint()
	{
		super();
	}

	public AjaxAwareAuthenticationEntryPoint(AuthenticationEntryPoint authenticationEntryPoint)
	{
		super();
		this.authenticationEntryPoint = authenticationEntryPoint;
	}

	public AuthenticationEntryPoint getAuthenticationEntryPoint()
	{
		return authenticationEntryPoint;
	}

	public void setAuthenticationEntryPoint(AuthenticationEntryPoint authenticationEntryPoint)
	{
		this.authenticationEntryPoint = authenticationEntryPoint;
	}

	public boolean isUseForward()
	{
		return useForward;
	}

	public void setUseForward(boolean useForward)
	{
		this.useForward = useForward;
	}

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException
	{
		if (isAjaxRequest(request))
		{
			if (this.isUseForward())
			{
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				RequestDispatcher dispatcher = request.getRequestDispatcher(ErrorController.ERROR_PAGE_URL);
				dispatcher.forward(request, response);
			}
			else
			{
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			}
		}
		else
			this.authenticationEntryPoint.commence(request, response, authException);
	}

	/**
	 * 是否ajax请求。
	 * 
	 * @param request
	 * @return
	 */
	protected boolean isAjaxRequest(HttpServletRequest request)
	{
		return WebUtils.isAjaxRequest(request);
	}
}
