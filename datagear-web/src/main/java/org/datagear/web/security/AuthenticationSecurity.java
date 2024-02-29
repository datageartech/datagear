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

package org.datagear.web.security;

import java.util.Collection;

import org.datagear.management.util.RoleSpec;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * 认证安全控制。
 * 
 * @author datagear@163.com
 *
 */
public class AuthenticationSecurity
{
	public static final GrantedAuthority GA_DATA_MANAGER = new SimpleGrantedAuthority(RoleSpec.ROLE_DATA_MANAGER);

	public static final GrantedAuthority GA_DATA_ANALYST = new SimpleGrantedAuthority(RoleSpec.ROLE_DATA_ANALYST);

	private boolean disableAnonymous = false;

	public AuthenticationSecurity()
	{
		super();
	}
	
	public AuthenticationSecurity(boolean disableAnonymous)
	{
		super();
		this.disableAnonymous = disableAnonymous;
	}

	public boolean isDisableAnonymous()
	{
		return disableAnonymous;
	}

	public void setDisableAnonymous(boolean disableAnonymous)
	{
		this.disableAnonymous = disableAnonymous;
	}

	/**
	 * 是否至少有{@linkplain RoleSpec#ROLE_DATA_MANAGER}权限。
	 * 
	 * @param auth
	 * @return
	 */
	public boolean hasDataManager(Authentication auth)
	{
		Collection<? extends GrantedAuthority> gas = auth.getAuthorities();

		// 禁止匿名用户
		if (containsAnonymous(gas))
			return false;

		return gas.contains(GA_DATA_MANAGER) || gas.contains(AuthUser.GA_ADMIN);
	}

	/**
	 * 是否至少有{@linkplain RoleSpec#ROLE_DATA_ANALYST}权限。
	 * 
	 * @param auth
	 * @return
	 */
	public boolean hasDataAnalyst(Authentication auth)
	{
		Collection<? extends GrantedAuthority> gas = auth.getAuthorities();

		if (this.disableAnonymous && containsAnonymous(gas))
			return false;

		return gas.contains(GA_DATA_ANALYST) || hasDataManager(auth);
	}

	/**
	 * 是否有{@linkplain AuthUser#ROLE_ADMIN}权限。
	 * 
	 * @param auth
	 * @return
	 */
	public boolean hasAdmin(Authentication auth)
	{
		Collection<? extends GrantedAuthority> gas = auth.getAuthorities();

		// 禁止匿名用户
		if (containsAnonymous(gas))
			return false;

		return gas.contains(AuthUser.GA_ADMIN);
	}

	/**
	 * 是否至少有{@linkplain AuthUser#ROLE_USER}权限。
	 * 
	 * @param auth
	 * @return
	 */
	public boolean hasUser(Authentication auth)
	{
		Collection<? extends GrantedAuthority> gas = auth.getAuthorities();

		// 禁止匿名用户
		if (containsAnonymous(gas))
			return false;

		return gas.contains(AuthUser.GA_USER) || hasAdmin(auth);
	}

	/**
	 * 是否至少有{@linkplain AuthUser#ROLE_ANONYMOUS}权限。
	 * 
	 * @param auth
	 * @return
	 */
	public boolean hasAnonymous(Authentication auth)
	{
		Collection<? extends GrantedAuthority> gas = auth.getAuthorities();

		return containsAnonymous(gas) || hasUser(auth);
	}

	/**
	 * 是否至少有访问系统的权限。
	 * 
	 * @param auth
	 * @return
	 */
	public boolean hasAccess(Authentication auth)
	{
		if (this.disableAnonymous)
			return hasUser(auth);
		else
			return hasAnonymous(auth);
	}

	/**
	 * 是否是匿名用户。
	 * 
	 * @param auth
	 * @return
	 */
	public boolean isAnonymous(Authentication auth)
	{
		Collection<? extends GrantedAuthority> gas = auth.getAuthorities();

		return containsAnonymous(gas);
	}

	/**
	 * 获取指定用户的{@linkplain ModulePermissions}。
	 * 
	 * @param auth
	 * @return
	 */
	public ModulePermissions resolveModulePermissions(Authentication auth)
	{
		boolean accessor = hasDataAnalyst(auth);
		boolean operator = hasDataManager(auth);
		boolean visible = accessor;

		ModulePermission schemaPermission = new ModulePermission(accessor, operator, visible);
		ModulePermission analysisProjectPermission = new ModulePermission(accessor, operator, visible);
		ModulePermission dataSetPermission = new ModulePermission(accessor, operator, visible);
		ModulePermission chartPermission = new ModulePermission(accessor, operator, visible);
		ModulePermission dashboardPermission = new ModulePermission(accessor, operator, visible);

		return new ModulePermissions(schemaPermission, analysisProjectPermission, dataSetPermission, chartPermission,
				dashboardPermission);
	}

	protected boolean containsAnonymous(Collection<? extends GrantedAuthority> gas)
	{
		return gas.contains(AuthUser.GA_ANONYMOUS);
	}
}
