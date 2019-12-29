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

import org.datagear.analysis.ChartPlugin;
import org.datagear.analysis.Icon;
import org.datagear.analysis.RenderStyle;
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
	 * 查找插件信息列表。
	 * 
	 * @param request
	 * @param keyword
	 * @return
	 */
	protected List<HtmlChartPluginInfo> findHtmlChartPluginInfos(HttpServletRequest request, String keyword)
	{
		List<HtmlChartPluginInfo> pluginInfos = new ArrayList<HtmlChartPluginInfo>();

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
					pluginInfos.add(toHtmlChartPluginInfo(htmlChartPlugin, renderStyle, locale));
				}
			}
		}

		return KeywordMatcher.<HtmlChartPluginInfo> match(pluginInfos, keyword,
				new KeywordMatcher.MatchValue<HtmlChartPluginInfo>()
				{
					@Override
					public String[] get(HtmlChartPluginInfo t)
					{
						return new String[] { t.getName(), t.getDesc() };
					}
				});
	}

	protected HtmlChartPluginInfo toHtmlChartPluginInfo(HtmlChartPlugin<?> chartPlugin, RenderStyle renderStyle,
			Locale locale)
	{
		HtmlChartPluginInfo pluginInfo = new HtmlChartPluginInfo();

		pluginInfo.setId(chartPlugin.getId());

		Label nameLabel = chartPlugin.getNameLabel();
		if (nameLabel != null)
			pluginInfo.setName(nameLabel.getValue(locale));

		Label descLabel = chartPlugin.getDescLabel();
		if (descLabel != null)
			pluginInfo.setDesc(descLabel.getValue(locale));

		Label manualLabel = chartPlugin.getManualLabel();
		if (manualLabel != null)
			pluginInfo.setManual(manualLabel.getValue(locale));

		Icon icon = chartPlugin.getIcon(renderStyle);
		pluginInfo.setHasIcon(icon != null);
		if (pluginInfo.isHasIcon())
			pluginInfo.setIconUrl(resolveIconUrl(chartPlugin));

		pluginInfo.setVersion(chartPlugin.getVersion());

		return pluginInfo;
	}

	protected String resolveIconUrl(HtmlChartPlugin<?> plugin)
	{
		return "/analysis/chartPlugin/icon/" + plugin.getId();
	}

	public static class HtmlChartPluginInfo implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private String id;

		private String name;

		private String desc;

		private String manual;

		private boolean hasIcon;

		private String iconUrl;

		private String version;

		public HtmlChartPluginInfo()
		{
			super();
		}

		public HtmlChartPluginInfo(String id, String name)
		{
			super();
			this.id = id;
			this.name = name;
		}

		public String getId()
		{
			return id;
		}

		public void setId(String id)
		{
			this.id = id;
		}

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		public String getDesc()
		{
			return desc;
		}

		public void setDesc(String desc)
		{
			this.desc = desc;
		}

		public String getManual()
		{
			return manual;
		}

		public void setManual(String manual)
		{
			this.manual = manual;
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

		public String getVersion()
		{
			return version;
		}

		public void setVersion(String version)
		{
			this.version = version;
		}
	}
}
