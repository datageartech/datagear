/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.management.service.impl;

import org.apache.ibatis.session.SqlSessionFactory;
import org.datagear.management.domain.Authorization;
import org.datagear.management.service.AuthorizationService;
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
	protected String getSqlNamespace()
	{
		return SQL_NAMESPACE;
	}
}
