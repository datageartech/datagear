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
