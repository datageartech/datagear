/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
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
import org.datagear.management.domain.User;
import org.datagear.management.service.AuthorizationService;
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

	public AuthorizationServiceImpl()
	{
		super();
	}

	public AuthorizationServiceImpl(SqlSessionFactory sqlSessionFactory)
	{
		super(sqlSessionFactory);
	}

	public AuthorizationServiceImpl(SqlSessionTemplate sqlSessionTemplate)
	{
		super(sqlSessionTemplate);
	}

	@Override
	public boolean add(User user, Authorization entity) throws PermissionDeniedException
	{
		// 只有管理员才可以模式匹配授权
		if (!user.isAdmin() && entity.isResourceTypePattern())
			throw new PermissionDeniedException();

		// TODO 校验用户是否对此资源有授权的权限

		return super.add(user, entity);
	}

	@Override
	public boolean update(User user, Authorization entity) throws PermissionDeniedException
	{
		// 只有管理员才可以模式匹配授权
		if (!user.isAdmin() && entity.isResourceTypePattern())
			throw new PermissionDeniedException();

		// TODO 校验用户是否对此资源有授权的权限

		return super.update(user, entity);
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
	public List<Authorization> queryForAppointResource(User user, String appointResource, Query query)
	{
		Map<String, Object> params = buildParamMap();
		addDataPermissionParameters(params, user);
		params.put("appointResource", appointResource);

		return query(query, params);
	}

	@Override
	protected Authorization getById(String id, Map<String, Object> params)
	{
		setAuthorizationQueryContext(params);

		return super.getById(id, params);
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
		addDataPermissionParameters(params, user, Authorization.RESOURCE_TYPE_AUTHORIZATION, false, true);
	}

	@Override
	protected String getSqlNamespace()
	{
		return SQL_NAMESPACE;
	}
}
