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

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSessionFactory;
import org.datagear.management.domain.Authorization;
import org.datagear.management.domain.DataPermissionEntity;
import org.datagear.management.domain.User;
import org.datagear.management.service.AuthorizationListener;
import org.datagear.management.service.AuthorizationService;
import org.datagear.management.service.DataPermissionEntityService;
import org.datagear.management.util.dialect.MbSqlDialect;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * {@linkplain AuthorizationService}实现类。
 * 
 * @author datagear@163.com
 *
 */
public class AuthorizationServiceImpl extends AbstractMybatisEntityService<String, Authorization>
		implements AuthorizationService, AuthorizationListenerAware
{
	protected static final String SQL_NAMESPACE = Authorization.class.getName();

	private List<? extends DataPermissionEntityService<?, ?>> resourceServices;

	private AuthorizationListener authorizationListener = null;

	public AuthorizationServiceImpl()
	{
		super();
	}

	public AuthorizationServiceImpl(SqlSessionFactory sqlSessionFactory, MbSqlDialect dialect,
			List<? extends DataPermissionEntityService<?, ?>> resourceServices)
	{
		super(sqlSessionFactory, dialect);
		this.resourceServices = resourceServices;
	}

	public AuthorizationServiceImpl(SqlSessionTemplate sqlSessionTemplate, MbSqlDialect dialect,
			List<? extends DataPermissionEntityService<?, ?>> resourceServices)
	{
		super(sqlSessionTemplate, dialect);
		this.resourceServices = resourceServices;
	}

	public List<? extends DataPermissionEntityService<?, ?>> getResourceServices()
	{
		return resourceServices;
	}

	public void setResourceServices(List<? extends DataPermissionEntityService<?, ?>> resourceServices)
	{
		this.resourceServices = resourceServices;
	}

	@Override
	public AuthorizationListener getAuthorizationListener()
	{
		return authorizationListener;
	}

	@Override
	public void setAuthorizationListener(AuthorizationListener authorizationListener)
	{
		this.authorizationListener = authorizationListener;
	}

	@Override
	public void add(Authorization entity)
	{
		super.add(entity);

		authorizationUpdated(entity.getResourceType(), entity.getResource());
	}

	@Override
	public boolean update(Authorization entity)
	{
		boolean updated = super.update(entity);

		if (updated)
			authorizationUpdated(entity.getResourceType(), entity.getResource());

		return updated;
	}

	@Override
	public boolean deleteById(String id)
	{
		Authorization authorization = getById(id);

		boolean deleted = (authorization == null ? false : super.deleteById(authorization.getId()));

		if (deleted)
			authorizationUpdated(authorization.getResourceType(), authorization.getResource());

		return deleted;
	}

	@Override
	public boolean isAllowAuthorization(User user, String resourceType, String resourceId)
	{
		if (isEmpty(resourceId) || isEmpty(resourceType))
			throw new IllegalArgumentException();

		DataPermissionEntityService<?, ?> resourceService = getResourceService(resourceType);

		if (resourceService == null)
			return false;

		DataPermissionEntity<?> resourceEntity = resourceService.getByStringId(user, resourceId);

		if (resourceEntity == null)
			return false;

		return Authorization.canAuthorize(resourceEntity, user);
	}

	@Override
	public int deleteByIds(String resourceType, String resource, String... ids)
	{
		Map<String, Object> params = buildParamMap();
		params.put("resourceType", resourceType);
		params.put("resource", resource);
		params.put("ids", ids);

		int count = updateMybatis("deleteByIdsForResource", params);

		if (count > 0)
			authorizationUpdated(resourceType, resource);

		return count;
	}

	@Override
	public int deleteByResource(String resourceType, String... resources)
	{
		Map<String, Object> params = buildParamMap();
		params.put("resourceType", resourceType);
		params.put("resources", resources);

		int count = updateMybatis("deleteByResource", params);

		if (count > 0)
			authorizationUpdated(resourceType, resources);

		return count;
	}

	@Override
	protected Authorization getByIdFromDB(String id, Map<String, Object> params)
	{
		setAuthorizationQueryContext(params);
		return super.getByIdFromDB(id, params);
	}

	@Override
	protected List<Authorization> query(String statement, Map<String, Object> params)
	{
		setAuthorizationQueryContext(params);
		return super.query(statement, params);
	}

	@Override
	protected List<Authorization> query(String statement, Map<String, Object> params, RowBounds rowBounds)
	{
		setAuthorizationQueryContext(params);
		return super.query(statement, params, rowBounds);
	}

	protected AuthorizationQueryContext setAuthorizationQueryContext(Map<String, Object> params)
	{
		AuthorizationQueryContext context = AuthorizationQueryContext.get();
		params.put("queryContext", context);

		return context;
	}

	protected void authorizationUpdated(String resourceType, String... resources)
	{
		if (this.authorizationListener != null)
			this.authorizationListener.authorizationUpdated(resourceType, resources);
	}

	/**
	 * 获取指定类型的资源服务。
	 * 
	 * @param resourceType
	 * @return 可能为{@code null}
	 */
	protected DataPermissionEntityService<?, ?> getResourceService(String resourceType)
	{
		DataPermissionEntityService<?, ?> resourceService = null;

		if (this.resourceServices != null)
		{
			for (DataPermissionEntityService<?, ?> rs : this.resourceServices)
			{
				if (resourceType.equals(rs.getResourceType()))
				{
					resourceService = rs;
					break;
				}
			}
		}

		return resourceService;
	}

	@Override
	protected String getSqlNamespace()
	{
		return SQL_NAMESPACE;
	}
}
