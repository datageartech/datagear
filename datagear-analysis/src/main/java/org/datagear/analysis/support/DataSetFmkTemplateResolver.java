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

package org.datagear.analysis.support;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import org.datagear.util.CacheService;
import org.springframework.cache.Cache.ValueWrapper;

import freemarker.cache.TemplateLoader;
import freemarker.core.OutputFormat;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * 专用于数据集模板且采用Freemarker作为模板语言的{@linkplain TemplateResolver}。
 * <p>
 * 此类的{@linkplain #setDataSetTemplateStandardConfig(Configuration)}定义了很多数据集模板规范，
 * 这些规范不应被更改，因为会影响用户已定义数据集的模板。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class DataSetFmkTemplateResolver implements TemplateResolver
{
	/**
	 * Freemarker数值输出格式：computer
	 * <p>
	 * 这个格式会按照计算机编程格式输出数值，即：区域无关、无分隔符、无指数格式，例如：12345678950401.12345678950401
	 * </p>
	 * <p>
	 * 具体参考Freemarker文档Built-ins for numbers章节的<code>c</code>指令。
	 * </p>
	 */
	public static final String FREEMARKER_NUMBER_FORMAT_COMPUTER = "computer";

	private NameTemplateLoader nameTemplateLoader;

	private Configuration configuration;

	public DataSetFmkTemplateResolver()
	{
		this(null, 1000);
	}

	public DataSetFmkTemplateResolver(OutputFormat outputFormat)
	{
		this(outputFormat, 1000);
	}

	public DataSetFmkTemplateResolver(OutputFormat outputFormat, int cacheCapacity)
	{
		super();
		this.nameTemplateLoader = new NameTemplateLoader();

		Configuration configuration = new Configuration(Configuration.VERSION_2_3_30);
		configuration.setCacheStorage(new freemarker.cache.MruCacheStorage(0, cacheCapacity));

		if (outputFormat != null)
			configuration.setOutputFormat(outputFormat);

		setConfiguration(configuration);
	}

	public NameTemplateLoader getNameTemplateLoader()
	{
		return nameTemplateLoader;
	}

	public void setNameTemplateLoader(NameTemplateLoader nameTemplateLoader)
	{
		this.nameTemplateLoader = nameTemplateLoader;

		if (this.configuration != null)
			this.configuration.setTemplateLoader(this.nameTemplateLoader);
	}

	public Configuration getConfiguration()
	{
		return configuration;
	}

	public void setConfiguration(Configuration configuration)
	{
		this.configuration = configuration;

		if (this.nameTemplateLoader != null)
			this.configuration.setTemplateLoader(this.nameTemplateLoader);

		setDataSetTemplateStandardConfig(this.configuration);
	}

	/**
	 * 设置用于数据集模板的语法规范。
	 * 
	 * @param configuration
	 */
	protected void setDataSetTemplateStandardConfig(Configuration configuration)
	{
		// 插值语法规范设置为："${...}"
		configuration.setInterpolationSyntax(Configuration.DOLLAR_INTERPOLATION_SYNTAX);

		// 标签语法规范设置为：<#if>...</#if>
		configuration.setTagSyntax(Configuration.ANGLE_BRACKET_TAG_SYNTAX);

		// 数值插值设置为标准格式
		configuration.setNumberFormat(FREEMARKER_NUMBER_FORMAT_COMPUTER);

		// 由于此类的模板策略是直接使用模板作为模板名，如果此方法设置为true，
		// 下面的NameTemplateLoader.findTemplateSource(String)的参数SQL会被加上Locale后缀导致逻辑出错，
		// 因此这里必须设置为false
		configuration.setLocalizedLookup(false);
	}

	/**
	 * 使用指定数据集参数值解析模板。
	 * 
	 * @param template
	 * @param paramValues
	 * @return
	 * @throws TemplateResolverException
	 */
	public String resolve(String template, Map<String, ?> paramValues) throws TemplateResolverException
	{
		return resolve(template, new TemplateContext(paramValues));
	}

	@Override
	public String resolve(String template, TemplateContext templateContext) throws TemplateResolverException
	{
		String re = null;

		Map<String, ?> values = templateContext.getValues();

		try
		{
			Template templateObj = this.configuration.getTemplate(template);
			StringWriter out = new StringWriter();
			templateObj.process(values, out);
			re = out.toString();
		}
		catch (IOException e)
		{
			throw new TemplateResolverException(e);
		}
		catch (TemplateException e)
		{
			throw new TemplateResolverException(e);
		}

		return re;
	}

	/**
	 * 直接使用名称作为模板的{@linkplain TemplateLoader}。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class NameTemplateLoader implements TemplateLoader
	{
		private CacheService cacheService = new CacheService(null, false, false, true);

		public NameTemplateLoader()
		{
			super();
		}

		public CacheService getCacheService()
		{
			return cacheService;
		}

		public void setCacheService(CacheService cacheService)
		{
			this.cacheService = cacheService;
		}

		@Override
		public void closeTemplateSource(Object templateSource) throws IOException
		{
		}

		@Override
		public Object findTemplateSource(String name) throws IOException
		{
			String key = getCacheKey(name);
			ValueWrapper tsWrapper = this.cacheService.get(key);
			Object ts = (tsWrapper == null ? null : tsWrapper.get());

			if (ts != null)
				return ts;

			ts = new NameTemplateSource(name, System.currentTimeMillis());
			this.cacheService.put(key, ts);

			return ts;
		}

		@Override
		public long getLastModified(Object templateSource)
		{
			return ((NameTemplateSource) templateSource).getLastModified();
		}

		@Override
		public Reader getReader(Object templateSource, String encoding) throws IOException
		{
			return new StringReader(((NameTemplateSource) templateSource).getName());
		}

		protected String getCacheKey(String name)
		{
			if (!this.cacheService.isShared())
				return name;

			return NameTemplateLoader.class.getName() + "." + name;
		}

		protected static class NameTemplateSource
		{
			private final String name;

			private final long lastModified;

			public NameTemplateSource(String name, long lastModified)
			{
				super();
				this.name = name;
				this.lastModified = lastModified;
			}

			public String getName()
			{
				return name;
			}

			public long getLastModified()
			{
				return lastModified;
			}

			@Override
			public int hashCode()
			{
				final int prime = 31;
				int result = 1;
				result = prime * result + ((name == null) ? 0 : name.hashCode());
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
				NameTemplateSource other = (NameTemplateSource) obj;
				if (name == null)
				{
					if (other.name != null)
						return false;
				}
				else if (!name.equals(other.name))
					return false;
				return true;
			}
		}
	}
}
