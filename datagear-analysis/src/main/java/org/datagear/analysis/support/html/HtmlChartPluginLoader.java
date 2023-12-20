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

package org.datagear.analysis.support.html;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.datagear.analysis.ChartPluginResource;
import org.datagear.analysis.support.FileChartPluginResource;
import org.datagear.analysis.support.JsonChartPluginPropertiesResolver;
import org.datagear.analysis.support.ZipEntryChartPluginResource;
import org.datagear.analysis.support.html.HtmlChartPluginJsDefResolver.JsDefContent;
import org.datagear.util.FileUtil;
import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;

/**
 * {@linkplain HtmlChartPlugin}加载器。
 * <p>
 * 此类从固定格式的文件夹或者ZIP文件中加载{@linkplain HtmlChartPlugin}。
 * </p>
 * <p>
 * 注意：加载后的{@linkplain HtmlChartPlugin}中与{@linkplain ChartPluginResource}相关的操作仍依赖原始文件。
 * </p>
 * <p>
 * 它支持的文件结构规范如下：
 * </p>
 * <code>
 * <pre>
 * |---- plugin.json
 * |---- renderer.js    //可选，当plugin.json里没有定义renderer（或chartRenderer）属性时必须
 * |---- ...
 * </pre>
 * </code>
 * <p>
 * <code>plugin.json</code>文件格式规范如下：
 * </p>
 * <code>
 * <pre>
 * {
 * 	//基本属性，参考{@linkplain JsonChartPluginPropertiesResolver}
 * 	...,
 * 	
 * 	//可选，图表渲染器JS对象定义，通常包含用于渲染图表的函数
 * 	//也可以不在此定义，而在单独的renderer.js文件中定义
 * 	renderer: {...},
 * 	//或者用于兼容旧版本（4.0.0及以前版本）的
 * 	chartRenderer: {...}
 * }
 * </pre>
 * </code>
 * <p>
 * 如果<code>plugin.json</code>中定义了插件图标，比如：
 * </p>
 * <p>
 * <code>icons : { "LIGHT" : "icons/light.png" }</code>
 * </p>
 * <p>
 * ，那么上述文件结构中还应有<code>icons/light.png</code>文件。
 * </p>
 * <code>renderer</code>（或者<code>chartRenderer</code>）用于定义{@linkplain HtmlChartPlugin#getRenderer()}内容。
 * </p>
 * <p>
 * 默认地，<code>plugin.json</code>、<code>renderer.js</code>文件应该为<code>UTF-8</code>编码。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class HtmlChartPluginLoader
{
	/**
	 * 插件JSON文件名
	 */
	public static final String FILE_NAME_PLUGIN = "plugin.json";

	/**
	 * 图表渲染器JS文件名
	 */
	public static final String FILE_NAME_RENDERER = "renderer.js";
	
	private HtmlChartPluginJsDefResolver htmlChartPluginJsDefResolver = new HtmlChartPluginJsDefResolver();

	private JsonChartPluginPropertiesResolver jsonChartPluginPropertiesResolver = new JsonChartPluginPropertiesResolver();

	/** 文件编码 */
	private String encoding = IOUtil.CHARSET_UTF_8;

	public HtmlChartPluginLoader()
	{
		super();
	}

	public HtmlChartPluginJsDefResolver getHtmlChartPluginJsDefResolver()
	{
		return htmlChartPluginJsDefResolver;
	}

	public void setHtmlChartPluginJsDefResolver(HtmlChartPluginJsDefResolver htmlChartPluginJsDefResolver)
	{
		this.htmlChartPluginJsDefResolver = htmlChartPluginJsDefResolver;
	}

	public JsonChartPluginPropertiesResolver getJsonChartPluginPropertiesResolver()
	{
		return jsonChartPluginPropertiesResolver;
	}

	public void setJsonChartPluginPropertiesResolver(
			JsonChartPluginPropertiesResolver jsonChartPluginPropertiesResolver)
	{
		this.jsonChartPluginPropertiesResolver = jsonChartPluginPropertiesResolver;
	}

	public String getEncoding()
	{
		return encoding;
	}

	public void setEncoding(String encoding)
	{
		this.encoding = encoding;
	}

	/**
	 * 给定目录是否是合法的{@linkplain HtmlChartPlugin}目录。
	 * 
	 * @param directory
	 * @return
	 */
	public boolean isHtmlChartPluginDirectory(File directory)
	{
		if (!directory.exists())
			return false;

		File pluginFile = FileUtil.getFile(directory, FILE_NAME_PLUGIN);

		return (pluginFile.exists());
	}

	/**
	 * 给定ZIP是否是合法的{@linkplain HtmlChartPlugin} ZIP。
	 * 
	 * @param file
	 * @return
	 * @throws HtmlChartPluginLoadException
	 */
	public boolean isHtmlChartPluginZip(File file) throws HtmlChartPluginLoadException
	{
		if (!file.exists() || !isZipFile(file))
			return false;

		ZipInputStream in = null;

		try
		{
			in = IOUtil.getZipInputStream(file);
			return isHtmlChartPluginZip(in);
		}
		catch (IOException e)
		{
			throw new HtmlChartPluginLoadException(e);
		}
		finally
		{
			IOUtil.close(in);
		}
	}

	/**
	 * 给定ZIP是否是合法的{@linkplain HtmlChartPlugin} ZIP。
	 * 
	 * @param in
	 * @return
	 * @throws HtmlChartPluginLoadException
	 */
	protected boolean isHtmlChartPluginZip(ZipInputStream in) throws HtmlChartPluginLoadException
	{
		ZipEntry zipEntry = null;

		int yes = 0;

		try
		{
			while ((zipEntry = in.getNextEntry()) != null)
			{
				String name = zipEntry.getName();

				if (zipEntry.isDirectory())
					;
				else if (name.equals(FILE_NAME_PLUGIN))
				{
					yes += 1;
				}

				in.closeEntry();
			}
		}
		catch (IOException e)
		{
			throw new HtmlChartPluginLoadException(e);
		}

		return (yes >= 1);
	}

	/**
	 * 从指定目录加载单个{@linkplain HtmlChartPlugin}。
	 * 
	 * @param directory
	 * @return {@code null}表示目录结构不合法
	 * @throws HtmlChartPluginLoadException
	 */
	public HtmlChartPlugin load(File directory) throws HtmlChartPluginLoadException
	{
		return loadSingleForDirectory(directory);
	}

	/**
	 * 从指定ZIP文件加载单个{@linkplain HtmlChartPlugin}。
	 * 
	 * @param zip
	 * @return {@code null}表示ZIP结构不合法
	 * @throws HtmlChartPluginLoadException
	 */
	public HtmlChartPlugin loadZip(File zip) throws HtmlChartPluginLoadException
	{
		return loadSingleForZip(zip);
	}

	/**
	 * 从指定ZIP输入流加载单个{@linkplain HtmlChartPlugin}。
	 * <p>
	 * 注意：此方法不会初始化{@linkplain HtmlChartPlugin#getResources()}。
	 * </p>
	 * 
	 * @param in
	 * @return {@code null}表示ZIP结构不合法
	 * @throws HtmlChartPluginLoadException
	 */
	public HtmlChartPlugin loadZip(ZipInputStream in) throws HtmlChartPluginLoadException
	{
		return loadSingleForZipInputStream(in);
	}

	/**
	 * 从指定文件加载单个{@linkplain HtmlChartPlugin}。
	 * 
	 * @param file
	 *            插件文件夹、插件ZIP包
	 * @return {@code null}表示目录结构不合法
	 * @throws HtmlChartPluginLoadException
	 */
	public HtmlChartPlugin loadFile(File file) throws HtmlChartPluginLoadException
	{
		HtmlChartPlugin plugin = null;

		if (file.isDirectory())
			plugin = loadSingleForDirectory(file);
		else if (isZipFile(file))
			plugin = loadSingleForZip(file);
		else
			plugin = loadFileExt(file);

		return plugin;
	}

	/**
	 * 从指定文件夹内加载多个{@linkplain HtmlChartPlugin}，没有，则返回空集合。
	 * <p>
	 * 文件夹内的可以包含插件文件夹或者插件ZIP包。
	 * </p>
	 * 
	 * @param directory
	 * @return
	 * @throws HtmlChartPluginLoadException
	 */
	public Set<HtmlChartPlugin> loadAll(File directory) throws HtmlChartPluginLoadException
	{
		if (!directory.isDirectory())
			throw new IllegalArgumentException("[directory] must be directory");

		Set<HtmlChartPlugin> plugins = new HashSet<>();

		File[] children = directory.listFiles();

		for (File child : children)
		{
			HtmlChartPlugin plugin = loadFile(child);

			if (plugin != null)
				plugins.add(plugin);
		}

		return plugins;
	}

	/**
	 * 设置插件资源{@linkplain HtmlChartPlugin#setResources(List)}。
	 * 
	 * @param plugin     插件
	 * @param pluginFile 用于加载上述插件的ZIP文件、文件夹
	 * @throws HtmlChartPluginLoadException
	 */
	public void inflateResources(HtmlChartPlugin plugin, File pluginFile) throws HtmlChartPluginLoadException
	{
		try
		{
			inflateChartPluginResources(plugin, pluginFile);
		}
		catch(HtmlChartPluginLoadException e)
		{
			throw e;
		}
		catch(Exception e)
		{
			throw new HtmlChartPluginLoadException(e);
		}
	}

	protected HtmlChartPlugin loadFileExt(File file) throws HtmlChartPluginLoadException
	{
		return null;
	}

	/**
	 * 从指定ZIP加载单个{@linkplain HtmlChartPlugin}。
	 * 
	 * @param zip
	 * @return {@code null}表示文件不合法
	 * @throws HtmlChartPluginLoadException
	 */
	protected HtmlChartPlugin loadSingleForZip(File zip) throws HtmlChartPluginLoadException
	{
		HtmlChartPlugin plugin = createHtmlChartPlugin();

		ZipInputStream in = null;

		try
		{
			in = IOUtil.getZipInputStream(zip);
			plugin = loadSingleForZipInputStream(in);
		}
		catch (HtmlChartPluginLoadException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			throw new HtmlChartPluginLoadException(e);
		}
		finally
		{
			IOUtil.close(in);
		}

		if (plugin != null)
		{
			try
			{
				inflateChartPluginResources(plugin, zip);
			}
			catch (HtmlChartPluginLoadException e)
			{
				throw e;
			}
			catch (Exception e)
			{
				throw new HtmlChartPluginLoadException(e);
			}
		}

		return plugin;
	}

	/**
	 * 从指定ZIP输入流加载单个{@linkplain HtmlChartPlugin}。
	 * <p>
	 * 注意：此方法不会初始化{@linkplain HtmlChartPlugin#getResources()}。
	 * </p>
	 * 
	 * @param in
	 * @return {@code null}表示文件不合法
	 * @throws HtmlChartPluginLoadException
	 */
	protected HtmlChartPlugin loadSingleForZipInputStream(ZipInputStream in) throws HtmlChartPluginLoadException
	{
		HtmlChartPlugin plugin = createHtmlChartPlugin();

		JsDefContent jsDefContent = null;

		try
		{
			ZipEntry zipEntry = null;
			while ((zipEntry = in.getNextEntry()) != null)
			{
				String name = zipEntry.getName();

				if (zipEntry.isDirectory())
					;
				else if (name.equals(FILE_NAME_PLUGIN))
				{
					Reader pluginIn = IOUtil.getReader(in, this.encoding);
					jsDefContent = this.htmlChartPluginJsDefResolver.resolve(pluginIn);
					inflateChartPluginProperties(plugin, jsDefContent);
				}
				else if (name.equals(FILE_NAME_RENDERER))
				{
					if (jsDefContent == null || !jsDefContent.hasPluginRenderer())
					{
						Reader rendererIn = IOUtil.getReader(in, this.encoding);
						String rendererCodeValue = IOUtil.readString(rendererIn, false);
						plugin.setRenderer(
								new StringJsChartRenderer(JsChartRenderer.CODE_TYPE_INVOKE, rendererCodeValue));
					}
				}

				in.closeEntry();
			}
		}
		catch (HtmlChartPluginLoadException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			throw new HtmlChartPluginLoadException(e);
		}

		// 设置为加载时间而不取文件上次修改时间，因为文件上次修改时间可能错乱
		plugin.setLastModified(System.currentTimeMillis());

		if (StringUtil.isEmpty(plugin.getId()) || StringUtil.isEmpty(plugin.getNameLabel()))
			plugin = null;

		return plugin;
	}

	/**
	 * 从指定目录加载单个{@linkplain HtmlChartPlugin}。
	 * 
	 * @param directory
	 * @return {@code null}表示文件不合法
	 * @throws HtmlChartPluginLoadException
	 */
	protected HtmlChartPlugin loadSingleForDirectory(File directory) throws HtmlChartPluginLoadException
	{
		File pluginFile = FileUtil.getFile(directory, FILE_NAME_PLUGIN);

		if (!pluginFile.exists())
			return null;

		HtmlChartPlugin plugin = createHtmlChartPlugin();

		Reader pluginIn = null;
		Reader rendererIn = null;

		try
		{
			pluginIn = IOUtil.getReader(pluginFile, this.encoding);
			JsDefContent jsDefContent = this.htmlChartPluginJsDefResolver.resolve(pluginIn);
			inflateChartPluginProperties(plugin, jsDefContent);
			
			if (!jsDefContent.hasPluginRenderer())
			{
				File rendererFile = FileUtil.getFile(directory, FILE_NAME_RENDERER);
				if (rendererFile.exists())
				{
					rendererIn = IOUtil.getReader(rendererFile, this.encoding);
					String rendererCodeValue = IOUtil.readString(rendererIn, false);
					plugin.setRenderer(new StringJsChartRenderer(JsChartRenderer.CODE_TYPE_INVOKE, rendererCodeValue));
				}
			}

			inflateChartPluginResources(plugin, directory);
		}
		catch (HtmlChartPluginLoadException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			throw new HtmlChartPluginLoadException(e);
		}
		finally
		{
			IOUtil.close(pluginIn);
			IOUtil.close(rendererIn);
		}

		// 设置为加载时间而不取文件上次修改时间，因为文件上次修改时间可能错乱
		plugin.setLastModified(System.currentTimeMillis());

		if (StringUtil.isEmpty(plugin.getId()) || StringUtil.isEmpty(plugin.getNameLabel()))
			plugin = null;

		return plugin;
	}

	protected void inflateChartPluginProperties(HtmlChartPlugin plugin, JsDefContent jsDefContent) throws Exception
	{
		if (!StringUtil.isEmpty(jsDefContent.getPluginJson()))
		{
			this.jsonChartPluginPropertiesResolver.resolveChartPluginProperties(plugin, jsDefContent.getPluginJson());

			// 内联渲染器格式
			if (jsDefContent.hasPluginRenderer())
			{
				String rendererCodeValue = jsDefContent.getPluginRenderer();
				plugin.setRenderer(new StringJsChartRenderer(JsChartRenderer.CODE_TYPE_OBJECT, rendererCodeValue));
			}
		}
	}

	protected void inflateChartPluginResources(HtmlChartPlugin plugin, File pluginFile) throws Exception
	{
		List<ChartPluginResource> resources = Collections.emptyList();

		if (pluginFile.isDirectory())
			resources = resolveChartPluginResourcesForDirectory(pluginFile);
		else
			resources = resolveChartPluginResourcesForZip(pluginFile);

		plugin.setResources(resources);
	}

	protected List<ChartPluginResource> resolveChartPluginResourcesForDirectory(File pluginDirectory) throws Exception
	{
		List<ChartPluginResource> resources = new ArrayList<ChartPluginResource>();
		inflateChartPluginResourcesForDirectory(resources, pluginDirectory, pluginDirectory);

		return (resources.isEmpty() ? Collections.emptyList() : resources);
	}

	protected void inflateChartPluginResourcesForDirectory(List<ChartPluginResource> resources, File pluginDirectory,
			File currentDirectory) throws Exception
	{
		File[] children = currentDirectory.listFiles();

		for (File child : children)
		{
			if (child.isDirectory())
			{
				inflateChartPluginResourcesForDirectory(resources, pluginDirectory, child);
			}
			else
			{
				String relativePath = FileUtil.getRelativePath(pluginDirectory, child);

				ChartPluginResource resource = new FileChartPluginResource(toChartPluginResourceName(relativePath),
						child);
				resources.add(resource);
			}
		}
	}

	protected List<ChartPluginResource> resolveChartPluginResourcesForZip(File pluginFileZip) throws Exception
	{
		List<ChartPluginResource> resources = new ArrayList<ChartPluginResource>();

		ZipInputStream in = null;

		try
		{
			in = IOUtil.getZipInputStream(pluginFileZip);

			ZipEntry zipEntry = null;
			while ((zipEntry = in.getNextEntry()) != null)
			{
				if (!zipEntry.isDirectory())
				{
					String name = zipEntry.getName();
					ChartPluginResource resource = new ZipEntryChartPluginResource(toChartPluginResourceName(name),
							pluginFileZip, name);
					resources.add(resource);
				}

				in.closeEntry();
			}
		}
		finally
		{
			IOUtil.close(in);
		}

		return (resources.isEmpty() ? Collections.emptyList() : resources);
	}

	/**
	 * 转换为{@linkplain ChartPluginResource}名称。
	 * 
	 * @param name
	 * @return
	 */
	public String toChartPluginResourceName(String name)
	{
		// 统一分隔符以兼容各操作系统
		return FileUtil.trimPath(name, FileUtil.PATH_SEPARATOR_SLASH);
	}

	/**
	 * 从输入流读取字符串。
	 * 
	 * @param in
	 * @param close
	 * @return
	 * @throws IOException
	 */
	protected String readScriptContent(InputStream in, boolean close) throws IOException
	{
		return IOUtil.readString(in, this.encoding, close);
	}

	protected boolean isZipFile(File file)
	{
		return FileUtil.isExtension(file, "zip");
	}

	protected HtmlChartPlugin createHtmlChartPlugin()
	{
		return new HtmlChartPlugin();
	}
}
