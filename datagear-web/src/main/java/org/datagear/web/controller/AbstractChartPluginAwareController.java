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

import javax.servlet.http.HttpServletRequest;

import org.datagear.analysis.Category;
import org.datagear.analysis.ChartDataSet;
import org.datagear.analysis.ChartDefinition;
import org.datagear.analysis.DashboardTheme;
import org.datagear.analysis.DataSetResult;
import org.datagear.analysis.DataSign;
import org.datagear.analysis.Icon;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.RenderException;
import org.datagear.analysis.support.CategorizationResolver;
import org.datagear.analysis.support.CategorizationResolver.Categorization;
import org.datagear.analysis.support.ProfileDataSet;
import org.datagear.analysis.support.html.DirectoryHtmlChartPluginManager;
import org.datagear.analysis.support.html.HtmlChart;
import org.datagear.analysis.support.html.HtmlChartPlugin;
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

	protected List<Categorization> resolveCategorizations(List<HtmlChartPluginVO> chartPluginVOs)
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
	protected HtmlChartPluginVO getHtmlChartPluginVO(HttpServletRequest request, String id)
	{
		List<HtmlChartPlugin> plugins = getDirectoryHtmlChartPluginManager().getAll(HtmlChartPlugin.class);

		if (plugins == null)
			return null;

		for (HtmlChartPlugin plugin : plugins)
		{
			if (plugin.getId().equals(id))
			{
				Locale locale = WebUtils.getLocale(request);
				String themeName = resolveChartPluginIconThemeName(request);
				return toHtmlChartPluginVO(plugin, themeName, locale);
			}
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
	protected List<HtmlChartPluginVO> findHtmlChartPluginVOs(HttpServletRequest request, String keyword)
	{
		List<HtmlChartPluginVO> pluginViews = new ArrayList<>();

		List<HtmlChartPlugin> plugins = getDirectoryHtmlChartPluginManager().getAll(HtmlChartPlugin.class);

		if (plugins != null)
		{
			Locale locale = WebUtils.getLocale(request);
			String themeName = resolveChartPluginIconThemeName(request);

			for (HtmlChartPlugin plugin : plugins)
				pluginViews.add(toHtmlChartPluginVO(plugin, themeName, locale));
		}

		return KeywordMatcher.<HtmlChartPluginVO> match(pluginViews, keyword,
				new KeywordMatcher.MatchValue<HtmlChartPluginVO>()
				{
					@Override
					public String[] get(HtmlChartPluginVO t)
					{
						return new String[] { (t.getNameLabel() == null ? null : t.getNameLabel().getValue()),
								(t.getDescLabel() == null ? null : t.getDescLabel().getValue()) };
					}
				});
	}

	protected HtmlChartPluginVO toHtmlChartPluginVO(HttpServletRequest request, HtmlChartPlugin chartPlugin)
	{
		Locale locale = WebUtils.getLocale(request);
		String themeName = resolveChartPluginIconThemeName(request);

		return toHtmlChartPluginVO(chartPlugin, themeName, locale);
	}

	protected HtmlChartPluginVO toHtmlChartPluginVO(HtmlChartPlugin chartPlugin, String themeName, Locale locale)
	{
		HtmlChartPluginVO pluginVO = new HtmlChartPluginVO();

		pluginVO.setId(chartPlugin.getId());

		pluginVO.setNameLabel(toConcreteLabel(chartPlugin.getNameLabel(), locale));
		pluginVO.setDescLabel(toConcreteLabel(chartPlugin.getDescLabel(), locale));
		pluginVO.setManualLabel(toConcreteLabel(chartPlugin.getManualLabel(), locale));

		pluginVO.setIconUrl(resolveIconUrl(chartPlugin, themeName));

		List<DataSign> dataSigns = chartPlugin.getDataSigns();
		if (dataSigns != null)
		{
			List<DataSign> dataSignVOs = new ArrayList<>(dataSigns.size());
			for (DataSign dataSign : dataSigns)
			{
				DataSign view = new DataSign(dataSign.getName(), dataSign.isRequired(), dataSign.isMultiple());
				view.setNameLabel(toConcreteLabel(dataSign.getNameLabel(), locale));
				view.setDescLabel(toConcreteLabel(dataSign.getDescLabel(), locale));

				dataSignVOs.add(view);
			}

			pluginVO.setDataSigns(dataSignVOs);
		}

		pluginVO.setVersion(chartPlugin.getVersion());
		pluginVO.setOrder(chartPlugin.getOrder());

		Category category = chartPlugin.getCategory();
		if (category != null)
		{
			Category categoryVO = new Category(category.getName());
			categoryVO.setNameLabel(toConcreteLabel(category.getNameLabel(), locale));
			categoryVO.setDescLabel(toConcreteLabel(category.getDescLabel(), locale));
			categoryVO.setOrder(category.getOrder());
			pluginVO.setCategory(categoryVO);
		}

		return pluginVO;
	}

	protected String resolveChartPluginIconThemeName(HttpServletRequest request)
	{
		DashboardTheme dashboardTheme = resolveDashboardTheme(request);
		return dashboardTheme.getName();
	}

	protected Label toConcreteLabel(Label label, Locale locale)
	{
		if (label == null)
			return null;

		String value = label.getValue(locale);
		if (value == null)
			value = "";

		return new Label(value);
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

		Icon icon = plugin.getIcon(themeName);
		return (icon == null ? null : resolveIconUrl(plugin));
	}

	protected String resolveIconUrl(HtmlChartPlugin plugin)
	{
		return "/analysis/chartPlugin/icon/" + plugin.getId();
	}

	protected ChartDataSetViewObj[] toChartDataSetViewObjs(ChartDataSet[] chartDataSets)
	{
		if (chartDataSets == null)
			return null;

		ChartDataSetViewObj[] viewObjs = new ChartDataSetViewObj[chartDataSets.length];

		for (int i = 0; i < chartDataSets.length; i++)
			viewObjs[i] = new ChartDataSetViewObj(chartDataSets[i]);

		return viewObjs;
	}

	/**
	 * {@linkplain HtmlChartPlugin}视图对象。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class HtmlChartPluginVO extends HtmlChartPlugin implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private String iconUrl = null;

		public HtmlChartPluginVO()
		{
			super();
		}

		public HtmlChartPluginVO(String id, Label nameLabel)
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
	public static class ChartDataSetViewObj extends ChartDataSet
	{
		public ChartDataSetViewObj()
		{
			super();
		}

		public ChartDataSetViewObj(ChartDataSet chartDataSet)
		{
			super();
			setDataSet(ProfileDataSet.valueOf(chartDataSet.getDataSet()));
			setPropertySigns(chartDataSet.getPropertySigns());
			setAlias(chartDataSet.getAlias());
			setParamValues(chartDataSet.getParamValues());
		}

		@JsonIgnore
		@Override
		public boolean isResultReady()
		{
			return false;
		}

		@JsonIgnore
		@Override
		public DataSetResult getResult()
		{
			return null;
		}
	}
}
