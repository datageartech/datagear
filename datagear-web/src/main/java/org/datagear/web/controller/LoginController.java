/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.datagear.web.config.ApplicationProperties;
import org.datagear.web.security.RememberNameAuthenticationFailureHandler;
import org.datagear.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 登录控制器。
 * 
 * @author datagear@163.com
 *
 */
@Controller
@RequestMapping("/login")
public class LoginController extends AbstractController
{
	/**
	 * 登录页
	 */
	public static final String LOGIN_PAGE = "/login";

	/**
	 * 登录参数：用户名
	 */
	public static final String LOGIN_PARAM_USER_NAME = "name";

	/**
	 * 登录参数：密码
	 */
	public static final String LOGIN_PARAM_PASSWORD = "password";

	/**
	 * 登录参数：记住登录
	 */
	public static final String LOGIN_PARAM_REMEMBER_ME = "rememberMe";

	@Autowired
	private ApplicationProperties applicationProperties;

	public LoginController()
	{
		super();
	}

	public ApplicationProperties getApplicationProperties()
	{
		return applicationProperties;
	}

	public void setApplicationProperties(ApplicationProperties applicationProperties)
	{
		this.applicationProperties = applicationProperties;
	}

	/**
	 * 打开登录界面。
	 * 
	 * @return
	 */
	@RequestMapping
	public String login(HttpServletRequest request, HttpServletResponse response)
	{
		// 参考org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler.saveException()
		AuthenticationException authenticationException = (AuthenticationException) request.getSession()
				.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);

		if (authenticationException != null)
			request.getSession().removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);

		request.setAttribute("loginUsername", resolveLoginUsername(request, response));
		request.setAttribute("authenticationFailed", (authenticationException != null));
		request.setAttribute("disableRegister", this.applicationProperties.isDisableRegister());
		request.setAttribute("currentUser", WebUtils.getUser(request, response).cloneNoPassword());
		setDetectNewVersionScriptAttr(request, response, this.applicationProperties.isDisableDetectNewVersion());

		return "/login";
	}

	protected String resolveLoginUsername(HttpServletRequest request, HttpServletResponse response)
	{
		HttpSession session = request.getSession();

		String username = (String) session.getAttribute(RegisterController.SESSION_KEY_REGISTER_USER_NAME);
		session.removeAttribute(RegisterController.SESSION_KEY_REGISTER_USER_NAME);

		if (isEmpty(username))
		{
			username = (String) session
					.getAttribute(RememberNameAuthenticationFailureHandler.SESSION_KEY_LOGIN_USER_NAME);
			session.removeAttribute(RememberNameAuthenticationFailureHandler.SESSION_KEY_LOGIN_USER_NAME);
		}

		if (username == null)
			username = "";

		return username;
	}

	/**
	 * 将用户名编码为可存储至Cookie的字符串。
	 * 
	 * @param username
	 * @return
	 */
	public static String encodeCookieUserName(String username)
	{
		if (username == null || username.isEmpty())
			return username;

		try
		{
			return URLEncoder.encode(username, "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			throw new ControllerException(e);
		}
	}

	/**
	 * 将{@linkplain #encodeCookieUserName(String)}的用户名解码。
	 * 
	 * @param username
	 * @return
	 */
	public static String decodeCookieUserName(String username)
	{
		if (username == null || username.isEmpty())
			return username;

		try
		{
			return WebUtils.decodeURL(username);
		}
		catch (UnsupportedEncodingException e)
		{
			throw new ControllerException(e);
		}
	}
}
