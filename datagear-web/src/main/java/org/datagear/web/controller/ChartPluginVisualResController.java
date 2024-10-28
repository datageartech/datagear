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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.analysis.ChartPlugin;
import org.datagear.analysis.ChartPluginResource;
import org.datagear.analysis.support.html.HtmlChartPlugin;
import org.datagear.analysis.support.html.HtmlChartPluginScriptObjectWriter;
import org.datagear.analysis.support.html.HtmlTplDashboardWidgetRenderer;
import org.datagear.management.service.HtmlTplDashboardWidgetEntityService;
import org.datagear.util.StringUtil;
import org.datagear.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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
	@Autowired
	private File tempDirectory;

	@Autowired
	private HtmlTplDashboardWidgetEntityService htmlTplDashboardWidgetEntityService;

	private HtmlChartPluginScriptObjectWriter htmlChartPluginScriptObjectWriter = new HtmlChartPluginScriptObjectWriter();

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

	public HtmlChartPluginScriptObjectWriter getHtmlChartPluginScriptObjectWriter()
	{
		return htmlChartPluginScriptObjectWriter;
	}

	public void setHtmlChartPluginScriptObjectWriter(
			HtmlChartPluginScriptObjectWriter htmlChartPluginScriptObjectWriter)
	{
		this.htmlChartPluginScriptObjectWriter = htmlChartPluginScriptObjectWriter;
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
	public void chartPluginManagerJs(HttpServletRequest request, HttpServletResponse response, WebRequest webRequest)
			throws Exception
	{
		List<HtmlChartPlugin> plugins = getDirectoryHtmlChartPluginManager().getAll(HtmlChartPlugin.class);
		List<HtmlChartPlugin> htmlChartPlugins = new ArrayList<>(plugins.size());
		long lastModified = -1;

		if (plugins != null)
		{
			for (HtmlChartPlugin plugin : plugins)
			{
				htmlChartPlugins.add(plugin);
				lastModified = Math.max(lastModified, plugin.getLastModified());
			}
		}

		HtmlTplDashboardWidgetRenderer renderer = getHtmlTplDashboardWidgetEntityService()
				.getHtmlTplDashboardWidgetRenderer();

		HtmlChartPlugin htmlChartPluginForGetWidgetException = renderer.getHtmlChartPluginForGetWidgetException();
		htmlChartPlugins.add(htmlChartPluginForGetWidgetException);
		lastModified = Math.max(lastModified, htmlChartPluginForGetWidgetException.getLastModified());

		if (webRequest.checkNotModified(lastModified))
			return;

		Locale locale = WebUtils.getLocale(request);
		response.setContentType(CONTENT_TYPE_JAVASCRIPT);
		setCacheControlNoCache(response);

		PrintWriter out = response.getWriter();

		out.println("(function(global)");
		out.println("{");

		out.println("var chartFactory = (global.chartFactory || (global.chartFactory = {}));");
		out.println(
				"var chartPluginManager = (chartFactory.chartPluginManager || (chartFactory.chartPluginManager = {}));");
		out.println("chartPluginManager.plugins = (chartPluginManager.plugins || {});");

		// @deprecated 兼容1.8.1版本的window.chartPluginManager变量名，未来版本会移除
		out.println();
		out.println("global.chartPluginManager = chartPluginManager;");

		out.println();
		out.println("chartPluginManager.get = function(id){ return this.plugins[id]; };");
		out.println();

		for (int i = 0, len = htmlChartPlugins.size(); i < len; i++)
		{
			HtmlChartPlugin plugin = htmlChartPlugins.get(i);
			String pluginVar = "plugin" + i;

			this.htmlChartPluginScriptObjectWriter.write(out, plugin, pluginVar, locale);

			// @deprecated
			// 兼容4.0.0版本的"+HtmlChartPlugin.PROPERTY_RENDERER_OLD+"属性名，未来版本会移除
			out.println(pluginVar + "." + HtmlChartPlugin.PROPERTY_RENDERER_OLD + " = " + pluginVar + "."
					+ HtmlChartPlugin.PROPERTY_RENDERER + ";");

			out.println("chartPluginManager.plugins[" + StringUtil.toJavaScriptString(plugin.getId()) + "] = "
					+ pluginVar + ";");
		}

		out.println("})(this);");
	}
}
