/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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

	private AuthenticationFailureHandlerImpl authenticationFailureHandlerImpl;

	private ApplicationProperties applicationProperties;

	private String loginCheckCodeParam = LoginController.LOGIN_PARAM_CHECK_CODE;

	private CheckCodeManager checkCodeManager;

	private AntPathRequestMatcher _loginProcessingRequestMatcher;

	public LoginLatchFilter()
	{
		super();
	}

	public LoginLatchFilter(String loginProcessingUrl,
			AuthenticationFailureHandlerImpl authenticationFailureHandlerImpl,
			ApplicationProperties applicationProperties, CheckCodeManager checkCodeManager)
	{
		super();
		setLoginProcessingUrl(loginProcessingUrl);
		this.authenticationFailureHandlerImpl = authenticationFailureHandlerImpl;
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

	public AuthenticationFailureHandlerImpl getAuthenticationFailureHandlerImpl()
	{
		return authenticationFailureHandlerImpl;
	}

	public void setAuthenticationFailureHandlerImpl(AuthenticationFailureHandlerImpl authenticationFailureHandlerImpl)
	{
		this.authenticationFailureHandlerImpl = authenticationFailureHandlerImpl;
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
			return;
		}

		if (!this.applicationProperties.isDisableLoginCheckCode())
		{
			String cc = getLoginCheckCode(req);

			if (!this.checkCodeManager.isCheckCode(req.getSession(), LoginController.CHECK_CODE_MODULE_LOGIN, cc))
			{
				this.authenticationFailureHandlerImpl.onAuthenticationFailure(req, res,
						new LoginCheckCodeErrorException(), false);

				return;
			}
		}

		if (AccessLatch.isLatched(this.authenticationFailureHandlerImpl.getIpLoginLatchRemain(req)))
		{
			this.authenticationFailureHandlerImpl.onAuthenticationFailure(req, res,
					new IpLoginLatchedException(), false);

			return;
		}

		if (AccessLatch.isLatched(this.authenticationFailureHandlerImpl.getUsernameLoginLatchRemain(req)))
		{
			this.authenticationFailureHandlerImpl.onAuthenticationFailure(req, res, new UsernameLoginLatchedException(),
					false);

			return;
		}

		chain.doFilter(request, response);
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
