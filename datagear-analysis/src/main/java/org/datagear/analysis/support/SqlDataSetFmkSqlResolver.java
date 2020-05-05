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

import org.datagear.util.Sql;

import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * Freemarker SQL模板{@linkplain SqlDataSetSqlResolver}。
 * <p>
 * 它将{@linkplain SqlDataSet#getSql()}作为Freemarker模板处理。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class SqlDataSetFmkSqlResolver implements SqlDataSetSqlResolver
{
	private SqlDataSetTemplateLoader dataSetTemplateLoader;

	private Configuration configuration;

	public SqlDataSetFmkSqlResolver()
	{
		super();
		int cacheCapacity = 500;
		this.dataSetTemplateLoader = new SqlDataSetTemplateLoader();
		this.dataSetTemplateLoader.setCapacity(cacheCapacity);
		this.configuration = new Configuration(Configuration.VERSION_2_3_30);
		this.configuration.setTemplateLoader(this.dataSetTemplateLoader);
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

		if (this.dataSetTemplateLoader != null)
			this.configuration.setTemplateLoader(this.dataSetTemplateLoader);
	}

	@Override
	public Sql resolve(SqlDataSet sqlDataSet, Map<String, ?> dataSetParamValues) throws SqlDataSetSqlResolverException
	{
		this.dataSetTemplateLoader.updateTemplate(sqlDataSet);

		String sql = null;

		try
		{
			Template template = this.configuration.getTemplate(this.dataSetTemplateLoader.getTemplateName(sqlDataSet));
			StringWriter out = new StringWriter();
			template.process(dataSetParamValues, out);
			sql = out.toString();
		}
		catch(IOException e)
		{
			throw new SqlDataSetSqlResolverException(e);
		}
		catch(TemplateException e)
		{
			throw new SqlDataSetSqlResolverException(e);
		}

		return Sql.valueOf(sql);
	}

	public static class SqlDataSetTemplateLoader implements TemplateLoader
	{
		private Map<String, SqlDataSetTemplateSource> sqlDataSetTemplateSources = new HashMap<String, SqlDataSetTemplateSource>();

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
		 * 将{@linkplain SqlDataSet#getSql()}更新为Freemarker模板。
		 * 
		 * @param sqlDataSet
		 * @return true 更新成功；false 未更新，因为{@linkplain SqlDataSet#getSql()}自上次以来未修改
		 */
		public boolean updateTemplate(SqlDataSet sqlDataSet)
		{
			String templateName = getTemplateName(sqlDataSet);

			Lock readLock = this._lock.readLock();

			try
			{
				readLock.lock();

				SqlDataSetTemplateSource sts = this.sqlDataSetTemplateSources.get(templateName);
				if (sts != null && sts.getSql().equals(sqlDataSet.getSql()))
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
				
				SqlDataSetTemplateSource sts = new SqlDataSetTemplateSource(sqlDataSet, currentTime);
				this.sqlDataSetTemplateSources.put(templateName, sts);

				if (this.sqlDataSetTemplateSources.size() >= this.capacity)
				{
					Collection<SqlDataSetTemplateSource> stss = this.sqlDataSetTemplateSources.values();
					List<SqlDataSetTemplateSource> list = new ArrayList<SqlDataSetTemplateSource>(stss.size());
					list.addAll(stss);
					Collections.sort(list, MRU_COMPARATOR);

					this.sqlDataSetTemplateSources.clear();

					int count = 0;
					for (SqlDataSetTemplateSource ele : list)
					{
						if(count > this.capacity || ele.getLastModified() < expiredTime)
							break;

						this.sqlDataSetTemplateSources.put(templateName, ele);
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
			private final String name;

			private final String sql;

			private final long lastModified;

			public SqlDataSetTemplateSource(SqlDataSet sqlDataSet, long lastModified)
			{
				super();
				this.name = sqlDataSet.getId();
				this.sql = sqlDataSet.getSql();
				this.lastModified = lastModified;
			}

			public String getName()
			{
				return name;
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
				SqlDataSetTemplateSource other = (SqlDataSetTemplateSource) obj;
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
