/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Locale;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.management.domain.User;
import org.datagear.util.Global;
import org.datagear.util.IDUtil;
import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;
import org.datagear.web.security.AuthUser;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.ThemeResolver;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * Web工具集。
 * 
 * @author datagear@163.com
 *
 */
public class WebUtils
{
	public static final String COOKIE_PAGINATION_SIZE = "PAGINATION_PAGE_SIZE";

	/**
	 * Servlet规范中参数会话ID名称，当客户端不支持cookie时，则应使用此参数传递会话ID，格式为：/aa/bb;jsessionid=[id]
	 */
	public static final String PARAM_JSESSIONID = "jsessionid";

	/** Servlet环境中存储操作消息的关键字 */
	public static final String KEY_OPERATION_MESSAGE = "operationMessage";

	/** 父页面ID关键字 */
	public static final String KEY_PARENT_PAGE_ID = "ppid";

	/** 最新版本脚本地址 */
	public static final String LATEST_VERSION_SCRIPT_LOCATION = Global.WEB_SITE + "/latest-version.js";

	/** Cookie中存储是否已执行过本次的检测新版本的名称 */
	public static final String COOKIE_DETECT_NEW_VERSION_RESOLVED = "DETECT_NEW_VERSION_RESOLVED";

	/**
	 * 获取应用上下文路径。
	 * 
	 * @param request
	 * @return
	 */
	public static String getContextPath(HttpServletRequest request)
	{
		return request.getContextPath();
	}

	/**
	 * 获取应用服务URL。
	 * <p>
	 * 例如：<br>
	 * {@code http://192.168.1.1}<br>
	 * {@code http://192.168.1.1:8080}<br>
	 * {@code http://server-name:8080}<br>
	 * {@code https://192.168.1.1}<br>
	 * {@code https://192.168.1.1:8080}<br>
	 * {@code http://server-name:8080}<br>
	 * {@code https://server-name:8080}
	 * </p>
	 * 
	 * @param request
	 * @return
	 */
	public static String getServerURL(HttpServletRequest request)
	{
		String schema = request.getScheme();
		String name = request.getServerName();
		int port = request.getServerPort();

		String url = schema + "://" + name;

		// 省略默认http端口
		if ("http".equalsIgnoreCase(schema) && port == 80)
			;
		// 省略默认https端口
		else if ("https".equalsIgnoreCase(schema) && port == 443)
			;
		else
			url = url + ":" + port;

		return url;
	}

	/**
	 * 获取当前用户（认证用户或者匿名用户）。
	 * <p>
	 * 此方法不会返回{@code null}。
	 * </p>
	 * 
	 * @return
	 */
	public static User getUser()
	{
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return getUser(authentication);
	}

	/**
	 * 获取当前用户（认证用户或者匿名用户）。
	 * <p>
	 * 此方法不会返回{@code null}。
	 * </p>
	 * 
	 * @param authentication
	 * @return
	 */
	public static User getUser(Authentication authentication)
	{
		User user = null;

		Object principal = authentication.getPrincipal();

		if (principal instanceof User)
		{
			user = (User) principal;
		}
		else if (principal instanceof AuthUser)
		{
			AuthUser ou = (AuthUser) principal;
			user = ou.getUser();
		}

		if (user == null)
			throw new IllegalStateException();

		return user;
	}

	/**
	 * 获取操作消息。
	 * 
	 * @param request
	 * @return
	 */
	public static OperationMessage getOperationMessage(HttpServletRequest request)
	{
		return (OperationMessage) request.getAttribute(KEY_OPERATION_MESSAGE);
	}

	/**
	 * 设置异常操作消息。
	 * 
	 * @param request
	 * @param operationMessage
	 * @return
	 */
	public static void setOperationMessage(HttpServletRequest request, OperationMessage operationMessage)
	{
		request.setAttribute(KEY_OPERATION_MESSAGE, operationMessage);
	}

	/**
	 * 获取{@linkplain Cookie}，没有则返回{@code null}。
	 * 
	 * @param request
	 * @param cookieName
	 * @return
	 */
	public static String getCookieValue(HttpServletRequest request, String cookieName)
	{
		Cookie[] cookies = request.getCookies();

		if (cookies == null)
			return null;

		for (Cookie cookie : cookies)
		{
			if (cookieName.equals(cookie.getName()))
				return cookie.getValue();
		}

		return null;
	}

	/**
	 * 设置Cookie。
	 * 
	 * @param request
	 * @param response
	 * @param name
	 * @param value
	 * @param age
	 */
	public static void setCookie(HttpServletRequest request, HttpServletResponse response, String name, String value,
			int age)
	{
		setCookie(request, response, name, value, age, null);
	}

	/**
	 * 设置Cookie。
	 * 
	 * @param request
	 * @param response
	 * @param name
	 * @param value
	 * @param age
	 * @param path
	 *            为{@code null}时则设置为应用根路径
	 */
	public static void setCookie(HttpServletRequest request, HttpServletResponse response, String name, String value,
			int age, String path)
	{
		if (StringUtil.isEmpty(path))
			path = getContextPath(request);

		Cookie cookie = new Cookie(name, value);
		cookie.setPath(path);
		cookie.setMaxAge(age);

		response.addCookie(cookie);
	}

	/**
	 * 获取请求{@linkplain Locale}。
	 * 
	 * @param request
	 * @return
	 */
	public static Locale getLocale(HttpServletRequest request)
	{
		return LocaleContextHolder.getLocale();
	}

	/**
	 * 获取当前主题。
	 * 
	 * @param request
	 * @return
	 * @throws IllegalStateException
	 */
	public static String getTheme(HttpServletRequest request) throws IllegalStateException
	{
		return getThemeResolver(request).resolveThemeName(request);
	}

	/**
	 * 获取当前{@linkplain ThemeResolver}，没有则抛出{@linkplain IllegalStateException}异常。
	 * 
	 * @param request
	 * @return
	 * @throws IllegalStateException
	 */
	public static ThemeResolver getThemeResolver(HttpServletRequest request) throws IllegalStateException
	{
		ThemeResolver themeResolver = RequestContextUtils.getThemeResolver(request);

		if (themeResolver == null)
			throw new IllegalStateException("No ThemeResolver found: not in a DispatcherServlet request?");

		return themeResolver;
	}

	/**
	 * 以秒数滚动时间。
	 * 
	 * @param date
	 * @param seconds
	 * @return
	 */
	public static java.util.Date addSeconds(java.util.Date date, int seconds)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);

		calendar.add(Calendar.SECOND, seconds);

		return calendar.getTime();
	}

	/**
	 * 判断是否是JSON响应。
	 * 
	 * @param response
	 * @return
	 */
	public static boolean isJsonResponse(HttpServletResponse response)
	{
		String contentType = response.getContentType();
		if (contentType == null)
			contentType = "";
		else
			contentType = contentType.toLowerCase();

		return (contentType.indexOf("json") >= 0);
	}

	/**
	 * 判断请求是否是ajax请求。
	 * 
	 * @param request
	 * @return
	 */
	public static boolean isAjaxRequest(HttpServletRequest request)
	{
		// 是否ajax请求，jquery库ajax可以使用此方案判断
		boolean ajaxRequest = (request.getHeader("x-requested-with") != null);
		return ajaxRequest;
	}

	/**
	 * 生成页面ID。
	 * <p>只包含{@code 0-9}、{@code a-z}，且以字母开头</p>
	 * 
	 * @return
	 */
	public static String generatePageId()
	{
		return "pid" + IDUtil.toStringOfMaxRadix();
	}

	/**
	 * 获取父页面ID。
	 * <p>
	 * 如果没有定义，则返回空字符串。
	 * </p>
	 * 
	 * @param request
	 * @return
	 */
	public static String getParentPageId(HttpServletRequest request)
	{
		String parentPageId = request.getParameter(KEY_PARENT_PAGE_ID);

		if (parentPageId == null)
			parentPageId = (String) request.getAttribute(KEY_PARENT_PAGE_ID);

		if (parentPageId == null)
			parentPageId = "";

		return parentPageId;
	}

	/**
	 * 从请求获取布尔值。
	 * 
	 * @param request
	 * @param name
	 * @param defaultValue
	 * @return
	 */
	public static boolean getBooleanValue(HttpServletRequest request, String name, boolean defaultValue)
	{
		Object value = request.getAttribute(name);

		if (value != null)
		{
			if (value instanceof Boolean)
				return (Boolean) value;

			return ("true".equals(value.toString()) || "1".equals(value.toString()));
		}

		String paramValue = request.getParameter(name);

		if (paramValue != null)
			return ("true".equals(paramValue) || "1".equals(paramValue));

		return defaultValue;
	}

	/**
	 * 从请求获取字符串值。
	 * 
	 * @param request
	 * @param name
	 * @param defaultValue
	 * @return
	 */
	public static String getStringValue(HttpServletRequest request, String name, String defaultValue)
	{
		Object value = request.getAttribute(name);

		if (value != null)
		{
			if (value instanceof String)
				return (String) value;

			return value.toString();
		}

		String paramValue = request.getParameter(name);

		if (paramValue != null)
			return paramValue;

		return defaultValue;
	}

	/**
	 * 为指定URL添加{@linkplain #PARAM_JSESSIONID}参数。
	 * <p>
	 * 当要保持会话而客户端不支持cookie时，应使用此方法为URL添加会话ID参数。
	 * </p>
	 * 
	 * @param url
	 * @param sessionId
	 * @return
	 */
	public static String addJsessionidParam(String url, String sessionId)
	{
		return url + ";" + PARAM_JSESSIONID + "=" + sessionId;
	}

	/**
	 * 解码URL。
	 * <p>
	 * URL中的非ASCII字符会被浏览器编码，应使用此方法解码，可解决中文URL问题。
	 * </p>
	 * 
	 * @param url
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String decodeURL(String url) throws UnsupportedEncodingException
	{
		if (url == null)
			return null;

		return URLDecoder.decode(url, IOUtil.CHARSET_UTF_8);
	}

	/**
	 * 编码URL。
	 * 
	 * @param url
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String encodeURL(String url) throws UnsupportedEncodingException
	{
		if (url == null)
			return null;

		return URLEncoder.encode(url, IOUtil.CHARSET_UTF_8);
	}

	/**
	 * 获取客户端地址。
	 * 
	 * @param request
	 * @return
	 */
	public static String getClientAddress(HttpServletRequest request)
	{
		String remoteAddress = request.getRemoteAddr();

		String headerAddress = request.getHeader("x-forwarded-for");

		if (StringUtil.isEmpty(headerAddress) || "unknown".equalsIgnoreCase(headerAddress))
		{
			headerAddress = request.getHeader("Proxy-Client-IP");
		}

		if (StringUtil.isEmpty(headerAddress) || "unknown".equalsIgnoreCase(headerAddress))
		{
			headerAddress = request.getHeader("WL-Proxy-Client-IP");
		}

		if (!StringUtil.isEmpty(headerAddress) && !"unknown".equalsIgnoreCase(headerAddress))
		{
			String[] adds = StringUtil.split(headerAddress, ",", true);

			if (adds != null && adds.length > 0)
				headerAddress = adds[0];
		}

		return (StringUtil.isEmpty(headerAddress) || "unknown".equalsIgnoreCase(headerAddress) ? remoteAddress
				: headerAddress);
	}
	
	public static void setEnableDetectNewVersionRequest(HttpServletRequest request)
	{
		request.setAttribute("enableDetectNewVersion", true);
	}
	
	public static boolean isEnableDetectNewVersionRequest(HttpServletRequest request)
	{
		return Boolean.TRUE.equals(request.getAttribute("enableDetectNewVersion"));
	}
}
