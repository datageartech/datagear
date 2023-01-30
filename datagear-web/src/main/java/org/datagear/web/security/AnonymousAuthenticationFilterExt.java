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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.datagear.management.domain.Role;
import org.datagear.management.domain.User;
import org.datagear.management.service.RoleService;
import org.datagear.util.IDUtil;
import org.datagear.web.util.WebUtils;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

/**
 * {@linkplain AnonymousAuthenticationFilter}扩展类。
 * <p>
 * 此类将匿名用户的{@linkplain Authentication#getPrincipal()}构建为{@linkplain AuthUser}类。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class AnonymousAuthenticationFilterExt extends AnonymousAuthenticationFilter
{
	public static final String SESSION_KEY_AUTH_USER_ANONYMOUS = "AUTH_USER_ANONYMOUS";

	public static final String COOKIE_USER_ID_ANONYMOUS = "USER_ID_ANONYMOUS";

	private String key;

	private Set<String> anonymousRoleIds = Collections.emptySet();

	private RoleService roleService = null;

	public AnonymousAuthenticationFilterExt(String key)
	{
		super(key, "anonymousUser", AuthorityUtils.createAuthorityList(AuthUser.ROLE_ANONYMOUS));
		this.key = key;
	}

	public Set<String> getAnonymousRoleIds()
	{
		return anonymousRoleIds;
	}

	public void setAnonymousRoleIds(Set<String> anonymousRoleIds)
	{
		this.anonymousRoleIds = anonymousRoleIds;
	}

	public RoleService getRoleService()
	{
		return roleService;
	}

	public void setRoleService(RoleService roleService)
	{
		this.roleService = roleService;
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException
	{
		if (SecurityContextHolder.getContext().getAuthentication() == null)
		{
			SecurityContextHolder.getContext()
					.setAuthentication(createAuthentication((HttpServletRequest) req, (HttpServletResponse) res));
		}

		chain.doFilter(req, res);
	}

	protected Authentication createAuthentication(HttpServletRequest request, HttpServletResponse response)
	{
		AnonymousAuthenticationToken authSuper = (AnonymousAuthenticationToken) super.createAuthentication(request);

		AuthUser principal = createAnonymousPrincipal(request, response);

		Set<GrantedAuthority> authorities = new HashSet<>();

		if (authSuper.getAuthorities() != null)
			authorities.addAll(authSuper.getAuthorities());
		if (principal.getAuthorities() != null)
			authorities.addAll(principal.getAuthorities());

		AnonymousAuthenticationToken auth = new AnonymousAuthenticationToken(this.key, principal, authorities);
		auth.setDetails(authSuper.getDetails());

		return auth;
	}

	protected AuthUser createAnonymousPrincipal(HttpServletRequest request, HttpServletResponse response)
	{
		HttpSession session = request.getSession();

		AuthUser principal = (AuthUser) session.getAttribute(SESSION_KEY_AUTH_USER_ANONYMOUS);

		if (principal == null)
		{
			String anonymousUserId = WebUtils.getCookieValue(request, COOKIE_USER_ID_ANONYMOUS);

			if (anonymousUserId == null || anonymousUserId.isEmpty())
			{
				anonymousUserId = IDUtil.uuid();
				WebUtils.setCookie(request, response, COOKIE_USER_ID_ANONYMOUS, anonymousUserId,
						60 * 60 * 24 * 365 * 10);
			}

			User anonymousUser = new User(anonymousUserId);
			anonymousUser.setName(anonymousUserId);
			anonymousUser.setRealName("ANONYMOUS");
			anonymousUser.setAdmin(false);
			anonymousUser.setAnonymous(true);
			anonymousUser.setCreateTime(new java.util.Date());
			anonymousUser.setRoles(buildAnonymousRoles(this.anonymousRoleIds));

			principal = new AuthUser(anonymousUser);

			session.setAttribute(SESSION_KEY_AUTH_USER_ANONYMOUS, principal);
		}

		return principal;
	}

	protected Set<Role> buildAnonymousRoles(Set<String> anonymousRoleIds)
	{
		Set<Role> roles = new HashSet<>();

		if (anonymousRoleIds != null)
		{
			List<String> idList = new ArrayList<>(anonymousRoleIds);
			String[] idArray = idList.toArray(new String[idList.size()]);

			List<Role> roleList = this.roleService.getByIds(idArray);

			for (Role role : roleList)
			{
				if (role != null)
					roles.add(role);
			}
		}

		return roles;
	}
}
