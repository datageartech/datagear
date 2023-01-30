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

package org.datagear.web.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.datagear.management.domain.Role;
import org.datagear.management.domain.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 认证用户。
 * 
 * @author datagear@163.com
 *
 */
public class AuthUser implements UserDetails
{
	/**
	 * 角色：系统管理员。
	 */
	public static final String ROLE_ADMIN = "ROLE_ADMIN";

	/**
	 * 角色：登录用户。
	 */
	public static final String ROLE_USER = "ROLE_USER";

	/**
	 * 角色：匿名用户。
	 */
	public static final String ROLE_ANONYMOUS = "ROLE_ANONYMOUS";

	private static final long serialVersionUID = 1L;

	private final User user;
	private final Set<GrantedAuthority> authorities;

	public AuthUser(User user)
	{
		super();
		this.user = user;

		this.authorities = new HashSet<>();

		Set<Role> roles = user.getRoles();
		if (roles != null && !roles.isEmpty())
		{
			for (Role role : roles)
			{
				// 未启用的角色不应加入
				if (!role.isEnabled())
					continue;

				this.authorities.add(new SimpleGrantedAuthority(role.getId()));
			}
		}

		if (user.isAnonymous())
		{
			this.authorities.add(new SimpleGrantedAuthority(ROLE_ANONYMOUS));
		}
		else
		{
			this.authorities.add(new SimpleGrantedAuthority(ROLE_USER));
			
			if (user.isAdmin())
			{
				this.authorities.add(new SimpleGrantedAuthority(ROLE_ADMIN));
			}
		}
	}

	/**
	 * 获取用户。
	 * 
	 * @return
	 */
	public User getUser()
	{
		return user;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities()
	{
		return this.authorities;
	}

	@Override
	public String getPassword()
	{
		return user.getPassword();
	}

	@Override
	public String getUsername()
	{
		return user.getName();
	}

	@Override
	public boolean isAccountNonExpired()
	{
		return true;
	}

	@Override
	public boolean isAccountNonLocked()
	{
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired()
	{
		return true;
	}

	@Override
	public boolean isEnabled()
	{
		return true;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [user=" + user + ", authorities=" + authorities + "]";
	}
}
