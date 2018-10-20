/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.security;

import org.datagear.management.domain.User;
import org.datagear.management.service.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * {@linkplain UserDetailsService}实现类。
 * 
 * @author datagear@163.com
 *
 */
public class UserDetailsServiceImpl implements UserDetailsService
{
	private UserService userService;

	public UserDetailsServiceImpl()
	{
		super();
	}

	public UserDetailsServiceImpl(UserService userService)
	{
		super();
		this.userService = userService;
	}

	public UserService getUserService()
	{
		return userService;
	}

	public void setUserService(UserService userService)
	{
		this.userService = userService;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
	{
		User user = this.userService.getByName(username);

		if (user == null)
			throw new UsernameNotFoundException("user name [" + username + "] not found");

		return new AuthUser(user);
	}
}
