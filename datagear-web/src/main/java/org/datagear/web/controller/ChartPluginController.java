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
import java.util.Comparator;
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
import org.datagear.analysis.ChartPluginDataSetRange;
import org.datagear.analysis.ChartPluginResource;
import org.datagear.analysis.support.ChartPluginCategorizationResolver;
import org.datagear.analysis.support.ChartPluginCategorizationResolver.Categorization;
import org.datagear.analysis.support.html.HtmlChartPlugin;
import org.datagear.analysis.support.html.HtmlChartPluginLoader;
import org.datagear.analysis.support.html.HtmlChartPluginUse;
import org.datagear.management.domain.HtmlChartPluginVo;
import org.datagear.util.FileUtil;
import org.datagear.util.IOUtil;
import org.datagear.util.KeywordMatcher;
import org.datagear.util.KeywordMatcher.MatchValue;
import org.datagear.util.StringUtil;
import org.datagear.util.i18n.Localizable;
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

	private ChartPluginCategorizationResolver chartPluginCategorizationResolver = new ChartPluginCategorizationResolver();

	private KeywordMatcher keywordMatcher = new KeywordMatcher();

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

	public ChartPluginCategorizationResolver getChartPluginCategorizationResolver()
	{
		return chartPluginCategorizationResolver;
	}

	public void setChartPluginCategorizationResolver(
			ChartPluginCategorizationResolver chartPluginCategorizationResolver)
	{
		this.chartPluginCategorizationResolver = chartPluginCategorizationResolver;
	}

	public KeywordMatcher getKeywordMatcher()
	{
		return keywordMatcher;
	}

	public void setKeywordMatcher(KeywordMatcher keywordMatcher)
	{
		this.keywordMatcher = keywordMatcher;
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

		List<HtmlChartPluginVo> pluginInfos = new ArrayList<>();

		Set<HtmlChartPlugin> loadedPlugins = resolveHtmlChartPluginsThrow(myTmpDirectory);
		Locale locale = WebUtils.getLocale(request);
		String themeName = resolveChartPluginIconThemeName(request);

		for (HtmlChartPlugin chartPlugin : loadedPlugins)
			pluginInfos.add(toHtmlChartPluginVo(chartPlugin, false, locale, themeName));

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

	@RequestMapping("/view/{id}")
	public String view(HttpServletRequest request, HttpServletResponse response, Model model,
			@PathVariable("id") String id)
	{
		setFormAction(model, REQUEST_ACTION_VIEW, SUBMIT_ACTION_NONE);

		HtmlChartPlugin plugin = getHtmlChartPlugin(id, true);
		setFormModel(model, toHtmlChartPluginVo(request, plugin, true));

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
	public PagingData<HtmlChartPluginVo> pagingQueryData(HttpServletRequest request, HttpServletResponse response,
			Model model, @RequestBody(required = false) DataFilterPagingQuery pagingQuery)
			throws Exception
	{
		pagingQuery = (pagingQuery == null ? new DataFilterPagingQuery() : pagingQuery);
		List<HtmlChartPluginVo> pluginVos = findHtmlChartPluginVos(request, pagingQuery.getKeyword(),
				pagingQuery.getDataFilter(), false, false, false);

		PagingData<HtmlChartPluginVo> pagingData = new PagingData<>(pagingQuery.getPage(), pluginVos.size(),
				pagingQuery.getPageSize());
		pagingData.setItems(pluginVos.subList(pagingData.getStartIndex(), pagingData.getEndIndex()));

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

		List<HtmlChartPluginVo> pluginVos = findHtmlChartPluginVos(request, pagingQuery.getKeyword(),
				pagingQuery.getDataFilter(), true, local, true);
		List<Categorization> categorizations = resolveCategorizations(pluginVos);
		simplifyForSelectData(categorizations);

		return categorizations;
	}

	@RequestMapping(value = "/detailValue/{id}", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public HtmlChartPlugin detailValue(HttpServletRequest request, @PathVariable("id") String id) throws Exception
	{
		HtmlChartPlugin plugin = getHtmlChartPlugin(id, true);
		plugin = toHtmlChartPluginVo(request, plugin, true);
		return plugin;
	}

	@RequestMapping("/manual/{id}")
	public String chartPluginManual(HttpServletRequest request, HttpServletResponse response, Model model,
			@PathVariable("id") String id)
	{
		setFormAction(model, REQUEST_ACTION_VIEW, SUBMIT_ACTION_NONE);

		HtmlChartPlugin plugin = getHtmlChartPlugin(id, true);
		setFormModel(model, toHtmlChartPluginVo(request, plugin, false));

		return "/chartPlugin/chartPlugin_manual";
	}

	@RequestMapping("/manualContent/{id}")
	public void chartPluginManualContent(HttpServletRequest request, HttpServletResponse response,
			WebRequest webRequest, @PathVariable("id") String pluginId) throws Exception
	{
		ChartPlugin chartPlugin = getDirectoryHtmlChartPluginManager().get(pluginId);

		if (chartPlugin == null)
		{
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		response.setContentType(CONTENT_TYPE_HTML);

		ChartPluginResource manualRes = chartPlugin.getResource(HtmlChartPluginLoader.FILE_NAME_MANUAL);

		// 没有时不必输出404
		if (manualRes != null)
			writeChartPluginResource(request, response, webRequest, chartPlugin, manualRes);
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

	protected List<Categorization> resolveCategorizations(List<HtmlChartPluginVo> chartPluginVOs)
	{
		return this.chartPluginCategorizationResolver.resolve(chartPluginVOs);
	}

	protected void simplifyForSelectData(List<Categorization> categorizations)
	{
		if (categorizations == null || categorizations.size() == 0)
			return;

		for (Categorization ct : categorizations)
		{
			List<ChartPlugin> plugins = ct.getChartPlugins();

			if (plugins == null)
				continue;

			for (ChartPlugin plugin : plugins)
			{
				if (!(plugin instanceof HtmlChartPlugin))
					continue;

				HtmlChartPlugin hp = (HtmlChartPlugin) plugin;

				// 不需要显示的都设为null，减少数据传输量
				hp.setCategoryInfos(null);
				hp.setDataSignSpec(null);
				hp.setResources(null);
			}
		}
	}

	/**
	 * 查找插件值对象列表。
	 * 
	 * @param request
	 * @param keyword
	 * @param apiVersion
	 *            允许{@code null}
	 * @param ignoreLib
	 * @param local
	 *            是否仅查询本地图表插件（{@linkplain ChartPlugin#getDataSetRange()}不为{@code null}，且各值都为0）
	 *            允许{@code null}
	 * @param forCategory
	 * @return
	 */
	protected List<HtmlChartPluginVo> findHtmlChartPluginVos(HttpServletRequest request, String keyword,
			String apiVersion, boolean ignoreLib, Boolean local, boolean forCategory)
	{
		List<HtmlChartPluginVo> pluginViews = new ArrayList<>();

		List<HtmlChartPlugin> plugins = getDirectoryHtmlChartPluginManager().getAll(HtmlChartPlugin.class,
				HTML_CHART_PLUGIN_SORT);

		if (plugins != null)
		{
			Locale locale = WebUtils.getLocale(request);
			String themeName = resolveChartPluginIconThemeName(request);
			boolean apiVersionEmpty = StringUtil.isEmpty(apiVersion);

			for (HtmlChartPlugin plugin : plugins)
			{
				if (ignoreLib && HtmlChartPluginUse.LIB.equals(plugin.getUse()))
					continue;

				if (!apiVersionEmpty && !apiVersion.equals(plugin.getApiVersion()))
					continue;

				if (local != null && Boolean.TRUE.equals(local)
						&& !ChartPluginDataSetRange.isStrictZeroRange(plugin.getDataSetRange()))
					continue;

				pluginViews.add(forCategory ? toHtmlChartPluginVoForCategory(plugin, themeName, locale)
						: toHtmlChartPluginVo(plugin, false, locale, themeName));
			}
		}

		return this.keywordMatcher.match(pluginViews, keyword, new MatchValue<HtmlChartPluginVo>()
		{
			@Override
			public String[] get(HtmlChartPluginVo t)
			{
				return new String[] { (t.getNameLabel() == null ? null : t.getNameLabel().getValue()),
						(t.getDescLabel() == null ? null : t.getDescLabel().getValue()), t.getAuthor() };
			}
		});
	}

	protected HtmlChartPluginVo toHtmlChartPluginVoForCategory(HtmlChartPlugin chartPlugin, String themeName,
			Locale locale)
	{
		HtmlChartPluginVo vo = toHtmlChartPluginVo(chartPlugin, false, locale, themeName);
		vo.setCategoryInfos(Localizable.toLocale(chartPlugin.getCategoryInfos(), locale));
		return vo;
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

	/**
	 * 图表插件排序器。
	 * <p>
	 * {@linkplain HtmlChartPlugin#getApiVersion()}越大越靠前、{@linkplain HtmlChartPlugin#getOrder()}越小越靠前。
	 * </p>
	 */
	protected static final Comparator<HtmlChartPlugin> HTML_CHART_PLUGIN_SORT = new Comparator<HtmlChartPlugin>()
	{
		@Override
		public int compare(HtmlChartPlugin o1, HtmlChartPlugin o2)
		{
			String apiVersion1 = o1.getApiVersion();
			String apiVersion2 = o2.getApiVersion();

			if (apiVersion1 == null)
				apiVersion1 = "";
			if (apiVersion2 == null)
				apiVersion2 = "";

			// 越大越靠前
			int re = (0 - apiVersion1.compareTo(apiVersion2));

			if (re == 0)
				re = Integer.valueOf(o1.getOrder()).compareTo(o2.getOrder());

			return re;
		}
	};
}
