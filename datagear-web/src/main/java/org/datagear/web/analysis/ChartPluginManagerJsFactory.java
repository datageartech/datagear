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

package org.datagear.web.analysis;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.datagear.analysis.ChartPlugin;
import org.datagear.analysis.ChartPluginConfigForm;
import org.datagear.analysis.ChartPluginManager;
import org.datagear.analysis.DataSign;
import org.datagear.analysis.DataSignSpec;
import org.datagear.analysis.form.FormProperty;
import org.datagear.analysis.form.PropertyType;
import org.datagear.analysis.support.JsonChartPluginPropertiesResolver;
import org.datagear.analysis.support.html.DashboardApiVersion;
import org.datagear.analysis.support.html.ExceptionMsgHtmlChartPlugin;
import org.datagear.analysis.support.html.HtmlChartPlugin;
import org.datagear.analysis.support.html.HtmlChartPluginJsDefResolver;
import org.datagear.analysis.support.html.HtmlChartPluginScriptObjectWriter;
import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;
import org.datagear.util.cache.CacheAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;

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
public class ChartPluginManagerJsFactory implements CacheAware
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ChartPluginManagerJsFactory.class);

	/**
	 * 默认块大小，大约{@code 500k}。
	 */
	public static final int DEFAULT_BLOCK_SIZE = 500 * 1024;

	private ChartPluginManager chartPluginManager;

	private Cache cache;

	private ExceptionMsgHtmlChartPlugin exceptionMsgHtmlChartPlugin = ExceptionMsgHtmlChartPlugin.INSTANCE;

	private HtmlChartPluginScriptObjectWriter htmlChartPluginScriptObjectWriter = HtmlChartPluginScriptObjectWriter.INSTANCE;

	/** 块字符数 */
	private int blockCharSize = DEFAULT_BLOCK_SIZE;

	public ChartPluginManagerJsFactory()
	{
		super();
	}

	public ChartPluginManagerJsFactory(ChartPluginManager chartPluginManager, Cache cache)
	{
		super();
		this.chartPluginManager = chartPluginManager;
		this.cache = cache;
	}

	public ChartPluginManager getChartPluginManager()
	{
		return chartPluginManager;
	}

	public void setChartPluginManager(ChartPluginManager chartPluginManager)
	{
		this.chartPluginManager = chartPluginManager;
	}

	@Override
	public Cache getCache()
	{
		return cache;
	}

	@Override
	public void setCache(Cache cache)
	{
		this.cache = cache;
	}

	public ExceptionMsgHtmlChartPlugin getExceptionMsgHtmlChartPlugin()
	{
		return exceptionMsgHtmlChartPlugin;
	}

	public void setExceptionMsgHtmlChartPlugin(ExceptionMsgHtmlChartPlugin exceptionMsgHtmlChartPlugin)
	{
		this.exceptionMsgHtmlChartPlugin = exceptionMsgHtmlChartPlugin;
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
		List<HtmlChartPlugin> filterPlugins = new ArrayList<>(plugins.size());
		boolean apiVersionEmpty = StringUtil.isEmpty(apiVersion);
		long lastModified = -1;

		if (plugins != null)
		{
			for (HtmlChartPlugin plugin : plugins)
			{
				if (!apiVersionEmpty && !apiVersion.equals(plugin.getApiVersion()))
					continue;

				filterPlugins.add(plugin);
				lastModified = Math.max(lastModified, plugin.getLastModified());
			}
		}
		
		String key = toCacheKey(locale, apiVersion, lastModified);
		ChartPluginManagerJs managerJs = getFromCache(key);

		if (managerJs != null)
			return managerJs;

		// 需要添加此插件，以支持显示后端渲染图表异常信息
		filterPlugins.add(getExceptionMsgHtmlChartPlugin());

		List<String> scripts = Collections.unmodifiableList(scriptsOf(filterPlugins, locale, apiVersion));
		managerJs = new ChartPluginManagerJs(key, scripts, lastModified);
		this.cache.put(key, managerJs);

		return managerJs;
	}

	/**
	 * 获取指定Key的{@linkplain ChartPluginManagerJs}。
	 * 
	 * @param key
	 * @return {@code null}表示没有
	 */
	public ChartPluginManagerJs getByKey(String key)
	{
		return getFromCache(key);
	}

	protected ChartPluginManagerJs getFromCache(String key)
	{
		ValueWrapper vw = this.cache.get(key);
		return (vw == null ? null : (ChartPluginManagerJs) vw.get());
	}

	protected List<String> scriptsOf(List<HtmlChartPlugin> plugins, Locale locale, String apiVersion)
	{
		List<String> cbs = new ArrayList<>();

		int blockSize = getBlockCharSize();
		String newLine = this.htmlChartPluginScriptObjectWriter.getNewLine();
		String managerVar = "CPM";
		StringBuilder buffer = new StringBuilder(blockSize);
		int pluginNumber = 1;

		try
		{
			appendManagerJsStart(apiVersion, buffer, managerVar, newLine);

			for (HtmlChartPlugin plugin : plugins)
			{
				appendPluginJs(apiVersion, buffer, plugin, managerVar, "plugin" + pluginNumber, locale);
				
				if (buffer.length() > blockSize)
				{
					appendManagerJsEnd(buffer, newLine);
					cbs.add(buffer.toString());

					buffer = new StringBuilder(blockSize);
					appendManagerJsStart(apiVersion, buffer, managerVar, newLine);
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

	protected void appendManagerJsStart(String apiVersion, StringBuilder buffer, String managerVar, String newLine)
	{
		buffer.append("(function(global) {");
		buffer.append(newLine);

		buffer.append("var CF = (global.chartFactory || (global.chartFactory = {}));");
		buffer.append(newLine);
		buffer.append("var " + managerVar + " = (CF.chartPluginManager || (CF.chartPluginManager = {}));");
		buffer.append(newLine);
		buffer.append(managerVar + ".plugins = (" + managerVar + ".plugins || {});");
		buffer.append(newLine);

		buffer.append("if(" + managerVar + ".get == null){" + managerVar
				+ ".get = function(id){ return this.plugins[id]; }; }");
		buffer.append(newLine);

		buffer.append("if(" + managerVar + ".getAll == null){" + managerVar
				+ ".getAll = function(){ return this.plugins; }; }");
		buffer.append(newLine);

		if (DashboardApiVersion.isV1(apiVersion))
		{
			// @deprecated
			// 兼容1.8.1版本的window.chartPluginManager变量名
			buffer.append("global.chartPluginManager = " + managerVar + ";");
			buffer.append(newLine);

			appendCompatFuncForV5_5_0(apiVersion, buffer, managerVar, newLine);
			buffer.append(newLine);
		}
	}

	protected void appendManagerJsEnd(StringBuilder buffer, String newLine)
	{
		buffer.append(newLine);
		buffer.append("})(this);");
	}

	protected void appendPluginJs(String apiVersion, StringBuilder buffer, HtmlChartPlugin plugin, String managerVar,
			String pluginVar, Locale locale) throws IOException
	{
		StringWriter out = new StringWriter();
		String newLine = this.htmlChartPluginScriptObjectWriter.getNewLine();

		try
		{
			this.htmlChartPluginScriptObjectWriter.write(out, plugin, pluginVar, locale);

			if (DashboardApiVersion.isV1(apiVersion))
			{
				appendPluginJsForV4_0_0(out, apiVersion, plugin, managerVar, pluginVar, locale);
				out.write(newLine);
				appendPluginJsForV5_5_0(out, apiVersion, plugin, managerVar, pluginVar, locale);
				out.write(newLine);
			}

			out.write(managerVar + ".plugins[" + StringUtil.toJavaScriptString(plugin.getId()) + "] = "
					+ pluginVar + ";");
			out.write(newLine);
		}
		finally
		{
			IOUtil.close(out);
		}

		buffer.append(out.toString());
	}
	
	/**
	 * @param apiVersion
	 * @param buffer
	 * @param managerVar
	 * @param newLine
	 * @deprecated 仅用于兼容5.5.0及以下版本的{@code org.datagear.analysis.ChartPluginAttribute}、
	 *             {@code org.datagear.analysis.ChartPlugin.attributes}、
	 *             {@code ChartPlugin.iconResourceNames}
	 */
	@Deprecated
	protected void appendCompatFuncForV5_5_0(String apiVersion, StringBuilder buffer, String managerVar, String newLine)
	{
		buffer.append(managerVar + ".compatForV5_5_0 = function(plugin){");

		buffer.append(newLine);
		buffer.append("  if(plugin." + ChartPlugin.PROPERTY_DATA_SIGN_SPEC + " != null){");
		buffer.append(newLine);
		buffer.append("    plugin." + JsonChartPluginPropertiesResolver.JSON_PROPERTY_DATA_SIGNS + "=plugin."
				+ ChartPlugin.PROPERTY_DATA_SIGN_SPEC + "." + DataSignSpec.PROPERTY_DATA_SIGNS + ";");
		buffer.append(newLine);
		buffer.append("    plugin." + ChartPlugin.PROPERTY_DATA_SIGN_SPEC + "=undefined;");
		buffer.append(newLine);
		buffer.append("  }");

		buffer.append(newLine);
		buffer.append("  plugin.iconResourceNames=plugin.icons;");
		buffer.append(newLine);

		buffer.append("  if(plugin." + ChartPlugin.PROPERTY_CONFIG_FORM + " != null){");
		buffer.append(newLine);
		buffer.append(
				"    var attributes = (plugin." + JsonChartPluginPropertiesResolver.JSON_PROPERTY_ATTRIBUTES
						+ " = plugin." + ChartPlugin.PROPERTY_CONFIG_FORM + "."
						+ ChartPluginConfigForm.PROPERTY_PROPERTIES + ");");
		buffer.append(newLine);
		buffer.append("    plugin." + ChartPlugin.PROPERTY_CONFIG_FORM + "=undefined;");
		buffer.append(newLine);
		buffer.append("    var attributesLen = (attributes == null ? 0 : attributes.length);");
		buffer.append(newLine);
		buffer.append("    for(var i=0; i<attributesLen; i++){");
		buffer.append(newLine);
		buffer.append("      var attr = attributes[i];");
		buffer.append(newLine);
		buffer.append("      if(attr." + FormProperty.PROPERTY_ADDITIONS + "){ " //
				+ "attr." + JsonChartPluginPropertiesResolver.JSON_PROPERTY_INPUT_ATTR_GROUP //
				+ " = attr." + FormProperty.PROPERTY_ADDITIONS + "[\""
				+ JsonChartPluginPropertiesResolver.INPUT_PROPERTY_ADDITION_OLD_GROUP + "\"]; }");
		buffer.append(newLine);
		buffer.append("      if(attr." + FormProperty.PROPERTY_TYPE + " == \"" + PropertyType.STRING + "\"){ " //
				+ "attr." + FormProperty.PROPERTY_TYPE + " = \"" + PropertyTypeV5_5_0.STRING + "\"; }");
		buffer.append(newLine);
		buffer.append("      else if(attr." + FormProperty.PROPERTY_TYPE + " == \"" + PropertyType.BOOLEAN + "\"){ " //
				+ "attr." + FormProperty.PROPERTY_TYPE + " = \"" + PropertyTypeV5_5_0.BOOLEAN + "\"; }");
		buffer.append(newLine);
		buffer.append("      else if(attr." + FormProperty.PROPERTY_TYPE + " == \"" + PropertyType.INTEGER + "\"){ " //
				+ "attr." + FormProperty.PROPERTY_TYPE + " = \"" + PropertyTypeV5_5_0.INTEGER + "\"; }");
		buffer.append(newLine);
		buffer.append("      else if(attr." + FormProperty.PROPERTY_TYPE + " == \"" + PropertyType.NUMBER + "\"){ " //
				+ "attr." + FormProperty.PROPERTY_TYPE + " = \"" + PropertyTypeV5_5_0.NUMBER + "\"; }");
		buffer.append(newLine);
		buffer.append("      else if(attr." + FormProperty.PROPERTY_TYPE + " == \"" + PropertyType.OBJECT + "\"){ " //
				+ "attr." + FormProperty.PROPERTY_TYPE + " = \"" + PropertyTypeV5_5_0.OBJECT + "\"; }");
		buffer.append(newLine);
		buffer.append("    }");
		buffer.append(newLine);
		buffer.append("  }");

		buffer.append(newLine);
		buffer.append("  " + managerVar + ".compatDataSignForV5_5_0(plugin."
				+ JsonChartPluginPropertiesResolver.JSON_PROPERTY_DATA_SIGNS + ");");
		buffer.append(newLine);

		buffer.append("};");

		buffer.append(newLine);
		buffer.append(managerVar + ".compatDataSignForV5_5_0 = function(dataSigns){");
		buffer.append(newLine);
		buffer.append("  if(dataSigns == null || dataSigns.length == 0) return;");
		buffer.append(newLine);
		buffer.append("  for(var i=0; i<dataSigns.length; i++){");
		buffer.append(newLine);
		buffer.append("    var targets = (dataSigns[i].targets == null ? [] : dataSigns[i].targets);");
		buffer.append(newLine);
		buffer.append("    for(var j=0; j<targets.length; j++){");
		buffer.append(newLine);
		buffer.append("      if(targets[j] == \"" + DataSign.TARGET_FIELD + "\"){ " //
				+ "targets[j] = \"" + DataSignTargetV5_5_0.TARGET_FIELD + "\"; }");
		buffer.append(newLine);
		buffer.append("      else if(targets[j] == \"" + DataSign.TARGET_DATASET + "\"){ " //
				+ "targets[j] = \"" + DataSignTargetV5_5_0.TARGET_DATASET + "\"; }");
		buffer.append(newLine);
		buffer.append("    }");
		buffer.append(newLine);
		buffer.append(
				"    " + managerVar + ".compatDataSignForV5_5_0(dataSigns[i]." + DataSign.PROPERTY_CHILDREN + ");");
		buffer.append(newLine);
		buffer.append("  }");
		buffer.append(newLine);
		buffer.append("};");
	}

	/**
	 * @param out
	 * @param apiVersion
	 * @param plugin
	 * @param managerVar
	 * @param pluginVar
	 * @param locale
	 * @throws IOException
	 * @deprecated 仅用于兼容5.5.0及以下版本的{@code org.datagear.analysis.ChartPlugin.attributes}
	 */
	@Deprecated
	protected void appendPluginJsForV5_5_0(Writer out, String apiVersion, HtmlChartPlugin plugin,
			String managerVar, String pluginVar, Locale locale) throws IOException
	{
		out.write(managerVar + ".compatForV5_5_0(" + pluginVar + ");");
	}

	/**
	 * 
	 * @param out
	 * @param apiVersion
	 * @param plugin
	 * @param managerVar
	 * @param pluginVar
	 * @param locale
	 * @throws IOException
	 * @deprecated 仅用于兼容4.0.0版本的{@code org.datagear.analysis.support.html.HtmlChartPlugin.chartRenderer}。
	 */
	@Deprecated
	protected void appendPluginJsForV4_0_0(Writer out, String apiVersion, HtmlChartPlugin plugin, String managerVar,
			String pluginVar, Locale locale) throws IOException
	{
		out.write(pluginVar + "." + HtmlChartPluginJsDefResolver.PLUGIN_PROPERTY_RENDERER_OLD + " = " + pluginVar + "."
				+ HtmlChartPlugin.PROPERTY_RENDERER + ";");
	}

	/**
	 * 获取缓存关键字。
	 * <p>
	 * 注意：返回的字符串不应包含特殊字符，因为会在{@linkplain WebHtmlTplDashboardImportBuilder}中作为URL参数使用。
	 * </p>
	 * 
	 * @param locale
	 * @param apiVersion
	 * @param lastModified
	 * @return
	 */
	protected String toCacheKey(Locale locale, String apiVersion, long lastModified)
	{
		byte[] keyBytes = (ChartPluginManagerJs.class.getSimpleName() + (locale == null ? "null" : locale.toString())
				+ (apiVersion == null ? "null" : apiVersion)
				+ Long.toHexString(lastModified)).getBytes(StandardCharsets.UTF_8);
		return Base64.getUrlEncoder().encodeToString(keyBytes);
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
		private final String key;
		private final List<String> scripts;
		private final long lastModified;

		public ChartPluginManagerJs(String key, List<String> scripts, long lastModified)
		{
			super();
			this.key = key;
			this.scripts = scripts;
			this.lastModified = lastModified;
		}

		public String getKey()
		{
			return key;
		}

		public List<String> getScripts()
		{
			return scripts;
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

	/**
	 * {@code 5.5.0}版本的{@linkplain DataSign}目标枚举，详细参考：{@code org.datagear.analysis.DataSign}。
	 * 
	 * @author datagear@163.com
	 * @deprecated 仅用于兼容{@code 5.5.0}相关格式
	 */
	@Deprecated
	protected static class DataSignTargetV5_5_0
	{
		/**
		 * 标记目标：字段
		 */
		public static final String TARGET_FIELD = "FIELD";

		/**
		 * 标记目标：数据集
		 */
		public static final String TARGET_DATASET = "DATASET";
	}

	/**
	 * {@code 5.5.0}版本的{@linkplain PropertyType}类型枚举，详细参考：{@code org.datagear.analysis.ChartPluginAttribute.DataType}。
	 * 
	 * @author datagear@163.com
	 * @deprecated 仅用于兼容{@code 5.5.0}相关格式
	 */
	@Deprecated
	protected static class PropertyTypeV5_5_0
	{
		/** 字符串 */
		public static final String STRING = "STRING";

		/** 布尔值 */
		public static final String BOOLEAN = "BOOLEAN";

		/** 整数 */
		public static final String INTEGER = "INTEGER";

		/** 数值 */
		public static final String NUMBER = "NUMBER";

		/** 对象 */
		public static final String OBJECT = "OBJECT";
	}
}
