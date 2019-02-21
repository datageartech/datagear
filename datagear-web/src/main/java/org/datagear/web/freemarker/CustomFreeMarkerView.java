/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.web.freemarker;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.datagear.web.util.WebUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerView;

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

	/** 变量：父页面ID关键字 */
	public static final String VAR_PARENT_PAGE_ID = WebUtils.KEY_PARENT_PAGE_ID;

	public CustomFreeMarkerView()
	{
		super();
	}

	@Override
	protected void exposeHelpers(Map<String, Object> model, HttpServletRequest request) throws Exception
	{
		super.exposeHelpers(model, request);

		model.put(VAR_CONTEXT_PATH, request.getContextPath());
		model.put(VAR_IS_AJAX_REQUEST, WebUtils.isAjaxRequest(request));

		model.put(VAR_PAGE_ID, WebUtils.generatePageId());
		model.put(VAR_PARENT_PAGE_ID, WebUtils.getParentPageId(request));
	}
}
