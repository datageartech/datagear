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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.analysis.ChartPlugin;
import org.datagear.analysis.ChartPluginResource;
import org.datagear.analysis.support.ChartPluginCategorizationResolver.Categorization;
import org.datagear.analysis.support.html.HtmlChartPlugin;
import org.datagear.analysis.support.html.HtmlChartPluginLoader;
import org.datagear.util.FileUtil;
import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;
import org.datagear.util.query.PagingData;
import org.datagear.web.util.OperationMessage;
import org.datagear.web.util.WebUtils;
import org.datagear.web.vo.DataFilterPagingQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

	@RequestMapping("/upload")
	public String upload(HttpServletRequest request, Model model)
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

		writeMultipartFile(multipartFile, zipFile);

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

		Set<HtmlChartPlugin> loadedPlugins = resolveHtmlChartPluginsThrow(myTmpDirectory);
		Locale locale = WebUtils.getLocale(request);
		String themeName = resolveChartPluginIconThemeName(request);

		for (HtmlChartPlugin chartPlugin : loadedPlugins)
			pluginInfos.add(toHtmlChartPluginView(chartPlugin, themeName, locale));

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
	public void download(HttpServletRequest request, HttpServletResponse response, Model model,
			@RequestParam("id") String[] ids) throws Exception
	{
		setDownloadResponseHeader(request, response, "chartPlugins.zip");
		response.setContentType(CONTENT_TYPE_OCTET_STREAM);

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

	@RequestMapping("/view")
	public String view(HttpServletRequest request, HttpServletResponse response, Model model,
			@RequestParam("id") String id)
	{
		setFormAction(model, REQUEST_ACTION_VIEW, SUBMIT_ACTION_NONE);

		HtmlChartPlugin plugin = (HtmlChartPlugin) getDirectoryHtmlChartPluginManager().get(id);
		setFormModel(model, toHtmlChartPluginView(request, plugin));

		return "/chartPlugin/chartPlugin_form";
	}

	@RequestMapping(value = "/delete", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> delete(HttpServletRequest request, HttpServletResponse response,
			@RequestBody String[] ids)
	{
		getDirectoryHtmlChartPluginManager().remove(ids);

		return optSuccessResponseEntity(request);
	}

	@RequestMapping("/manage")
	public String manage(HttpServletRequest request, Model model)
	{
		model.addAttribute(KEY_REQUEST_ACTION, REQUEST_ACTION_MANAGE);
		setReadonlyAction(model);
		return "/chartPlugin/chartPlugin_table";
	}

	@RequestMapping(value = "/pagingQueryData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public PagingData<HtmlChartPluginView> pagingQueryData(HttpServletRequest request, HttpServletResponse response,
			Model model, @RequestBody(required = false) DataFilterPagingQuery pagingQuery)
			throws Exception
	{
		pagingQuery = (pagingQuery == null ? new DataFilterPagingQuery() : pagingQuery);
		List<HtmlChartPluginView> chartPluginViews = findHtmlChartPluginViews(request, pagingQuery.getKeyword(),
				pagingQuery.getDataFilter(), false);

		PagingData<HtmlChartPluginView> pagingData = new PagingData<>(pagingQuery.getPage(), chartPluginViews.size(),
				pagingQuery.getPageSize());
		pagingData.setItems(chartPluginViews.subList(pagingData.getStartIndex(), pagingData.getEndIndex()));

		return pagingData;
	}

	@RequestMapping("/select")
	public String select(HttpServletRequest request, Model model,
			@RequestParam(value = "local", required = false) Boolean local)
	{
		setSelectAction(request, model);
		model.addAttribute("local", local);

		return "/chartPlugin/chartPlugin_select";
	}

	@RequestMapping(value = "/selectData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public List<Categorization> selectData(HttpServletRequest request, HttpServletResponse response,
			Model model, @RequestParam(value = "local", required = false) Boolean local,
			@RequestBody(required = false) DataFilterPagingQuery pagingQuery) throws Exception
	{
		pagingQuery = (pagingQuery == null ? new DataFilterPagingQuery() : pagingQuery);

		List<HtmlChartPluginView> htmlChartPluginViews = findHtmlChartPluginViews(request, pagingQuery.getKeyword(),
				pagingQuery.getDataFilter(), local);
		List<Categorization> categorizations = resolveCategorizations(htmlChartPluginViews);

		return categorizations;
	}

	@RequestMapping("/icon/{pluginId:.+}")
	public void chartPluginIcon(HttpServletRequest request, HttpServletResponse response, WebRequest webRequest,
			@PathVariable("pluginId") String pluginId,
			@RequestParam(value = "tmpPluginFileName", required = false) String tmpPluginFileName) throws Exception
	{
		ChartPlugin chartPlugin = null;

		if (isEmpty(tmpPluginFileName))
			chartPlugin = getDirectoryHtmlChartPluginManager().get(pluginId);
		else
		{
			File tmpPluginFile = FileUtil.getFile(this.tempDirectory, tmpPluginFileName, false);

			if (tmpPluginFile.exists())
			{
				Set<HtmlChartPlugin> plugins = resolveHtmlChartPlugins(tmpPluginFile);
				for (HtmlChartPlugin p : plugins)
				{
					if (pluginId.equals(p.getId()))
					{
						chartPlugin = p;
						break;
					}
				}
			}
		}

		if (chartPlugin == null)
		{
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		String themeName = resolveChartPluginIconThemeName(request);
		String iconResName = chartPlugin.getIconResourceName(themeName);
		ChartPluginResource iconResource = (StringUtil.isEmpty(iconResName) ? null
				: chartPlugin.getResource(iconResName));

		writeChartPluginResource(request, response, webRequest, chartPlugin, iconResource);
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
