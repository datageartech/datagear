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

package org.datagear.web.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.analysis.support.ErrorMessageDashboardResult;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

/**
 * 图表展示控制器。
 * 
 * @author datagear@163.com
 * @deprecated 兼容旧版本功能URL的控制器，这些功能URL将在未来版本移除
 *
 */
@Deprecated
@Controller
@RequestMapping(ChartVisualCompatController.PATH_PREFIX)
public class ChartVisualCompatController extends ChartVisualController
{
	/** 展示页路径前缀 */
	public static final String PATH_PREFIX = "/chart";

	public ChartVisualCompatController()
	{
		super();
	}

	@Override
	@RequestMapping({ "/show/{id}/", "/show/{id}" })
	public void show(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@PathVariable("id") String id) throws Exception
	{
		super.show(request, response, model, id);
	}

	@Override
	@RequestMapping("/show/{id}/**")
	public void showResource(HttpServletRequest request, HttpServletResponse response, WebRequest webRequest,
			org.springframework.ui.Model model, @PathVariable("id") String id) throws Exception
	{
		super.showResource(request, response, webRequest, model, id);
	}

	@Override
	@RequestMapping(value = "/showData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ErrorMessageDashboardResult showData(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model, @RequestBody DashboardQueryForm form) throws Exception
	{
		return super.showData(request, response, model, form);
	}

	@Override
	@RequestMapping(value = HEARTBEAT_TAIL_URL, produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public Map<String, Object> heartbeat(HttpServletRequest request, HttpServletResponse response, String dashboardId)
			throws Throwable
	{
		return super.heartbeat(request, response, dashboardId);
	}

	@Override
	@RequestMapping(value = UNLOAD_TAIL_URL, produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public Map<String, Object> unloadDashboard(HttpServletRequest request, HttpServletResponse response,
			String dashboardId) throws Throwable
	{
		return super.unloadDashboard(request, response, dashboardId);
	}

	/**
	 * 解析展示路径。
	 * 
	 * @param request
	 * @param chartId
	 * @return
	 */
	@Override
	protected String resolveShowPath(HttpServletRequest request, String chartId)
	{
		return PATH_PREFIX + "/show/" + chartId + "/";
	}

	/**
	 * 解析加载数据路径。
	 * 
	 * @param request
	 * @return
	 */
	@Override
	protected String resolveDataPath(HttpServletRequest request)
	{
		return PATH_PREFIX + "/showData";
	}

	/**
	 * 解析加载图表路径。
	 * 
	 * @param request
	 * @return
	 */
	@Override
	protected String resolveLoadChartPath(HttpServletRequest request)
	{
		return PATH_PREFIX + "/loadChart";
	}

	/**
	 * 解析心跳路径。
	 * 
	 * @param request
	 * @return
	 */
	@Override
	protected String resolveHeartbeatPath(HttpServletRequest request)
	{
		return PATH_PREFIX + HEARTBEAT_TAIL_URL;
	}

	/**
	 * 解析卸载看板路径。
	 * 
	 * @param request
	 * @return
	 */
	@Override
	protected String resolveUnloadPath(HttpServletRequest request)
	{
		return PATH_PREFIX + UNLOAD_TAIL_URL;
	}
}
