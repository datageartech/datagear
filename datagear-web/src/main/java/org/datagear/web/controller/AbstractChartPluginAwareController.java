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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.analysis.ChartPlugin;
import org.datagear.analysis.ChartPluginResource;
import org.datagear.analysis.DashboardTheme;
import org.datagear.analysis.DataSetBind;
import org.datagear.analysis.DataSetResult;
import org.datagear.analysis.support.ProfileDataSet;
import org.datagear.analysis.support.html.DirectoryHtmlChartPluginManager;
import org.datagear.analysis.support.html.HtmlChartPlugin;
import org.datagear.analysis.support.html.HtmlChartPluginLoadException;
import org.datagear.analysis.support.html.HtmlChartPluginLoader;
import org.datagear.management.domain.DataSetBindVO;
import org.datagear.management.domain.HtmlChartPluginVo;
import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;
import org.datagear.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.request.WebRequest;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 抽象插件相关的控制器。
 * 
 * @author datagear@163.com
 *
 */
public class AbstractChartPluginAwareController extends AbstractDataAnalysisController implements ServletContextAware
{
	@Autowired
	private DirectoryHtmlChartPluginManager directoryHtmlChartPluginManager;

	private ServletContext servletContext;

	public AbstractChartPluginAwareController()
	{
		super();
	}

	public DirectoryHtmlChartPluginManager getDirectoryHtmlChartPluginManager()
	{
		return directoryHtmlChartPluginManager;
	}

	public void setDirectoryHtmlChartPluginManager(DirectoryHtmlChartPluginManager directoryHtmlChartPluginManager)
	{
		this.directoryHtmlChartPluginManager = directoryHtmlChartPluginManager;
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

		try
		{
			loaded = resolveHtmlChartPluginsThrow(directory);
		}
		catch (HtmlChartPluginLoadException e)
		{
		}

		return loaded;
	}

	protected Set<HtmlChartPlugin> resolveHtmlChartPluginsThrow(File directory) throws HtmlChartPluginLoadException
	{
		HtmlChartPluginLoader loader = getDirectoryHtmlChartPluginManager().getHtmlChartPluginLoader();
		return loader.loadAll(directory);
	}

	protected HtmlChartPlugin getHtmlChartPlugin(String id, boolean nonNull)
	{
		ChartPlugin plugin = (id == null ? null : getDirectoryHtmlChartPluginManager().get(id));

		if (plugin != null && !(plugin instanceof HtmlChartPlugin))
			plugin = null;

		if (plugin == null)
		{
			if(nonNull)
				checkNonNullEntity(plugin);
			else
				return null;
		}

		return (HtmlChartPlugin) plugin;
	}

	protected HtmlChartPluginVo toHtmlChartPluginVo(HttpServletRequest request, HtmlChartPlugin plugin, boolean detail)
	{
		if (plugin == null)
			return null;

		Locale locale = WebUtils.getLocale(request);
		String themeName = resolveChartPluginIconThemeName(request);
		return toHtmlChartPluginVo(plugin, detail, locale, themeName);
	}

	protected HtmlChartPluginVo toHtmlChartPluginVo(HtmlChartPlugin plugin, boolean detail, Locale locale,
			String themeName)
	{
		if (plugin == null)
			return null;

		HtmlChartPluginVo vo = new HtmlChartPluginVo(plugin, detail, locale);
		inflatePluginThemeIcons(vo, themeName);

		return vo;
	}

	protected String resolveChartPluginIconThemeName(HttpServletRequest request)
	{
		DashboardTheme dashboardTheme = resolveDashboardTheme(request);
		return dashboardTheme.getName();
	}

	protected void inflatePluginThemeIcons(HtmlChartPlugin plugin, String themeName)
	{
		if (plugin == null)
			return;

		String iconResName = plugin.getIconResourceName(themeName);

		if (StringUtil.isEmpty(iconResName))
			plugin.setIcons(Collections.emptyMap());
		else
			plugin.setIcons(Collections.singletonMap(ChartPlugin.DEFAULT_ICON_THEME_NAME, iconResName));
	}

	/**
	 * 解析插件图标URL，没有则返回{@code null}
	 * 
	 * @param plugin
	 * @param themeName
	 * @return
	 */
	protected String resolveIconUrl(HtmlChartPlugin plugin, String themeName)
	{
		if (plugin == null)
			return null;

		String iconResName = plugin.getIconResourceName(themeName);
		return (StringUtil.isEmpty(iconResName) ? null : resolveIconUrl(plugin));
	}

	protected String resolveIconUrl(HtmlChartPlugin plugin)
	{
		return "/chartPlugin/icon/" + plugin.getId();
	}

	protected DataSetBindView[] toDataSetBindViews(DataSetBind[] dataSetBinds)
	{
		if (dataSetBinds == null)
			return null;

		DataSetBindView[] views = new DataSetBindView[dataSetBinds.length];

		for (int i = 0; i < dataSetBinds.length; i++)
			views[i] = new DataSetBindView(dataSetBinds[i]);

		return views;
	}

	/**
	 * {@linkplain DataSetBind}视图对象。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class DataSetBindView extends DataSetBindVO
	{
		private static final long serialVersionUID = 1L;

		public DataSetBindView()
		{
			super();
		}

		public DataSetBindView(DataSetBind dataSetBind)
		{
			super();
			setDataSet(ProfileDataSet.valueOf(dataSetBind.getDataSet()));
			setDataSetSigns(dataSetBind.getDataSetSigns());
			setFieldSigns(dataSetBind.getFieldSigns());
			setAlias(dataSetBind.getAlias());
			setAttachment(dataSetBind.isAttachment());
			setQuery(dataSetBind.getQuery());
			setFieldAliases(dataSetBind.getFieldAliases());
			setFieldOrders(dataSetBind.getFieldOrders());
		}

		@JsonIgnore
		@Override
		public DataSetResult getResult()
		{
			return null;
		}
	}
}
