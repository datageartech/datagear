/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.freemarker;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.datagear.web.util.WebUtils;
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
	public static final String VAR_PAGE_ID = WebUtils.KEY_PAGE_ID;

	/** 变量：页面ID关键字 */
	public static final String VAR_PID = "pid";

	/** 变量：当前用户关键字 */
	public static final String VAR_CURRENT_USER = "currentUser";

	/** 变量：父页面ID关键字 */
	public static final String VAR_PARENT_PAGE_ID = WebUtils.KEY_PARENT_PAGE_ID;

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

		model.put(VAR_CONTEXT_PATH, WebUtils.getContextPath(request));
		model.put(VAR_IS_AJAX_REQUEST, WebUtils.isAjaxRequest(request));
		String pageId = WebUtils.generatePageId();
		model.put(VAR_PAGE_ID, pageId);
		model.put(VAR_PID, pageId);
		model.put(VAR_PARENT_PAGE_ID, WebUtils.getParentPageId(request));
		model.put(VAR_STATICS, BEANS_WRAPPER.getStaticModels());
		model.put(VAR_CURRENT_USER, WebUtils.getUser());
	}
}
