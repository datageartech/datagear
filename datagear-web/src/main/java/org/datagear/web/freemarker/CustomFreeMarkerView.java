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

package org.datagear.web.freemarker;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.datagear.management.domain.User;
import org.datagear.util.Global;
import org.datagear.web.config.ApplicationProperties;
import org.datagear.web.security.AuthenticationUserGetter;
import org.datagear.web.util.DetectNewVersionScriptResolver;
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

	/** 变量：客户端缓存码 */
	public static final String VAR_CLIENT_CACHE_CODE = "clientCacheCode";

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

	/** 变量：检测新版本结果 */
	public static final String VAR_DETECT_NEW_VERSION_RESULT = "detectNewVersionResult";

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
		
		model.put(VAR_PAGE_ID, WebUtils.generatePageId());
		model.put(VAR_PARENT_PAGE_ID, WebUtils.getParentPageId(request));
		model.put(VAR_CONTEXT_PATH, WebUtils.getContextPath(request));
		model.put(VAR_CLIENT_CACHE_CODE, clientCacheCode(request));
		model.put(VAR_IS_AJAX_REQUEST, WebUtils.isAjaxRequest(request));
		model.put(VAR_STATICS, BEANS_WRAPPER.getStaticModels());

		ApplicationContext ac = WebApplicationContextUtils.getWebApplicationContext(request.getServletContext());
		exposeHelpersOfApplicationContext(model, request, ac);
	}

	protected void exposeHelpersOfApplicationContext(Map<String, Object> model, HttpServletRequest request,
			ApplicationContext applicationContext) throws Exception
	{
		ApplicationProperties applicationProperties = applicationContext.getBean(ApplicationProperties.class);
		AuthenticationUserGetter userGetter = applicationContext.getBean(AuthenticationUserGetter.class);
		DetectNewVersionScriptResolver detectNewVersionScriptResolver = applicationContext
				.getBean(DetectNewVersionScriptResolver.class);

		// 当部署在Tomcat时（8.5.61），对于类似“/static/not-exists.js”、“/chart/not-exists”的请求，
		// 在被导向至“/error”后，此时如果这里使用userGetter.getUser()会抛出空指针异常，
		// 因此，这里改为采用userGetter.getUserNullable()，由页面自己处理null。
		// 目前“/error”页面已改为不会用到用户信息，而其他页面没有发现null的情况。
		User currentUser = userGetter.getUserNullable();

		model.put(VAR_CURRENT_USER, currentUser);
		model.put(VAR_CONFIG_PROPERTIES, applicationProperties);
		model.put(VAR_DETECT_NEW_VERSION_RESULT, detectNewVersionScriptResolver.buildIf(request));
	}

	protected String clientCacheCode(HttpServletRequest request)
	{
		return Global.VERSION;
	}
}
