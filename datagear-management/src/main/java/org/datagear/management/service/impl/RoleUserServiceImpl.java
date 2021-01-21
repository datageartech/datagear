/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSessionFactory;
import org.datagear.management.domain.Role;
import org.datagear.management.domain.RoleUser;
import org.datagear.management.domain.User;
import org.datagear.management.service.RoleUserService;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.PagingQuery;
import org.datagear.persistence.Query;
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
	public List<RoleUser> queryForRole(Role role, Query query)
	{
		Map<String, Object> param = buildParamMap();
		param.put("role", role);

		return query("queryForRole", query, param);
	}

	@Override
	public PagingData<RoleUser> pagingQueryForRole(Role role, PagingQuery query)
	{
		Map<String, Object> param = buildParamMap();
		param.put("role", role);

		return pagingQuery("pagingQueryForRole", query, param);
	}

	@Override
	protected String getSqlNamespace()
	{
		return SQL_NAMESPACE;
	}
}
