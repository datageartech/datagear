/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.management.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSessionFactory;
import org.datagear.management.domain.Authorization;
import org.datagear.management.domain.User;
import org.datagear.management.service.AuthorizationService;
import org.datagear.management.service.PermissionDeniedException;
import org.datagear.persistence.Query;
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

		return super.add(user, entity);
	}

	@Override
	public boolean update(User user, Authorization entity) throws PermissionDeniedException
	{
		// 只有管理员才可以模式匹配授权
		if (!user.isAdmin() && entity.isResourceTypePattern())
			throw new PermissionDeniedException();

		return super.update(user, entity);
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
		setAuthorizationQueryLabel(params);

		return super.getById(id, params);
	}

	@Override
	protected List<Authorization> query(String statement, Query query, Map<String, Object> params)
	{
		setAuthorizationQueryLabel(params);

		return super.query(statement, query, params);
	}

	protected AuthorizationQueryLabel setAuthorizationQueryLabel(Map<String, Object> params)
	{
		AuthorizationQueryLabel label = ServiceContext.get()
				.getValue(AuthorizationQueryLabel.CUSTOM_QUERY_PARAMETER_NAME);

		if (label == null)
			label = new AuthorizationQueryLabel();

		params.put("querylabel", label);

		return label;
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
