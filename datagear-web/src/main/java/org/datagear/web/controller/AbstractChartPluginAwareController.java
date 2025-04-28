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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.analysis.Category;
import org.datagear.analysis.ChartPlugin;
import org.datagear.analysis.ChartPluginAttribute;
import org.datagear.analysis.ChartPluginResource;
import org.datagear.analysis.DashboardTheme;
import org.datagear.analysis.DataSetBind;
import org.datagear.analysis.DataSetResult;
import org.datagear.analysis.DataSign;
import org.datagear.analysis.support.ChartPluginCategorizationResolver;
import org.datagear.analysis.support.ChartPluginCategorizationResolver.Categorization;
import org.datagear.analysis.support.ProfileDataSet;
import org.datagear.analysis.support.html.DirectoryHtmlChartPluginManager;
import org.datagear.analysis.support.html.HtmlChartPlugin;
import org.datagear.analysis.support.html.HtmlChartPluginLoadException;
import org.datagear.analysis.support.html.HtmlChartPluginLoader;
import org.datagear.management.domain.DataSetBindVO;
import org.datagear.management.domain.HtmlChartPluginVo;
import org.datagear.util.IOUtil;
import org.datagear.util.KeywordMatcher;
import org.datagear.util.KeywordMatcher.MatchValue;
import org.datagear.util.StringUtil;
import org.datagear.util.i18n.Label;
import org.datagear.util.i18n.LabelUtil;
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

	private ChartPluginCategorizationResolver chartPluginCategorizationResolver = new ChartPluginCategorizationResolver();

	private KeywordMatcher keywordMatcher = new KeywordMatcher();

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

	public ServletContext getServletContext()
	{
		return servletContext;
	}

	@Override
	public void setServletContext(ServletContext servletContext)
	{
		this.servletContext = servletContext;
	}

	protected List<Categorization> resolveCategorizations(List<HtmlChartPluginView> chartPluginVOs)
	{
		return this.chartPluginCategorizationResolver.resolve(chartPluginVOs);
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

	/**
	 * 根据ID获取。
	 * 
	 * @param request
	 * @param id
	 * @return 返回{@code null}表示未找到
	 */
	protected HtmlChartPluginView getHtmlChartPluginView(HttpServletRequest request, String id)
	{
		List<HtmlChartPlugin> plugins = getDirectoryHtmlChartPluginManager().getAll(HtmlChartPlugin.class);

		if (plugins == null)
			return null;

		Locale locale = WebUtils.getLocale(request);
		String themeName = resolveChartPluginIconThemeName(request);
		
		for (HtmlChartPlugin plugin : plugins)
		{
			if (plugin.getId().equals(id))
				return toHtmlChartPluginView(plugin, themeName, locale);
		}

		return null;
	}

	/**
	 * 查找插件视图对象列表。
	 * 
	 * @param request
	 * @param keyword
	 * @return
	 */
	protected List<HtmlChartPluginView> findHtmlChartPluginViews(HttpServletRequest request, String keyword)
	{
		List<HtmlChartPluginView> pluginViews = new ArrayList<>();

		List<HtmlChartPlugin> plugins = getDirectoryHtmlChartPluginManager().getAll(HtmlChartPlugin.class);

		if (plugins != null)
		{
			Locale locale = WebUtils.getLocale(request);
			String themeName = resolveChartPluginIconThemeName(request);

			for (HtmlChartPlugin plugin : plugins)
				pluginViews.add(toHtmlChartPluginView(plugin, themeName, locale));
		}

		return this.keywordMatcher.match(pluginViews, keyword, new MatchValue<HtmlChartPluginView>()
				{
					@Override
					public String[] get(HtmlChartPluginView t)
					{
						return new String[] { (t.getNameLabel() == null ? null : t.getNameLabel().getValue()),
						(t.getDescLabel() == null ? null : t.getDescLabel().getValue()), t.getAuthor() };
					}
				});
	}

	protected HtmlChartPluginView toHtmlChartPluginView(HttpServletRequest request, HtmlChartPlugin chartPlugin)
	{
		Locale locale = WebUtils.getLocale(request);
		String themeName = resolveChartPluginIconThemeName(request);

		return toHtmlChartPluginView(chartPlugin, themeName, locale);
	}

	protected HtmlChartPluginView toHtmlChartPluginView(HtmlChartPlugin chartPlugin, String themeName, Locale locale)
	{
		HtmlChartPluginView pluginView = new HtmlChartPluginView();

		pluginView.setId(chartPlugin.getId());
		LabelUtil.concrete(chartPlugin, pluginView, locale);
		pluginView.setIconUrl(resolveIconUrl(chartPlugin, themeName));
		pluginView.setDataSigns(DataSign.clone(chartPlugin.getDataSigns(), locale));
		pluginView.setDataSetRange(chartPlugin.getDataSetRange());
		pluginView.setVersion(chartPlugin.getVersion());
		pluginView.setOrder(chartPlugin.getOrder());
		pluginView.setCategories(Category.clone(chartPlugin.getCategories(), locale));
		pluginView.setCategoryOrders(chartPlugin.getCategoryOrders());
		pluginView.setAttributes(ChartPluginAttribute.clone(chartPlugin.getAttributes(), locale));
		pluginView.setAuthor(chartPlugin.getAuthor());
		pluginView.setContact(chartPlugin.getContact());
		pluginView.setIssueDate(chartPlugin.getIssueDate());
		pluginView.setPlatformVersion(chartPlugin.getPlatformVersion());

		return pluginView;
	}

	protected String resolveChartPluginIconThemeName(HttpServletRequest request)
	{
		DashboardTheme dashboardTheme = resolveDashboardTheme(request);
		return dashboardTheme.getName();
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
	 * {@linkplain HtmlChartPlugin}视图对象。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class HtmlChartPluginView extends HtmlChartPluginVo implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private String iconUrl = null;

		public HtmlChartPluginView()
		{
			super();
		}

		public HtmlChartPluginView(String id, Label nameLabel)
		{
			super(id, nameLabel);
		}

		public String getIconUrl()
		{
			return iconUrl;
		}

		public void setIconUrl(String iconUrl)
		{
			this.iconUrl = iconUrl;
		}
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
