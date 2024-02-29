/*
 * Copyright 2018-2024 datagear.tech
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

import freemarker.cache.TemplateLoader;
import freemarker.core.OutputFormat;
import freemarker.core.TemplateClassResolver;
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

	private final ThreadLocaleTemplateLoader threadLocaleTemplateLoader = new ThreadLocaleTemplateLoader();

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

		Configuration configuration = new Configuration(Configuration.VERSION_2_3_30);
		configuration.setCacheStorage(new freemarker.cache.MruCacheStorage(0, cacheCapacity));

		if (outputFormat != null)
			configuration.setOutputFormat(outputFormat);

		setConfiguration(configuration);
	}

	public Configuration getConfiguration()
	{
		return configuration;
	}

	protected void setConfiguration(Configuration configuration)
	{
		this.configuration = configuration;

		this.configuration.setTemplateLoader(this.threadLocaleTemplateLoader);
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

		// 此类的模板策略不需要国际化支持，应禁用
		configuration.setLocalizedLookup(false);

		// 禁用"?new"指令，这里不需此特性，并且此指令会导致远程命令执行安全漏洞
		configuration.setNewBuiltinClassResolver(TemplateClassResolver.ALLOWS_NOTHING_RESOLVER);

		// 禁用"?api"指令，这里不需要此特性，并且此指令会导致安全问题
		configuration.setAPIBuiltinEnabled(false);
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

		ThreadLocaleTemplateLoader.setTemplate(template);

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
		finally
		{
			ThreadLocaleTemplateLoader.removeTemplate();
		}

		return re;
	}

	/**
	 * 使用{@linkplain ThreadLocaleTemplateLoader#setTemplate(String)}作为模板的{@linkplain TemplateLoader}。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static class ThreadLocaleTemplateLoader implements TemplateLoader
	{
		protected static final ThreadLocal<String> TEMPLATE_THREAD_LOCAL = new ThreadLocal<String>();

		protected static final long LAST_MODIFLED = System.currentTimeMillis();

		public ThreadLocaleTemplateLoader()
		{
			super();
		}

		@Override
		public void closeTemplateSource(Object templateSource) throws IOException
		{
		}

		@Override
		public Object findTemplateSource(String name) throws IOException
		{
			// 注意：
			// 这里的实现不能直接使用name作为模板，因为它可能并不是原始的DataSetFmkTemplateResolver.resolve()方法传入的模板,
			// 因为这里的name是经过了freemarker.cache.TemplateNameFormat处理的
			TemplateContentSource ts = new TemplateContentSource(name, TEMPLATE_THREAD_LOCAL.get(), LAST_MODIFLED);
			return ts;
		}

		@Override
		public long getLastModified(Object templateSource)
		{
			return ((TemplateContentSource) templateSource).getLastModified();
		}

		@Override
		public Reader getReader(Object templateSource, String encoding) throws IOException
		{
			return new StringReader(((TemplateContentSource) templateSource).getTemplate());
		}

		public static void setTemplate(String template)
		{
			TEMPLATE_THREAD_LOCAL.set(template);
		}

		public static void removeTemplate()
		{
			TEMPLATE_THREAD_LOCAL.remove();
		}

		protected static class TemplateContentSource
		{
			private final String name;
			private final String template;
			private final long lastModified;

			public TemplateContentSource(String name, String template, long lastModified)
			{
				super();
				this.name = name;
				this.template = template;
				this.lastModified = lastModified;
			}

			public String getName()
			{
				return name;
			}

			public String getTemplate()
			{
				return template;
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
				result = prime * result + (int) (lastModified ^ (lastModified >>> 32));
				result = prime * result + ((name == null) ? 0 : name.hashCode());
				result = prime * result + ((template == null) ? 0 : template.hashCode());
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
				TemplateContentSource other = (TemplateContentSource) obj;
				if (lastModified != other.lastModified)
					return false;
				if (name == null)
				{
					if (other.name != null)
						return false;
				}
				else if (!name.equals(other.name))
					return false;
				if (template == null)
				{
					if (other.template != null)
						return false;
				}
				else if (!template.equals(other.template))
					return false;
				return true;
			}
		}
	}
}
