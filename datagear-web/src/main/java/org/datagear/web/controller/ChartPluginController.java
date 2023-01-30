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

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.analysis.ChartPlugin;
import org.datagear.analysis.ChartPluginResource;
import org.datagear.analysis.support.ChartPluginCategorizationResolver.Categorization;
import org.datagear.analysis.support.html.HtmlChartPlugin;
import org.datagear.analysis.support.html.HtmlChartPluginLoadException;
import org.datagear.analysis.support.html.HtmlChartPluginLoader;
import org.datagear.analysis.support.html.HtmlChartPluginScriptObjectWriter;
import org.datagear.analysis.support.html.HtmlTplDashboardWidgetRenderer;
import org.datagear.management.service.HtmlTplDashboardWidgetEntityService;
import org.datagear.persistence.PagingQuery;
import org.datagear.util.FileUtil;
import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;
import org.datagear.web.util.OperationMessage;
import org.datagear.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;

/**
 * 图表插件控制器。
 * 
 * @author datagear@163.com
 *
 */
@Controller
@RequestMapping("/chartPlugin")
public class ChartPluginController extends AbstractChartPluginAwareController implements ServletContextAware
{
	@Autowired
	private File tempDirectory;

	@Autowired
	private HtmlTplDashboardWidgetEntityService htmlTplDashboardWidgetEntityService;

	private HtmlChartPluginScriptObjectWriter htmlChartPluginScriptObjectWriter = new HtmlChartPluginScriptObjectWriter();

	private ServletContext servletContext;

	public ChartPluginController()
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

	public ServletContext getServletContext()
	{
		return servletContext;
	}

	@Override
	public void setServletContext(ServletContext servletContext)
	{
		this.servletContext = servletContext;
	}

	@RequestMapping("/upload")
	public String upload(HttpServletRequest request, org.springframework.ui.Model model)
	{
		setFormAction(model, REQUEST_ACTION_UPLOAD, SUBMIT_ACTION_SAVE_UPLOAD);
		
		return "/chartPlugin/chartPlugin_upload";
	}

	@RequestMapping(value = "/uploadFile", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public Map<String, Object> uploadFile(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("file") MultipartFile multipartFile) throws Exception
	{
		File myTmpDirectory = FileUtil.generateUniqueDirectory(this.tempDirectory);

		String zipFileName = multipartFile.getOriginalFilename();
		if (StringUtil.isEmpty(zipFileName))
			zipFileName = "plugin.zip";

		if (!FileUtil.isExtension(zipFileName, "zip"))
			zipFileName += ".zip";

		File zipFile = FileUtil.getFile(myTmpDirectory, zipFileName);

		InputStream in = null;
		OutputStream out = null;
		try
		{
			in = multipartFile.getInputStream();
			out = IOUtil.getOutputStream(zipFile);
			IOUtil.write(in, out);
		}
		finally
		{
			IOUtil.close(in);
			IOUtil.close(out);
		}

		String pluginFileName = FileUtil.getRelativePath(this.tempDirectory, myTmpDirectory);

		// 如果ZIP里包含多个插件包，则应解压
		HtmlChartPluginLoader loader = getDirectoryHtmlChartPluginManager().getHtmlChartPluginLoader();
		if (!loader.isHtmlChartPluginZip(zipFile))
		{
			ZipInputStream zin = null;
			try
			{
				zin = IOUtil.getZipInputStream(zipFile);
				IOUtil.unzip(zin, myTmpDirectory);
			}
			finally
			{
				IOUtil.close(zin);
			}
		}

		List<HtmlChartPluginView> pluginInfos = new ArrayList<>();
		Set<HtmlChartPlugin> loadedPlugins = resolveHtmlChartPlugins(myTmpDirectory);
		Locale locale = WebUtils.getLocale(request);
		String themeName = resolveChartPluginIconThemeName(request);

		try
		{
			for (HtmlChartPlugin chartPlugin : loadedPlugins)
				pluginInfos.add(toHtmlChartPluginView(chartPlugin, themeName, locale));
		}
		catch (HtmlChartPluginLoadException e)
		{
		}

		Map<String, Object> results = new HashMap<>();
		results.put("pluginFileName", pluginFileName);
		results.put("pluginInfos", pluginInfos);

		return results;
	}

	@RequestMapping(value = "/saveUpload", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveUpload(HttpServletRequest request, HttpServletResponse response,
			@RequestBody saveUploadForm form) throws Exception
	{
		String pluginFileName = form.getPluginFileName();

		if (StringUtil.isEmpty(pluginFileName))
			throw new IllegalInputException();

		File tmpFile = FileUtil.getFile(this.tempDirectory, pluginFileName);

		Set<HtmlChartPlugin> uploads = getDirectoryHtmlChartPluginManager().upload(tmpFile);

		return optSuccessResponseEntity(request, "chartPlugin.upload.finish", uploads.size());
	}

	@RequestMapping("/download")
	public void download(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String[] ids) throws Exception
	{
		response.addHeader("Content-Disposition",
				"attachment;filename=" + toResponseAttachmentFileName(request, response, "chartPlugins.zip"));
		response.setContentType("application/octet-stream");

		ZipOutputStream zout = IOUtil.getZipOutputStream(response.getOutputStream());

		try
		{
			getDirectoryHtmlChartPluginManager().download(zout, ids);
		}
		finally
		{
			zout.flush();
			zout.close();
		}
	}

	@RequestMapping(value = "/delete", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> delete(HttpServletRequest request, HttpServletResponse response,
			@RequestBody String[] ids)
	{
		getDirectoryHtmlChartPluginManager().remove(ids);

		return optSuccessResponseEntity(request);
	}

	@RequestMapping("/query")
	public String query(HttpServletRequest request, org.springframework.ui.Model model)
	{
		model.addAttribute(KEY_REQUEST_ACTION, REQUEST_ACTION_QUERY);
		setReadonlyActionByRole(model, WebUtils.getUser());
		return "/chartPlugin/chartPlugin_table";
	}

	@RequestMapping(value = "/queryData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public List<HtmlChartPluginView> queryData(HttpServletRequest request, HttpServletResponse response,
			final org.springframework.ui.Model springModel, @RequestBody(required = false) PagingQuery pagingQueryParam)
			throws Exception
	{
		final PagingQuery pagingQuery = inflatePagingQuery(request, pagingQueryParam);

		return findHtmlChartPluginViews(request, pagingQuery.getKeyword());
	}

	@RequestMapping("/select")
	public String select(HttpServletRequest request, org.springframework.ui.Model model)
	{
		List<HtmlChartPluginView> htmlChartPluginViews = findHtmlChartPluginViews(request, null);
		List<Categorization> categorizations = resolveCategorizations(htmlChartPluginViews);

		addAttributeForWriteJson(model, "categorizations", categorizations);

		setSelectAction(request, model);
		return "/chartPlugin/chartPlugin_select";
	}

	@RequestMapping(value = "/selectData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public List<Categorization> selectData(HttpServletRequest request, HttpServletResponse response,
			final org.springframework.ui.Model springModel, @RequestBody(required = false) PagingQuery pagingQueryParam)
			throws Exception
	{
		final PagingQuery pagingQuery = inflatePagingQuery(request, pagingQueryParam);

		List<HtmlChartPluginView> htmlChartPluginViews = findHtmlChartPluginViews(request, pagingQuery.getKeyword());
		List<Categorization> categorizations = resolveCategorizations(htmlChartPluginViews);

		return categorizations;
	}

	@RequestMapping("/icon/{pluginId:.+}")
	public void chartPluginIcon(HttpServletRequest request, HttpServletResponse response, WebRequest webRequest,
			@PathVariable("pluginId") String pluginId,
			@RequestParam(value="tmpPluginFileName", required = false) String tmpPluginFileName) throws Exception
	{
		ChartPlugin chartPlugin = null;
		
		if(isEmpty(tmpPluginFileName))
			chartPlugin = getDirectoryHtmlChartPluginManager().get(pluginId);
		else
		{
			File tmpPluginFile = FileUtil.getFile(this.tempDirectory, tmpPluginFileName, false);
			
			if(tmpPluginFile.exists())
			{
				Set<HtmlChartPlugin> plugins = resolveHtmlChartPlugins(tmpPluginFile);
				for(HtmlChartPlugin p : plugins)
				{
					if(pluginId.equals(p.getId()))
					{
						chartPlugin = p;
						break;
					}
				}
			}
		}

		if(chartPlugin == null)
		{
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		String themeName = resolveChartPluginIconThemeName(request);
		String iconResName = chartPlugin.getIconResourceName(themeName);
		ChartPluginResource iconResource = (StringUtil.isEmpty(iconResName) ? null :  chartPlugin.getResource(iconResName));
		
		writeChartPluginResource(request, response, webRequest, chartPlugin, iconResource);
	}

	@RequestMapping("/resource/{pluginId:.+}/**")
	public void chartPluginResource(HttpServletRequest request, HttpServletResponse response, WebRequest webRequest,
			@PathVariable("pluginId") String pluginId) throws Exception
	{
		ChartPlugin chartPlugin = getDirectoryHtmlChartPluginManager().get(pluginId);
		
		if(chartPlugin == null)
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

		out.println();
		out.println("//@deprecated 兼容1.8.1版本的window.chartPluginManager变量名，未来版本会移除");
		out.println("global.chartPluginManager = chartPluginManager;");
		
		out.println();
		out.println("chartPluginManager.get = function(id){ return this.plugins[id]; };");
		out.println();

		for (int i = 0, len = htmlChartPlugins.size(); i < len; i++)
		{
			HtmlChartPlugin plugin = htmlChartPlugins.get(i);
			String pluginVar = "plugin" + i;

			this.htmlChartPluginScriptObjectWriter.write(out, plugin, pluginVar, locale);

			out.println("//@deprecated 兼容4.0.0版本的"+HtmlChartPlugin.PROPERTY_RENDERER_OLD+"属性名，未来版本会移除");
			out.println(pluginVar + "."+ HtmlChartPlugin.PROPERTY_RENDERER_OLD +" = " + pluginVar + "."+ HtmlChartPlugin.PROPERTY_RENDERER +";");
			
			out.println("chartPluginManager.plugins[" + StringUtil.toJavaScriptString(plugin.getId()) + "] = "
					+ pluginVar + ";");
		}

		out.println("})(this);");
	}
	
	protected void writeChartPluginResource(HttpServletRequest request, HttpServletResponse response,
			WebRequest webRequest, ChartPlugin chartPlugin, ChartPluginResource resource) throws Exception
	{
		if (resource == null)
		{
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		long lastModified = resource.getLastModified();
		if (webRequest.checkNotModified(lastModified))
			return;

		setContentTypeByName(request, response, servletContext, resource.getName());
		setCacheControlNoCache(response);

		InputStream in = null;
		OutputStream out = response.getOutputStream();

		try
		{
			in = resource.getInputStream();
			IOUtil.write(in, out);
		}
		finally
		{
			IOUtil.close(in);
		}
	}

	protected Set<HtmlChartPlugin> resolveHtmlChartPlugins(File directory)
	{
		Set<HtmlChartPlugin> loaded = Collections.emptySet();

		HtmlChartPluginLoader loader = getDirectoryHtmlChartPluginManager().getHtmlChartPluginLoader();
		
		try
		{
			loaded = loader.loadAll(directory);
		}
		catch (HtmlChartPluginLoadException e)
		{
		}
		
		return loaded;
	}

	public static class saveUploadForm implements ControllerForm
	{
		private static final long serialVersionUID = 1L;

		private String pluginFileName;

		public saveUploadForm()
		{
			super();
		}

		public String getPluginFileName()
		{
			return pluginFileName;
		}

		public void setPluginFileName(String pluginFileName)
		{
			this.pluginFileName = pluginFileName;
		}
	}
}
