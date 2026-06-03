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
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
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
 * |---- renderer.js                //可选，当plugin.json里没有定义renderer（或chartRenderer）属性时必须
 * |---- plugin-datasignspec.json   //可选，当希望在单独文件中定义插件的dataSigns时使用，格式应为：[ ... ]
 * |---- plugin-configform.json     //可选，当希望在单独文件中定义插件的configForm时使用，格式应为：{ ... }
 * |---- manual.md                  //可选，使用手册文件，此文件可有可无，不影响插件解析流程，这里主要是定义使用手册文件名规范
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

	/**
	 * 插件数据标记JSON文件名
	 */
	public static final String FILE_NAME_DATASIGNSPEC = "plugin-datasignspec.json";

	/**
	 * 插件配置表单JSON文件名
	 */
	public static final String FILE_NAME_CONFIGFORM = "plugin-configform.json";
	
	/**
	 * 使用手册Markdown文件名。
	 * <p>
	 * 此文件可有可无，不影响插件解析流程，这里主要是定义使用手册文件名规范。
	 * </p>
	 */
	public static final String FILE_NAME_MANUAL = "manual.md";

	private HtmlChartPluginJsDefResolver pluginJsDefResolver = new HtmlChartPluginJsDefResolver();

	private JsonHtmlChartPluginPropertiesResolver<HtmlChartPlugin> jsonPluginPropertiesResolver = new JsonHtmlChartPluginPropertiesResolver<HtmlChartPlugin>();

	private HtmlChartPluginScriptObjectWriter pluginScriptObjectWriter = HtmlChartPluginScriptObjectWriter.INSTANCE;

	private HtmlRenderContextScriptObjectWriter renderContextScriptObjectWriter = HtmlRenderContextScriptObjectWriter.INSTANCE;

	private HtmlChartScriptObjectWriter chartScriptObjectWriter = HtmlChartScriptObjectWriter.INSTANCE;

	private HtmlChartPluginLoadedProcessor loadedProcessor = null;

	/** 文件编码 */
	private String encoding = IOUtil.CHARSET_UTF_8;

	public HtmlChartPluginLoader()
	{
		super();
	}

	public HtmlChartPluginJsDefResolver getPluginJsDefResolver()
	{
		return pluginJsDefResolver;
	}

	public void setPluginJsDefResolver(HtmlChartPluginJsDefResolver pluginJsDefResolver)
	{
		this.pluginJsDefResolver = pluginJsDefResolver;
	}

	public JsonHtmlChartPluginPropertiesResolver<HtmlChartPlugin> getJsonPluginPropertiesResolver()
	{
		return jsonPluginPropertiesResolver;
	}

	public void setJsonPluginPropertiesResolver(
			JsonHtmlChartPluginPropertiesResolver<HtmlChartPlugin> jsonPluginPropertiesResolver)
	{
		this.jsonPluginPropertiesResolver = jsonPluginPropertiesResolver;
	}

	public HtmlChartPluginScriptObjectWriter getPluginScriptObjectWriter()
	{
		return pluginScriptObjectWriter;
	}

	public void setPluginScriptObjectWriter(HtmlChartPluginScriptObjectWriter pluginScriptObjectWriter)
	{
		this.pluginScriptObjectWriter = pluginScriptObjectWriter;
	}

	public HtmlRenderContextScriptObjectWriter getRenderContextScriptObjectWriter()
	{
		return renderContextScriptObjectWriter;
	}

	public void setRenderContextScriptObjectWriter(HtmlRenderContextScriptObjectWriter renderContextScriptObjectWriter)
	{
		this.renderContextScriptObjectWriter = renderContextScriptObjectWriter;
	}

	public HtmlChartScriptObjectWriter getChartScriptObjectWriter()
	{
		return chartScriptObjectWriter;
	}

	public void setChartScriptObjectWriter(HtmlChartScriptObjectWriter chartScriptObjectWriter)
	{
		this.chartScriptObjectWriter = chartScriptObjectWriter;
	}

	public HtmlChartPluginLoadedProcessor getLoadedProcessor()
	{
		return loadedProcessor;
	}

	public void setLoadedProcessor(HtmlChartPluginLoadedProcessor loadedProcessor)
	{
		this.loadedProcessor = loadedProcessor;
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
		HtmlChartPlugin plugin = loadSingleForDirectory(directory);
		processLoadedPlugin(plugin);
		return plugin;
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
		HtmlChartPlugin plugin = loadSingleForZip(zip);
		processLoadedPlugin(plugin);
		return plugin;
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
		try
		{
			HtmlChartPlugin plugin = loadSingleForZipInputStream(in);
			processLoadedPlugin(plugin);
			return plugin;
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

		processLoadedPlugin(plugin);

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
			processLoadedPlugin(plugin);

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
			throw new HtmlChartPluginLoadException(e, zip.getName());
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
				throw new HtmlChartPluginLoadException(e, zip.getName());
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
	 * @throws Exception
	 */
	protected HtmlChartPlugin loadSingleForZipInputStream(ZipInputStream in) throws Exception
	{
		HtmlChartPlugin plugin = createHtmlChartPlugin();

		Reader pluginIn = null;
		Reader dataSignSpecIn = null;
		Reader configFormIn = null;
		Reader rendererIn = null;

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
					Reader reader = IOUtil.getReader(in, this.encoding);
					pluginIn = new StringReader(IOUtil.readString(reader, false));
				}
				else if (name.equals(FILE_NAME_DATASIGNSPEC))
				{
					Reader reader = IOUtil.getReader(in, this.encoding);
					dataSignSpecIn = new StringReader(IOUtil.readString(reader, false));
				}
				else if (name.equals(FILE_NAME_CONFIGFORM))
				{
					Reader reader = IOUtil.getReader(in, this.encoding);
					configFormIn = new StringReader(IOUtil.readString(reader, false));
				}
				else if (name.equals(FILE_NAME_RENDERER))
				{
					Reader reader = IOUtil.getReader(in, this.encoding);
					rendererIn = new StringReader(IOUtil.readString(reader, false));
				}

				in.closeEntry();
			}

			if (pluginIn != null)
				inflateChartPluginProperties(plugin, pluginIn, dataSignSpecIn, configFormIn, rendererIn);
		}
		finally
		{
			IOUtil.close(pluginIn);
			IOUtil.close(dataSignSpecIn);
			IOUtil.close(configFormIn);
			IOUtil.close(rendererIn);
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
		Reader dataSignSpecIn = null;
		Reader configFormIn = null;
		Reader rendererIn = null;

		try
		{
			File dataSignsFile = FileUtil.getFile(directory, FILE_NAME_DATASIGNSPEC);
			File configFormFile = FileUtil.getFile(directory, FILE_NAME_CONFIGFORM);
			File rendererFile = FileUtil.getFile(directory, FILE_NAME_RENDERER);

			pluginIn = IOUtil.getReader(pluginFile, this.encoding);
			dataSignSpecIn = (dataSignsFile.exists() ? IOUtil.getReader(dataSignsFile, this.encoding) : null);
			configFormIn = (configFormFile.exists() ? IOUtil.getReader(configFormFile, this.encoding) : null);
			rendererIn = (rendererFile.exists() ? IOUtil.getReader(rendererFile, this.encoding) : null);

			inflateChartPluginProperties(plugin, pluginIn, dataSignSpecIn, configFormIn, rendererIn);
			inflateChartPluginResources(plugin, directory);
		}
		catch (HtmlChartPluginLoadException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			throw new HtmlChartPluginLoadException(e, directory.getName());
		}
		finally
		{
			IOUtil.close(pluginIn);
			IOUtil.close(dataSignSpecIn);
			IOUtil.close(configFormIn);
			IOUtil.close(rendererIn);
		}

		// 设置为加载时间而不取文件上次修改时间，因为文件上次修改时间可能错乱
		plugin.setLastModified(System.currentTimeMillis());

		if (StringUtil.isEmpty(plugin.getId()) || StringUtil.isEmpty(plugin.getNameLabel()))
			plugin = null;

		return plugin;
	}

	/**
	 * 填充插件属性。
	 * 
	 * @param plugin
	 * @param pluginJsonIn
	 * @param dataSignSpecIn
	 *            允许{@code null}
	 * @param configFormIn
	 *            允许{@code null}
	 * @param rendererIn
	 *            允许{@code null}
	 * @throws Exception
	 */
	protected void inflateChartPluginProperties(HtmlChartPlugin plugin, Reader pluginJsonIn, Reader dataSignSpecIn,
			Reader configFormIn, Reader rendererIn) throws Exception
	{
		JsonHtmlChartPluginPropertiesResolver<HtmlChartPlugin> propertiesResolver = createPluginPropertiesResolver(
				plugin);

		// 渲染器在独立文件中定义
		if (rendererIn != null)
		{
			propertiesResolver.resolveProperties(pluginJsonIn, dataSignSpecIn, configFormIn);
			String rendererCodeValue = IOUtil.readString(rendererIn, false);
			plugin.setRenderer(new StringJsChartRenderer(JsChartRenderer.CODE_TYPE_INVOKE, rendererCodeValue));
		}
		else
		{
			JsDefContent jsDefContent = this.pluginJsDefResolver.resolve(pluginJsonIn);

			if (!StringUtil.isEmpty(jsDefContent.getPluginJson()))
			{
				propertiesResolver.resolveProperties(jsDefContent.getPluginJson());

				// 内联渲染器格式
				if (jsDefContent.hasPluginRenderer())
				{
					String rendererCodeValue = jsDefContent.getPluginRenderer();
					plugin.setRenderer(new StringJsChartRenderer(JsChartRenderer.CODE_TYPE_OBJECT, rendererCodeValue));
				}
			}
		}

		plugin.setPluginWriter(getPluginScriptObjectWriter());
		plugin.setRenderContextWriter(getRenderContextScriptObjectWriter());
		plugin.setChartWriter(getChartScriptObjectWriter());
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

	protected void processLoadedPlugin(HtmlChartPlugin plugin)
	{
		if (plugin == null || this.loadedProcessor == null)
			return;

		this.loadedProcessor.process(plugin);
	}

	protected HtmlChartPlugin createHtmlChartPlugin()
	{
		return new HtmlChartPlugin();
	}

	protected JsonHtmlChartPluginPropertiesResolver<HtmlChartPlugin> createPluginPropertiesResolver(
			HtmlChartPlugin plugin)
	{
		return new JsonHtmlChartPluginPropertiesResolver<HtmlChartPlugin>(plugin);
	}
}
