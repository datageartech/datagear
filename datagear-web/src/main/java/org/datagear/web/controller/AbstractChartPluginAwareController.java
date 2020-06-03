/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.web.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.datagear.analysis.Category;
import org.datagear.analysis.Chart;
import org.datagear.analysis.ChartDefinition;
import org.datagear.analysis.DataSign;
import org.datagear.analysis.Icon;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.RenderException;
import org.datagear.analysis.RenderStyle;
import org.datagear.analysis.support.AbstractChartPlugin;
import org.datagear.analysis.support.CategorizationResolver;
import org.datagear.analysis.support.CategorizationResolver.Categorization;
import org.datagear.analysis.support.html.DirectoryHtmlChartPluginManager;
import org.datagear.analysis.support.html.HtmlChartPlugin;
import org.datagear.util.i18n.Label;
import org.datagear.web.util.KeywordMatcher;
import org.datagear.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;

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
				RenderStyle renderStyle = resolveRenderStyle(request);
				return toHtmlChartPluginVO(plugin, renderStyle, locale);
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
			RenderStyle renderStyle = resolveRenderStyle(request);

			for (HtmlChartPlugin plugin : plugins)
				pluginViews.add(toHtmlChartPluginVO(plugin, renderStyle, locale));
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
		RenderStyle renderStyle = resolveRenderStyle(request);

		return toHtmlChartPluginVO(chartPlugin, renderStyle, locale);
	}

	protected HtmlChartPluginVO toHtmlChartPluginVO(HtmlChartPlugin chartPlugin, RenderStyle renderStyle, Locale locale)
	{
		HtmlChartPluginVO pluginVO = new HtmlChartPluginVO();

		pluginVO.setId(chartPlugin.getId());

		pluginVO.setNameLabel(toConcreteLabel(chartPlugin.getNameLabel(), locale));
		pluginVO.setDescLabel(toConcreteLabel(chartPlugin.getDescLabel(), locale));
		pluginVO.setManualLabel(toConcreteLabel(chartPlugin.getManualLabel(), locale));

		Icon icon = chartPlugin.getIcon(renderStyle);
		pluginVO.setHasIcon(icon != null);
		if (pluginVO.isHasIcon())
			pluginVO.setIconUrl(resolveIconUrl(chartPlugin));

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

	protected Label toConcreteLabel(Label label, Locale locale)
	{
		if (label == null)
			return null;

		String value = label.getValue(locale);
		if (value == null)
			value = "";

		return new Label(value);
	}

	protected String resolveIconUrl(HtmlChartPlugin plugin)
	{
		return "/analysis/chartPlugin/icon/" + plugin.getId();
	}

	/**
	 * {@linkplain HtmlChartPlugin}视图对象。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class HtmlChartPluginVO extends AbstractChartPlugin implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private boolean hasIcon;

		private String iconUrl;

		public HtmlChartPluginVO()
		{
			super();
		}

		public HtmlChartPluginVO(String id, Label nameLabel)
		{
			super(id, nameLabel);
		}

		public boolean isHasIcon()
		{
			return hasIcon;
		}

		public void setHasIcon(boolean hasIcon)
		{
			this.hasIcon = hasIcon;
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
		public Chart renderChart(RenderContext renderContext, ChartDefinition chartDefinition) throws RenderException
		{
			throw new UnsupportedOperationException();
		}
	}
}
