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

package org.datagear.management.domain;

import org.springframework.beans.BeanUtils;

/**
 * 角色。
 * 
 * @author datagear@163.com
 *
 */
public class Role extends AbstractStringIdEntity implements CloneableEntity
{
	private static final long serialVersionUID = 1L;

	/**
	 * 内置角色前缀。
	 */
	public static final String BUILTIN_ROLE_PREFIX = "ROLE_";

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

	/** 名称 */
	private String name;

	/** 描述 */
	private String description = "";

	/** 是否启用 */
	private boolean enabled = true;

	public Role()
	{
		super();
	}

	public Role(String id, String name)
	{
		super(id);
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	@Override
	public Role clone()
	{
		Role entity = new Role();
		BeanUtils.copyProperties(this, entity);

		return entity;
	}

	/**
	 * 给定角色ID是否是内置角色。
	 * 
	 * @param roleId
	 * @return
	 */
	public static boolean isBuiltinRole(String roleId)
	{
		return roleId != null && roleId.startsWith(BUILTIN_ROLE_PREFIX);
	}
}
