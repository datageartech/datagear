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

import java.io.File;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.analysis.ChartPlugin;
import org.datagear.analysis.ChartPluginResource;
import org.datagear.analysis.support.html.HtmlChartPlugin;
import org.datagear.management.service.HtmlTplDashboardWidgetEntityService;
import org.datagear.util.StringUtil;
import org.datagear.web.analysis.ChartPluginManagerJsFactory;
import org.datagear.web.analysis.ChartPluginManagerJsFactory.ChartPluginManagerJs;
import org.datagear.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.request.WebRequest;

/**
 * 图表/看板展示功能中图表插件相关资源控制器。
 * 
 * @author datagear@163.com
 *
 */
@Controller
@RequestMapping("/vres/plugin")
public class ChartPluginVisualResController extends AbstractChartPluginAwareController implements ServletContextAware
{
	/**
	 * 加载图表插件JS脚本参数名：{@linkplain ChartPluginManagerJsFactory#getByKey(String)}需要的Key参数名
	 */
	public static final String MANAGER_JS_KEY_PARAM = "key";

	/**
	 * 加载图表插件JS脚本参数名：块号
	 */
	public static final String MANAGER_JS_BLOCK_PARAM = "block";

	/**
	 * 加载图表插件JS脚本参数名：过滤的插件API版本{@linkplain HtmlChartPlugin#getApiVersion()}
	 */
	public static final String API_VERSION_PARAM = "api";

	@Autowired
	private File tempDirectory;

	@Autowired
	private HtmlTplDashboardWidgetEntityService htmlTplDashboardWidgetEntityService;

	@Autowired
	private ChartPluginManagerJsFactory chartPluginManagerJsFactory;

	public ChartPluginVisualResController()
	{
		super();
	}

	public File getTempDirectory()
	{
		return tempDirectory;
	}

	public void setTempDirectory(File tempDirectory)
	{
		this.tempDirectory = tempDirectory;
	}

	public HtmlTplDashboardWidgetEntityService getHtmlTplDashboardWidgetEntityService()
	{
		return htmlTplDashboardWidgetEntityService;
	}

	public void setHtmlTplDashboardWidgetEntityService(
			HtmlTplDashboardWidgetEntityService htmlTplDashboardWidgetEntityService)
	{
		this.htmlTplDashboardWidgetEntityService = htmlTplDashboardWidgetEntityService;
	}

	public ChartPluginManagerJsFactory getChartPluginManagerJsFactory()
	{
		return chartPluginManagerJsFactory;
	}

	public void setChartPluginManagerJsFactory(ChartPluginManagerJsFactory chartPluginManagerJsFactory)
	{
		this.chartPluginManagerJsFactory = chartPluginManagerJsFactory;
	}

	@RequestMapping("/resource/{pluginId:.+}/**")
	public void chartPluginResource(HttpServletRequest request, HttpServletResponse response, WebRequest webRequest,
			@PathVariable("pluginId") String pluginId) throws Exception
	{
		ChartPlugin chartPlugin = getDirectoryHtmlChartPluginManager().get(pluginId);

		if (chartPlugin == null)
		{
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		String resName = resolvePathAfter(request, "/resource/" + pluginId + "/");
		// 处理可能的中文资源名
		resName = WebUtils.decodeURL(resName);

		ChartPluginResource resource = chartPlugin.getResource(resName);

		writeChartPluginResource(request, response, webRequest, chartPlugin, resource);
	}

	@RequestMapping("/chartPluginManager.js")
	public void chartPluginManagerJs(HttpServletRequest request, HttpServletResponse response, WebRequest webRequest,
			@RequestParam(value = MANAGER_JS_KEY_PARAM, required = false) String managerJsKey,
			@RequestParam(value = MANAGER_JS_BLOCK_PARAM, required = false) Integer managerJsBlock,
			@RequestParam(value = API_VERSION_PARAM, required = false) String apiVersion) throws Exception
	{
		ChartPluginManagerJs managerJs = null;

		if (!StringUtil.isEmpty(managerJsKey))
		{
			managerJs = this.chartPluginManagerJsFactory.getByKey(managerJsKey);
		}
		else
		{
			managerJs = this.chartPluginManagerJsFactory.latest(WebUtils.getLocale(request), apiVersion);
		}

		long lastModified = (managerJs == null ? -1 : managerJs.getLastModified());

		if (webRequest.checkNotModified(lastModified))
			return;

		response.setContentType(CONTENT_TYPE_JAVASCRIPT);
		setCacheControlNoCache(response);

		PrintWriter out = response.getWriter();

		if (managerJs != null)
		{
			if (managerJsBlock != null)
			{
				String script = (managerJs == null ? null : managerJs.getScript(managerJsBlock));

				if (script != null)
					out.write(script);
			}
			else
			{
				List<String> scripts = managerJs.getScripts();
				for (String script : scripts)
				{
					out.println(script);
				}
			}
		}
	}
}
