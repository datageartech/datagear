/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.management.service.impl;

import java.util.Map;

import org.apache.ibatis.session.SqlSessionFactory;
import org.datagear.management.domain.Role;
import org.datagear.management.domain.RoleUser;
import org.datagear.management.domain.User;
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
	public RoleUser getByRoleAndUser(Role role, User user)
	{
		Map<String, Object> paramMap = buildParamMapWithIdentifierQuoteParameter();
		paramMap.put("role", role);
		paramMap.put("user", user);

		return selectOneMybatis("getByRoleAndUser", paramMap);
	}

	@Override
	public boolean exists(Role role, User user)
	{
		return (getByRoleAndUser(role, user) != null);
	}

	@Override
	public boolean[] addIfInexistence(RoleUser... roleUsers)
	{
		int length = roleUsers.length;
		boolean[] re = new boolean[length];

		for (int i = 0; i < roleUsers.length; i++)
		{
			RoleUser roleUser = roleUsers[i];

			if (exists(roleUser.getRole(), roleUser.getUser()))
				re[i] = false;
			else
			{
				add(roleUser);
				re[i] = true;
			}
		}

		return re;
	}

	@Override
	protected String getSqlNamespace()
	{
		return SQL_NAMESPACE;
	}
}
