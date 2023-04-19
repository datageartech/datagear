/*
 * Copyright 2018-2023 datagear.tech
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

package org.datagear.management.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.datagear.management.domain.Role;

/**
 * 角色规范。
 * <p>
 * 定义系统内置角色。
 * </p>
 * <p>
 * 内置角色会在系统执行{@code org/datagear/management/ddl/datagear.sql}时写入系统数据库中。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class RoleSpec
{
	/**
	 * 内置角色前缀。
	 */
	protected static final String BUILTIN_ROLE_PREFIX = "ROLE_";

	/**
	 * 角色ID：{@code ROLE_REGISTRY} 注册用户。
	 * <p>
	 * 系统新添加和注册的用户都会关联此角色，便于用户针对注册用户授权。
	 * </p>
	 */
	public static final String ROLE_REGISTRY = BUILTIN_ROLE_PREFIX + "REGISTRY";

	/**
	 * 角色ID：{@code ROLE_DATA_ADMIN} 数据管理员。
	 * <p>
	 * 可以管理（添加、编辑、查看、删除）数据源、数据集、图表、看板。
	 * </p>
	 */
	public static final String ROLE_DATA_MANAGER = BUILTIN_ROLE_PREFIX + "DATA_ADMIN";

	/**
	 * 角色ID：{@code ROLE_DATA_ANALYST} 数据分析员。
	 * <p>
	 * 仅可查看数据源、数据集、图表、看板，展示图表和看板。
	 * </p>
	 */
	public static final String ROLE_DATA_ANALYST = BUILTIN_ROLE_PREFIX + "DATA_ANALYST";

	private final Set<String> builtinRoleIds = new HashSet<String>();

	public RoleSpec()
	{
		super();
		addBuiltinRoleId(ROLE_REGISTRY);
		addBuiltinRoleId(ROLE_DATA_MANAGER);
		addBuiltinRoleId(ROLE_DATA_ANALYST);
	}

	/**
	 * 获取所有内置角色ID。
	 * 
	 * @return
	 */
	public Set<String> getBuiltinRoleIds()
	{
		return Collections.unmodifiableSet(this.builtinRoleIds);
	}

	/**
	 * 添加内置角色ID。
	 * 
	 * @param roleId
	 */
	protected void addBuiltinRoleId(String roleId)
	{
		this.builtinRoleIds.add(roleId);
	}

	/**
	 * 给定角色ID是否是内置角色。
	 * 
	 * @param roleId
	 * @return
	 */
	public boolean isBuiltinRole(String roleId)
	{
		return roleId != null && this.builtinRoleIds.contains(roleId);
	}

	/**
	 * 给定角色ID是否是内置角色。
	 * 
	 * @param roleId
	 * @return
	 */
	public boolean isBuiltinRole(Role role)
	{
		String roleId = (role == null ? null : role.getId());
		return isBuiltinRole(roleId);
	}
}
