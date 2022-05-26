/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.web.controller.LoginController;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

/**
 * 记住登录用户名的{@linkplain AuthenticationFailureHandler}。
 * 
 * @author datagear@163.com
 *
 */
public class RememberNameAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler
{
	public static final String SESSION_KEY_LOGIN_USER_NAME = RememberNameAuthenticationFailureHandler.class.getName()
			+ ".loginUserName";

	private String usernameParam = LoginController.LOGIN_PARAM_USER_NAME;

	public RememberNameAuthenticationFailureHandler()
	{
		super(LoginController.LOGIN_PAGE);
	}

	public String getUsernameParam()
	{
		return usernameParam;
	}

	public void setUsernameParam(String usernameParam)
	{
		this.usernameParam = usernameParam;
	}

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException
	{
		saveUserName(request, response, exception);
		super.onAuthenticationFailure(request, response, exception);
	}

	protected void saveUserName(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception)
	{
		String username = request.getParameter(this.getUsernameParam());
		request.getSession().setAttribute(SESSION_KEY_LOGIN_USER_NAME, username);
	}
}
