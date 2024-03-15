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
	 * @throws NullPointerException
	 *             当未获取到时抛出此异常
	 */
	public User getUser() throws NullPointerException
	{
		Authentication authentication = getAuthentication();
		return getUser(authentication);
	}

	/**
	 * 获取当前用户（认证用户或者匿名用户）。
	 * 
	 * @return {@code null}表示未取到
	 */
	public User getUserNullable()
	{
		Authentication authentication = getAuthenticationNullable();
		return getUserNullable(authentication);
	}

	/**
	 * 从{@linkplain Authentication#getPrincipal()}中获取当前用户（认证用户或者匿名用户）。
	 * <p>
	 * 此方法不会返回{@code null}。
	 * </p>
	 * 
	 * @param authentication
	 * @return
	 * @throws NullPointerException
	 *             当未获取到时抛出此异常
	 */
	public User getUser(Authentication authentication) throws NullPointerException
	{
		User user = getUserInner(authentication);

		if (user == null)
			throw new NullPointerException("Null user in authentication");

		return user;
	}

	/**
	 * 从{@linkplain Authentication#getPrincipal()}中获取当前用户（认证用户或者匿名用户）。
	 * <p>
	 * 此方法不会返回{@code null}。
	 * </p>
	 * 
	 * @param authentication
	 *            允许{@code null}
	 * @return
	 * @throws NullPointerException
	 *             当未获取到时抛出此异常
	 */
	public User getUserNullable(Authentication authentication)
	{
		return getUserInner(authentication);
	}

	/**
	 * 获取当前{@linkplain Authentication}。
	 * <p>
	 * 此方法不会返回{@code null}。
	 * </p>
	 * 
	 * @return
	 * @throws NullPointerException
	 *             当未获取到时抛出此异常
	 */
	public Authentication getAuthentication() throws NullPointerException
	{
		Authentication auth = getAuthenticationInner();

		if (auth == null)
			throw new NullPointerException("Null authentication");

		return auth;
	}

	/**
	 * 获取当前{@linkplain Authentication}。
	 * 
	 * @return {@code null}表示未取到
	 */
	public Authentication getAuthenticationNullable()
	{
		return getAuthenticationInner();
	}

	/**
	 * 从{@linkplain Authentication#getPrincipal()}中获取当前用户（认证用户或者匿名用户）。
	 * 
	 * @param authentication
	 *            允许{@code null}
	 * @return {@code null}表示未取到
	 */
	protected User getUserInner(Authentication authentication)
	{
		User user = null;

		Object principal = (authentication == null ? null : authentication.getPrincipal());

		if (principal == null)
		{
			user = null;
		}
		else if (principal instanceof User)
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
		else
		{
			user = getUserInnerExt(authentication, principal);
		}

		return user;
	}

	protected User getUserInnerExt(Authentication authentication, Object principal)
	{
		throw new UnsupportedOperationException(
				"Unsupported get user from [" + (principal.getClass().getSimpleName()) + "]");
	}

	/**
	 * 获取当前{@linkplain Authentication}。
	 * 
	 * @return {@code null}表示未取到
	 */
	protected Authentication getAuthenticationInner()
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
