/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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
	public static final String ROLE_ADMIN = "ROLE_ADMIN";

	public static final String ROLE_USER = "ROLE_USER";

	public static final String ROLE_ANONYMOUS = "ROLE_ANONYMOUS";

	private static final long serialVersionUID = 1L;

	private final User user;
	private final Set<GrantedAuthority> authorities;

	public AuthUser(User user)
	{
		super();
		this.user = user;

		this.authorities = new HashSet<GrantedAuthority>();

		if (user.isAnonymous())
			this.authorities.add(new SimpleGrantedAuthority(ROLE_ANONYMOUS));
		else
		{
			this.authorities.add(new SimpleGrantedAuthority(ROLE_USER));

			if (user.isAdmin())
				this.authorities.add(new SimpleGrantedAuthority(ROLE_ADMIN));
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
