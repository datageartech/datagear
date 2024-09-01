/*
 * Copyright 2018-present datagear.tech
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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.web.config.ApplicationProperties;
import org.datagear.web.controller.LoginController;
import org.datagear.web.util.CheckCodeManager;
import org.datagear.web.util.accesslatch.AccessLatch;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * 登录安全控制过滤器。
 * 
 * @author datagear@163.com
 *
 */
public class LoginLatchFilter implements Filter
{
	private String loginProcessingUrl;

	private AuthenticationFailureHandlerExt authenticationFailureHandlerExt;

	private ApplicationProperties applicationProperties;

	private String loginCheckCodeParam = LoginController.LOGIN_PARAM_CHECK_CODE;

	private CheckCodeManager checkCodeManager;

	private AntPathRequestMatcher _loginProcessingRequestMatcher;

	public LoginLatchFilter()
	{
		super();
	}

	public LoginLatchFilter(String loginProcessingUrl,
			AuthenticationFailureHandlerExt authenticationFailureHandlerExt,
			ApplicationProperties applicationProperties, CheckCodeManager checkCodeManager)
	{
		super();
		setLoginProcessingUrl(loginProcessingUrl);
		this.authenticationFailureHandlerExt = authenticationFailureHandlerExt;
		this.applicationProperties = applicationProperties;
		this.checkCodeManager = checkCodeManager;
	}

	public String getLoginProcessingUrl()
	{
		return loginProcessingUrl;
	}

	public void setLoginProcessingUrl(String loginProcessingUrl)
	{
		this.loginProcessingUrl = loginProcessingUrl;
		this._loginProcessingRequestMatcher = new AntPathRequestMatcher(loginProcessingUrl, "POST");
	}

	public AuthenticationFailureHandlerExt getAuthenticationFailureHandlerExt()
	{
		return authenticationFailureHandlerExt;
	}

	public void setAuthenticationFailureHandlerExt(AuthenticationFailureHandlerExt authenticationFailureHandlerExt)
	{
		this.authenticationFailureHandlerExt = authenticationFailureHandlerExt;
	}

	public ApplicationProperties getApplicationProperties()
	{
		return applicationProperties;
	}

	public void setApplicationProperties(ApplicationProperties applicationProperties)
	{
		this.applicationProperties = applicationProperties;
	}

	public String getLoginCheckCodeParam()
	{
		return loginCheckCodeParam;
	}

	public void setLoginCheckCodeParam(String loginCheckCodeParam)
	{
		this.loginCheckCodeParam = loginCheckCodeParam;
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
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException
	{
		HttpServletRequest req = (HttpServletRequest)request;
		HttpServletResponse res = (HttpServletResponse)response;
		
		if (!requiresCheck(req, res))
		{
			chain.doFilter(request, response);
		}
		else
		{
			doFilterLoginLatch(req, res, chain);
		}
	}

	protected void doFilterLoginLatch(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException
	{
		if (!this.applicationProperties.isDisableLoginCheckCode())
		{
			String cc = getLoginCheckCode(request);

			if (!this.checkCodeManager.isCheckCode(request.getSession(), LoginController.CHECK_CODE_MODULE_LOGIN, cc))
			{
				// 应该废弃使用过的验证码
				this.checkCodeManager.removeCheckCode(request.getSession(), LoginController.CHECK_CODE_MODULE_LOGIN);

				this.authenticationFailureHandlerExt.onAuthenticationFailure(request, response,
						new LoginCheckCodeErrorException(), false);

				return;
			}
		}

		if (AccessLatch.isLatched(this.authenticationFailureHandlerExt.getIpLoginLatchRemain(request)))
		{
			this.authenticationFailureHandlerExt.onAuthenticationFailure(request, response,
					new IpLoginLatchedException(), false);

			return;
		}

		if (AccessLatch.isLatched(this.authenticationFailureHandlerExt.getUsernameLoginLatchRemain(request)))
		{
			this.authenticationFailureHandlerExt.onAuthenticationFailure(request, response, new UsernameLoginLatchedException(),
					false);

			return;
		}

		chain.doFilter(request, response);

		// 应该废弃使用过的验证码
		if (!this.applicationProperties.isDisableLoginCheckCode())
		{
			this.checkCodeManager.removeCheckCode(request.getSession(), LoginController.CHECK_CODE_MODULE_LOGIN);
		}
	}

	protected String getLoginCheckCode(HttpServletRequest request)
	{
		return request.getParameter(this.loginCheckCodeParam);
	}

	protected boolean requiresCheck(HttpServletRequest request, HttpServletResponse response)
	{
		return this._loginProcessingRequestMatcher.matches(request);
	}
}
