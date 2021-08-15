/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSessionFactory;
import org.datagear.management.domain.Entity;
import org.datagear.management.service.EntityService;
import org.datagear.management.util.dialect.MbSqlDialect;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.PagingQuery;
import org.datagear.persistence.Query;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.cache.Cache;

/**
 * 抽象基于Mybatis的{@linkplain EntityService}实现类。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractMybatisEntityService<ID, T extends Entity<ID>> extends AbstractMybatisService<T>
		implements EntityService<ID, T>
{
	private Cache cache = null;
	
	/** 查询操作时缓存实体数目 */
	private int cacheCountForQuery = 50;

	public AbstractMybatisEntityService()
	{
		super();
	}

	public AbstractMybatisEntityService(SqlSessionFactory sqlSessionFactory, MbSqlDialect dialect)
	{
		super(sqlSessionFactory, dialect);
	}

	public AbstractMybatisEntityService(SqlSessionTemplate sqlSessionTemplate, MbSqlDialect dialect)
	{
		super(sqlSessionTemplate, dialect);
	}

	public Cache getCache()
	{
		return cache;
	}

	public void setCache(Cache cache)
	{
		this.cache = cache;
	}

	public int getCacheCountForQuery()
	{
		return cacheCountForQuery;
	}

	public void setCacheCountForQuery(int cacheCountForQuery)
	{
		this.cacheCountForQuery = cacheCountForQuery;
	}

	@Override
	public boolean add(T entity)
	{
		return super.add(entity);
	}

	@Override
	public boolean update(T entity)
	{
		return super.update(entity);
	}

	@Override
	public boolean deleteById(ID id)
	{
		return deleteById(id, buildParamMap());
	}

	@Override
	public boolean[] deleteByIds(ID[] ids)
	{
		boolean[] re = new boolean[ids.length];

		for (int i = 0; i < ids.length; i++)
			re[i] = deleteById(ids[i]);

		return re;
	}

	@Override
	public T getById(ID id)
	{
		return getById(id, buildParamMap());
	}

	@Override
	public List<T> query(Query query)
	{
		return super.query(query);
	}

	@Override
	public PagingData<T> pagingQuery(PagingQuery pagingQuery)
	{
		return super.pagingQuery(pagingQuery);
	}

	/**
	 * 获取。
	 * <p>
	 * 此方法内部会执行{@linkplain #postProcessSelect(Object)}。
	 * </p>
	 * 
	 * @param id
	 * @param params
	 * @return
	 */
	protected T getById(ID id, Map<String, Object> params)
	{
		return getById(id, params, true);
	}

	/**
	 * 获取。
	 * 
	 * @param id
	 * @param params
	 * @param postProcessSelect
	 *            是否内部执行{@linkplain #postProcessSelect(Object)}
	 * @return
	 */
	protected T getById(ID id, Map<String, Object> params, boolean postProcessSelect)
	{
		T entity = cacheGet(id);

		if (entity == null)
		{
			params.put("id", id);
			entity = selectOneMybatis("getById", params);

			cachePut(id, entity);
		}

		if (postProcessSelect && entity != null)
			entity = postProcessSelect(entity);

		return entity;
	}

	@Override
	protected boolean update(T entity, Map<String, Object> params)
	{
		cacheEvict(entity.getId());

		return super.update(entity, params);
	}

	/**
	 * 删除。
	 * 
	 * @param id
	 * @param params
	 * @return
	 */
	protected boolean deleteById(ID id, Map<String, Object> params)
	{
		cacheEvict(id);

		params.put("id", id);

		return (deleteMybatis("deleteById", params) > 0);
	}

	@Override
	protected List<T> query(String statement, Map<String, Object> params, boolean postProcessSelects)
	{
		List<T> list = super.query(statement, params, false);

		cachePut(list);

		if (postProcessSelects)
			postProcessSelects(list);

		return list;
	}

	@Override
	protected List<T> query(String statement, Map<String, Object> params, RowBounds rowBounds,
			boolean postProcessSelects)
	{
		List<T> list = super.query(statement, params, rowBounds, false);

		cachePut(list);

		if (postProcessSelects)
			postProcessSelects(list);

		return list;
	}

	/**
	 * 是否开启缓存。
	 * <p>
	 * 子类应注意是否需要重写{@linkplain #cacheCloneValue(Entity)}方法。
	 * </p>
	 * 
	 * @return
	 */
	protected boolean cacheEnabled()
	{
		return (getCache() != null);
	}

	/**
	 * 拷贝缓存值。
	 * <p>
	 * 当从缓存中取出对象时、将对象放入缓存时，进行拷贝。
	 * </p>
	 * <p>
	 * 此方法默认返回原对象，子类应根据实际情况（对象是否会被修改），决定是否需要真正拷贝。
	 * </p>
	 * 
	 * @param value
	 * @return
	 */
	protected T cacheCloneValue(T value)
	{
		return value;
	}

	protected void cachePut(ID id, T value)
	{
		if (!cacheEnabled())
			return;

		if (value != null)
			value = cacheCloneValue(value);

		cachePut(getCache(), value.getId(), value);
	}

	protected void cachePut(List<T> values)
	{
		if (!cacheEnabled())
			return;

		int count = Math.min(values.size(), this.getCacheCountForQuery());

		for (int i = 0; i < count; i++)
		{
			T value = values.get(i);

			if (value != null)
			{
				value = cacheCloneValue(value);
				cachePut(getCache(), value.getId(), value);
			}
		}
	}

	protected T cacheGet(ID key)
	{
		if (!cacheEnabled())
			return null;

		T value = cacheGet(getCache(), key);

		if (value != null)
			value = cacheCloneValue(value);

		return value;
	}

	protected void cacheEvict(ID key)
	{
		if (!cacheEnabled())
			return;

		cacheEvict(getCache(), key);
	}

	protected void cacheInvalidate()
	{
		if (!cacheEnabled())
			return;

		cacheInvalidate(getCache());
	}

	protected void cachePut(Cache cache, Object key, Object value)
	{
		if (cache == null)
			return;

		cache.put(key, value);
	}

	@SuppressWarnings("unchecked")
	protected <TT> TT cacheGet(Cache cache, Object key)
	{
		if (cache == null)
			return null;

		return (TT) cache.get(key);
	}

	protected void cacheEvict(Cache cache, Object key)
	{
		if (cache == null)
			return;

		cache.evict(key);
	}

	protected void cacheInvalidate(Cache cache)
	{
		if (cache == null)
			return;

		cache.invalidate();
	}
}
