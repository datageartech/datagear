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

package org.datagear.web.util;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Locale;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.util.Global;
import org.datagear.util.IDUtil;
import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;
import org.springframework.context.i18n.LocaleContextHolder;
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

	/** Servlet环境中存储操作消息的关键字 */
	public static final String KEY_OPERATION_MESSAGE = "operationMessage";

	/** 父页面ID关键字 */
	public static final String KEY_PARENT_PAGE_ID = "ppid";

	/** 最新版本脚本地址 */
	public static final String LATEST_VERSION_SCRIPT_LOCATION = Global.WEB_SITE + "/latest-version.js";

	/** Cookie中存储是否已执行过本次的检测新版本的名称 */
	public static final String COOKIE_DETECT_NEW_VERSION_RESOLVED = Global.NAME_SHORT_UCUS + "DETECT_NEW_VERSION_RESOLVED";

	/**
	 * 首页URL。
	 */
	public static final String INDEX_PAGE_URL = "/";

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
	 * 获取应用首页路径。
	 * <p>
	 * 通常是：{@code "/"}、{@code "context-path/"}
	 * </p>
	 * 
	 * @param request
	 * @return
	 */
	public static String getIndexPath(HttpServletRequest request)
	{
		return request.getContextPath() + "/";
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
	 * 获取请求头的{@code Referer}值。
	 * 
	 * @param request
	 * @return
	 */
	public static String getReferer(HttpServletRequest request)
	{
		String ref = request.getHeader("Referer");
		return (ref == null ? "" : ref);
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
			path = getContextPath(request) + "/";

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

		return StringUtil.decodeURL(url, IOUtil.CHARSET_UTF_8);
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
		return StringUtil.encodeURL(url, IOUtil.CHARSET_UTF_8);
	}

	/**
	 * 编码路径URL。
	 * <p>
	 * 将字符串中除了'/'的字符都进行URL编码。
	 * </p>
	 * 
	 * @param url
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String encodePathURL(String url) throws UnsupportedEncodingException
	{
		return StringUtil.encodePathURL(url, IOUtil.CHARSET_UTF_8);
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

		String headerAddress = "";

		if (StringUtil.isEmpty(headerAddress) || "unknown".equalsIgnoreCase(headerAddress))
		{
			headerAddress = request.getHeader("x-forwarded-for");
		}

		if (StringUtil.isEmpty(headerAddress) || "unknown".equalsIgnoreCase(headerAddress))
		{
			headerAddress = request.getHeader("X-Real-IP");
		}

		if (StringUtil.isEmpty(headerAddress) || "unknown".equalsIgnoreCase(headerAddress))
		{
			headerAddress = request.getHeader("Remote-Host");
		}

		if (StringUtil.isEmpty(headerAddress) || "unknown".equalsIgnoreCase(headerAddress))
		{
			headerAddress = request.getHeader("Remote_Addr");
		}

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
	
	/**
	 * 解析请求路径中{@code pathPrefix}之后的路径名。
	 * 
	 * @param request
	 * @param pathPrefix
	 * @return
	 * @see {@linkplain #resolvePathAfter(String, String)}
	 */
	public static String resolvePathAfter(HttpServletRequest request, String pathPrefix)
	{
		return resolvePathAfter(request.getRequestURI(), pathPrefix);
	}
	
	/**
	 * 解析URL中{@code pathPrefix}之后的路径名。
	 * <p>
	 * 返回的路径名将会移除路径参数、请求参数、锚点参数，比如对于{@code "/a/b;jsessionid=156D0D8332?param=value#anchor"}将只会返回{@code "/a/b"}。
	 * </p>
	 * 
	 * @param url
	 * @param pathPrefix
	 *            为空或{@code null}，则返回整个请求路径
	 * @return 返回{@code null}表明路径不包含{@code pathPrefix}
	 */
	public static String resolvePathAfter(String url, String pathPrefix)
	{
		String re;

		if (StringUtil.isEmpty(pathPrefix))
		{
			re = url;
		}
		else if (url.endsWith(pathPrefix))
		{
			re = "";
		}
		else
		{
			int idx = url.indexOf(pathPrefix);

			if (idx < 0)
			{
				re = null;
			}
			else
			{
				re = url.substring(idx + pathPrefix.length());
			}
		}

		// 删除锚点“...#anchor”
		if (re != null)
		{
			int idx = re.indexOf('#');

			if (idx >= 0)
			{
				re = re.substring(0, idx);
			}
		}

		// 删除请求参数“...?p=v”
		if (re != null)
		{
			int idx = re.indexOf('?');

			if (idx >= 0)
			{
				re = re.substring(0, idx);
			}
		}

		// 删除路径参数“...;a=1;b=2”
		if (re != null)
		{
			int idx = re.indexOf(';');

			if (idx >= 0)
			{
				re = re.substring(0, idx);
			}
		}

		return re;
	}

	public static void setEnableDetectNewVersionRequest(HttpServletRequest request)
	{
		request.setAttribute("enableDetectNewVersion", true);
	}
	
	public static boolean isEnableDetectNewVersionRequest(HttpServletRequest request)
	{
		return Boolean.TRUE.equals(request.getAttribute("enableDetectNewVersion"));
	}

	/**
	 * 为URL添加请求参数。
	 * 
	 * @param url
	 * @param name
	 * @param value
	 * @return
	 */
	public static String addUrlParam(String url, String name, String value)
	{
		String re = url;

		// 锚点
		String anchor = null;

		int aidx = url.indexOf('#');
		if (aidx >= 0)
		{
			re = url.substring(0, aidx);
			anchor = url.substring(aidx, url.length());
		}

		int qidx = re.indexOf('?');

		if (qidx < 0)
			re += "?";
		else
			re += "&";

		re += name + "=" + value;

		if (anchor != null)
			re += anchor;

		return re;
	}
}
