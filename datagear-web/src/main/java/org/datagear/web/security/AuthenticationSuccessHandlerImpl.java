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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.management.domain.User;
import org.datagear.web.controller.LoginController;
import org.datagear.web.util.CheckCodeManager;
import org.datagear.web.util.WebUtils;
import org.datagear.web.util.accesslatch.UsernameLoginLatch;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * 用于ajax的{@linkplain AuthenticationSuccessHandler}。
 * 
 * @author datagear@163.com
 *
 */
public class AuthenticationSuccessHandlerImpl implements AuthenticationSuccessHandler
{
	private UsernameLoginLatch usernameLoginLatch;

	private CheckCodeManager checkCodeManager;

	public AuthenticationSuccessHandlerImpl(UsernameLoginLatch usernameLoginLatch, CheckCodeManager checkCodeManager)
	{
		super();
		this.usernameLoginLatch = usernameLoginLatch;
		this.checkCodeManager = checkCodeManager;
	}

	public UsernameLoginLatch getUsernameLoginLatch()
	{
		return usernameLoginLatch;
	}

	public void setUsernameLoginLatch(UsernameLoginLatch usernameLoginLatch)
	{
		this.usernameLoginLatch = usernameLoginLatch;
	}

	public CheckCodeManager getCheckCodeManager()
	{
		return checkCodeManager;
	}

	public void setCheckCodeManager(CheckCodeManager checkCodeManager)
	{
		this.checkCodeManager = checkCodeManager;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException
	{
		clearLoginCheckCode(request, response, authentication);
		clearUsernameLoginLatch(request, response, authentication);

		request.getRequestDispatcher("/login/success").forward(request, response);
	}

	protected void clearLoginCheckCode(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication)
	{
		this.checkCodeManager.removeCheckCode(request.getSession(), LoginController.CHECK_CODE_MODULE_LOGIN);
	}

	protected void clearUsernameLoginLatch(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication)
	{
		User user = WebUtils.getUser(authentication);
		this.usernameLoginLatch.clear(user.getName());
	}
}
