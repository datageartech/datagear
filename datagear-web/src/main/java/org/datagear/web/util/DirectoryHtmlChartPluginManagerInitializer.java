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

package org.datagear.web.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.datagear.analysis.support.JsonSupport;
import org.datagear.analysis.support.html.DirectoryHtmlChartPluginManager;
import org.datagear.analysis.support.html.HtmlChartPlugin;
import org.datagear.util.FileUtil;
import org.datagear.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * {@linkplain DirectoryHtmlChartPluginManager}初始化器。
 * <p>
 * 此类的{@linkplain #init()}方法首先调用{@linkplain DirectoryHtmlChartPluginManager#init()}方法，
 * 然后加载{@linkplain #getClasspathPatterns()}类路径（默认为：<code>org/datagear/web/builtInHtmlChartPlugins/*.zip</code>）的{@linkplain HtmlChartPlugin}。
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

	private ResourcePatternResolver resourcePatternResolver;

	private String[] classpathPatterns = new String[] { DEFAULT_CLASSPATH_PATTERN };

	private DirectoryHtmlChartPluginManager directoryHtmlChartPluginManager;

	/** 临时文件目录，用于存放临时文件 */
	private File tmpDirectory;

	/** 已载入过的图表插件上次修改时间信息存储文件 */
	private File builtinChartPluginLastModifiedFile;

	public DirectoryHtmlChartPluginManagerInitializer()
	{
		super();
	}

	public DirectoryHtmlChartPluginManagerInitializer(ResourcePatternResolver resourcePatternResolver,
			DirectoryHtmlChartPluginManager directoryHtmlChartPluginManager,
			File tmpDirectory, File builtinChartPluginLastModifiedFile)
	{
		super();
		this.resourcePatternResolver = resourcePatternResolver;
		this.directoryHtmlChartPluginManager = directoryHtmlChartPluginManager;
		this.tmpDirectory = tmpDirectory;
		this.builtinChartPluginLastModifiedFile = builtinChartPluginLastModifiedFile;
	}

	public ResourcePatternResolver getResourcePatternResolver()
	{
		return resourcePatternResolver;
	}

	public void setResourcePatternResolver(ResourcePatternResolver resourcePatternResolver)
	{
		this.resourcePatternResolver = resourcePatternResolver;
	}

	public String[] getClasspathPatterns()
	{
		return classpathPatterns;
	}

	public void setClasspathPatterns(String[] classpathPatterns)
	{
		this.classpathPatterns = classpathPatterns;
	}

	public DirectoryHtmlChartPluginManager getDirectoryHtmlChartPluginManager()
	{
		return directoryHtmlChartPluginManager;
	}

	public void setDirectoryHtmlChartPluginManager(DirectoryHtmlChartPluginManager directoryHtmlChartPluginManager)
	{
		this.directoryHtmlChartPluginManager = directoryHtmlChartPluginManager;
	}

	public File getTmpDirectory()
	{
		return tmpDirectory;
	}

	public void setTmpDirectory(File tmpDirectory)
	{
		this.tmpDirectory = tmpDirectory;
	}

	public File getBuiltinChartPluginLastModifiedFile()
	{
		return builtinChartPluginLastModifiedFile;
	}

	public void setBuiltinChartPluginLastModifiedFile(File builtinChartPluginLastModifiedFile)
	{
		this.builtinChartPluginLastModifiedFile = builtinChartPluginLastModifiedFile;
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
			loadHtmlChartPlugins();
		}
		catch (Throwable t)
		{
			if (LOGGER.isErrorEnabled())
				LOGGER.error("Load built-in " + HtmlChartPlugin.class.getSimpleName() + "s error :", t);
		}
	}

	protected void loadHtmlChartPlugins() throws IOException
	{
		Map<String, Number> prevLastModifieds = prevLastModifieds();
		Map<String, Number> thisLastModifieds = new HashMap<String, Number>();

		File tmpDirectory = createTmpWorkDirectory();

		for(String classpathPattern : this.classpathPatterns)
		{
			Resource[] resources = this.resourcePatternResolver.getResources(classpathPattern);
	
			if (resources != null)
			{
				for (Resource resource : resources)
				{
					String name = resource.getFilename();
					long thisLastModified = lastModified(resource);
					Number prevLastModified = prevLastModifieds.get(name);
		
					if (prevLastModified == null || prevLastModified.longValue() != thisLastModified)
					{
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
		
						thisLastModifieds.put(name, thisLastModified);
		
						if (LOGGER.isDebugEnabled())
							LOGGER.debug(
									"The built-in chart plugin file [" + name + "] has been changed, reload is to be done");
					}
					else
					{
						if (LOGGER.isDebugEnabled())
							LOGGER.debug("The built-in chart plugin file [" + name + "] has no change, reload is ignored");
					}
				}
			}
		}

		if (!thisLastModifieds.isEmpty())
		{
			Set<HtmlChartPlugin> plugins = this.directoryHtmlChartPluginManager.upload(tmpDirectory);

			Set<String> pluginIds = new HashSet<>();
			for (HtmlChartPlugin plugin : plugins)
				pluginIds.add(plugin.getId());

			if (LOGGER.isInfoEnabled())
			{
				LOGGER.info("Reloaded the following " + plugins.size() + " built-in chart plugins :");
				LOGGER.info(pluginIds.toString());
			}

			Map<String, Number> saveLastModifieds = new HashMap<String, Number>(prevLastModifieds);
			saveLastModifieds.putAll(thisLastModifieds);
			saveLastModifieds(saveLastModifieds);
		}
		else
		{
			if (LOGGER.isInfoEnabled())
				LOGGER.info("No built-in chart plugin need reload");
		}
	}

	protected File createTmpWorkDirectory() throws IOException
	{
		return FileUtil.generateUniqueDirectory(this.tmpDirectory);
	}

	/**
	 * 获取上一次加载插件资源的时间。
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected Map<String, Number> prevLastModifieds()
	{
		Map<String, Number> map = new HashMap<String, Number>();

		if (builtinChartPluginLastModifiedFile == null || !builtinChartPluginLastModifiedFile.exists())
			return map;

		Reader reader = null;
		try
		{
			reader = IOUtil.getReader(this.builtinChartPluginLastModifiedFile, IOUtil.CHARSET_UTF_8);
			map = JsonSupport.parseNonStardand(reader, Map.class);
		}
		catch(Throwable t)
		{
		}
		finally
		{
			IOUtil.close(reader);
		}

		return map;
	}

	protected void saveLastModifieds(Map<String, Number> map)
	{
		String json = JsonSupport.generate(map, "{}");

		Writer writer = null;
		try
		{
			writer = IOUtil.getWriter(this.builtinChartPluginLastModifiedFile, IOUtil.CHARSET_UTF_8);
			writer.write(json);
		}
		catch(Throwable t)
		{
		}
		finally
		{
			IOUtil.close(writer);
		}
	}

	protected long lastModified(Resource resource)
	{
		try
		{
			return resource.lastModified();
		}
		catch(Throwable t)
		{
			return System.currentTimeMillis();
		}
	}
}
