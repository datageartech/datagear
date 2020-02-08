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
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.datagear.analysis.Chart;
import org.datagear.analysis.ChartDataSet;
import org.datagear.analysis.ChartPlugin;
import org.datagear.analysis.DataSign;
import org.datagear.analysis.Icon;
import org.datagear.analysis.RenderException;
import org.datagear.analysis.RenderStyle;
import org.datagear.analysis.support.AbstractChartPlugin;
import org.datagear.analysis.support.html.DirectoryHtmlChartPluginManager;
import org.datagear.analysis.support.html.HtmlChartPlugin;
import org.datagear.analysis.support.html.HtmlRenderContext;
import org.datagear.util.i18n.Label;
import org.datagear.web.convert.ClassDataConverter;
import org.datagear.web.util.KeywordMatcher;
import org.datagear.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

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

	public AbstractChartPluginAwareController()
	{
		super();
	}

	public AbstractChartPluginAwareController(MessageSource messageSource, ClassDataConverter classDataConverter,
			DirectoryHtmlChartPluginManager directoryHtmlChartPluginManager)
	{
		super(messageSource, classDataConverter);
		this.directoryHtmlChartPluginManager = directoryHtmlChartPluginManager;
	}

	public DirectoryHtmlChartPluginManager getDirectoryHtmlChartPluginManager()
	{
		return directoryHtmlChartPluginManager;
	}

	public void setDirectoryHtmlChartPluginManager(DirectoryHtmlChartPluginManager directoryHtmlChartPluginManager)
	{
		this.directoryHtmlChartPluginManager = directoryHtmlChartPluginManager;
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
		List<HtmlChartPluginVO> pluginViews = new ArrayList<HtmlChartPluginVO>();

		List<ChartPlugin<HtmlRenderContext>> plugins = getDirectoryHtmlChartPluginManager()
				.getAll(HtmlRenderContext.class);

		if (plugins != null)
		{
			Locale locale = WebUtils.getLocale(request);
			RenderStyle renderStyle = resolveRenderStyle(request);

			for (ChartPlugin<HtmlRenderContext> plugin : plugins)
			{
				if (plugin instanceof HtmlChartPlugin<?>)
				{
					HtmlChartPlugin<HtmlRenderContext> htmlChartPlugin = (HtmlChartPlugin<HtmlRenderContext>) plugin;
					pluginViews.add(toHtmlChartPluginVO(htmlChartPlugin, renderStyle, locale));
				}
			}
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

	protected HtmlChartPluginVO toHtmlChartPluginVO(HtmlChartPlugin<?> chartPlugin, RenderStyle renderStyle,
			Locale locale)
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

		pluginVO.setVersion(chartPlugin.getVersion());

		List<DataSign> dataSigns = chartPlugin.getDataSigns();
		if (dataSigns != null)
		{
			List<DataSign> dataSignVOs = new ArrayList<DataSign>(dataSigns.size());
			for (DataSign dataSign : dataSigns)
			{
				DataSign view = new DataSign(dataSign.getName(), dataSign.isRequired(),
						dataSign.isMultiple());
				view.setNameLabel(toConcreteLabel(dataSign.getNameLabel(), locale));
				view.setDescLabel(toConcreteLabel(dataSign.getDescLabel(), locale));

				dataSignVOs.add(view);
			}

			pluginVO.setDataSigns(dataSignVOs);
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

	protected String resolveIconUrl(HtmlChartPlugin<?> plugin)
	{
		return "/analysis/chartPlugin/icon/" + plugin.getId();
	}

	/**
	 * {@linkplain HtmlChartPlugin}视图对象。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class HtmlChartPluginVO extends AbstractChartPlugin<HtmlRenderContext> implements Serializable
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
		public Chart renderChart(HtmlRenderContext renderContext, Map<String, ?> chartPropertyValues,
				ChartDataSet... chartDataSets) throws RenderException
		{
			throw new UnsupportedOperationException();
		}
	}
}
