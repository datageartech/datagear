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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.datagear.analysis.Category;
import org.datagear.analysis.ChartDataSet;
import org.datagear.analysis.ChartDefinition;
import org.datagear.analysis.ChartPluginAttribute;
import org.datagear.analysis.ChartPluginResource;
import org.datagear.analysis.DashboardTheme;
import org.datagear.analysis.DataSetResult;
import org.datagear.analysis.DataSign;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.RenderException;
import org.datagear.analysis.support.ChartPluginCategorizationResolver;
import org.datagear.analysis.support.ChartPluginCategorizationResolver.Categorization;
import org.datagear.analysis.support.ProfileDataSet;
import org.datagear.analysis.support.html.DirectoryHtmlChartPluginManager;
import org.datagear.analysis.support.html.HtmlChart;
import org.datagear.analysis.support.html.HtmlChartPlugin;
import org.datagear.analysis.support.html.HtmlChartPluginScriptObjectWriter;
import org.datagear.analysis.support.html.HtmlChartScriptObjectWriter;
import org.datagear.analysis.support.html.HtmlRenderContextScriptObjectWriter;
import org.datagear.analysis.support.html.JsChartRenderer;
import org.datagear.management.domain.ChartDataSetVO;
import org.datagear.util.KeywordMatcher;
import org.datagear.util.KeywordMatcher.MatchValue;
import org.datagear.util.StringUtil;
import org.datagear.util.i18n.Label;
import org.datagear.util.i18n.LabelUtil;
import org.datagear.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 抽象插件相关的控制器。
 * 
 * @author datagear@163.com
 *
 */
public class AbstractChartPluginAwareController extends AbstractDataAnalysisController
{
	@Autowired
	private DirectoryHtmlChartPluginManager directoryHtmlChartPluginManager;

	private ChartPluginCategorizationResolver chartPluginCategorizationResolver = new ChartPluginCategorizationResolver();

	private KeywordMatcher keywordMatcher = new KeywordMatcher();

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

	protected List<Categorization> resolveCategorizations(List<HtmlChartPluginView> chartPluginVOs)
	{
		return this.chartPluginCategorizationResolver.resolve(chartPluginVOs);
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
								(t.getDescLabel() == null ? null : t.getDescLabel().getValue()) };
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

	protected ChartDataSetView[] toChartDataSetViews(ChartDataSet[] chartDataSets)
	{
		if (chartDataSets == null)
			return null;

		ChartDataSetView[] views = new ChartDataSetView[chartDataSets.length];

		for (int i = 0; i < chartDataSets.length; i++)
			views[i] = new ChartDataSetView(chartDataSets[i]);

		return views;
	}

	/**
	 * {@linkplain HtmlChartPlugin}视图对象。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class HtmlChartPluginView extends HtmlChartPlugin implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private String iconUrl = null;

		public HtmlChartPluginView()
		{
			super();
		}

		public HtmlChartPluginView(String id, Label nameLabel)
		{
			super.setId(id);
			super.setNameLabel(nameLabel);
		}

		public String getIconUrl()
		{
			return iconUrl;
		}

		public void setIconUrl(String iconUrl)
		{
			this.iconUrl = iconUrl;
		}

		@Override
		public JsChartRenderer getRenderer()
		{
			return super.getRenderer();
		}

		@JsonIgnore
		@Override
		public HtmlChartPluginScriptObjectWriter getPluginWriter()
		{
			return super.getPluginWriter();
		}

		@JsonIgnore
		@Override
		public HtmlRenderContextScriptObjectWriter getRenderContextWriter()
		{
			return super.getRenderContextWriter();
		}

		@JsonIgnore
		@Override
		public HtmlChartScriptObjectWriter getChartWriter()
		{
			return super.getChartWriter();
		}

		@JsonIgnore
		@Override
		public List<ChartPluginResource> getResources()
		{
			return super.getResources();
		}

		@JsonIgnore
		@Override
		public Map<String, String> getIconResourceNames()
		{
			return super.getIconResourceNames();
		}

		@JsonIgnore
		@Override
		public String getElementTagName()
		{
			return super.getElementTagName();
		}

		@JsonIgnore
		@Override
		public long getLastModified()
		{
			return super.getLastModified();
		}

		@JsonIgnore
		@Override
		public String getNewLine()
		{
			return super.getNewLine();
		}

		@Override
		public int getOrder()
		{
			return super.getOrder();
		}

		@Override
		public HtmlChart renderChart(ChartDefinition chartDefinition, RenderContext renderContext)
				throws RenderException
		{
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * {@linkplain ChartDataSet}视图对象。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class ChartDataSetView extends ChartDataSetVO
	{
		private static final long serialVersionUID = 1L;

		public ChartDataSetView()
		{
			super();
		}

		public ChartDataSetView(ChartDataSet chartDataSet)
		{
			super();
			setDataSet(ProfileDataSet.valueOf(chartDataSet.getDataSet()));
			setPropertySigns(chartDataSet.getPropertySigns());
			setAlias(chartDataSet.getAlias());
			setAttachment(chartDataSet.isAttachment());
			setQuery(chartDataSet.getQuery());
			setPropertyAliases(chartDataSet.getPropertyAliases());
			setPropertyOrders(chartDataSet.getPropertyOrders());
		}

		@JsonIgnore
		@Override
		public DataSetResult getResult()
		{
			return null;
		}
	}
}
