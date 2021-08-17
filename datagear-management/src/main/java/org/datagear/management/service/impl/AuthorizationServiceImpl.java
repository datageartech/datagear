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
import org.datagear.management.domain.Authorization;
import org.datagear.management.domain.DataPermissionEntity;
import org.datagear.management.domain.User;
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
		implements AuthorizationService
{
	protected static final String SQL_NAMESPACE = Authorization.class.getName();

	private List<? extends DataPermissionEntityService<?, ?>> resourceServices;

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
	public boolean add(Authorization entity)
	{
		boolean re = super.add(entity);

		if (re)
			permissionUpdated(entity.getResourceType(), entity.getResource());

		return re;
	}

	@Override
	public boolean deleteById(String id)
	{
		Authorization authorization = getById(id);

		if (authorization != null)
			permissionUpdated(authorization.getResourceType(), authorization.getResource());

		return super.deleteById(id);
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
			permissionUpdated(resourceType, resource);

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
			permissionUpdated(resourceType, resources);

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

	protected void permissionUpdated(String resourceType, String... resources)
	{
		DataPermissionEntityService<?, ?> resourceService = getResourceService(resourceType);

		if (resourceService != null)
			resourceService.permissionUpdated(resources);
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
