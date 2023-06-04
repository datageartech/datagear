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

import org.datagear.management.domain.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 从{@linkplain Authentication}中获取{@linkplain User}。
 * 
 * @author datagear@163.com
 *
 */
public class AuthenticationUserGetter
{
	public AuthenticationUserGetter()
	{
		super();
	}

	/**
	 * 获取当前用户（认证用户或者匿名用户）。
	 * <p>
	 * 此方法不会返回{@code null}。
	 * </p>
	 * 
	 * @return
	 */
	public User getUser()
	{
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return getUser(authentication);
	}

	/**
	 * 从{@linkplain Authentication#getPrincipal()}中获取当前用户（认证用户或者匿名用户）。
	 * <p>
	 * 此方法不会返回{@code null}。
	 * </p>
	 * 
	 * @param authentication
	 * @return
	 */
	public User getUser(Authentication authentication)
	{
		User user = null;

		Object principal = authentication.getPrincipal();

		if (principal instanceof User)
		{
			user = (User) principal;
		}
		else if (principal instanceof AuthUser)
		{
			AuthUser ou = (AuthUser) principal;
			user = ou.getUser();
		}
		else if (principal instanceof UserAwarePrincipal)
		{
			UserAwarePrincipal up = (UserAwarePrincipal) principal;
			user = up.getPrincipalUser();
		}

		if (user == null)
			throw new IllegalStateException();

		return user;
	}

	/**
	 * 获取当前{@linkplain Authentication}。
	 * 
	 * @return
	 */
	public Authentication getAuthentication()
	{
		return SecurityContextHolder.getContext().getAuthentication();
	}

	/**
	 * 用户Principal。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static interface UserAwarePrincipal
	{
		/**
		 * 获取{@linkplain User}。
		 * 
		 * @return {@code null}表示无用户信息
		 */
		User getPrincipalUser();
	}
}
