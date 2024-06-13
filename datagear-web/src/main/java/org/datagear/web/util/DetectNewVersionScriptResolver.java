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

import java.io.Serializable;

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

	/** 是否禁用 */
	private boolean disabled = false;

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

	public boolean isDisabled()
	{
		return disabled;
	}

	public void setDisabled(boolean disabled)
	{
		this.disabled = disabled;
	}

	/**
	 * 如果没有检测过新版本，尝试将将请求标记为：开启检测新版本功能。
	 * <p>
	 * 如果{@linkplain #isDisabled()}为{@code true}，将忽略。
	 * </p>
	 * <p>
	 * 要使此功能起效，浏览器端应在检测新版本后设置{@linkplain #getDetectedVersionCookieName()}的值及有效期。
	 * </p>
	 * 
	 * @param request
	 */
	public boolean enableIf(HttpServletRequest request)
	{
		boolean enable;

		if (this.isDisabled())
		{
			enable = false;
		}
		else
		{
			String detectedVersion = WebUtils.getCookieValue(request, getDetectedVersionCookieName());
			// 允许detectedVersion为空字符串，表明已检测过但是没有检测到
			enable = (detectedVersion == null);
		}

		request.setAttribute("enableDetectNewVersion", enable);

		return enable;
	}

	/**
	 * 尝试将请求标记为：开启检测新版本功能。
	 * <p>
	 * 如果{@linkplain #isDisabled()}为{@code true}，将忽略。
	 * </p>
	 * 
	 * @param request
	 * @return
	 */
	public boolean enable(HttpServletRequest request)
	{
		boolean enable = !this.isDisabled();
		request.setAttribute("enableDetectNewVersion", enable);

		return enable;
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
	 * 构建{@linkplain DetectResult}。
	 * <p>
	 * 如果{@linkplain #isEnable(HttpServletRequest)}为{@code false}，返回{@linkplain DetectResult#getScript()}将是空字符串：{@code ""}。
	 * </p>
	 * 
	 * @param request
	 * @return
	 */
	public DetectResult buildIf(HttpServletRequest request)
	{
		String script = "";

		if (isEnable(request))
		{
			script = buildScript(request);
		}

		return new DetectResult(getLatestVersionVar(request), getDetectedVersionCookieName(),
				getCurrentVersion(request), script);
	}

	/**
	 * 构建{@linkplain DetectResult}。
	 * 
	 * @param request
	 * @return
	 */
	public DetectResult build(HttpServletRequest request)
	{
		String script = buildScript(request);
		return new DetectResult(getLatestVersionVar(request), getDetectedVersionCookieName(),
				getCurrentVersion(request), script);
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
	protected String buildScript(HttpServletRequest request)
	{
		String src = getLatestVersionScriptLocation(request);

		// 由于浏览器安全限制，在https域内不可以引用http资源，
		// 所以这里要特殊处理，即便官网目前没有开通https服务
		if (WebUtils.isSecureHttpScheme(request))
			src = getLatestVersionScriptLocationHttps(request);

		return "<script src=\"" + src + "\" type=\"text/javascript\" async></script>";
	}

	/**
	 * 最新版脚本中的版本号变量名。
	 * 
	 * @param request
	 * @return
	 */
	protected String getLatestVersionVar(HttpServletRequest request)
	{
		return "DATA_GEAR_LATEST_VERSION";
	}

	/**
	 * 获取当前版本号。
	 * 
	 * @param request
	 * @return
	 */
	protected String getCurrentVersion(HttpServletRequest request)
	{
		return Global.VERSION;
	}

	/**
	 * 获取最新版脚本链接。
	 * 
	 * @param request
	 * @return
	 */
	protected String getLatestVersionScriptLocation(HttpServletRequest request)
	{
		return LATEST_VERSION_SCRIPT_LOCATION;
	}

	/**
	 * 获取最新版脚本链接-https。
	 * 
	 * @param request
	 * @return
	 */
	protected String getLatestVersionScriptLocationHttps(HttpServletRequest request)
	{
		return LATEST_VERSION_SCRIPT_LOCATION_HTTPS;
	}

	/**
	 * 检测结果。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class DetectResult implements Serializable
	{
		private static final long serialVersionUID = 1L;

		/** 最新版变量名 */
		private String latestVersionVar;

		/** Cookie中用于存储检测到新版本的名称 */
		private String versionCookieName;

		/** 当前版本 */
		private String currentVersion;

		/** 脚本 */
		private String script = "";

		public DetectResult()
		{
			super();
		}

		public DetectResult(String latestVersionVar, String versionCookieName, String currentVersion)
		{
			super();
			this.latestVersionVar = latestVersionVar;
			this.versionCookieName = versionCookieName;
			this.currentVersion = currentVersion;
		}

		public DetectResult(String latestVersionVar, String versionCookieName, String currentVersion, String script)
		{
			super();
			this.latestVersionVar = latestVersionVar;
			this.versionCookieName = versionCookieName;
			this.currentVersion = currentVersion;
			this.script = script;
		}

		public String getLatestVersionVar()
		{
			return latestVersionVar;
		}

		public void setLatestVersionVar(String latestVersionVar)
		{
			this.latestVersionVar = latestVersionVar;
		}

		public String getVersionCookieName()
		{
			return versionCookieName;
		}

		public void setVersionCookieName(String versionCookieName)
		{
			this.versionCookieName = versionCookieName;
		}

		public String getCurrentVersion()
		{
			return currentVersion;
		}

		public void setCurrentVersion(String currentVersion)
		{
			this.currentVersion = currentVersion;
		}

		public String getScript()
		{
			return script;
		}

		public void setScript(String script)
		{
			this.script = script;
		}
	}
}
