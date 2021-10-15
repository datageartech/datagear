/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.analysis.ChartPlugin;
import org.datagear.analysis.Icon;
import org.datagear.analysis.support.CategorizationResolver.Categorization;
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
public class ChartPluginController extends AbstractChartPluginAwareController
{
	@Autowired
	private File tempDirectory;

	@Autowired
	private HtmlTplDashboardWidgetEntityService htmlTplDashboardWidgetEntityService;

	private HtmlChartPluginScriptObjectWriter htmlChartPluginScriptObjectWriter = new HtmlChartPluginScriptObjectWriter();

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

	@RequestMapping("/upload")
	public String upload(HttpServletRequest request, org.springframework.ui.Model model)
	{
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

		String pluginFileName = FileUtil.getRelativePath(this.tempDirectory, zipFile);

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

		List<HtmlChartPluginVO> pluginInfos = new ArrayList<>();

		HtmlChartPluginLoader loader = getDirectoryHtmlChartPluginManager().getHtmlChartPluginLoader();

		Locale locale = WebUtils.getLocale(request);
		String themeName = resolveChartPluginIconThemeName(request);

		try
		{
			Set<HtmlChartPlugin> loaded = loader.loads(zipFile);

			for (HtmlChartPlugin chartPlugin : loaded)
				pluginInfos.add(toHtmlChartPluginVO(chartPlugin, themeName, locale));
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

		return buildOperationMessageSuccessResponseEntity(request, "chartPlugin.upload.finish", uploads.size());
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

		return buildOperationMessageDeleteSuccessResponseEntity(request);
	}

	@RequestMapping("/query")
	public String query(HttpServletRequest request, org.springframework.ui.Model model)
	{
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "chartPlugin.manageChartPlugin");

		return "/chartPlugin/chartPlugin_grid";
	}

	@RequestMapping(value = "/queryData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public List<HtmlChartPluginVO> queryData(HttpServletRequest request, HttpServletResponse response,
			final org.springframework.ui.Model springModel, @RequestBody(required = false) PagingQuery pagingQueryParam)
			throws Exception
	{
		final PagingQuery pagingQuery = inflatePagingQuery(request, pagingQueryParam);

		return findHtmlChartPluginVOs(request, pagingQuery.getKeyword());
	}

	@RequestMapping("/select")
	public String select(HttpServletRequest request, org.springframework.ui.Model model)
	{
		List<HtmlChartPluginVO> htmlChartPluginVOs = findHtmlChartPluginVOs(request, null);
		List<Categorization> categorizations = resolveCategorizations(htmlChartPluginVOs);

		model.addAttribute("categorizations", toWriteJsonTemplateModel(categorizations));

		return "/chartPlugin/chartPlugin_select";
	}

	@RequestMapping(value = "/selectData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public List<Categorization> selectData(HttpServletRequest request, HttpServletResponse response,
			final org.springframework.ui.Model springModel, @RequestBody(required = false) PagingQuery pagingQueryParam)
			throws Exception
	{
		final PagingQuery pagingQuery = inflatePagingQuery(request, pagingQueryParam);

		List<HtmlChartPluginVO> htmlChartPluginVOs = findHtmlChartPluginVOs(request, pagingQuery.getKeyword());
		List<Categorization> categorizations = resolveCategorizations(htmlChartPluginVOs);

		return categorizations;
	}

	@RequestMapping(value = "/icon/{pluginId:.+}", produces = CONTENT_TYPE_JSON)
	public void getPluginIcon(HttpServletRequest request, HttpServletResponse response, WebRequest webRequest,
			@PathVariable("pluginId") String pluginId) throws Exception
	{
		ChartPlugin chartPlugin = getDirectoryHtmlChartPluginManager().get(pluginId);

		if (chartPlugin == null)
			throw new FileNotFoundException();

		String themeName = resolveChartPluginIconThemeName(request);
		Icon icon = chartPlugin.getIcon(themeName);

		if (icon == null)
			throw new FileNotFoundException();

		long lastModified = icon.getLastModified();
		if (webRequest.checkNotModified(lastModified))
			return;

		response.setContentType("image/" + icon.getType());

		OutputStream out = response.getOutputStream();
		InputStream iconIn = null;

		try
		{
			iconIn = icon.getInputStream();
			IOUtil.write(iconIn, out);
		}
		finally
		{
			IOUtil.close(iconIn);
		}
	}

	@RequestMapping("/chartPluginManager.js")
	public void getChartPluginManagerJs(HttpServletRequest request, HttpServletResponse response, WebRequest webRequest)
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

		response.setContentType(CONTENT_TYPE_JAVASCRIPT);

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

			this.htmlChartPluginScriptObjectWriter.write(out, plugin, pluginVar);

			out.println("chartPluginManager.plugins[\"" + WebUtils.escapeJavaScriptStringValue(plugin.getId())
					+ "\"] = " + pluginVar + ";");
		}

		out.println("})(this);");
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
