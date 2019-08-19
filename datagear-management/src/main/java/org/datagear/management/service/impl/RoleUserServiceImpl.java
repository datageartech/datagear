/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.management.service.impl;

import org.apache.ibatis.session.SqlSessionFactory;
import org.datagear.management.domain.RoleUser;
import org.datagear.management.service.RoleUserService;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * {@linkplain RoleUserService}实现类。
 * 
 * @author datagear@163.com
 *
 */
public class RoleUserServiceImpl extends AbstractMybatisEntityService<String, RoleUser> implements RoleUserService
{
	protected static final String SQL_NAMESPACE = RoleUser.class.getName();

	public RoleUserServiceImpl()
	{
		super();
	}

	public RoleUserServiceImpl(SqlSessionFactory sqlSessionFactory)
	{
		super(sqlSessionFactory);
	}

	public RoleUserServiceImpl(SqlSessionTemplate sqlSessionTemplate)
	{
		super(sqlSessionTemplate);
	}

	@Override
	protected String getSqlNamespace()
	{
		return SQL_NAMESPACE;
	}
}
