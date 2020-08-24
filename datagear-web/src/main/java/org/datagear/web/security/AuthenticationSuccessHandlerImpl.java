/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.security;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.management.domain.User;
import org.datagear.management.service.CreateUserEntityService;
import org.datagear.web.util.WebContextPath;
import org.datagear.web.util.WebUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * {@linkplain AuthenticationSuccessHandler}实现类。
 * 
 * @author datagear@163.com
 *
 */
public class AuthenticationSuccessHandlerImpl implements AuthenticationSuccessHandler
{
	private List<CreateUserEntityService> createUserEntityServices;

	public AuthenticationSuccessHandlerImpl()
	{
		super();
	}

	public List<CreateUserEntityService> getCreateUserEntityServices()
	{
		return createUserEntityServices;
	}

	public void setCreateUserEntityServices(List<CreateUserEntityService> createUserEntityServices)
	{
		this.createUserEntityServices = createUserEntityServices;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException
	{
		mergeAnonymousUserEntities(request, response, authentication);

		response.sendRedirect(WebContextPath.getWebContextPath(request).concat(request, "/"));
	}

	/**
	 * 将匿名用户的数据迁移至登录用户上。
	 * 
	 * @param request
	 * @param response
	 * @param loginUser
	 */
	protected void mergeAnonymousUserEntities(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication)
	{
		User loginUser = WebUtils.getUser(authentication);

		if (loginUser == null)
			return;

		// 不迁移至管理员用户上
		if (loginUser.isAdmin())
			return;

		String anonymousUserId = WebUtils.getCookieValue(request, WebUtils.COOKIE_USER_ID_ANONYMOUS);

		if (anonymousUserId != null && !anonymousUserId.isEmpty())
		{
			if (this.createUserEntityServices != null)
			{
				for (CreateUserEntityService service : this.createUserEntityServices)
				{
					service.updateCreateUserId(anonymousUserId, loginUser.getId());
				}
			}
		}
	}
}
