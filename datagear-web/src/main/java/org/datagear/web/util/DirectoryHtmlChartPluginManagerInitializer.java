/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.datagear.analysis.support.html.DirectoryHtmlChartPluginManager;
import org.datagear.analysis.support.html.HtmlChartPlugin;
import org.datagear.util.FileUtil;
import org.datagear.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * {@linkplain DirectoryHtmlChartPluginManager}初始化器。
 * <p>
 * 此类的{@linkplain #init()}方法首先调用{@linkplain DirectoryHtmlChartPluginManager#init()}方法，
 * 然后加载<code>org/datagear/web/builtInHtmlChartPlugins/*.zip</code>类路径的{@linkplain HtmlChartPlugin}。
 * </p>
 * <p>
 * 创建此类的实例后，需要调用{@linkplain #init()}执行初始化。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class DirectoryHtmlChartPluginManagerInitializer
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryHtmlChartPluginManagerInitializer.class);

	public static final String DEFAULT_CLASSPATH_PATTERN = "classpath:org/datagear/web/builtInHtmlChartPlugins/*.zip";

	private String classpathPattern = DEFAULT_CLASSPATH_PATTERN;

	private DirectoryHtmlChartPluginManager directoryHtmlChartPluginManager;

	private PathMatchingResourcePatternResolver _PathMatchingResourcePatternResolver;

	public DirectoryHtmlChartPluginManagerInitializer()
	{
		super();
		this._PathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver(
				DirectoryHtmlChartPluginManagerInitializer.class.getClassLoader());
	}

	public DirectoryHtmlChartPluginManagerInitializer(DirectoryHtmlChartPluginManager directoryHtmlChartPluginManager)
	{
		super();
		this.directoryHtmlChartPluginManager = directoryHtmlChartPluginManager;
		this._PathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver(
				DirectoryHtmlChartPluginManagerInitializer.class.getClassLoader());
	}

	public String getClasspathPattern()
	{
		return classpathPattern;
	}

	public void setClasspathPattern(String classpathPattern)
	{
		this.classpathPattern = classpathPattern;
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
	 * 初始化。
	 */
	public void init()
	{
		if (LOGGER.isInfoEnabled())
			LOGGER.info("Start init " + DirectoryHtmlChartPluginManager.class.getSimpleName());

		this.directoryHtmlChartPluginManager.init();
		load();

		if (LOGGER.isInfoEnabled())
			LOGGER.info("Finish init " + DirectoryHtmlChartPluginManager.class.getSimpleName());
	}

	protected void load()
	{
		try
		{
			loadHtmlChartPlugins(this.classpathPattern);
		}
		catch (Throwable t)
		{
			if (LOGGER.isErrorEnabled())
				LOGGER.error("Load built-in " + HtmlChartPlugin.class.getSimpleName() + "s error :", t);
		}
	}

	protected void loadHtmlChartPlugins(String classpathPattern) throws IOException
	{
		Resource[] resources = this._PathMatchingResourcePatternResolver.getResources(classpathPattern);

		if (resources == null || resources.length == 0)
			return;

		File tmpDirectory = FileUtil.createTempDirectory();

		for (Resource resource : resources)
		{
			String name = resource.getFilename();
			File file = FileUtil.getFile(tmpDirectory, name);

			InputStream in = null;

			try
			{
				in = resource.getInputStream();
				IOUtil.write(in, file);
			}
			finally
			{
				IOUtil.close(in);
			}
		}

		Set<HtmlChartPlugin> plugins = this.directoryHtmlChartPluginManager.upload(tmpDirectory);

		Set<String> pluginIds = new HashSet<>();
		for (HtmlChartPlugin plugin : plugins)
			pluginIds.add(plugin.getId());

		if (LOGGER.isInfoEnabled())
			LOGGER.info("Loaded the following built-in " + HtmlChartPlugin.class.getSimpleName() + "s :"
					+ pluginIds.toString());
	}
}
