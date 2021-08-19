/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSessionFactory;
import org.datagear.management.domain.Role;
import org.datagear.management.service.DeleteBuiltinRoleDeniedException;
import org.datagear.management.service.RoleService;
import org.datagear.management.util.dialect.MbSqlDialect;
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

	public RoleServiceImpl(SqlSessionFactory sqlSessionFactory, MbSqlDialect dialect)
	{
		super(sqlSessionFactory, dialect);
	}

	public RoleServiceImpl(SqlSessionTemplate sqlSessionTemplate, MbSqlDialect dialect)
	{
		super(sqlSessionTemplate, dialect);
	}

	@Override
	public List<Role> getByIds(String... ids)
	{
		List<Role> roles = new ArrayList<>(ids.length);

		for (String id : ids)
			roles.add(getById(id));

		return roles;
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
