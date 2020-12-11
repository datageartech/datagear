/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.management.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	public Set<Role> findByUserId(String userId)
	{
		Set<Role> set = new HashSet<>();

		Map<String, Object> params = buildParamMap();
		params.put("userId", userId);

		List<Role> roles = query("findByUserId", params);

		set.addAll(roles);

		return set;
	}

	@Override
	public List<Role> getByIds(String... ids)
	{
		List<Role> roles = new ArrayList<Role>(ids.length);

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
