/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.analysis.support;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
		super();
		int cacheCapacity = 500;
		this.dataSetTemplateLoader = new SqlDataSetTemplateLoader();
		this.dataSetTemplateLoader.setCapacity(cacheCapacity);
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
	}

	@Override
	public String resolve(String sql, Map<String, ?> values) throws TemplateSqlResolverException
	{
		this.dataSetTemplateLoader.updateTemplate(sql);

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

	protected boolean needHandleAsTemplate(SqlDataSet sqlDataSet)
	{
		return sqlDataSet.hasParam();
	}

	public static class SqlDataSetTemplateLoader implements TemplateLoader
	{
		private Map<String, SqlDataSetTemplateSource> sqlDataSetTemplateSources = new HashMap<>();

		private int capacity = 500;

		private int expiredSeconds = 60 * 10;

		private ReadWriteLock _lock = new ReentrantReadWriteLock();

		public SqlDataSetTemplateLoader()
		{
			super();
		}

		public int getCapacity()
		{
			return capacity;
		}

		public void setCapacity(int capacity)
		{
			this.capacity = capacity;
		}

		public int getExpiredSeconds()
		{
			return expiredSeconds;
		}

		public void setExpiredSeconds(int expiredSeconds)
		{
			this.expiredSeconds = expiredSeconds;
		}

		public String getTemplateName(SqlDataSet sqlDataSet)
		{
			return sqlDataSet.getId();
		}

		/**
		 * 将给定SQL更新为Freemarker模板。
		 * 
		 * @param sql
		 * @return true 更新成功；false 未更新，因为{@code sql}自上次以来未修改
		 */
		public boolean updateTemplate(String sql)
		{
			Lock readLock = this._lock.readLock();

			try
			{
				readLock.lock();

				SqlDataSetTemplateSource sts = this.sqlDataSetTemplateSources.get(sql);
				if (sts != null)
					return false;
			}
			finally
			{
				readLock.unlock();
			}

			Lock writeLock = this._lock.writeLock();
			try
			{
				writeLock.lock();

				long currentTime = System.currentTimeMillis();
				long expiredTime = currentTime - this.expiredSeconds * 1000;

				SqlDataSetTemplateSource sts = new SqlDataSetTemplateSource(sql, currentTime);
				this.sqlDataSetTemplateSources.put(sql, sts);

				if (this.sqlDataSetTemplateSources.size() >= this.capacity)
				{
					Collection<SqlDataSetTemplateSource> stss = this.sqlDataSetTemplateSources.values();
					List<SqlDataSetTemplateSource> list = new ArrayList<>(stss.size());
					list.addAll(stss);
					Collections.sort(list, MRU_COMPARATOR);

					this.sqlDataSetTemplateSources.clear();

					int count = 0;
					for (SqlDataSetTemplateSource ele : list)
					{
						if (count > this.capacity || ele.getLastModified() < expiredTime)
							break;

						this.sqlDataSetTemplateSources.put(sql, ele);

						count++;
					}
				}

				return true;
			}
			finally
			{
				writeLock.unlock();
			}
		}

		@Override
		public void closeTemplateSource(Object templateSource) throws IOException
		{
		}

		@Override
		public Object findTemplateSource(String name) throws IOException
		{
			Lock readLock = this._lock.readLock();

			try
			{
				readLock.lock();

				return this.sqlDataSetTemplateSources.get(name);
			}
			finally
			{
				readLock.unlock();
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

		private static final Comparator<SqlDataSetTemplateSource> MRU_COMPARATOR = new Comparator<SqlDataSetTemplateSource>()
		{
			@Override
			public int compare(SqlDataSetTemplateSource o1, SqlDataSetTemplateSource o2)
			{
				return (0 - (int) (o1.getLastModified() - o2.getLastModified()));
			}
		};
	}
}
