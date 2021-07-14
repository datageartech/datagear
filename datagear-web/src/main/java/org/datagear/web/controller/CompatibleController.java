/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.util.StringUtil;
import org.datagear.web.util.WebUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 兼容旧版URL控制器。
 * 
 * @author datagear@163.com
 * @deprecated 兼容旧版本功能URL的控制器，这些功能URL将在未来版本移除
 */
@Deprecated
@Controller
public class CompatibleController extends AbstractController
{
	/**
	 * 展示图表。
	 * 
	 * @param request
	 * @param response
	 * @param webRequest
	 * @param model
	 * @param id
	 * @throws Exception
	 * 
	 * @deprecated 兼容2.6.0版本的图表展示功能URL，因为展示链接可能已被被外部系统iframe嵌入，为了降低升级风险，这里做兼容支持，将在未来版本移除
	 */
	@Deprecated
	@RequestMapping("/analysis/chart/show/**/*")
	public void showChart(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model) throws Exception
	{
		String redirectTo = resolveRedirectToURL(request, "/analysis/chart/show/", "/chart/show/");
		response.sendRedirect(redirectTo);
	}

	/**
	 * 展示看板。
	 * 
	 * @param request
	 * @param response
	 * @param webRequest
	 * @param model
	 * @param id
	 * @throws Exception
	 * 
	 * @deprecated 兼容2.6.0版本的看板展示功能URL，因为展示链接可能已被被外部系统iframe嵌入，为了降低升级风险，这里做兼容支持，将在未来版本移除
	 */
	@Deprecated
	@RequestMapping("/analysis/dashboard/show/**/*")
	public void showDashboard(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model)
			throws Exception
	{
		String redirectTo = resolveRedirectToURL(request, "/analysis/dashboard/show/", "/dashboard/show/");
		response.sendRedirect(redirectTo);
	}

	protected String resolveRedirectToURL(HttpServletRequest request, String sourceUrlPrefix, String redirectUrlPrefix)
	{
		String redirectTo = WebUtils.getContextPath(request) + redirectUrlPrefix;

		String resName = resolvePathAfter(request, sourceUrlPrefix);

		if (!StringUtil.isEmpty(resName))
			redirectTo += resName;

		String qs = request.getQueryString();
		if (!StringUtil.isEmpty(qs))
			redirectTo = redirectTo + "?" + qs;
		
		return redirectTo;
	}
}
