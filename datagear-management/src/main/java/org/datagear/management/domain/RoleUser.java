/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.domain;

import org.springframework.beans.BeanUtils;

/**
 * 角色-用户。
 * 
 * @author datagear@163.com
 *
 */
public class RoleUser extends AbstractStringIdEntity implements CloneableEntity
{
	private static final long serialVersionUID = 1L;

	/** 角色 */
	private Role role;

	/** 用户 */
	private User user;

	public RoleUser()
	{
		super();
	}

	public RoleUser(String id, Role role, User user)
	{
		super(id);
		this.role = role;
		this.user = user;
	}

	public Role getRole()
	{
		return role;
	}

	public void setRole(Role role)
	{
		this.role = role;
	}

	public User getUser()
	{
		return user;
	}

	public void setUser(User user)
	{
		this.user = user;
	}

	@Override
	public RoleUser clone()
	{
		RoleUser entity = new RoleUser();
		BeanUtils.copyProperties(this, entity);

		return entity;
	}
}
