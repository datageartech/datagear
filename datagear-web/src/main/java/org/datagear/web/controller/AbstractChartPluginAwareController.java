/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
import org.datagear.analysis.ChartPluginResource;
import org.datagear.analysis.DashboardTheme;
import org.datagear.analysis.DataSetResult;
import org.datagear.analysis.DataSign;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.RenderException;
import org.datagear.analysis.support.CategorizationResolver;
import org.datagear.analysis.support.CategorizationResolver.Categorization;
import org.datagear.analysis.support.ProfileDataSet;
import org.datagear.analysis.support.html.DirectoryHtmlChartPluginManager;
import org.datagear.analysis.support.html.HtmlChart;
import org.datagear.analysis.support.html.HtmlChartPlugin;
import org.datagear.management.domain.ChartDataSetVO;
import org.datagear.util.StringUtil;
import org.datagear.util.i18n.Label;
import org.datagear.web.util.KeywordMatcher;
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

	private CategorizationResolver categorizationResolver = new CategorizationResolver();

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

	public CategorizationResolver getCategorizationResolver()
	{
		return categorizationResolver;
	}

	public void setCategorizationResolver(CategorizationResolver categorizationResolver)
	{
		this.categorizationResolver = categorizationResolver;
	}

	protected List<Categorization> resolveCategorizations(List<HtmlChartPluginView> chartPluginVOs)
	{
		return this.categorizationResolver.resolve(chartPluginVOs);
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

		return KeywordMatcher.<HtmlChartPluginView> match(pluginViews, keyword,
				new KeywordMatcher.MatchValue<HtmlChartPluginView>()
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

		pluginView.setNameLabel(toConcreteLabel(chartPlugin.getNameLabel(), locale));
		pluginView.setDescLabel(toConcreteLabel(chartPlugin.getDescLabel(), locale));

		pluginView.setIconUrl(resolveIconUrl(chartPlugin, themeName));

		List<DataSign> dataSigns = chartPlugin.getDataSigns();
		if (dataSigns != null)
		{
			List<DataSign> dataSignViews = new ArrayList<>(dataSigns.size());
			for (DataSign dataSign : dataSigns)
			{
				DataSign view = new DataSign(dataSign.getName(), dataSign.isRequired(), dataSign.isMultiple());
				view.setNameLabel(toConcreteLabel(dataSign.getNameLabel(), locale));
				view.setDescLabel(toConcreteLabel(dataSign.getDescLabel(), locale));

				dataSignViews.add(view);
			}

			pluginView.setDataSigns(dataSignViews);
		}

		pluginView.setVersion(chartPlugin.getVersion());
		pluginView.setOrder(chartPlugin.getOrder());

		List<Category> categories = chartPlugin.getCategories();
		if (categories != null)
		{
			List<Category> categoryViews = new ArrayList<Category>(categories.size());

			for(Category category : categories)
			{
				Category categoryView = new Category(category.getName());
				categoryView.setNameLabel(toConcreteLabel(category.getNameLabel(), locale));
				categoryView.setDescLabel(toConcreteLabel(category.getDescLabel(), locale));
				categoryView.setOrder(category.getOrder());

				categoryViews.add(categoryView);
			}

			pluginView.setCategories(categoryViews);
		}

		pluginView.setCategoryOrders(chartPlugin.getCategoryOrders());

		return pluginView;
	}

	protected String resolveChartPluginIconThemeName(HttpServletRequest request)
	{
		DashboardTheme dashboardTheme = resolveDashboardTheme(request);
		return dashboardTheme.getName();
	}

	protected Label toConcreteLabel(Label label, Locale locale)
	{
		return Label.concrete(label, locale);
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

		@JsonIgnore
		@Override
		public int getOrder()
		{
			return super.getOrder();
		}

		@Override
		public HtmlChart renderChart(RenderContext renderContext, ChartDefinition chartDefinition)
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
