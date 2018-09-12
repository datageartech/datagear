/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.web.util;

import java.sql.Connection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.datagear.dbmodel.AbstractDbModelFactory;
import org.datagear.dbmodel.CachedDbModelFactory;
import org.datagear.dbmodel.DatabaseModelResolver;
import org.datagear.dbmodel.DbModelFactoryException;
import org.datagear.model.Model;
import org.datagear.model.ModelManager;
import org.datagear.model.support.DefaultModelManager;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * {@linkplain CachedDbModelFactory}实现类。
 * 
 * @author datagear@163.com
 *
 */
public class CachedDbModelFactoryImpl extends AbstractDbModelFactory implements CachedDbModelFactory
{
	/** 缓存值的最大数 */
	private int maximumSize = 100;

	/** 缓存过期分钟数 */
	private int expireAfterAccessMinutes = 60 * 72;

	private LoadingCache<String, ModelManager> cache = null;

	public CachedDbModelFactoryImpl()
	{
		super();
	}

	public CachedDbModelFactoryImpl(DatabaseModelResolver databaseModelResolver)
	{
		super(databaseModelResolver);
	}

	public int getMaximumSize()
	{
		return maximumSize;
	}

	public void setMaximumSize(int maximumSize)
	{
		this.maximumSize = maximumSize;
	}

	public int getExpireAfterAccessMinutes()
	{
		return expireAfterAccessMinutes;
	}

	public void setExpireAfterAccessMinutes(int expireAfterAccessMinutes)
	{
		this.expireAfterAccessMinutes = expireAfterAccessMinutes;
	}

	/**
	 * 初始化。
	 */
	public void init()
	{
		this.cache = CacheBuilder.newBuilder().maximumSize(this.maximumSize)
				.expireAfterAccess(this.expireAfterAccessMinutes * 60, TimeUnit.SECONDS)
				.build(new CacheLoader<String, ModelManager>()
				{
					@Override
					public ModelManager load(String schema) throws Exception
					{
						return new DefaultModelManager();
					}
				});
	}

	@Override
	public Model getCachedModel(String schema, String tableName)
	{
		String modelName = resolveModelName(tableName);

		ModelManager modelManager = getModelManager(schema);

		Model model = null;

		synchronized (modelManager)
		{
			model = modelManager.get(modelName);
		}

		return model;
	}

	@Override
	public void removeCachedModel(String schema)
	{
		this.cache.invalidate(schema);
	}

	@Override
	public void removeCachedModel(String schema, String tableName)
	{
		String modelName = resolveModelName(tableName);

		ModelManager modelManager = getModelManager(schema);

		synchronized (modelManager)
		{
			modelManager.remove(modelName);
		}
	}

	@Override
	public Model getModel(Connection cn, String schema, String tableName)
	{
		String modelName = resolveModelName(tableName);

		ModelManager modelManager = getModelManager(schema);

		Model model = null;

		synchronized (modelManager)
		{
			model = modelManager.get(modelName);

			if (model == null)
				model = loadModelAndAdd(cn, schema, tableName, modelManager);
		}

		return model;
	}

	/**
	 * 获取指定模式的{@linkplain ModelManager}。
	 * 
	 * @param schema
	 * @return
	 */
	protected ModelManager getModelManager(String schema)
	{
		try
		{
			return this.cache.get(schema);
		}
		catch (ExecutionException e)
		{
			throw new DbModelFactoryException(e);
		}
	}
}
