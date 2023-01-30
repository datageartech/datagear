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
	@RequestMapping("/analysis/chart/show/**")
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
	@RequestMapping("/analysis/dashboard/show/**")
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
