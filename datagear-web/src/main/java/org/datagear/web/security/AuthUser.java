/*
 * Copyright 2018-present datagear.tech
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
import java.util.Collections;
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

	public static final GrantedAuthority GA_ANONYMOUS = new SimpleGrantedAuthority(ROLE_ANONYMOUS);

	public static final GrantedAuthority GA_USER = new SimpleGrantedAuthority(ROLE_USER);

	public static final GrantedAuthority GA_ADMIN = new SimpleGrantedAuthority(ROLE_ADMIN);

	private static final long serialVersionUID = 1L;

	private User user;

	private Set<GrantedAuthority> authorities = Collections.emptySet();

	private boolean accountNonExpired = true;

	private boolean accountNonLocked = true;

	private boolean credentialsNonExpired = true;

	private boolean enabled = true;

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
			this.authorities.add(GA_ANONYMOUS);
		}
		else
		{
			this.authorities.add(GA_USER);

			if (user.isAdmin())
				this.authorities.add(GA_ADMIN);
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

	public void setUser(User user)
	{
		this.user = user;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities()
	{
		return this.authorities;
	}

	public void setAuthorities(Set<GrantedAuthority> authorities)
	{
		this.authorities = authorities;
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
		return this.accountNonExpired;
	}

	public void setAccountNonExpired(boolean accountNonExpired)
	{
		this.accountNonExpired = accountNonExpired;
	}

	@Override
	public boolean isAccountNonLocked()
	{
		return this.accountNonLocked;
	}

	public void setAccountNonLocked(boolean accountNonLocked)
	{
		this.accountNonLocked = accountNonLocked;
	}

	@Override
	public boolean isCredentialsNonExpired()
	{
		return this.credentialsNonExpired;
	}

	public void setCredentialsNonExpired(boolean credentialsNonExpired)
	{
		this.credentialsNonExpired = credentialsNonExpired;
	}

	@Override
	public boolean isEnabled()
	{
		return this.enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [user=" + user + ", authorities=" + authorities + "]";
	}
}
