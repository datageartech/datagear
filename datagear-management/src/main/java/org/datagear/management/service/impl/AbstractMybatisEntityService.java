/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.service.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSessionFactory;
import org.datagear.management.domain.AnalysisProject;
import org.datagear.management.domain.AnalysisProjectAwareEntity;
import org.datagear.management.domain.CloneableEntity;
import org.datagear.management.domain.CreateUserEntity;
import org.datagear.management.domain.DataSetResDirectory;
import org.datagear.management.domain.DirectoryFileDataSetEntity;
import org.datagear.management.domain.Entity;
import org.datagear.management.domain.User;
import org.datagear.management.service.AnalysisProjectService;
import org.datagear.management.service.CreateUserEntityService;
import org.datagear.management.service.DataSetResDirectoryService;
import org.datagear.management.service.EntityService;
import org.datagear.management.service.UserService;
import org.datagear.management.util.dialect.MbSqlDialect;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.PagingQuery;
import org.datagear.persistence.Query;
import org.datagear.util.StringUtil;
import org.mybatis.spring.SqlSessionTemplate;
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
	private ServiceCache cache = null;
	
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

	public ServiceCache getCache()
	{
		return cache;
	}

	public void setCache(ServiceCache cache)
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
	public void add(T entity)
	{
		super.add(entity);
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
		entity = postProcessGetNullable(entity);

		return entity;
	}

	/**
	 * 后置处理获取操作结果。
	 * <p>
	 * 子类应重写此方法，加载此实的共享属性值（有独立生命周期），因为这个实体可能是从缓存中取出的，共享属性值可能已过时。
	 * </p>
	 * <p>
	 * 注意：加载此实体的私有属性值应通过重写{@linkplain #getByIdFromDB(Object, Map)}来实现，这样它们才会被加入缓存。
	 * </p>
	 * <p>
	 * 此方法的默认实现是：直接返回{@code entity}。
	 * </p>
	 * 
	 * @see #getById(Object) getById(Object)会在其内部调用此方法
	 */
	@Override
	protected T postProcessGet(T entity)
	{
		return super.postProcessGet(entity);
	}

	@Override
	public List<T> query(Query query)
	{
		return super.query(query, true);
	}

	@Override
	public PagingData<T> pagingQuery(PagingQuery pagingQuery)
	{
		return super.pagingQuery(pagingQuery, true);
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
		if (id == null)
			return null;

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
	 * @return 可能返回{@code null}
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
	 * 更新创建用户。
	 * <p>
	 * 此方法主要为子类实现{@linkplain CreateUserEntityService#updateCreateUserId(String, String)}提供支持。
	 * </p>
	 * 
	 * @param oldUserId
	 * @param newUserId
	 * @return
	 * @see #updateCreateUserId(String[], String)
	 */
	protected int updateCreateUserId(String oldUserId, String newUserId)
	{
		String[] oldUserIds = new String[] { oldUserId };
		return updateCreateUserId(oldUserIds, newUserId);
	}

	/**
	 * 更新创建用户。
	 * <p>
	 * 此方法调用底层的{@code updateCreateUserId} SQL。
	 * </p>
	 * <p>
	 * 此方法主要为子类实现{@linkplain CreateUserEntityService#updateCreateUserId(String[], String)}提供支持。
	 * </p>
	 * 
	 * @param oldUserIds
	 * @param newUserId
	 * @return
	 */
	protected int updateCreateUserId(String[] oldUserIds, String newUserId)
	{
		Map<String, Object> params = buildParamMap();
		params.put("oldUserIds", oldUserIds);
		params.put("newUserId", newUserId);

		int count = updateMybatis("updateCreateUserId", params);

		if (count > 0)
			cacheInvalidate();

		return count;
	}

	/**
	 * 如果{@linkplain CreateUserEntity#getCreateUser()}不为空，
	 * 则使用{@linkplain UserService#getByIdNoPassword(String)}对其进行更新。
	 * 
	 * @param entity
	 *            允许为{@code null}
	 * @param service
	 */
	protected void inflateCreateUserEntity(CreateUserEntity<?> entity, UserService service)
	{
		User user = (entity == null ? null : entity.getCreateUser());

		// 匿名用户应直接返回，不然下面会将其置为null
		if (user != null && user.isAnonymous())
			return;

		String userId = (user == null ? null : user.getId());
		
		if(!StringUtil.isEmpty(userId))
			entity.setCreateUser(service.getByIdNoPassword(userId));
	}

	/**
	 * 如果{@linkplain AnalysisProjectAwareEntity#getAnalysisProject()}不为空，
	 * 则使用{@linkplain AnalysisProjectService#getById(String)}对其进行更新。
	 * 
	 * @param entity  允许为{@code null}
	 * @param service
	 */
	protected void inflateAnalysisProjectAwareEntity(AnalysisProjectAwareEntity<?> entity,
			AnalysisProjectService service)
	{
		AnalysisProject ap = (entity == null ? null : entity.getAnalysisProject());
		String apId = (ap == null ? null : ap.getId());

		if (!StringUtil.isEmpty(ap))
			entity.setAnalysisProject(service.getById(apId));
	}

	/**
	 * 如果{@linkplain DirectoryFileDataSetEntity#getDataSetResDirectory()}不为空，
	 * 则使用{@linkplain DataSetResDirectoryService#getById(String)}对其进行更新。
	 * 
	 * @param entity  允许为{@code null}
	 * @param service
	 */
	protected void inflateDirectoryFileDataSetEntity(DirectoryFileDataSetEntity entity,
			DataSetResDirectoryService service)
	{
		DataSetResDirectory dsd = (entity == null ? null : entity.getDataSetResDirectory());
		String dsdId = (dsd == null ? null : dsd.getId());

		if (!StringUtil.isEmpty(dsdId))
			entity.setDataSetResDirectory(service.getById(dsdId));
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
		if (!isCacheEnabled())
			return null;

		ValueWrapper valueWrapper = this.cache.get(toCacheKey(id));

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
		if (!isCacheEnabled())
			return;

		if (value != null)
			value = cacheCloneEntity(value);

		this.cache.put(toCacheKey(id), value);
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
		if (!isCacheEnabled())
			return;

		int count = Math.min(values.size(), this.getCacheCountForQuery());

		for (int i = 0; i < count; i++)
		{
			T value = values.get(i);

			if (value != null)
			{
				value = cacheCloneEntity(value);
				this.cache.put(toCacheKey(value.getId()), value);
			}
		}
	}

	protected void cacheEvict(ID id)
	{
		if (!isCacheEnabled())
			return;

		this.cache.evictImmediately(toCacheKey(id));
	}

	protected void cacheInvalidate()
	{
		if (!isCacheEnabled())
			return;

		this.cache.invalidate();
	}

	protected boolean isCacheEnabled()
	{
		return (this.cache != null && this.cache.isEnable());
	}

	/**
	 * 克隆缓存实体。
	 * <p>
	 * 参考{@linkplain #cacheGet(Object)}、{@linkplain #cachePut(Object, Entity)}、{@linkplain #cachePutQueryResult(List)}。
	 * </p>
	 * <p>
	 * 如果{@linkplain #getCache()}的{@linkplain ServiceCache#isSerialized()}为{@code false}（比如进程内缓存），应遵循{@linkplain CloneableEntity#clone()}规则；
	 * 否则，可直接返回原实体。
	 * </p>
	 * <p>
	 * 此方法默认是现是：当需要克隆时，如果{@code value}是{@linkplain CloneableEntity}，则返回{@linkplain CloneableEntity#clone()}，否则，返回原对象。
	 * </p>
	 * <p>
	 * 调用此方法前应确保{@linkplain #isCacheEnabled()}为{@code true}。
	 * </p>
	 * 
	 * @param value
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected T cacheCloneEntity(T value)
	{
		if (this.cache.isSerialized())
			return value;

		if (value instanceof CloneableEntity)
			return (T) ((CloneableEntity) value).clone();

		return value;
	}

	/**
	 * 获取指定实体ID的缓存关键字。
	 * <p>
	 * 调用此方法前应确保{@linkplain #isCacheEnabled()}为{@code true}。
	 * </p>
	 * 
	 * @param id
	 * @return
	 */
	protected Object toCacheKey(ID id)
	{
		if (this.cache.isShared())
			return new GlobalEntityCacheKey<ID>(getSqlNamespace(), id);
		else
			return id;
	}

	/**
	 * 全局实体缓存KEY。
	 * 
	 * @author datagear@163.com
	 *
	 * @param <ID>
	 */
	protected static class GlobalEntityCacheKey<ID> implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private final String namespace;

		private final ID id;

		public GlobalEntityCacheKey(String namespace, ID id)
		{
			super();
			this.namespace = namespace;
			this.id = id;
		}

		public String getNamespace()
		{
			return namespace;
		}

		public ID getId()
		{
			return id;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((id == null) ? 0 : id.hashCode());
			result = prime * result + ((namespace == null) ? 0 : namespace.hashCode());
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
			GlobalEntityCacheKey<?> other = (GlobalEntityCacheKey<?>) obj;
			if (id == null)
			{
				if (other.id != null)
					return false;
			}
			else if (!id.equals(other.id))
				return false;
			if (namespace == null)
			{
				if (other.namespace != null)
					return false;
			}
			else if (!namespace.equals(other.namespace))
				return false;
			return true;
		}
	}
}
