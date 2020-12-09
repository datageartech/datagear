/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.management.service.impl;

import java.util.Map;

import org.apache.ibatis.session.SqlSessionFactory;
import org.datagear.management.domain.Role;
import org.datagear.management.service.DeleteBuiltinRoleDeniedException;
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
	protected boolean deleteById(String id, Map<String, Object> params)
	{
		if (Role.isBuiltinRole(id))
			throw new DeleteBuiltinRoleDeniedException(id);

		return super.deleteById(id, params);
	}

	@Override
	protected String getSqlNamespace()
	{
		return SQL_NAMESPACE;
	}
}
