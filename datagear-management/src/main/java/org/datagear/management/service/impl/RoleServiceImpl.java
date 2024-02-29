/*
 * Copyright 2018-2024 datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package org.datagear.management.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSessionFactory;
import org.datagear.management.domain.Role;
import org.datagear.management.service.AuthorizationListener;
import org.datagear.management.service.DeleteBuiltinRoleDeniedException;
import org.datagear.management.service.RoleService;
import org.datagear.management.util.RoleSpec;
import org.datagear.management.util.dialect.MbSqlDialect;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * {@linkplain RoleService}实现类。
 * 
 * @author datagear@163.com
 *
 */
public class RoleServiceImpl extends AbstractMybatisEntityService<String, Role>
		implements RoleService, AuthorizationListenerAware
{
	protected static final String SQL_NAMESPACE = Role.class.getName();

	private RoleSpec roleSpec = new RoleSpec();

	private AuthorizationListener authorizationListener = null;

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

	public RoleSpec getRoleSpec()
	{
		return roleSpec;
	}

	public void setRoleSpec(RoleSpec roleSpec)
	{
		this.roleSpec = roleSpec;
	}

	@Override
	public AuthorizationListener getAuthorizationListener()
	{
		return authorizationListener;
	}

	@Override
	public void setAuthorizationListener(AuthorizationListener authorizationListener)
	{
		this.authorizationListener = authorizationListener;
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
	protected boolean update(Role entity, Map<String, Object> params)
	{
		boolean updated = super.update(entity, params);

		if (updated && this.authorizationListener != null)
			this.authorizationListener.permissionUpdated();

		return updated;
	}

	@Override
	protected boolean deleteById(String id, Map<String, Object> params)
	{
		if (this.roleSpec.isBuiltinRole(id))
			throw new DeleteBuiltinRoleDeniedException(id);

		boolean deleted = super.deleteById(id, params);

		if (deleted && this.authorizationListener != null)
			this.authorizationListener.permissionUpdated();

		return deleted;
	}

	@Override
	protected String getSqlNamespace()
	{
		return SQL_NAMESPACE;
	}
}
