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

package org.datagear.web.util;

import javax.servlet.http.HttpServletRequest;

import org.datagear.util.Global;

/**
 * 检测新版本支持类。
 * 
 * @author datagear@163.com
 *
 */
public class DetectNewVersionScriptResolver
{
	/** 最新版本脚本地址 */
	protected static final String LATEST_VERSION_SCRIPT_LOCATION = Global.WEB_SITE + "/latest-version.js";

	/** 最新版本脚本地址 - https */
	protected static final String LATEST_VERSION_SCRIPT_LOCATION_HTTPS = Global.WEB_SITE_HTTPS + "/latest-version.js";

	/** Cookie中用于存储检测到新版本的名称 */
	private String detectedVersionCookieName = Global.NAME_SHORT_UCUS + "DETECTED_VERSION";

	public DetectNewVersionScriptResolver()
	{
		super();
	}

	public String getDetectedVersionCookieName()
	{
		return detectedVersionCookieName;
	}

	public void setDetectedVersionCookieName(String detectedVersionCookieName)
	{
		this.detectedVersionCookieName = detectedVersionCookieName;
	}

	/**
	 * 如果没有检测过新版本，将请求标记为：开启检测新版本功能。
	 * <p>
	 * 要使此功能起效，浏览器端应在检测新版本后设置{@linkplain #getDetectedVersionCookieName()}的值及有效期。
	 * </p>
	 * 
	 * @param request
	 */
	public boolean enableIf(HttpServletRequest request)
	{
		String detectedVersion = WebUtils.getCookieValue(request, getDetectedVersionCookieName());
		// 允许detectedVersion为空字符串，表明已检测过但是没有检测到
		boolean enable = (detectedVersion == null);

		request.setAttribute("enableDetectNewVersion", enable);

		return enable;
	}

	/**
	 * 将请求标记为：开启检测新版本功能
	 * 
	 * @param request
	 */
	public void enable(HttpServletRequest request)
	{
		request.setAttribute("enableDetectNewVersion", true);
	}

	/**
	 * 请求是否标记为：开启检测新版本功能
	 * 
	 * @param request
	 * @return
	 */
	public boolean isEnable(HttpServletRequest request)
	{
		return Boolean.TRUE.equals(request.getAttribute("enableDetectNewVersion"));
	}

	/**
	 * 如果{@linkplain #isEnable(HttpServletRequest)}为{@code true}，则构建检测新版本的JS脚本，否则返回空字符串{@code ""}。
	 * 
	 * @param request
	 * @return
	 */
	public String buildScriptIf(HttpServletRequest request)
	{
		String script = "";

		if (isEnable(request))
		{
			script = buildScript(request);
		}

		return script;
	}

	/**
	 * 如果{@code disable}为{@code true}，直接返回空字符串{@code ""}；否则，按照{@linkplain #buildScriptIf(HttpServletRequest)}的逻辑构建。
	 * 
	 * @param request
	 * @param disable
	 * @return
	 */
	public String buildScriptIf(HttpServletRequest request, boolean disable)
	{
		if (disable)
			return "";

		return buildScriptIf(request);
	}

	/**
	 * 构建检测新版本的JS脚本，格式为：
	 * <p>
	 * http：
	 * </p>
	 * <p>
	 * <code>
	 * &lt;script src="http://www.datagear.tech/latest-version.js" type="text/javascript" async&gt;&lt;/script&gt;
	 * </code>
	 * </p>
	 * <p>
	 * https：
	 * </p>
	 * <p>
	 * <code>
	 * &lt;script src="http://www.datagear.tech/latest-version.js" type="text/javascript" async&gt;&lt;/script&gt;
	 * </code>
	 * </p>
	 * 
	 * @param request
	 * @return
	 */
	public String buildScript(HttpServletRequest request)
	{
		String src = latestVersionScriptLocation();

		// 由于浏览器安全限制，在https域内不可以引用http资源，
		// 所以这里要特殊处理，即便官网目前没有开通https服务
		if (WebUtils.isSecureHttpScheme(request))
			src = latestVersionScriptLocationHttps();

		return "<script src=\"" + src + "\" type=\"text/javascript\" async></script>";
	}

	protected String latestVersionScriptLocation()
	{
		return LATEST_VERSION_SCRIPT_LOCATION;
	}

	protected String latestVersionScriptLocationHttps()
	{
		return LATEST_VERSION_SCRIPT_LOCATION_HTTPS;
	}
}
