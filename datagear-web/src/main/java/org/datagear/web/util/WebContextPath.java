/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.web.util;

import javax.servlet.http.HttpServletRequest;

/**
 * Web上下文路径。
 * 
 * @author datagear@163.com
 *
 */
public class WebContextPath
{
	private String subContextPath = "";

	public WebContextPath()
	{
		super();
	}

	public String getSubContextPath()
	{
		return subContextPath;
	}

	public void setSubContextPath(String subContextPath)
	{
		if (!isValidSubContextPath(subContextPath))
			throw new IllegalArgumentException("[extPath] must be start with '/' and not end with '/'");

		this.subContextPath = subContextPath;
	}

	/**
	 * 获取Web上下文路径。
	 * 
	 * @param request
	 * @return
	 */
	public String get(HttpServletRequest request)
	{
		String contextPath = getServletContextPath(request);
		return contextPath + this.subContextPath;
	}

	/**
	 * 连接Web上下文路径和子路径。
	 * 
	 * @param request
	 * @param path
	 * @return
	 */
	public String concat(HttpServletRequest request, String path)
	{
		if (!path.startsWith("/"))
			throw new IllegalArgumentException("[path] must be start with '/'");

		return get(request) + path;
	}

	/**
	 * 获取指定路径相对于Servlet上下文的路径。
	 * 
	 * @param path
	 * @return
	 */
	public String getPath(String path)
	{
		if (!path.startsWith("/"))
			path = "/" + path;

		return this.subContextPath + path;
	}

	/**
	 * 获取Servlet上下文路径。
	 * 
	 * @param request
	 * @return
	 */
	public String getServletContextPath(HttpServletRequest request)
	{
		String contextPath = request.getContextPath();

		// 处理某些不规范的Servlet容器情况
		if ("/".equals(contextPath))
			contextPath = "";

		return contextPath;
	}

	/**
	 * 是否合法的子上下文路径。
	 * 
	 * @param subContextPath
	 * @return
	 */
	public static boolean isValidSubContextPath(String subContextPath)
	{
		if (subContextPath == null)
			return false;

		if (subContextPath.isEmpty())
			return true;

		return subContextPath.matches("^/[^/]+(/[^/]+)*$");
	}

	/**
	 * 获取{@linkplain WebContextPath}。
	 * <p>
	 * 此方法不会返回{@code null}。
	 * </p>
	 * 
	 * @param request
	 * @return
	 */
	public static WebContextPath getWebContextPath(HttpServletRequest request)
	{
		WebContextPath webContextPath = (WebContextPath) request.getAttribute(REQUEST_ATTR_NAME);
		if (webContextPath == null)
		{
			webContextPath = new WebContextPath();
			setWebContextPath(request, webContextPath);
		}

		return webContextPath;
	}

	/**
	 * 设置{@linkplain WebContextPath}。
	 * 
	 * @param request
	 * @param webContextPath
	 */
	public static void setWebContextPath(HttpServletRequest request, WebContextPath webContextPath)
	{
		request.setAttribute(REQUEST_ATTR_NAME, webContextPath);
	}

	public static final String REQUEST_ATTR_NAME = WebContextPath.class.getName();
}
