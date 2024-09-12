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

package org.datagear.management.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSessionFactory;
import org.datagear.management.domain.Authorization;
import org.datagear.management.domain.DataIdPermission;
import org.datagear.management.domain.DataPermissionEntity;
import org.datagear.management.domain.Entity;
import org.datagear.management.domain.User;
import org.datagear.management.service.AnalysisProjectAwareEntityService;
import org.datagear.management.service.AuthorizationService;
import org.datagear.management.service.DataPermissionEntityService;
import org.datagear.management.service.PermissionDeniedException;
import org.datagear.management.util.DataPermissionSpec;
import org.datagear.management.util.dialect.MbSqlDialect;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.PagingQuery;
import org.datagear.persistence.Query;
import org.datagear.util.StringUtil;
import org.datagear.util.cache.CollectionCacheValue;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;

/**
 * 抽象基于Mybatis的{@linkplain DataPermissionEntityService}实现类。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractMybatisDataPermissionEntityService<ID, T extends DataPermissionEntity & Entity<ID>>
		extends AbstractMybatisEntityService<ID, T> implements DataPermissionEntityService<ID, T>
{
	private AuthorizationService authorizationService;

	private Cache permissionCache = null;

	/**
	 * 查询操作时缓存权限的记录数目。
	 * <p>
	 * 默认不开启了，影响查询性能，而且缓存价值也不大。
	 * </p>
	 */
	private int permissionCacheCountForQuery = 0;

	/**
	 * 每条记录权限缓存存储的最多用户权限数。
	 */
	private int permissionCacheMaxLength = 10;

	private DataPermissionSpec dataPermissionSpec = new DataPermissionSpec();

	public AbstractMybatisDataPermissionEntityService()
	{
		super();
	}

	public AbstractMybatisDataPermissionEntityService(SqlSessionFactory sqlSessionFactory, MbSqlDialect dialect,
			AuthorizationService authorizationService)
	{
		super(sqlSessionFactory, dialect);
		this.authorizationService = authorizationService;
	}

	public AbstractMybatisDataPermissionEntityService(SqlSessionTemplate sqlSessionTemplate, MbSqlDialect dialect,
			AuthorizationService authorizationService)
	{
		super(sqlSessionTemplate, dialect);
		this.authorizationService = authorizationService;
	}

	public AuthorizationService getAuthorizationService()
	{
		return authorizationService;
	}

	public void setAuthorizationService(AuthorizationService authorizationService)
	{
		this.authorizationService = authorizationService;
	}

	public Cache getPermissionCache()
	{
		return permissionCache;
	}

	public void setPermissionCache(Cache permissionCache)
	{
		this.permissionCache = permissionCache;
	}

	public int getPermissionCacheCountForQuery()
	{
		return permissionCacheCountForQuery;
	}

	public void setPermissionCacheCountForQuery(int permissionCacheCountForQuery)
	{
		this.permissionCacheCountForQuery = permissionCacheCountForQuery;
	}

	public int getPermissionCacheMaxLength()
	{
		return permissionCacheMaxLength;
	}

	public void setPermissionCacheMaxLength(int permissionCacheMaxLength)
	{
		this.permissionCacheMaxLength = permissionCacheMaxLength;
	}

	public DataPermissionSpec getDataPermissionSpec()
	{
		return dataPermissionSpec;
	}

	public void setDataPermissionSpec(DataPermissionSpec dataPermissionSpec)
	{
		this.dataPermissionSpec = dataPermissionSpec;
	}

	@Override
	public int getPermission(User user, ID id)
	{
		List<ID> ids = new ArrayList<>(1);
		ids.add(id);

		List<Integer> permissions = getPermissions(user, ids);

		return permissions.get(0);
	}

	@Override
	public int[] getPermissions(User user, ID[] ids)
	{
		List<ID> idList = Arrays.asList(ids);

		List<Integer> permissions = getPermissions(user, idList);

		int[] re = new int[permissions.size()];

		for (int i = 0; i < re.length; i++)
			re[i] = permissions.get(i);

		return re;
	}

	@Override
	public void add(User user, T entity) throws PermissionDeniedException
	{
		super.add(entity);
	}

	@Override
	public boolean update(User user, T entity) throws PermissionDeniedException
	{
		checkUpdatePermission(user, entity.getId());
		return super.update(entity);
	}

	protected int checkUpdatePermission(User user, ID id) throws PermissionDeniedException
	{
		int permission = getPermission(user, id);

		if (!Authorization.canEdit(permission))
			throw new PermissionDeniedException();

		return permission;
	}

	@Override
	public boolean deleteById(User user, ID id) throws PermissionDeniedException
	{
		checkDeletePermission(user, id);
		return super.deleteById(id);
	}

	protected int checkDeletePermission(User user, ID id) throws PermissionDeniedException
	{
		int permission = getPermission(user, id);

		if (!Authorization.canDelete(permission))
			throw new PermissionDeniedException();

		return permission;
	}

	@Override
	public boolean[] deleteByIds(User user, ID[] ids) throws PermissionDeniedException
	{
		int[] permissions = getPermissions(user, ids);

		for (int i = 0; i < permissions.length; i++)
		{
			if (!Authorization.canDelete(permissions[i]))
				throw new PermissionDeniedException();
		}

		boolean[] re = new boolean[ids.length];

		for (int i = 0; i < ids.length; i++)
			re[i] = super.deleteById(ids[i]);

		return re;
	}

	@Override
	public T getById(User user, ID id) throws PermissionDeniedException
	{
		int permission = getPermission(user, id);

		if (PERMISSION_NOT_FOUND == permission)
			return null;

		if (!Authorization.canRead(permission))
			throw new PermissionDeniedException();

		T entity = getById(id);

		if (entity != null)
			entity.setDataPermission(permission);

		return entity;
	}

	@Override
	public T getByIdForEdit(User user, ID id) throws PermissionDeniedException
	{
		int permission = getPermission(user, id);

		if (PERMISSION_NOT_FOUND == permission)
			return null;

		if (!Authorization.canEdit(permission))
			throw new PermissionDeniedException();

		T entity = getById(id);

		if (entity != null)
			entity.setDataPermission(permission);

		return entity;
	}

	@Override
	public List<T> query(User user, Query query)
	{
		Map<String, Object> params = buildParamMap();
		addDataPermissionParameters(params, user);

		return query(query, params, true);
	}

	@Override
	public PagingData<T> pagingQuery(User user, PagingQuery pagingQuery)
	{
		return pagingQuery(user, pagingQuery, null);
	}

	@Override
	public PagingData<T> pagingQuery(User user, PagingQuery pagingQuery, String dataFilter)
	{
		Map<String, Object> params = buildParamMap();
		addDataPermissionParameters(params, user);
		setDataFilterParam(params, dataFilter);

		return pagingQuery(pagingQuery, params, true);
	}

	@Override
	public void authorizationUpdated(String resourceType, String... resources)
	{
		authorizationUpdatedInner(resourceType, resources);
	}

	protected boolean authorizationUpdatedInner(String resourceType, String... resources)
	{
		if (!getResourceType().equals(resourceType))
			return false;

		if (!isPermissionCacheEnabled())
			return false;

		for (String resource : resources)
			this.permissionCache.evict(toPermissionCacheKeyOfStr(resource));

		return true;
	}

	@Override
	public void permissionUpdated()
	{
		permissionCacheInvalidate();
	}

	protected void setDataFilterParam(Map<String, Object> params, String dataFilter)
	{
		this.dataPermissionSpec.setFilterParam(params, dataFilter);
	}

	protected PagingData<T> pagingQueryForAnalysisProjectId(User user, PagingQuery pagingQuery, String dataFilter,
			String analysisProjectId, boolean postProcessQuery)
	{
		Map<String, Object> params = buildParamMap();
		addDataPermissionParameters(params, user);
		setDataFilterParam(params, dataFilter);

		if (!StringUtil.isEmpty(analysisProjectId))
			params.put(AnalysisProjectAwareEntityService.QUERY_PARAM_ANALYSIS_PROJECT_ID, analysisProjectId);

		return pagingQuery(pagingQuery, params, postProcessQuery);
	}

	@Override
	protected boolean deleteById(ID id, Map<String, Object> params)
	{
		boolean deleted = super.deleteById(id, params);

		if (deleted)
			this.authorizationService.deleteByResource(getResourceType(), id.toString());

		return deleted;
	}

	@Override
	protected int updateCreateUserId(String[] oldUserIds, String newUserId)
	{
		int count = super.updateCreateUserId(oldUserIds, newUserId);

		if (count > 0)
			permissionCacheInvalidate();

		return count;
	}

	@Override
	protected List<T> query(String statement, Map<String, Object> params)
	{
		List<T> list = super.query(statement, params);

		permissionCachePutQueryResult(statement, params, list);

		return list;
	}

	@Override
	protected List<T> query(String statement, Map<String, Object> params, RowBounds rowBounds)
	{
		List<T> list = super.query(statement, params, rowBounds);

		permissionCachePutQueryResult(statement, params, list);

		return list;
	}

	/**
	 * 获取权限列表。
	 * <p>
	 * 如果指定ID的记录不存在，对应权限值将返回{@linkplain #PERMISSION_NOT_FOUND}权限值。
	 * </p>
	 * 
	 * @param user
	 * @param ids
	 * @return
	 */
	protected List<Integer> getPermissions(User user, List<ID> ids)
	{
		int len = ids.size();

		Map<ID, Integer> permissions = getPermissionsFromCache(user, ids);

		List<ID> noCachedIds = null;

		if (permissions.isEmpty())
			noCachedIds = ids;
		else
		{
			for (int i = 0; i < len; i++)
			{
				ID id = ids.get(i);
				Integer permission = permissions.get(id);

				if (permission == null)
				{
					if (noCachedIds == null)
						noCachedIds = new ArrayList<ID>(len);

					noCachedIds.add(id);
				}
			}
		}

		if (noCachedIds != null)
			getPermissionsFromDB(user, noCachedIds, permissions, true);

		List<Integer> re = new ArrayList<>(len);

		for (int i = 0; i < len; i++)
		{
			ID id = ids.get(i);
			Integer permission = permissions.get(id);

			if (permission == null)
				permission = PERMISSION_NOT_FOUND;

			re.add(permission);
		}

		return re;
	}

	/**
	 * 获取缓存中的权限。
	 * 
	 * @param user
	 * @param ids
	 * @return
	 */
	protected Map<ID, Integer> getPermissionsFromCache(User user, List<ID> ids)
	{
		Map<ID, Integer> permissions = new HashMap<ID, Integer>();

		if (!isPermissionCacheEnabled())
			return permissions;

		String userId = user.getId();

		for (int i = 0, len = ids.size(); i < len; i++)
		{
			ID id = ids.get(i);
			Integer permission = permissionCacheGet(id, userId);

			if (permission != null)
				permissions.put(id, permission);
		}

		return permissions;
	}

	/**
	 * 获取底层数据库的权限。
	 * 
	 * @param user
	 * @param ids
	 * @param permissions
	 * @param cache
	 */
	protected void getPermissionsFromDB(User user, List<ID> ids, Map<ID, Integer> permissions, boolean cache)
	{
		if(ids.isEmpty())
			return;
		
		String userId = user.getId();

		Map<String, Object> params = buildParamMap();
		addDataPermissionParameters(params, user);
		params.put("ids", ids);

		List<DataIdPermission> dataPermissions = selectListMybatis("getDataIdPermissions", params);

		for (int i = 0, len = ids.size(); i < len; i++)
		{
			Integer permission = null;
			ID id = ids.get(i);
			String idStr = id.toString();

			for (DataIdPermission p : dataPermissions)
			{
				if (idStr.equals(p.getDataId()))
				{
					permission = p.getDataPermission();
					break;
				}
			}

			if (permission == null)
				permission = PERMISSION_NOT_FOUND;
			
			permissions.put(id, permission);

			// PERMISSION_NOT_FOUND权限不应加入缓存，因为可能在缓存之后插入了相同id的记录，导致缓存错误
			if (cache && permission.intValue() != PERMISSION_NOT_FOUND)
				permissionCachePut(id, userId, permission);
		}
	}

	protected Integer permissionCacheGet(ID id, String userId)
	{
		if (!isPermissionCacheEnabled())
			return null;

		ValueWrapper valueWrapper = this.permissionCache.get(toPermissionCacheKey(id));
		UserIdPermissionCacheValue upcv = (valueWrapper == null ? null
				: (UserIdPermissionCacheValue) valueWrapper.get());

		return (upcv == null ? null : upcv.getPermission(userId));
	}

	protected void permissionCachePut(ID id, String userId, int permission)
	{
		if (!isPermissionCacheEnabled())
			return;

		Object key = toPermissionCacheKey(id);

		ValueWrapper valueWrapper = this.permissionCache.get(key);
		UserIdPermissionCacheValue upcv = (valueWrapper == null ? null
				: (UserIdPermissionCacheValue) valueWrapper.get());

		if (upcv == null)
			upcv = new UserIdPermissionCacheValue();

		upcv.add(new UserIdPermission(userId, permission), this.permissionCacheMaxLength);

		// 注意：无论upcv之前是否存在于缓存，这里都应再次执行存入缓存操作
		this.permissionCache.put(key, upcv);
	}

	protected void permissionCachePutQueryResult(String statement, Map<String, Object> params, List<T> result)
	{
		if (params == null || result == null || this.permissionCacheCountForQuery <= 0)
			return;

		if (!isPermissionCacheEnabled())
			return;

		User user = this.dataPermissionSpec.getParamCurrentUser(params);

		if (user == null)
			return;

		String userId = user.getId();

		int count = 0;

		for (T t : result)
		{
			if (count >= this.permissionCacheCountForQuery)
				break;

			ID id = (t == null ? null : t.getId());

			if (id == null)
				continue;

			int permission = t.getDataPermission();

			if (Authorization.isLegalPermission(permission))
			{
				permissionCachePut(id, userId, permission);
				count++;
			}
		}
	}

	protected void permissionCacheInvalidate()
	{
		if (!isPermissionCacheEnabled())
			return;

		this.permissionCache.invalidate();
	}

	/**
	 * 获取指定实体ID的权限缓存关键字。
	 * <p>
	 * 调用此方法前应确保{@linkplain #isPermissionCacheEnabled()}为{@code true}。
	 * </p>
	 * 
	 * @param id
	 *            实体ID，允许{@code null}
	 * @return
	 */
	protected Object toPermissionCacheKey(ID id)
	{
		String idStr = (id == null ? "" : id.toString());
		return toPermissionCacheKeyOfStr(idStr);
	}

	/**
	 * 获取指定实体ID的权限缓存关键字。
	 * <p>
	 * 调用此方法前应确保{@linkplain #isPermissionCacheEnabled()}为{@code true}。
	 * </p>
	 * 
	 * @param id
	 *            实体ID
	 * @return
	 */
	protected Object toPermissionCacheKeyOfStr(String id)
	{
		return new GlobalPermissionCacheKey<String>(getSqlNamespace(), id);
	}

	/**
	 * 是否启用了权限缓存。
	 * 
	 * @return
	 */
	protected boolean isPermissionCacheEnabled()
	{
		return (this.permissionCache != null);
	}

	/**
	 * 添加数据权限SQL参数。
	 * <p>
	 * 子类可以重写此方法重新设置数据权限SQL参数。
	 * </p>
	 * <p>
	 * 此方法默认实现是：调用{@linkplain #addDataPermissionParameters(Map, User, String, boolean)}，
	 * 其中，{@code resourceType}为{@linkplain #getResourceType()}、{@code resourceHasCreator}为{@code true}。
	 * </p>
	 * 
	 * @param params
	 * @param user
	 */
	protected void addDataPermissionParameters(Map<String, Object> params, User user)
	{
		addDataPermissionParameters(params, user, getResourceType(), true);
	}

	/**
	 * 添加数据权限SQL参数。
	 * 
	 * @param params
	 * @param user
	 * @param resourceType
	 * @param resourceHasCreator
	 */
	protected void addDataPermissionParameters(Map<String, Object> params, User user, String resourceType,
			boolean resourceHasCreator)
	{
		this.dataPermissionSpec.setParams(params, user, resourceType, resourceHasCreator);
	}

	/**
	 * 全局权限缓存KEY。
	 * 
	 * @author datagear@163.com
	 * 
	 * @param <ID>
	 */
	public static class GlobalPermissionCacheKey<ID> extends GlobalEntityCacheKey<ID>
	{
		private static final long serialVersionUID = 1L;

		public GlobalPermissionCacheKey(String namespace, ID id)
		{
			super(namespace, id);
		}

		@Override
		public int hashCode()
		{
			return super.hashCode();
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			return true;
		}

		@Override
		public String toString()
		{
			return getClass().getSimpleName() + " [namespace=" + getNamespace() + ", id=" + getId() + "]";
		}
	}

	/**
	 * 用户权限集缓存值。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class UserIdPermissionCacheValue extends CollectionCacheValue<UserIdPermission>
	{
		private static final long serialVersionUID = 1L;

		public UserIdPermissionCacheValue()
		{
			super();
		}

		public Integer getPermission(String userId)
		{
			UserIdPermission up = find(t ->
			{
				return StringUtil.isEquals(userId, t.getUserId());
			});

			return (up == null ? null : up.getPermission());
		}

		/**
		 * 添加。
		 * <p>
		 * 注意：执行此操作后应执行存入缓存操作。
		 * </p>
		 * 
		 * @param up
		 * @param maxSize
		 */
		public void add(UserIdPermission up, int maxSize)
		{
			add(up, (t) ->
			{
				return StringUtil.isEquals(up.getUserId(), t.getUserId());
			}, maxSize);
		}
	}

	/**
	 * 用户权限。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class UserIdPermission implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private String userId;

		private Integer permission;

		public UserIdPermission()
		{
			super();
		}

		public UserIdPermission(String userId, Integer permission)
		{
			super();
			this.userId = userId;
			this.permission = permission;
		}

		public String getUserId()
		{
			return userId;
		}

		public void setUserId(String userId)
		{
			this.userId = userId;
		}

		public Integer getPermission()
		{
			return permission;
		}

		public void setPermission(Integer permission)
		{
			this.permission = permission;
		}
	}
}
