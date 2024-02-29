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

package org.datagear.management.domain;

import org.datagear.management.util.RoleSpec;
import org.springframework.beans.BeanUtils;

/**
 * 角色。
 * <p>
 * 系统内置角色参考{@linkplain RoleSpec}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class Role extends AbstractStringIdEntity implements CloneableEntity
{
	private static final long serialVersionUID = 1L;

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
}
