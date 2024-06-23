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
import org.datagear.web.util.OperationMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.request.WebRequest;

/**
 * 看板展示控制器（兼容5.0.0版本的{@code "/dashboard/*"}展示功能）。
 * 
 * @author datagear@163.com
 * @deprecated 兼容旧版本功能URL的控制器，这些功能URL将在未来版本移除
 *
 */
@Deprecated
@Controller
@RequestMapping(DashboardVisualCompatController.PATH_PREFIX)
public class DashboardVisualCompatController extends DashboardVisualController implements ServletContextAware
{
	/** 展示页路径前缀 */
	public static final String PATH_PREFIX = "/dashboard";

	public DashboardVisualCompatController()
	{
		super();
	}

	@Override
	@RequestMapping({ "/auth/{id}", "/auth/{id}/**" })
	public String showAuth(HttpServletRequest request, HttpServletResponse response, Model model,
			@PathVariable("id") String id,
			@RequestParam(value = DASHBOARD_SHOW_AUTH_PARAM_NAME, required = false) String name) throws Exception
	{
		return super.showAuth(request, response, model, id, name);
	}

	@Override
	@RequestMapping(value = "/authcheck", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> showAuthCheck(HttpServletRequest request, HttpServletResponse response,
			Model model, @RequestBody ShowAuthCheckForm form) throws Exception
	{
		return super.showAuthCheck(request, response, model, form);
	}

	@Override
	@RequestMapping({ "/show/{id}/", "/show/{id}" })
	public void show(HttpServletRequest request, HttpServletResponse response, Model model,
			@PathVariable("id") String id) throws Exception
	{
		super.show(request, response, model, id);
	}

	@Override
	@RequestMapping("/show/{id}/**")
	public void showResource(HttpServletRequest request, HttpServletResponse response, WebRequest webRequest,
			Model model, @PathVariable("id") String id) throws Exception
	{
		super.showResource(request, response, webRequest, model, id);
	}

	@Override
	@RequestMapping("/show/#{applicationProperties.getDashboardGlobalResUrlPrefixName()}/**")
	public void showGlobalResource(HttpServletRequest request, HttpServletResponse response, WebRequest webRequest)
			throws Exception
	{
		super.showGlobalResource(request, response, webRequest);
	}

	/**
	 * 看板数据。
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @param form
	 * @return
	 * @throws Exception
	 */
	@Override
	@RequestMapping(value = "/showData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ErrorMessageDashboardResult showData(HttpServletRequest request, HttpServletResponse response,
			Model model, @RequestBody DashboardQueryForm form) throws Exception
	{
		return super.showData(request, response, model, form);
	}

	@Override
	@RequestMapping(value = "/loadChart", produces = CONTENT_TYPE_JSON)
	public void loadChart(HttpServletRequest request, HttpServletResponse response, Model model,
			@RequestParam(LOAD_CHART_PARAM_DASHBOARD_ID) String dashboardId,
			@RequestParam(LOAD_CHART_PARAM_CHART_WIDGET_ID) String[] chartWidgetIds,
			@RequestParam(value = LOAD_CHART_FOR_EDITOR_PARAM, required = false) String loadChartForEditorStr)
			throws Throwable
	{
		super.loadChart(request, response, model, dashboardId, chartWidgetIds, loadChartForEditorStr);
	}

	@Override
	@RequestMapping(value = HEARTBEAT_TAIL_URL, produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public Map<String, Object> heartbeat(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(HEARTBEAT_PARAM_DASHBOARD_ID) String dashboardId) throws Throwable
	{
		return super.heartbeat(request, response, dashboardId);
	}

	@Override
	@RequestMapping(value = UNLOAD_TAIL_URL, produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public Map<String, Object> unloadDashboard(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(UNLOAD_PARAM_DASHBOARD_ID) String dashboardId) throws Throwable
	{
		return super.unloadDashboard(request, response, dashboardId);
	}

	@Override
	@RequestMapping(SERVER_TIME_TAIL_URL)
	public void serverTimeJs(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		super.serverTimeJs(request, response);
	}

	@Override
	protected String resolveShowPath(HttpServletRequest request, String dashboardId)
	{
		return PATH_PREFIX + "/show/" + dashboardId + "/";
	}

	@Override
	protected String resolveDataPath(HttpServletRequest request)
	{
		return PATH_PREFIX + "/showData";
	}

	@Override
	protected String resolveLoadChartPath(HttpServletRequest request)
	{
		return PATH_PREFIX + "/loadChart";
	}

	@Override
	protected String resolveHeartbeatPath(HttpServletRequest request)
	{
		return PATH_PREFIX + HEARTBEAT_TAIL_URL;
	}

	@Override
	protected String resolveUnloadPath(HttpServletRequest request)
	{
		return PATH_PREFIX + UNLOAD_TAIL_URL;
	}

	@Override
	protected String resolveGlobalResPath(HttpServletRequest request)
	{
		return PATH_PREFIX + "/show/" + getApplicationProperties().getDashboardGlobalResUrlPrefixName() + "/";
	}

	@Override
	protected String resolveAuthPath(HttpServletRequest request, String dashboardId)
	{
		return PATH_PREFIX + "/auth/" + dashboardId + "/";
	}

	@Override
	protected String resolveAuthSubmitPath(HttpServletRequest request)
	{
		return PATH_PREFIX + "/authcheck";
	}
}
