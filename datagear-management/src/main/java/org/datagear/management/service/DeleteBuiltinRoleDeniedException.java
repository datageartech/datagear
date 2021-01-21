/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
