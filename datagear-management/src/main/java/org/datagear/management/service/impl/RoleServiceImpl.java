/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.management.service.impl;

import org.apache.ibatis.session.SqlSessionFactory;
import org.datagear.management.domain.Role;
import org.datagear.management.service.RoleService;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * {@linkplain RoleService}实现类。
 * 
 * @author datagear@163.com
 *
 */
public class RoleServiceImpl extends AbstractMybatisEntityService<String, Role> implements RoleService
{
	protected static final String SQL_NAMESPACE = Role.class.getName();

	public RoleServiceImpl()
	{
		super();
	}

	public RoleServiceImpl(SqlSessionFactory sqlSessionFactory)
	{
		super(sqlSessionFactory);
	}

	public RoleServiceImpl(SqlSessionTemplate sqlSessionTemplate)
	{
		super(sqlSessionTemplate);
	}

	@Override
	protected String getSqlNamespace()
	{
		return SQL_NAMESPACE;
	}
}
