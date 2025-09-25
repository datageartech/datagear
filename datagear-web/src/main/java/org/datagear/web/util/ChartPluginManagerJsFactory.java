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
 * 看板展示页的{@code chartPluginManager.js}脚本工厂。
 * <p>
 * 此类将{@linkplain #getChartPluginManager()}中的所有{@linkplain HtmlChartPlugin}按照预设块大小拆分构建为{@linkplain ChartPluginManagerJs}，
 * 使得后续看板展示页可以分块引入，避免单个文件过大加载缓慢。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ChartPluginManagerJsFactory
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ChartPluginManagerJsFactory.class);

	/**
	 * 默认块大小，大约{@code 500k}。
	 */
	public static final int DEFAULT_BLOCK_SIZE = 500 * 1024;

	private ChartPluginManager chartPluginManager;

	private HtmlChartPluginScriptObjectWriter htmlChartPluginScriptObjectWriter = new HtmlChartPluginScriptObjectWriter();

	/** 块字符数 */
	private int blockCharSize = DEFAULT_BLOCK_SIZE;

	private ConcurrentMap<Key, ChartPluginManagerJs> _latestJss = new ConcurrentHashMap<>();

	private Cache<String, ChartPluginManagerJs> _cache;

	public ChartPluginManagerJsFactory()
	{
		this(null);
	}

	public ChartPluginManagerJsFactory(ChartPluginManager chartPluginManager)
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
	 * 获取最新{@linkplain ChartPluginManagerJs}。
	 * 
	 * @param locale
	 * @param apiVersion
	 *            允许{@code null}
	 * @return
	 */
	public ChartPluginManagerJs latest(Locale locale, String apiVersion)
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
		ChartPluginManagerJs latest = this._latestJss.get(key);

		if (latest != null && latest.getLastModified() == lastModified)
			return latest;

		String id = IDUtil.randomIdOnTime20();
		List<String> scripts = Collections.unmodifiableList(scriptsOf(htmlChartPlugins, locale));
		ChartPluginManagerJs managerJs = new ChartPluginManagerJs(id, scripts, lastModified);

		this._latestJss.putIfAbsent(key, managerJs);
		this._cache.put(id, managerJs);

		return managerJs;
	}

	/**
	 * 获取指定ID的{@linkplain ChartPluginManagerJs}。
	 * 
	 * @param id
	 * @return {@code null}表示没有
	 */
	public ChartPluginManagerJs getById(String id)
	{
		Collection<ChartPluginManagerJs> latests = this._latestJss.values();

		for (ChartPluginManagerJs jb : latests)
		{
			if (jb.getId().equals(id))
				return jb;
		}

		return this._cache.getIfPresent(id);
	}

	protected List<String> scriptsOf(List<HtmlChartPlugin> plugins, Locale locale)
	{
		List<String> cbs = new ArrayList<>();

		int blockSize = getBlockCharSize();
		String newLine = this.htmlChartPluginScriptObjectWriter.getNewLine();
		String managerVar = "CPM";
		StringBuilder buffer = new StringBuilder(blockSize);
		int pluginNumber = 1;

		try
		{
			appendManagerJsStart(buffer, managerVar, newLine);

			for (HtmlChartPlugin plugin : plugins)
			{
				appendPluginJs(buffer, plugin, managerVar, "plugin" + pluginNumber, locale);
				
				if (buffer.length() > blockSize)
				{
					appendManagerJsEnd(buffer, newLine);
					cbs.add(buffer.toString());

					buffer = new StringBuilder(blockSize);
					appendManagerJsStart(buffer, managerVar, newLine);
				}

				pluginNumber++;
			}

			appendManagerJsEnd(buffer, newLine);
			cbs.add(buffer.toString());
		}
		catch (IOException e)
		{
			LOGGER.error("Generate [chartPluginManager-*.js] buffer error", e);
		}

		return cbs;
	}

	protected void appendManagerJsStart(StringBuilder buffer, String managerVar, String newLine)
	{
		buffer.append("(function(global) {");
		buffer.append(newLine);

		buffer.append("var CF = (global.chartFactory || (global.chartFactory = {}));");
		buffer.append(newLine);
		buffer.append("var " + managerVar + " = (CF.chartPluginManager || (CF.chartPluginManager = {}));");
		buffer.append(newLine);
		buffer.append(managerVar + ".plugins = (" + managerVar + ".plugins || {});");

		// @deprecated 兼容1.8.1版本的window.chartPluginManager变量名，未来版本会移除
		buffer.append("global.chartPluginManager = " + managerVar + ";");
		buffer.append(newLine);

		buffer.append("if(" + managerVar + ".get == null){" + managerVar
				+ ".get = function(id){ return this.plugins[id]; }; }");
		buffer.append(newLine);
	}

	protected void appendManagerJsEnd(StringBuilder buffer, String newLine)
	{
		buffer.append(newLine);
		buffer.append("})(this);");
	}

	@SuppressWarnings("deprecation")
	protected void appendPluginJs(StringBuilder buffer, HtmlChartPlugin plugin, String managerVar, String pluginVar,
			Locale locale) throws IOException
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

			out.write(managerVar + ".plugins[" + StringUtil.toJavaScriptString(plugin.getId()) + "] = "
					+ pluginVar + ";");
			out.write(this.htmlChartPluginScriptObjectWriter.getNewLine());
		}
		finally
		{
			IOUtil.close(out);
		}

		buffer.append(out.toString());
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
	 * 看板展示页的{@code chartPluginManager.js}脚本，
	 * 其{@linkplain ChartPluginManagerJs#getScripts()}中的每个元素都是一个经拆分后的{@code chartPluginManager.js}脚本。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class ChartPluginManagerJs
	{
		private final String id;

		private final List<String> scripts;

		private final long lastModified;

		public ChartPluginManagerJs(String id, List<String> scripts, long lastModified)
		{
			super();
			this.id = id;
			this.scripts = scripts;
			this.lastModified = lastModified;
		}

		public List<String> getScripts()
		{
			return scripts;
		}

		public String getId()
		{
			return id;
		}

		public long getLastModified()
		{
			return lastModified;
		}

		public int getScriptCount()
		{
			return (this.scripts == null ? 0 : this.scripts.size());
		}

		public String getScript(int idx)
		{
			if (idx < 0 || idx >= this.getScriptCount())
				return null;

			return this.scripts.get(idx);
		}
	}
}
