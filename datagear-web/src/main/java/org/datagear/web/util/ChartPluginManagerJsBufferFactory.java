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

package org.datagear.web.util;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.datagear.analysis.ChartPluginManager;
import org.datagear.analysis.support.html.HtmlChartPlugin;
import org.datagear.analysis.support.html.HtmlChartPluginScriptObjectWriter;
import org.datagear.util.IDUtil;
import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * 看板展示页的{@code chartPluginManager.js}缓冲区工厂。
 * <p>
 * 此类将{@linkplain #getChartPluginManager()}中的所有{@linkplain HtmlChartPlugin}按照预设块大小拆分构建为{@linkplain ChartPluginManagerJsBuffer}，
 * 使得后续看板展示页可以分块引入，避免单个文件过大加载缓慢。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ChartPluginManagerJsBufferFactory
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ChartPluginManagerJsBufferFactory.class);

	/**
	 * 默认块大小，大约{@code 500k}。
	 */
	public static final int DEFAULT_BLOCK_SIZE = 500 * 1024;

	private ChartPluginManager chartPluginManager;

	private HtmlChartPluginScriptObjectWriter htmlChartPluginScriptObjectWriter = new HtmlChartPluginScriptObjectWriter();

	/** 块字符数 */
	private int blockCharSize = DEFAULT_BLOCK_SIZE;

	private ConcurrentMap<Key, ChartPluginManagerJsBuffer> _latestJsBuffers = new ConcurrentHashMap<>();

	private Cache<String, ChartPluginManagerJsBuffer> _cache;

	public ChartPluginManagerJsBufferFactory()
	{
		this(null);
	}

	public ChartPluginManagerJsBufferFactory(ChartPluginManager chartPluginManager)
	{
		super();
		this.chartPluginManager = chartPluginManager;

		this._cache = Caffeine.newBuilder().maximumSize(100)
				.expireAfterAccess(60, TimeUnit.SECONDS).build();
	}

	public ChartPluginManager getChartPluginManager()
	{
		return chartPluginManager;
	}

	public void setChartPluginManager(ChartPluginManager chartPluginManager)
	{
		this.chartPluginManager = chartPluginManager;
	}

	public HtmlChartPluginScriptObjectWriter getHtmlChartPluginScriptObjectWriter()
	{
		return htmlChartPluginScriptObjectWriter;
	}

	public void setHtmlChartPluginScriptObjectWriter(
			HtmlChartPluginScriptObjectWriter htmlChartPluginScriptObjectWriter)
	{
		this.htmlChartPluginScriptObjectWriter = htmlChartPluginScriptObjectWriter;
	}

	public int getBlockCharSize()
	{
		return blockCharSize;
	}

	public void setBlockCharSize(int blockCharSize)
	{
		this.blockCharSize = blockCharSize;
	}

	/**
	 * 获取最新{@linkplain ChartPluginManagerJsBuffer}。
	 * 
	 * @param locale
	 * @param apiVersion
	 *            允许{@code null}
	 * @return
	 */
	public ChartPluginManagerJsBuffer latest(Locale locale, String apiVersion)
	{
		List<HtmlChartPlugin> plugins = chartPluginManager.getAll(HtmlChartPlugin.class);
		List<HtmlChartPlugin> htmlChartPlugins = new ArrayList<>(plugins.size());
		boolean apiVersionEmpty = StringUtil.isEmpty(apiVersion);
		long lastModified = -1;

		if (plugins != null)
		{
			for (HtmlChartPlugin plugin : plugins)
			{
				if (!apiVersionEmpty && !apiVersion.equals(plugin.getApiVersion()))
					continue;

				htmlChartPlugins.add(plugin);
				lastModified = Math.max(lastModified, plugin.getLastModified());
			}
		}
		
		// TOTO 添加 htmlChartPluginForGetWidgetException

		Key key = new Key(locale, apiVersion);
		ChartPluginManagerJsBuffer latest = this._latestJsBuffers.get(key);

		if (latest != null && latest.getLastModified() == lastModified)
			return latest;

		String id = IDUtil.randomIdOnTime20();
		List<CharBuffer> buffers = Collections.unmodifiableList(jsBuffersOf(htmlChartPlugins, locale));
		ChartPluginManagerJsBuffer jsBuffer = new ChartPluginManagerJsBuffer(id, buffers, lastModified);

		this._latestJsBuffers.putIfAbsent(key, jsBuffer);
		this._cache.put(id, jsBuffer);

		return jsBuffer;
	}

	/**
	 * 获取指定ID的{@linkplain ChartPluginManagerJsBuffer}。
	 * 
	 * @param id
	 * @return {@code null}表示没有
	 */
	public ChartPluginManagerJsBuffer getById(String id)
	{
		Collection<ChartPluginManagerJsBuffer> latests = this._latestJsBuffers.values();

		for (ChartPluginManagerJsBuffer jb : latests)
		{
			if (jb.getId().equals(id))
				return jb;
		}

		return this._cache.getIfPresent(id);
	}

	protected List<CharBuffer> jsBuffersOf(List<HtmlChartPlugin> plugins, Locale locale)
	{
		List<CharBuffer> cbs = new ArrayList<>();

		String newLine = this.htmlChartPluginScriptObjectWriter.getNewLine();
		String chartPluginManagerVar = "CPM";
		String tail = "})(this);" + newLine;
		int tolerantCount = 100;
		CharBuffer buffer = CharBuffer.allocate(this.blockCharSize);
		int pluginIdx = 0;

		try
		{
			writeChartPluginJsStart(buffer, newLine);

			for (HtmlChartPlugin plugin : plugins)
			{
				String pluginJs = jsStringOf(plugin, chartPluginManagerVar, "plugin" + pluginIdx, locale);
				
				boolean hasSpace = (buffer.remaining() >= (pluginJs.length() + tail.length() + tolerantCount));
				
				if (!hasSpace)
				{
					if (pluginIdx == 0)
					{
						// 插件尺寸太大，超过缓冲容量，只能忽略
						if (LOGGER.isWarnEnabled())
							LOGGER.warn("HtmlChartPlugin with id [" + plugin.getId() + "] is too big, ignored!");

						continue;
					}
					else
					{
						buffer.put(tail);
						buffer.flip();
						cbs.add(buffer);

						buffer = CharBuffer.allocate(this.blockCharSize);
						pluginIdx = 0;
						writeChartPluginJsStart(buffer, newLine);
					}
				}
				else
				{
					buffer.put(pluginJs);
					pluginIdx++;
				}
			}

			buffer.put(tail);
			buffer.flip();
			cbs.add(buffer);
		}
		catch (IOException e)
		{
			LOGGER.error("Generate [chartPluginManager-*.js] buffer error", e);
		}

		return cbs;
	}

	protected void writeChartPluginJsStart(CharBuffer buffer, String newLine)
	{
		buffer.put("(function(global) {");
		buffer.put(newLine);

		buffer.put("var CF = (global.chartFactory || (global.chartFactory = {}));");
		buffer.put(newLine);
		buffer.put("var CPM = (CF.chartPluginManager || (CF.chartPluginManager = {}));");
		buffer.put(newLine);
		buffer.put("CPM.plugins = (CPM.plugins || {});");

		// @deprecated 兼容1.8.1版本的window.chartPluginManager变量名，未来版本会移除
		buffer.put("global.chartPluginManager = CPM;");
		buffer.put(newLine);

		buffer.put("if(CPM.get == null){ CPM.get = function(id){ return this.plugins[id]; }; }");
		buffer.put(newLine);
	}

	@SuppressWarnings("deprecation")
	protected String jsStringOf(HtmlChartPlugin plugin, String chartPluginManagerVar, String pluginVar, Locale locale)
			throws IOException
	{
		StringWriter out = new StringWriter();

		try
		{
			this.htmlChartPluginScriptObjectWriter.write(out, plugin, pluginVar, locale);

			// @deprecated
			// 兼容4.0.0版本的"+HtmlChartPlugin.PROPERTY_RENDERER_OLD+"属性名，未来版本会移除
			out.write(pluginVar + "." + HtmlChartPlugin.PROPERTY_RENDERER_OLD + " = " + pluginVar + "."
					+ HtmlChartPlugin.PROPERTY_RENDERER + ";");
			out.write(this.htmlChartPluginScriptObjectWriter.getNewLine());

			out.write(chartPluginManagerVar + ".plugins[" + StringUtil.toJavaScriptString(plugin.getId()) + "] = "
					+ pluginVar + ";");
			out.write(this.htmlChartPluginScriptObjectWriter.getNewLine());
		}
		finally
		{
			IOUtil.close(out);
		}

		return out.toString();
	}
	
	protected static class Key implements Serializable
	{
		private static final long serialVersionUID = 1L;
		
		private final Locale locale;
		private final String apiVersion;
		
		public Key(Locale locale, String apiVersion)
		{
			super();
			this.locale = locale;
			this.apiVersion = apiVersion;
		}

		public Locale getLocale()
		{
			return locale;
		}

		public String getApiVersion()
		{
			return apiVersion;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((apiVersion == null) ? 0 : apiVersion.hashCode());
			result = prime * result + ((locale == null) ? 0 : locale.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Key other = (Key) obj;
			if (apiVersion == null)
			{
				if (other.apiVersion != null)
					return false;
			}
			else if (!apiVersion.equals(other.apiVersion))
				return false;
			if (locale == null)
			{
				if (other.locale != null)
					return false;
			}
			else if (!locale.equals(other.locale))
				return false;
			return true;
		}
	}

	/**
	 * 看板展示页的{@code chartPluginManager.js}缓冲区，
	 * 其{@linkplain ChartPluginManagerJsBuffer#getBuffers()}中的每个{@linkplain CharBuffer}里都包含一个{@code chartPluginManager.js}。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class ChartPluginManagerJsBuffer
	{
		private final String id;

		private final List<CharBuffer> buffers;

		private final long lastModified;

		public ChartPluginManagerJsBuffer(String id, List<CharBuffer> buffers, long lastModified)
		{
			super();
			this.id = id;
			this.buffers = buffers;
			this.lastModified = lastModified;
		}

		public List<CharBuffer> getBuffers()
		{
			return buffers;
		}

		public String getId()
		{
			return id;
		}

		public long getLastModified()
		{
			return lastModified;
		}

		public int getBufferCount()
		{
			return (this.buffers == null ? 0 : this.buffers.size());
		}

		public CharBuffer getBuffer(int idx)
		{
			if (idx < 0 || idx >= this.getBufferCount())
				return null;

			return this.buffers.get(idx);
		}
	}
}
