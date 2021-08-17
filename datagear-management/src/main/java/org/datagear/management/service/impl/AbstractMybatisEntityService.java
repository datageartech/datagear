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
import org.datagear.management.domain.CloneableEntity;
import org.datagear.management.domain.Entity;
import org.datagear.management.service.EntityService;
import org.datagear.management.util.dialect.MbSqlDialect;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.PagingQuery;
import org.datagear.persistence.Query;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.support.SimpleValueWrapper;

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
	
	/**
	 * 查询操作时缓存实体数目。
	 * <p>
	 * 默认为{@code 0}，不缓存查询操作结果。
	 * </p>
	 * <p>
	 * 谨慎设置此值，因为目前为了提高查询效率，有些服务实现类里查询返回的并不是完全的可用的实体，不能作为缓存使用（比如，未加载一对多值对象）。
	 * </p>
	 */
	private int cacheCountForQuery = 0;

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
		T entity = getById(id, buildParamMap());

		if (entity != null)
			entity = postProcessGet(entity);

		return entity;
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
	 * 获取实体。
	 * <p>
	 * 此方法先从缓存中获取实体，如果没有，则调用{@linkplain #getByIdFromDB(Object, Map)}从底层数据库获取。
	 * </p>
	 * <p>
	 * 注意：在调用此方法获取实体后，应重新设置其引用的实体对象属性值，以保证它们是最新的。
	 * </p>
	 * 
	 * @param id
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected T getById(ID id, Map<String, Object> params)
	{
		T entity = null;

		ValueWrapper entityWrapper = cacheGet(id);

		if (entityWrapper != null)
		{
			entity = (T) entityWrapper.get();
		}
		else
		{
			entity = getByIdFromDB(id, params);

			cachePut(id, entity);
		}

		return entity;
	}

	/**
	 * 从底层数据库获取实体。
	 * <p>
	 * 此方法调用底层的{@code getById} SQL。
	 * </p>
	 * 
	 * @param id
	 * @param params
	 * @return
	 */
	protected T getByIdFromDB(ID id, Map<String, Object> params)
	{
		params.put("id", id);
		return selectOneMybatis("getById", params);
	}

	@Override
	protected boolean update(T entity, Map<String, Object> params)
	{
		cacheEvict(entity.getId());

		return super.update(entity, params);
	}

	/**
	 * 删除。
	 * <p>
	 * 此方法调用底层的{@code deleteById} SQL。
	 * </p>
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
	protected List<T> query(String statement, Map<String, Object> params)
	{
		List<T> list = super.query(statement, params);

		cachePutQueryResult(list);

		return list;
	}

	@Override
	protected List<T> query(String statement, Map<String, Object> params, RowBounds rowBounds)
	{
		List<T> list = super.query(statement, params, rowBounds);

		cachePutQueryResult(list);

		return list;
	}

	/**
	 * 是否开启缓存。
	 * <p>
	 * 子类应注意是否需要重写{@linkplain #cacheCloneEntity(Entity)}方法。
	 * </p>
	 * 
	 * @return
	 */
	protected boolean cacheEnabled()
	{
		return (getCache() != null);
	}

	/**
	 * 克隆缓存实体。
	 * <p>
	 * 参考{@linkplain #cacheGet(Object)}、{@linkplain #cachePut(Object, Entity)}、{@linkplain #cachePutQueryResult(List)}。
	 * </p>
	 * <p>
	 * 对于无序列化缓存（比如进程内缓存），应遵循{@linkplain CloneableEntity#clone()}规则；对于序列化缓存，则可直接返回原实体。
	 * </p>
	 * <p>
	 * 此方法默认是现是：如果{@code value}是{@linkplain CloneableEntity}，则返回{@linkplain CloneableEntity#clone()}，否则，返回原对象。
	 * </p>
	 * 
	 * @param value
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected T cacheCloneEntity(T value)
	{
		if (value instanceof CloneableEntity)
			return (T) ((CloneableEntity) value).clone();

		return value;
	}

	/**
	 * 获取指定实体ID的缓存关键字。
	 * 
	 * @param id
	 * @return
	 */
	protected Object toCacheKey(ID id)
	{
		return id;
	}

	/**
	 * 从缓存中读取实体。
	 * <p>
	 * 此方法将使用{@linkplain #cacheCloneEntity(Entity)}返回克隆后的实体对象。
	 * </p>
	 * 
	 * @param id
	 * @return
	 */
	protected ValueWrapper cacheGet(ID id)
	{
		if (!cacheEnabled())
			return null;

		ValueWrapper valueWrapper = cacheGet(getCache(), toCacheKey(id));

		if (valueWrapper == null)
			return null;

		@SuppressWarnings("unchecked")
		T value = (T) valueWrapper.get();

		if (value != null)
			value = cacheCloneEntity(value);

		return new SimpleValueWrapper(value);
	}

	/**
	 * 将实体存入缓存。
	 * <p>
	 * 此方法将使用{@linkplain #cacheCloneEntity(Entity)}缓存克隆后的实体对象。
	 * </p>
	 * 
	 * @param id
	 * @param value
	 */
	protected void cachePut(ID id, T value)
	{
		if (!cacheEnabled())
			return;

		if (value != null)
			value = cacheCloneEntity(value);

		cachePut(getCache(), toCacheKey(id), value);
	}

	/**
	 * 将实体存入缓存。
	 * <p>
	 * 此方法将使用{@linkplain #cacheCloneEntity(Entity)}缓存克隆后的实体对象。
	 * </p>
	 * 
	 * @param values
	 */
	protected void cachePutQueryResult(List<T> values)
	{
		if (!cacheEnabled())
			return;

		int count = Math.min(values.size(), this.getCacheCountForQuery());

		for (int i = 0; i < count; i++)
		{
			T value = values.get(i);

			if (value != null)
			{
				value = cacheCloneEntity(value);
				cachePut(getCache(), toCacheKey(value.getId()), value);
			}
		}
	}

	protected void cacheEvict(ID id)
	{
		if (!cacheEnabled())
			return;

		cacheEvict(getCache(), toCacheKey(id));
	}

	protected void cacheInvalidate()
	{
		if (!cacheEnabled())
			return;

		cacheInvalidate(getCache());
	}
}
