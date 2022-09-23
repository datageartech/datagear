/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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

	/** 临时文件目录，用于存放临时文件 */
	private File tmpDirectory = null;

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

	public File getTmpDirectory()
	{
		return tmpDirectory;
	}

	public void setTmpDirectory(File tmpDirectory)
	{
		this.tmpDirectory = tmpDirectory;
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
	 * 从指定目录加载单个{@linkplain HtmlChartPlugin}，如果目录结构不合法，将返回{@code null}。
	 * 
	 * @param directory
	 * @return
	 * @throws HtmlChartPluginLoadException
	 */
	public HtmlChartPlugin load(File directory) throws HtmlChartPluginLoadException
	{
		return loadSingleForDirectory(directory, null);
	}

	/**
	 * 从指定ZIP文件加载单个{@linkplain HtmlChartPlugin}，如果ZIP文件结构不合法，将返回{@code null}。
	 * 
	 * @param zip
	 * @return
	 * @throws HtmlChartPluginLoadException
	 */
	public HtmlChartPlugin loadZip(File zip) throws HtmlChartPluginLoadException
	{
		return loadSingleForZip(zip);
	}

	/**
	 * 从指定文件加载单个{@linkplain HtmlChartPlugin}，如果文件结构不合法，将返回{@code null}。
	 * 
	 * @param file
	 *            插件文件夹、插件ZIP包
	 * @return
	 * @throws HtmlChartPluginLoadException
	 */
	public HtmlChartPlugin loadFile(File file) throws HtmlChartPluginLoadException
	{
		HtmlChartPlugin plugin = null;

		if (file.isDirectory())
			plugin = loadSingleForDirectory(file, null);
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

	protected HtmlChartPlugin loadFileExt(File file) throws HtmlChartPluginLoadException
	{
		return null;
	}

	protected HtmlChartPlugin loadSingleForZip(File zip) throws HtmlChartPluginLoadException
	{
		ZipInputStream in = null;

		try
		{
			in = IOUtil.getZipInputStream(zip);
		}
		catch (Exception e)
		{
			IOUtil.close(in);
			throw new HtmlChartPluginLoadException(e);
		}

		try
		{
			File tmpDirectory = createTmpWorkDirectory();
			IOUtil.unzip(in, tmpDirectory);
			HtmlChartPlugin chartPlugin = loadSingleForDirectory(tmpDirectory, zip);
			FileUtil.deleteFile(tmpDirectory);

			return chartPlugin;
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
	 * 从指定目录加载单个{@linkplain HtmlChartPlugin}，返回{@code null}表示文件不合法。
	 * 
	 * @param directory
	 * @param pluginZip
	 *            当{@code directory}是由ZIP包解压而得时的原始ZIP包，否则为{@code null}
	 * @return
	 * @throws HtmlChartPluginLoadException
	 */
	protected HtmlChartPlugin loadSingleForDirectory(File directory, File pluginZip) throws HtmlChartPluginLoadException
	{
		File pluginFile = FileUtil.getFile(directory, FILE_NAME_PLUGIN);

		if (!pluginFile.exists())
			return null;

		HtmlChartPlugin plugin = null;

		Reader pluginIn = null;
		Reader rendererIn = null;

		try
		{
			pluginIn = IOUtil.getReader(pluginFile, this.encoding);
			JsDefContent jsDefContent = this.htmlChartPluginJsDefResolver.resolve(pluginIn);
			
			if (!StringUtil.isEmpty(jsDefContent.getPluginJson()))
			{
				String rendererCodeType = "";
				String rendererCodeValue = "";
				
				if(jsDefContent.hasPluginRenderer())
				{
					rendererCodeType = JsChartRenderer.CODE_TYPE_OBJECT;
					rendererCodeValue = jsDefContent.getPluginRenderer();
				}
				else
				{
					File rendererFile = FileUtil.getFile(directory, FILE_NAME_RENDERER);
					if(rendererFile.exists())
					{
						rendererCodeType = JsChartRenderer.CODE_TYPE_INVOKE;

						rendererIn = IOUtil.getReader(rendererFile, this.encoding);
						rendererCodeValue = IOUtil.readString(rendererIn, false);
					}
				}
				
				if (!StringUtil.isEmpty(rendererCodeType) && !StringUtil.isEmpty(rendererCodeValue))
				{
					plugin = createHtmlChartPlugin();
	
					this.jsonChartPluginPropertiesResolver.resolveChartPluginProperties(plugin,
							jsDefContent.getPluginJson());
					plugin.setRenderer(new StringJsChartRenderer(rendererCodeType, rendererCodeValue));
					inflateChartPluginResources(plugin, (pluginZip == null ? directory : pluginZip));
	
					if (StringUtil.isEmpty(plugin.getId()) || StringUtil.isEmpty(plugin.getNameLabel()))
						plugin = null;
				}
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
		finally
		{
			IOUtil.close(pluginIn);
			IOUtil.close(rendererIn);
		}

		// 设置为加载时间而不取文件上次修改时间，因为文件上次修改时间可能错乱
		if (plugin != null)
			plugin.setLastModified(System.currentTimeMillis());

		return plugin;
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

	protected File createTmpWorkDirectory() throws IOException
	{
		if (this.tmpDirectory != null)
			return FileUtil.generateUniqueDirectory(this.tmpDirectory);
		else
			return FileUtil.createTempDirectory();
	}
}
