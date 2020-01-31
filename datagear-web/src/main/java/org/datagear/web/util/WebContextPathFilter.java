/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.web.util;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * {@linkplain WebContextPath}过滤器。
 * <p>
 * 此类使用{@linkplain #getWebContextPath()}调用{@linkplain WebContextPath#setWebContextPath(javax.servlet.http.HttpServletRequest, WebContextPath)}，
 * 使整个请求处理过程都可以通过{@linkplain WebContextPath#getServletContextPath(javax.servlet.http.HttpServletRequest)}获取这里的{@linkplain #getWebContextPath()}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class WebContextPathFilter implements Filter
{
	private WebContextPath webContextPath = new WebContextPath();

	public WebContextPathFilter()
	{
		super();
	}

	public WebContextPath getWebContextPath()
	{
		return webContextPath;
	}

	public void setWebContextPath(WebContextPath webContextPath)
	{
		this.webContextPath = webContextPath;
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException
	{
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException
	{
		HttpServletRequest req = (HttpServletRequest) request;
		WebContextPath.setWebContextPath(req, this.webContextPath);

		chain.doFilter(request, response);
	}

	@Override
	public void destroy()
	{
	}
}
