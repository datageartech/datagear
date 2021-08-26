/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Value;
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
	@Value("${disableRegister}")
	private boolean disableRegister = false;

	@Value("${disableDetectNewVersion}")
	private boolean disableDetectNewVersion;

	public LoginController()
	{
		super();
	}

	public boolean isDisableRegister()
	{
		return disableRegister;
	}

	public void setDisableRegister(boolean disableRegister)
	{
		this.disableRegister = disableRegister;
	}

	public boolean isDisableDetectNewVersion()
	{
		return disableDetectNewVersion;
	}

	public void setDisableDetectNewVersion(boolean disableDetectNewVersion)
	{
		this.disableDetectNewVersion = disableDetectNewVersion;
	}

	/**
	 * 打开登录界面。
	 * 
	 * @return
	 */
	@RequestMapping
	public String login(HttpServletRequest request, HttpServletResponse response)
	{
		String loginUser = (String) request.getSession()
				.getAttribute(org.datagear.web.controller.RegisterController.SESSION_KEY_REGISTER_USER_NAME);

		// 参考org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler.saveException()
		AuthenticationException authenticationException = (AuthenticationException) request.getSession()
				.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);

		if (authenticationException != null)
			request.getSession().removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);

		if (loginUser == null)
			loginUser = "";

		request.setAttribute("loginUser", loginUser);
		request.setAttribute("authenticationFailed", (authenticationException != null));
		request.setAttribute("disableRegister", this.disableRegister);
		request.setAttribute("currentUser", WebUtils.getUser(request, response).cloneNoPassword());
		setDetectNewVersionScriptAttr(request, response, this.disableDetectNewVersion);

		return "/login";
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
			return URLDecoder.decode(username, "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			throw new ControllerException(e);
		}
	}
}
