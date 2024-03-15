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
	public Set<HtmlChartPlugin> load()
	{
		Set<HtmlChartPlugin> plugins = this.htmlChartPluginLoader.loadAll(this.directory);

		if (plugins != null)
		{
			for (HtmlChartPlugin plugin : plugins)
				this.chartPluginManager.register(plugin);
		}

		return plugins;
	}
}
