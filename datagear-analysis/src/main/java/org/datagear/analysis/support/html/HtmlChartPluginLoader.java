/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support.html;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.datagear.analysis.Icon;
import org.datagear.analysis.RenderStyle;
import org.datagear.analysis.support.BytesIcon;
import org.datagear.analysis.support.JsonChartPluginPropertiesResolver;
import org.datagear.analysis.support.LocationIcon;
import org.datagear.util.FileUtil;
import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;

/**
 * {@linkplain HtmlChartPlugin}加载器。
 * <p>
 * 此类从固定格式的文件夹或者ZIP文件中加载{@linkplain HtmlChartPlugin}。
 * </p>
 * <p>
 * 它支持的文件结构规范如下：
 * </p>
 * <code>
 * <pre>
 * |---- properties.json
 * |---- chart.js
 * </pre>
 * </code>
 * <p>
 * <code>properties.json</code>用于定义{@linkplain HtmlChartPlugin}本身的属性。
 * </p>
 * <p>
 * 如果<code>properties.json</code>中定义了插件图标，比如：
 * </p>
 * <p>
 * <code>icons : { "LIGHT" : "icons/light.png" }</code>
 * </p>
 * <p>
 * ，那么上述文件结构中还应有<code>icons/light.png</code>文件。
 * </p>
 * <code>chart.js</code>用于定义{@linkplain HtmlChartPlugin}的图表渲染逻辑。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class HtmlChartPluginLoader
{
	public static final String NAME_PROPERTIES = "properties.json";

	public static final String NAME_CHART = "chart.js";

	private JsonChartPluginPropertiesResolver jsonChartPluginPropertiesResolver = new JsonChartPluginPropertiesResolver();

	/** 文件编码 */
	private String encoding = "UTF-8";

	public HtmlChartPluginLoader()
	{
		super();
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
	 * 从指定目录加载单个{@linkplain HtmlChartPlugin}，如果目录结构不合法，将返回{@code null}。
	 * 
	 * @param directory
	 * @return
	 * @throws HtmlChartPluginLoadException
	 */
	public HtmlChartPlugin<?> load(File directory) throws HtmlChartPluginLoadException
	{
		return loadSingleForDirectory(directory);
	}

	/**
	 * 从指定ZIP文件加载单个{@linkplain HtmlChartPlugin}，如果ZIP文件结构不合法，将返回{@code null}。
	 * 
	 * @param zip
	 * @return
	 * @throws HtmlChartPluginLoadException
	 */
	public HtmlChartPlugin<?> loadZip(File zip) throws HtmlChartPluginLoadException
	{
		return loadSingleForZip(zip);
	}

	/**
	 * 从指定ZIP输入流加载单个{@linkplain HtmlChartPlugin}，如果ZIP文件结构不合法，将返回{@code null}。
	 * 
	 * @param in
	 * @return
	 * @throws HtmlChartPluginLoadException
	 */
	public HtmlChartPlugin<?> loadZip(ZipInputStream in) throws HtmlChartPluginLoadException
	{
		return loadSingleForZip(in);
	}

	/**
	 * 从指定文件加载单个{@linkplain HtmlChartPlugin}，如果文件结构不合法，将返回{@code null}。
	 * 
	 * @param file
	 * @return
	 * @throws HtmlChartPluginLoadException
	 */
	public HtmlChartPlugin<?> loadFile(File file) throws HtmlChartPluginLoadException
	{
		HtmlChartPlugin<?> plugin = null;

		if (file.isDirectory())
			plugin = loadSingleForDirectory(file);
		else if (file.getName().toLowerCase().endsWith(".zip"))
			plugin = loadSingleForZip(file);
		else
			plugin = loadFileExt(file);

		return plugin;
	}

	/**
	 * 从指定目录加载多个{@linkplain HtmlChartPlugin}，没有，则返回空集合。
	 * <p>
	 * 目录中的每个子文件夹、ZIP文件将被认为是单个{@linkplain HtmlChartPlugin}进行加载。
	 * </p>
	 * 
	 * @param directory
	 * @return
	 * @throws HtmlChartPluginLoadException
	 */
	public Set<HtmlChartPlugin<?>> loads(File directory) throws HtmlChartPluginLoadException
	{
		Set<HtmlChartPlugin<?>> plugins = new HashSet<HtmlChartPlugin<?>>();

		File[] children = directory.listFiles();

		for (File child : children)
		{
			HtmlChartPlugin<?> plugin = loadFile(child);

			if (plugin != null)
				plugins.add(plugin);
		}

		return plugins;
	}

	protected HtmlChartPlugin<?> loadFileExt(File file) throws HtmlChartPluginLoadException
	{
		return null;
	}

	protected HtmlChartPlugin<?> loadSingleForDirectory(File directory) throws HtmlChartPluginLoadException
	{
		File propFile = new File(directory, NAME_PROPERTIES);
		File chartFile = new File(directory, NAME_CHART);

		if (!propFile.exists() || !chartFile.exists())
			return null;

		HtmlChartPlugin<?> plugin = null;

		InputStream propIn = null;
		InputStream chartIn = null;

		try
		{
			propIn = IOUtil.getInputStream(propFile);
			chartIn = IOUtil.getInputStream(chartFile);

			Map<String, Object> properties = this.jsonChartPluginPropertiesResolver.resolveChartPluginProperties(propIn,
					this.encoding);
			String scriptContent = readScriptContent(chartIn, false);

			plugin = createHtmlChartPlugin();

			this.jsonChartPluginPropertiesResolver.setChartPluginProperties(plugin, properties);
			plugin.setScriptContent(new StringScriptContent(scriptContent));

			plugin.setIcons(toBytesIconsInDirectory(directory, plugin.getIcons()));
		}
		catch (Exception e)
		{
			throw new HtmlChartPluginLoadException(e);
		}
		finally
		{
			IOUtil.close(propIn);
			IOUtil.close(chartIn);
		}

		return plugin;
	}

	protected Map<RenderStyle, Icon> toBytesIconsInDirectory(File directory, Map<RenderStyle, Icon> icons)
			throws IOException
	{
		if (icons == null || icons.isEmpty())
			return icons;

		Map<RenderStyle, Icon> bytesIcons = new HashMap<RenderStyle, Icon>();

		for (Map.Entry<RenderStyle, Icon> entry : icons.entrySet())
		{
			Icon icon = entry.getValue();

			BytesIcon bytesIcon = toBytesIconInDirectory(directory, icon);

			if (bytesIcon != null)
				bytesIcons.put(entry.getKey(), bytesIcon);
		}

		return bytesIcons;
	}

	protected BytesIcon toBytesIconInDirectory(File directory, Icon icon) throws IOException
	{
		if (icon instanceof LocationIcon)
		{
			String subPath = ((LocationIcon) icon).getLocation();

			if (StringUtil.isEmpty(subPath))
				return null;

			File iconFile = FileUtil.getFile(directory, subPath);

			return readBytesIcon(iconFile);
		}
		else
			return null;
	}

	protected HtmlChartPlugin<?> loadSingleForZip(File zip) throws HtmlChartPluginLoadException
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
			return loadSingleForZip(in);
		}
		finally
		{
			IOUtil.close(in);
		}
	}

	protected HtmlChartPlugin<?> loadSingleForZip(ZipInputStream in) throws HtmlChartPluginLoadException
	{
		ZipEntry zipEntry = null;

		HtmlChartPlugin<?> plugin = null;
		Map<String, Object> properties = null;
		String scriptContent = null;

		Map<String, File> resourceFiles = new HashMap<String, File>();

		try
		{
			while ((zipEntry = in.getNextEntry()) != null)
			{
				String name = zipEntry.getName();

				if (zipEntry.isDirectory())
					;
				else if (name.equals(NAME_PROPERTIES))
				{
					properties = this.jsonChartPluginPropertiesResolver.resolveChartPluginProperties(in, this.encoding);
				}
				else if (name.equals(NAME_CHART))
				{
					scriptContent = readScriptContent(in, false);
				}
				else
				{
					File tmpFile = File.createTempFile(HtmlChartPluginLoader.class.getSimpleName(),
							"." + FileUtil.getExtension(name));
					IOUtil.write(in, tmpFile);
					resourceFiles.put(name, tmpFile);
				}

				in.closeEntry();
			}

			if (properties != null && !StringUtil.isEmpty(scriptContent))
			{
				plugin = createHtmlChartPlugin();
				this.jsonChartPluginPropertiesResolver.setChartPluginProperties(plugin, properties);
				plugin.setScriptContent(new StringScriptContent(scriptContent));
				plugin.setIcons(toBytesIconsForFileMap(resourceFiles, plugin.getIcons()));
			}

			// 清除临时文件
			if (!resourceFiles.isEmpty())
			{
				Collection<File> tmpFiles = resourceFiles.values();
				for (File tmpFile : tmpFiles)
					FileUtil.deleteFile(tmpFile);
			}
		}
		catch (IOException e)
		{
			throw new HtmlChartPluginLoadException(e);
		}

		return plugin;
	}

	protected Map<RenderStyle, Icon> toBytesIconsForFileMap(Map<String, File> fileMap, Map<RenderStyle, Icon> icons)
			throws IOException
	{
		if (icons == null || icons.isEmpty())
			return icons;

		Map<RenderStyle, Icon> bytesIcons = new HashMap<RenderStyle, Icon>();

		for (Map.Entry<RenderStyle, Icon> entry : icons.entrySet())
		{
			Icon icon = entry.getValue();

			BytesIcon bytesIcon = toBytesIconForFileMap(fileMap, icon);

			if (bytesIcon != null)
				bytesIcons.put(entry.getKey(), bytesIcon);
		}

		return bytesIcons;
	}

	protected BytesIcon toBytesIconForFileMap(Map<String, File> fileMap, Icon icon) throws IOException
	{
		if (icon instanceof LocationIcon)
		{
			String iconPath = ((LocationIcon) icon).getLocation();

			if (StringUtil.isEmpty(iconPath))
				return null;

			File iconFile = fileMap.get(iconPath);

			if (iconFile == null)
			{
				iconPath = FileUtil.deletePathSeparatorHead(FileUtil.trimPath(iconPath));

				for (Map.Entry<String, File> entry : fileMap.entrySet())
				{
					String name = FileUtil.deletePathSeparatorHead(FileUtil.trimPath(entry.getKey()));

					if (iconPath.equals(name))
					{
						iconFile = entry.getValue();
						break;
					}
				}
			}

			return readBytesIcon(iconFile);
		}
		else
			return null;
	}

	/**
	 * 从文件读取{@linkplain BytesIcon}，文件不存在则返回{@code null}。
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	protected BytesIcon readBytesIcon(File file) throws IOException
	{
		if (file == null || !file.exists())
			return null;

		String type = FileUtil.getExtension(file);
		if (type == null)
			type = "";

		InputStream in = IOUtil.getInputStream(file);
		byte[] bytes = IOUtil.readBytes(in, true);

		return BytesIcon.valueOf(type, bytes, file.lastModified());
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

	protected HtmlChartPlugin<?> createHtmlChartPlugin()
	{
		return new HtmlChartPlugin<HtmlRenderContext>();
	}
}
