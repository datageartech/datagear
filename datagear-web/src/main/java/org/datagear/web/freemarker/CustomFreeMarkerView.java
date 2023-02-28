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

package org.datagear.web.freemarker;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.datagear.util.StringUtil;
import org.datagear.web.config.ApplicationProperties;
import org.datagear.web.util.WebUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerView;

import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;

/**
 * 自定义{@linkplain FreeMarkerView}，实现一些本系统需要的特性。
 * 
 * @author datagear@163.com
 *
 */
public class CustomFreeMarkerView extends FreeMarkerView
{
	/** 变量：是否是ajax请求 */
	public static final String VAR_IS_AJAX_REQUEST = "isAjaxRequest";

	/** 变量：应用根路径 */
	public static final String VAR_CONTEXT_PATH = "contextPath";

	/** 变量：页面ID关键字 */
	public static final String VAR_PAGE_ID = "pid";

	/** 变量：父页面ID关键字 */
	public static final String VAR_PARENT_PAGE_ID = WebUtils.KEY_PARENT_PAGE_ID;

	/** 变量：当前用户关键字 */
	public static final String VAR_CURRENT_USER = "currentUser";

	/** 变量：应用配置属性对象 */
	public static final String VAR_CONFIG_PROPERTIES = "configProperties";

	/** 变量：访问Java静态变量关键字 */
	public static final String VAR_STATICS = "statics";

	private static final BeansWrapper BEANS_WRAPPER = new BeansWrapperBuilder(
			Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS).build();

	public CustomFreeMarkerView()
	{
		super();
	}

	@Override
	protected void exposeHelpers(Map<String, Object> model, HttpServletRequest request) throws Exception
	{
		super.exposeHelpers(model, request);
		
		ApplicationProperties applicationProperties = getApplicationProperties(request);

		model.put(VAR_PAGE_ID, WebUtils.generatePageId());
		model.put(VAR_PARENT_PAGE_ID, WebUtils.getParentPageId(request));
		model.put(VAR_CONTEXT_PATH, WebUtils.getContextPath(request));
		model.put(VAR_IS_AJAX_REQUEST, WebUtils.isAjaxRequest(request));
		model.put(VAR_CURRENT_USER, WebUtils.getUser());
		model.put(VAR_STATICS, BEANS_WRAPPER.getStaticModels());
		model.put(VAR_CONFIG_PROPERTIES, applicationProperties);
		
		if(WebUtils.isEnableDetectNewVersionRequest(request))
			setDetectNewVersionScriptAttr(model, request, applicationProperties.isDisableDetectNewVersion());
		else
			setDetectNewVersionScriptAttr(model, request, true);
	}
	
	protected ApplicationProperties getApplicationProperties(HttpServletRequest request)
	{
		ApplicationContext ac = WebApplicationContextUtils.getWebApplicationContext(request.getServletContext());
		return ac.getBean(ApplicationProperties.class);
	}

	/**
	 * 将检测新版本的JS脚本以{@code detectNewVersionScript}关键字存入{@code model}。
	 * 
	 * @param model
	 * @param request
	 * @param disableDetectNewVersion
	 */
	protected void setDetectNewVersionScriptAttr(Map<String, Object> model, HttpServletRequest request,
			boolean disableDetectNewVersion)
	{
		String script = buildDetectNewVersionScript(request, disableDetectNewVersion);
		model.put("detectNewVersionScript", script);
	}

	/**
	 * 构建检测新版本的JS脚本，格式为：
	 * <p>
	 * <code>
	 * &lt;script src="http://www.datagear.tech/latest-version.js" type="text/javascript"&gt;&lt;/script&gt;
	 * </code>
	 * </p>
	 * <p>
	 * 如果{@code disableDetectNewVersion}为{@code true}，或者还未到达下一次检测时间，将直接返回空字符串：{@code ""}。
	 * </p>
	 * 
	 * @param request
	 * @param disableDetectNewVersion
	 * @return
	 */
	protected String buildDetectNewVersionScript(HttpServletRequest request, boolean disableDetectNewVersion)
	{
		String script = "";

		if (!disableDetectNewVersion)
		{
			String resolved = WebUtils.getCookieValue(request, WebUtils.COOKIE_DETECT_NEW_VERSION_RESOLVED);
			disableDetectNewVersion = StringUtil.toBoolean(resolved);
		}

		if (!disableDetectNewVersion)
			script = "<script src=\"" + WebUtils.LATEST_VERSION_SCRIPT_LOCATION + "\" type=\"text/javascript\"></script>";

		return script;
	}
}
