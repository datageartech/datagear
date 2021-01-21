/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.datagear.management.domain.Authorization;
import org.datagear.management.domain.DataIdPermission;
import org.datagear.management.domain.DataPermissionEntity;
import org.datagear.management.domain.User;
import org.datagear.management.service.AuthorizationService;
import org.datagear.management.service.DataPermissionEntityService;
import org.datagear.management.service.PermissionDeniedException;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.PagingQuery;
import org.datagear.persistence.Query;
import org.datagear.util.IDUtil;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * {@linkplain AuthorizationService}实现类。
 * 
 * @author datagear@163.com
 *
 */
public class AuthorizationServiceImpl extends AbstractMybatisDataPermissionEntityService<String, Authorization>
		implements AuthorizationService
{
	protected static final String SQL_NAMESPACE = Authorization.class.getName();

	private List<? extends DataPermissionEntityService<?, ?>> resourceServices;

	public AuthorizationServiceImpl()
	{
		super();
	}

	public AuthorizationServiceImpl(SqlSessionFactory sqlSessionFactory,
			List<? extends DataPermissionEntityService<?, ?>> resourceServices)
	{
		super(sqlSessionFactory);
		this.resourceServices = resourceServices;
	}

	public AuthorizationServiceImpl(SqlSessionTemplate sqlSessionTemplate,
			List<? extends DataPermissionEntityService<?, ?>> resourceServices)
	{
		super(sqlSessionTemplate);
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
	public String getResourceType()
	{
		return Authorization.AUTHORIZATION_RESOURCE_TYPE;
	}

	@Override
	public boolean add(User user, Authorization entity) throws PermissionDeniedException
	{
		checkCanSaveAuthorization(user, entity);
		return super.add(user, entity);
	}

	@Override
	public boolean update(User user, Authorization entity) throws PermissionDeniedException
	{
		checkCanSaveAuthorization(user, entity);
		return super.update(user, entity);
	}

	@Override
	public Authorization getByStringId(User user, String id) throws PermissionDeniedException
	{
		return super.getById(user, id);
	}

	@Override
	public int deleteByResource(String resourceType, String... resources)
	{
		Map<String, Object> params = buildParamMapWithIdentifierQuoteParameter();
		params.put("resourceType", resourceType);
		params.put("resources", resources);

		return updateMybatis("deleteByResource", params);
	}

	@Override
	public Integer getPermissionForPatternSource(User user, String resourceType, String patternSource)
	{
		if (user.isAdmin())
			return Authorization.PERMISSION_MAX;

		int unsetPermission = -9;

		Map<String, Object> params = buildParamMap();
		addDataPermissionParameters(params, user, resourceType, true, false);
		params.put(DATA_PERMISSION_PARAM_UNSET_PERMISSION, unsetPermission);

		params.put("placeholderId", IDUtil.uuid());
		params.put("patternSource", escapeForSqlStringValue(patternSource));

		List<DataIdPermission> dataIdPermissions = selectListMybatis("getDataIdPermissionForPatternSource", params);

		DataIdPermission dataIdPermission = (dataIdPermissions == null || dataIdPermissions.isEmpty() ? null
				: dataIdPermissions.get(0));

		return (dataIdPermission == null || dataIdPermission.getDataPermission() == unsetPermission ? null
				: dataIdPermission.getDataPermission());
	}

	@Override
	public List<Authorization> queryForAssignedResource(User user, String assignedResource, Query query)
	{
		Map<String, Object> params = buildParamMap();
		addDataPermissionParameters(params, user);
		params.put("assignedResource", assignedResource);

		return query(query, params);
	}

	@Override
	protected Authorization getById(String id, Map<String, Object> params, boolean postProcessSelect)
	{
		setAuthorizationQueryContext(params);

		return super.getById(id, params, postProcessSelect);
	}

	@Override
	protected List<Authorization> query(String statement, Query query, Map<String, Object> params)
	{
		setAuthorizationQueryContext(params);

		return super.query(statement, query, params);
	}

	@Override
	protected PagingData<Authorization> pagingQuery(String statement, PagingQuery pagingQuery,
			Map<String, Object> params)
	{
		setAuthorizationQueryContext(params);

		return super.pagingQuery(statement, pagingQuery, params);
	}

	/**
	 * 检查用户是否可以保存授权。
	 * 
	 * @param user
	 * @param authorization
	 */
	protected void checkCanSaveAuthorization(User user, Authorization authorization)
	{
		if (user.isAdmin())
			return;

		// 只有管理员才可以模式匹配授权
		if (authorization.isResourceTypePattern())
			throw new PermissionDeniedException();

		// 检查用户是否有对应资源的授权权限

		String resourceId = authorization.getResource();
		String resourceType = authorization.getResourceType();

		if (isEmpty(resourceId) || isEmpty(resourceType))
			throw new IllegalArgumentException();

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

		if (resourceService == null)
			throw new PermissionDeniedException();

		DataPermissionEntity<?> resourceEntity = resourceService.getByStringId(user, resourceId);

		if (resourceEntity == null)
			throw new PermissionDeniedException();

		if (!Authorization.canAuthorize(resourceEntity, user))
			throw new PermissionDeniedException();
	}

	protected AuthorizationQueryContext setAuthorizationQueryContext(Map<String, Object> params)
	{
		AuthorizationQueryContext context = AuthorizationQueryContext.get();

		params.put("queryContext", context);

		// 针对特定资源的查询
		if (context.hasResourceType())
		{
			params.put("resourceType", context.getResourceType());

			try
			{
				String sqlId = Authorization.class.getName() + ".resourceNameQueryView." + context.getResourceType();
				Configuration configuration = getSqlSession().getConfiguration();
				MappedStatement mappedStatement = configuration.getMappedStatement(sqlId);

				if (mappedStatement != null)
				{
					SqlSource sqlSource = mappedStatement.getSqlSource();
					BoundSql boundSql = sqlSource.getBoundSql(new Object());
					String resourceQueryView = boundSql.getSql();

					params.put("resourceNameQueryView", resourceQueryView);
				}
			}
			catch (Throwable t)
			{
			}
		}

		return context;
	}

	@Override
	protected void addDataPermissionParameters(Map<String, Object> params, User user)
	{
		addDataPermissionParameters(params, user, getResourceType(), false, true);
	}

	@Override
	protected String getSqlNamespace()
	{
		return SQL_NAMESPACE;
	}
}
