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

import org.datagear.web.util.accesslatch.AccessLatch;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * 登录频次控制过滤器。
 * 
 * @author datagear@163.com
 *
 */
public class LoginLatchFilter implements Filter
{
	private String loginProcessingUrl;

	private AuthenticationFailureHandlerImpl authenticationFailureHandlerImpl;

	private AntPathRequestMatcher _loginProcessingRequestMatcher;

	public LoginLatchFilter()
	{
		super();
	}

	public LoginLatchFilter(String loginProcessingUrl,
			AuthenticationFailureHandlerImpl authenticationFailureHandlerImpl)
	{
		super();
		setLoginProcessingUrl(loginProcessingUrl);
		this.authenticationFailureHandlerImpl = authenticationFailureHandlerImpl;
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

		if (AccessLatch.isLatched(this.authenticationFailureHandlerImpl.getIpLoginLatchRemain(req)))
		{
			this.authenticationFailureHandlerImpl.onAuthenticationFailure(req, res,
					new IpLoginLatchedException(), false);
		}
		else if (AccessLatch.isLatched(this.authenticationFailureHandlerImpl.getUsernameLoginLatchRemain(req)))
		{
				this.authenticationFailureHandlerImpl.onAuthenticationFailure(req, res,
						new UsernameLoginLatchedException(), false);
		}
		else
			chain.doFilter(request, response);
	}

	protected boolean requiresCheck(HttpServletRequest request, HttpServletResponse response)
	{
		return this._loginProcessingRequestMatcher.matches(request);
	}
}
