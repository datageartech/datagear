/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.analysis.support;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * Freemarker {@linkplain TemplateSqlResolver}。
 * <p>
 * 此类可解析由Freemarker编写的SQL语句。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class TemplateFmkSqlResolver implements TemplateSqlResolver
{
	private SqlDataSetTemplateLoader dataSetTemplateLoader;

	private Configuration configuration;

	public TemplateFmkSqlResolver()
	{
		this(100);
	}

	public TemplateFmkSqlResolver(int cacheCapacity)
	{
		super();
		this.dataSetTemplateLoader = new SqlDataSetTemplateLoader(cacheCapacity);
		this.configuration = new Configuration(Configuration.VERSION_2_3_30);
		this.configuration.setTemplateLoader(this.dataSetTemplateLoader);
		setSqlTemplateStandardConfig(this.configuration);
		this.configuration.setCacheStorage(new freemarker.cache.MruCacheStorage(0, cacheCapacity));
	}

	public SqlDataSetTemplateLoader getDataSetTemplateLoader()
	{
		return dataSetTemplateLoader;
	}

	public void setDataSetTemplateLoader(SqlDataSetTemplateLoader dataSetTemplateLoader)
	{
		this.dataSetTemplateLoader = dataSetTemplateLoader;

		if (this.configuration != null)
			this.configuration.setTemplateLoader(this.dataSetTemplateLoader);
	}

	public Configuration getConfiguration()
	{
		return configuration;
	}

	public void setConfiguration(Configuration configuration)
	{
		this.configuration = configuration;
		setSqlTemplateStandardConfig(this.configuration);

		if (this.dataSetTemplateLoader != null)
			this.configuration.setTemplateLoader(this.dataSetTemplateLoader);
	}

	public void setSqlTemplateStandardConfig(Configuration configuration)
	{
		// 插值语法规范设置为："${...}"
		configuration.setInterpolationSyntax(Configuration.DOLLAR_INTERPOLATION_SYNTAX);

		// 标签语法规范设置为：<#if>...</#if>
		configuration.setTagSyntax(Configuration.ANGLE_BRACKET_TAG_SYNTAX);

		// 数值插值设置为SQL标准格式
		configuration.setNumberFormat("0.########");

		// 由于此类的模板策略是直接使用SQL语句作为模板名和模板内容，如果此方法设置为true，
		// 下面的SqlDataSetTemplateLoader.findTemplateSource(String)的参数SQL会被加上Locale后缀导致逻辑出错，
		// 因此这里必须设置为false
		configuration.setLocalizedLookup(false);
	}

	@Override
	public String resolve(String sql, Map<String, ?> values) throws TemplateSqlResolverException
	{
		String re = null;

		try
		{
			Template template = this.configuration.getTemplate(sql);
			StringWriter out = new StringWriter();
			template.process(values, out);
			re = out.toString();
		}
		catch (IOException e)
		{
			throw new TemplateSqlResolverException(e);
		}
		catch (TemplateException e)
		{
			throw new TemplateSqlResolverException(e);
		}

		return re;
	}

	public static class SqlDataSetTemplateLoader implements TemplateLoader
	{
		private Cache<String, SqlDataSetTemplateSource> sqlDataSetTemplateCache;

		public SqlDataSetTemplateLoader(int cacheCapacity)
		{
			super();

			this.sqlDataSetTemplateCache = CacheBuilder.newBuilder()
					.maximumSize(cacheCapacity).expireAfterAccess(60 * 24, TimeUnit.MINUTES)
					.build();
		}

		protected Cache<String, SqlDataSetTemplateSource> getSqlDataSetTemplateCache()
		{
			return sqlDataSetTemplateCache;
		}

		protected void setSqlDataSetTemplateCache(Cache<String, SqlDataSetTemplateSource> sqlDataSetTemplateCache)
		{
			this.sqlDataSetTemplateCache = sqlDataSetTemplateCache;
		}

		@Override
		public void closeTemplateSource(Object templateSource) throws IOException
		{
		}

		@Override
		public Object findTemplateSource(String sql) throws IOException
		{
			try
			{
				return this.sqlDataSetTemplateCache.get(sql, new Callable<SqlDataSetTemplateSource>()
				{
					@Override
					public SqlDataSetTemplateSource call() throws Exception
					{
						return new SqlDataSetTemplateSource(sql, System.currentTimeMillis());
					}
				});
			}
			catch(ExecutionException e)
			{
				throw new IOException("find template source in cache exception", e);
			}
		}

		@Override
		public long getLastModified(Object templateSource)
		{
			return ((SqlDataSetTemplateSource) templateSource).getLastModified();
		}

		@Override
		public Reader getReader(Object templateSource, String encoding) throws IOException
		{
			return new StringReader(((SqlDataSetTemplateSource) templateSource).getSql());
		}

		protected static class SqlDataSetTemplateSource
		{
			private final String sql;

			private final long lastModified;

			public SqlDataSetTemplateSource(String sql, long lastModified)
			{
				super();
				this.sql = sql;
				this.lastModified = lastModified;
			}

			public String getSql()
			{
				return sql;
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
				result = prime * result + ((sql == null) ? 0 : sql.hashCode());
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
				SqlDataSetTemplateSource other = (SqlDataSetTemplateSource) obj;
				if (sql == null)
				{
					if (other.sql != null)
						return false;
				}
				else if (!sql.equals(other.sql))
					return false;
				return true;
			}
		}
	}
}
