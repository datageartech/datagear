/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support.html;

import java.io.File;
import java.util.Set;

import org.datagear.analysis.ChartPluginManager;
import org.datagear.util.FileUtil;

/**
 * 加载{@linkplain HtmlChartPlugin} Bean。
 * <p>
 * 调用它的{@linkplain #load()}方法将从{@linkplain #getDirectory()}目录加载所有{@linkplain HtmlChartPlugin}，
 * 并存入{@linkplain #getChartPluginManager()}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class LoadHtmlChartPluginBean
{
	private File directory;

	private ChartPluginManager chartPluginManager;

	private HtmlChartPluginLoader htmlChartPluginLoader = new HtmlChartPluginLoader();

	public LoadHtmlChartPluginBean()
	{
	}

	public LoadHtmlChartPluginBean(File directory, ChartPluginManager chartPluginManager)
	{
		super();
		this.directory = directory;
		this.chartPluginManager = chartPluginManager;
	}

	public LoadHtmlChartPluginBean(String directory, ChartPluginManager chartPluginManager)
	{
		super();
		this.directory = FileUtil.getDirectory(directory);
		this.chartPluginManager = chartPluginManager;
	}

	public File getDirectory()
	{
		return directory;
	}

	public void setDirectory(File directory)
	{
		this.directory = directory;
	}

	public void setDirectoryString(String directory)
	{
		this.directory = FileUtil.getDirectory(directory);
	}

	public ChartPluginManager getChartPluginManager()
	{
		return chartPluginManager;
	}

	public void setChartPluginManager(ChartPluginManager chartPluginManager)
	{
		this.chartPluginManager = chartPluginManager;
	}

	public HtmlChartPluginLoader getHtmlChartPluginLoader()
	{
		return htmlChartPluginLoader;
	}

	public void setHtmlChartPluginLoader(HtmlChartPluginLoader htmlChartPluginLoader)
	{
		this.htmlChartPluginLoader = htmlChartPluginLoader;
	}

	/**
	 * 加载。
	 */
	public Set<HtmlChartPlugin<?>> load()
	{
		Set<HtmlChartPlugin<?>> plugins = this.htmlChartPluginLoader.loads(this.directory);

		if (plugins != null)
		{
			for (HtmlChartPlugin<?> plugin : plugins)
				this.chartPluginManager.register(plugin);
		}

		return plugins;
	}
}
