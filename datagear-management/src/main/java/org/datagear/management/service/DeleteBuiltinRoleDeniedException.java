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

package org.datagear.management.service;

import org.datagear.management.domain.Role;

/**
 * 删除内置角色异常。
 * <p>
 * 当{@linkplain RoleService#deleteById(String)}、{@linkplain RoleService#deleteByIds(String[])}
 * 试图删除内置角色时（参考{@linkplain Role#isBuiltinRole(String)}），将抛出此异常。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class DeleteBuiltinRoleDeniedException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	private String roleId;

	public DeleteBuiltinRoleDeniedException(String roleId)
	{
		super("Delete built-in role [" + roleId + "] denied");
		this.roleId = roleId;
	}

	public DeleteBuiltinRoleDeniedException(String roleId, String message)
	{
		super(message);
		this.roleId = roleId;
	}

	public DeleteBuiltinRoleDeniedException(String roleId, Throwable cause)
	{
		super(cause);
		this.roleId = roleId;
	}

	public DeleteBuiltinRoleDeniedException(String roleId, String message, Throwable cause)
	{
		super(message, cause);
		this.roleId = roleId;
	}

	public String getRoleId()
	{
		return roleId;
	}

	protected void setRoleId(String roleId)
	{
		this.roleId = roleId;
	}
}
